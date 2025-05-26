package com.ssafy.trip.payment.service;

import com.siot.IamportRestClient.IamportClient;
import com.ssafy.trip.payment.dao.PaymentDao;
import com.ssafy.trip.payment.model.OrderDto;
import com.ssafy.trip.payment.model.Payment;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.service.ReservationService;
import com.ssafy.trip.exception.InvalidRequestException;
import com.ssafy.trip.exception.PaymentProcessingException;
import com.ssafy.trip.exception.ResourceNotFoundException;
import com.ssafy.trip.payment.dto.PaymentCompleteRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;
    private final IamportClient iamportClient;
    private final ReservationService reservationService;

    /** 아임포트 결제 검증 및 DB 저장 */
    @Override
    @Transactional
    public Payment validateAndFinalizePayment(PaymentCompleteRequestDto paymentCompleteDto, Long userId)
            throws IamportResponseException, IOException, SQLException, IllegalStateException, ResourceNotFoundException {
        
        String impUid = paymentCompleteDto.getImpUid();
        String merchantUid = paymentCompleteDto.getMerchantUid();

        // 1. 아임포트 서버에서 결제 정보 조회
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                iamportClient.paymentByImpUid(impUid);
        com.siot.IamportRestClient.response.Payment iamportPayment = iamportResponse.getResponse();

        if (iamportPayment == null) {
            log.error("아임포트에서 결제 정보를 찾을 수 없습니다. imp_uid: {}", impUid);
            throw new PaymentProcessingException("아임포트에서 결제 정보를 조회할 수 없습니다.");
        }

        // 2. merchant_uid로 우리 시스템의 예약 정보 조회 및 예상 결제 금액 계산
        List<Reservation> reservations = reservationService.getPendingReservationsByMerchantUid(merchantUid);
        if (reservations == null || reservations.isEmpty()) {
            log.error("주문 ID(merchant_uid: {})에 해당하는 PENDING_PAYMENT 상태의 예약 정보를 찾을 수 없습니다.", merchantUid);
            // 이 경우, 아임포트 결제는 되었으나 우리 시스템에 매칭되는 예약이 없으므로, 해당 아임포트 결제를 취소해야 함.
            // iamportClient.cancelPaymentByImpUid(new CancelData(impUid, true, new BigDecimal(0))); // 전액 환불
            throw new ResourceNotFoundException("주문 ID에 해당하는 예약 정보를 찾을 수 없습니다. 관리자에게 문의하세요. (주문번호: " + merchantUid + ")");
        }

        BigDecimal expectedAmount = reservations.stream()
                                          .map(Reservation::getTotalPrice)
                                          .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 결제 검증 (금액, 상태 등)
        if (!"paid".equalsIgnoreCase(iamportPayment.getStatus())) {
            log.error("결제 미완료 상태입니다. 아임포트 상태: {}, imp_uid: {}", iamportPayment.getStatus(), impUid);
            // 클라이언트에서 이미 paid 상태를 확인하고 요청했겠지만, 서버에서도 한번 더 확인
            throw new PaymentProcessingException("결제가 완료되지 않았습니다. 상태: " + iamportPayment.getStatus());
        }

        if (iamportPayment.getAmount().compareTo(expectedAmount) != 0) {
            log.error("결제 금액 불일치. 아임포트 금액: {}, 시스템 계산 금액: {}, imp_uid: {}",
                    iamportPayment.getAmount(), expectedAmount, impUid);
            // 금액 불일치 시, 해당 아임포트 결제 취소 처리
            // iamportClient.cancelPaymentByImpUid(new CancelData(impUid, true, iamportPayment.getAmount()));
            throw new PaymentProcessingException("결제 금액이 일치하지 않습니다. 관리자에게 문의하세요.");
        }
        
        // 4. 우리 DB에 이미 해당 merchant_uid 또는 imp_uid로 결제가 완료되었는지 확인 (중복 처리 방지)
        Payment existingPayment = paymentDao.selectByMerchantUid(merchantUid);
        if (existingPayment != null && "PAID".equalsIgnoreCase(existingPayment.getStatus())) {
            log.warn("이미 처리된 주문입니다. merchant_uid: {}", merchantUid);
            // 이미 성공적으로 처리된 결제라면 성공으로 간주하고 정보를 반환하거나, 에러로 처리할 수 있음.
            // 여기서는 IllegalStateException을 발생시켜 중복 요청임을 알림.
            throw new IllegalStateException("이미 처리된 주문입니다.");
        }
        Payment existingPaymentByImpUid = paymentDao.selectByPaymentKey(impUid);
         if (existingPaymentByImpUid != null && "PAID".equalsIgnoreCase(existingPaymentByImpUid.getStatus())) {
            log.warn("이미 처리된 결제입니다. imp_uid: {}", impUid);
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }

        // 5. Payment 객체 생성 및 DB 저장
        Payment payment = Payment.builder()
                .userId(userId)
                .reservationId(reservations.get(0).getReservationId()) // 대표 예약 ID 하나를 저장 (필요시 수정)
                .merchantUid(merchantUid)
                .paymentMethod(iamportPayment.getPayMethod() != null ? iamportPayment.getPayMethod().toUpperCase() : "UNKNOWN")
                .paymentKey(impUid)
                .amount(iamportPayment.getAmount())
                .currency(iamportPayment.getCurrency() != null ? iamportPayment.getCurrency() : "KRW")
                .status("PAID") // "paid"를 우리 시스템의 "PAID" 상태로 매핑
                .cardInfo(iamportPayment.getCardName() + " " + iamportPayment.getCardNumber()) // 카드사 + 번호 일부
                .bankInfo(iamportPayment.getBankName())
                .pgProvider(iamportPayment.getPgProvider())
                .pgTid(iamportPayment.getPgTid())
                .receiptUrl(iamportPayment.getReceiptUrl())
                .paidAt(iamportPayment.getPaidAt() != null ? iamportPayment.getPaidAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // paymentDao.insert(payment) 또는 insertPaymentWithMerchantUid(payment) 사용
        // insertPaymentWithMerchantUid는 merchant_uid를 포함하여 저장하는 새 DAO 메소드 가정
        int insertedCount = paymentDao.insertPaymentWithMerchantUid(payment);
        if (insertedCount == 0 || payment.getPaymentId() == null) { // useGeneratedKeys 등으로 ID가 설정되어야 함
             log.error("결제 정보 저장 실패. merchant_uid: {}", merchantUid);
             throw new SQLException("결제 정보를 DB에 저장하는데 실패했습니다.");
        }

        // 6. 예약 상태 업데이트
        try {
            reservationService.updateReservationsAfterPayment(merchantUid, "CONFIRMED", "PAID");
        } catch (ResourceNotFoundException | SQLException e) {
            log.error("결제 후 예약 상태 변경 실패. merchant_uid: {}, 원인: {}", merchantUid, e.getMessage());
            // 여기서는 이미 결제는 성공했고 DB에도 기록했으므로, 예약 상태 변경 실패는 심각한 문제.
            // 관리자 알림, 또는 해당 merchant_uid에 대한 후처리 작업 큐잉 등 고려 필요.
            // 일단 예외를 다시 던져서 트랜잭션 롤백 유도 (만약 이 메소드 전체가 @Transactional이고, 예외 전파 시)
            // 다만, 아임포트 결제는 이미 완료된 상태이므로, 이 부분은 신중한 에러 핸들링 및 보상 트랜잭션 고려 필요.
            throw new PaymentProcessingException("결제는 완료되었으나 예약 상태 업데이트에 실패했습니다. 관리자에게 문의하세요. (주문번호: " + merchantUid + ")", e);
        }
        
        log.info("결제 성공 및 처리 완료: merchantUid={}, impUid={}, amount={}", merchantUid, impUid, payment.getAmount());
        return paymentDao.selectById(payment.getPaymentId()); // 저장 후 ID가 채워진 Payment 객체 반환
    }

    @Override
    @Transactional
    public IamportResponse<com.siot.IamportRestClient.response.Payment> cancelPayment(
            Long paymentId, String reason, Long userId)
            throws IamportResponseException, IOException, SQLException, IllegalStateException, ResourceNotFoundException {
        
        Payment payment = paymentDao.selectById(paymentId);
        if (payment == null) {
            throw new ResourceNotFoundException("취소할 결제 정보를 찾을 수 없습니다. ID: " + paymentId);
        }

        // 사용자 권한 검증 (본인 결제 또는 관리자 등)
        if (!payment.getUserId().equals(userId)) {
             // 관리자 권한 체크 로직 추가 가능
            log.warn("사용자 ID {}가 결제 ID {} (사용자 {}) 취소 시도. 권한 없음.", userId, paymentId, payment.getUserId());
            throw new SecurityException("해당 결제를 취소할 권한이 없습니다."); // SecurityException 또는 AccessDeniedException 사용 고려
        }

        if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
            log.warn("이미 취소되었거나 결제가 완료되지 않은 건입니다. Payment ID: {}, Status: {}", paymentId, payment.getStatus());
            throw new IllegalStateException("이미 취소되었거나 결제가 완료되지 않은 건은 취소할 수 없습니다.");
        }

        // 아임포트 취소 호출
        CancelData cancelData = new CancelData(payment.getPaymentKey(), true, payment.getAmount()); // paymentKey가 imp_uid
        // 부분 취소를 원할 경우 checksum 등 추가 파라미터 필요
        // cancelData.setReason(reason);
        // 위에서 checksum을 계산하여 전달해야 함. 여기서는 checksum 없이 전액환불 시도.
        // 만약 checksum이 필요하다면 아임포트 문서를 참조하여 서버에서 계산 후 전달.
        // 지금은 checksum 없이 시도. (아임포트 기본 설정에 따라 동작)

        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportCancelResponse = 
                iamportClient.cancelPaymentByImpUid(cancelData);

        com.siot.IamportRestClient.response.Payment cancelledIamportPayment = iamportCancelResponse.getResponse();

        if (cancelledIamportPayment != null && "cancelled".equalsIgnoreCase(cancelledIamportPayment.getStatus())) {
            // 우리 DB 결제 상태 업데이트
            payment.setStatus("CANCELLED");
            payment.setCancelReason(reason);
            payment.setCancelledAt(LocalDateTime.now()); // 취소 시간 기록 필드 필요 (Payment 모델에 추가 가정)
            payment.setUpdatedAt(LocalDateTime.now());
            paymentDao.updatePaymentAfterCancel(payment); // 상태, 취소사유, 취소시간 등 업데이트하는 DAO 메소드 필요

            // 예약 상태 업데이트
            try {
                reservationService.cancelReservationsByMerchantUid(payment.getMerchantUid(), "CANCELLED", "REFUNDED");
            } catch (SQLException e) {
                log.error("결제 취소 후 예약 상태 변경 실패. merchant_uid: {}, 원인: {}", payment.getMerchantUid(), e.getMessage());
                // 아임포트 결제는 취소되었으나 예약 상태 변경 실패.
                // 이 경우 관리자 알림 및 수동 처리 필요.
                // 이미 Iamport 결제 취소는 성공했으므로 이 트랜잭션을 롤백하면 안됨. 별도 처리.
                // throw new PaymentProcessingException(...) -> 이 경우 롤백됨. 주의!
                // 여기서는 로깅만 하고 넘어감. 실제 운영 시에는 보상 트랜잭션 또는 수동처리 플로우 필요.
            }
            log.info("결제 취소 성공: paymentId={}, impUid={}, merchantUid={}", paymentId, payment.getPaymentKey(), payment.getMerchantUid());
        } else {
            log.error("아임포트 결제 취소 실패. imp_uid: {}. 응답: {}", payment.getPaymentKey(), iamportCancelResponse.getMessage());
            // 실패 원인에 따라 사용자에게 메시지 전달 또는 재시도 안내
            String errorMessage = iamportCancelResponse.getMessage() != null ? iamportCancelResponse.getMessage() : "알 수 없는 오류로 결제 취소에 실패했습니다.";
            throw new PaymentProcessingException("아임포트 결제 취소 실패: " + errorMessage);
        }
        return iamportCancelResponse;
    }

    @Override
    @Transactional
    public Payment cancelInternalPayment(Long paymentId, String reason, Long userId)
            throws SQLException, ResourceNotFoundException, IllegalStateException {
        Payment payment = paymentDao.selectById(paymentId);
        if (payment == null) {
            throw new ResourceNotFoundException("취소할 결제 정보를 찾을 수 없습니다. ID: " + paymentId);
        }

        // 사용자 권한 검증 (본인 결제 또는 관리자 등)
        if (!payment.getUserId().equals(userId)) {
            // 관리자 권한 체크 로직 추가 가능
            log.warn("사용자 ID {}가 내부 결제 ID {} (사용자 {}) 취소 시도. 권한 없음.", userId, paymentId, payment.getUserId());
            throw new SecurityException("해당 결제를 내부적으로 취소할 권한이 없습니다.");
        }

        if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
            log.warn("이미 취소되었거나 결제가 완료되지 않은 건은 내부적으로 취소할 수 없습니다. Payment ID: {}, Status: {}", paymentId, payment.getStatus());
            throw new IllegalStateException("이미 취소되었거나 결제가 완료되지 않은 건은 내부적으로 취소할 수 없습니다.");
        }

        // 우리 DB 결제 상태 업데이트
        payment.setStatus("CANCELLED");
        payment.setCancelReason(reason);
        payment.setCancelledAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        int updatedRows = paymentDao.updatePaymentAfterCancel(payment);

        if (updatedRows == 0) {
            log.error("내부 결제 취소 중 DB 업데이트 실패. Payment ID: {}", paymentId);
            throw new SQLException("내부 결제 정보 업데이트에 실패했습니다.");
        }

        // 예약 상태 업데이트
        try {
            reservationService.cancelReservationsByMerchantUid(payment.getMerchantUid(), "CANCELLED", "REFUNDED");
        } catch (SQLException e) {
            log.error("내부 결제 취소 후 예약 상태 변경 실패. merchant_uid: {}, 원인: {}", payment.getMerchantUid(), e.getMessage());
            // 이 경우 관리자 알림 및 수동 처리 필요. 여기서는 로깅 후 예외를 다시 던져 트랜잭션 롤백 유도.
            throw new PaymentProcessingException("결제는 내부적으로 취소되었으나 예약 상태 업데이트에 실패했습니다. 관리자에게 문의하세요. (주문번호: " + payment.getMerchantUid() + ")", e);
        }
        log.info("내부 결제 취소 성공: paymentId={}, merchantUid={}", paymentId, payment.getMerchantUid());
        return paymentDao.selectById(paymentId); // 업데이트된 결제 정보 반환
    }

    @Override
    public Payment getPaymentByIdAndUserId(Long paymentId, Long userId) throws SQLException, ResourceNotFoundException {
        Payment payment = paymentDao.selectByIdAndUserId(paymentId, userId); // DAO에 해당 메소드 필요
        if (payment == null) {
            throw new ResourceNotFoundException("결제 정보를 찾을 수 없거나 해당 사용자의 결제가 아닙니다.");
        }
        return payment;
    }

    @Override
    public Page<Payment> getPaymentsByUserId(Long userId, Pageable pageable) throws SQLException {
        List<Payment> payments = paymentDao.selectPageByUserId(userId, pageable.getOffset(), pageable.getPageSize()); // DAO에 해당 메소드 필요
        long total = paymentDao.countByUserId(userId); // DAO에 해당 메소드 필요
        return new PageImpl<>(payments, pageable, total);
    }

    @Override
    public Payment getPaymentByMerchantUid(String merchantUid) throws SQLException {
        return paymentDao.selectByMerchantUid(merchantUid);
    }

    @Override
    @Transactional
    public String saveOrder(OrderDto orderDto) {
        Payment payment = orderDto.toEntity();
        try {
            Long paymentId = paymentDao.insert(payment);
            return paymentId != null ? "success" : "fail";
        } catch (SQLException e) {
            log.error("Order 결제 저장 실패: {}", e.getMessage(), e);
            return "fail";
        }
    }

    @Override
    @Transactional
    public Long createPayment(Payment payment) throws SQLException {
        return paymentDao.insert(payment);
    }

    @Override
    public Payment getPaymentById(Long paymentId) throws SQLException {
        return paymentDao.selectById(paymentId);
    }

    @Override
    public Payment getPaymentByReservationId(Long reservationId) throws SQLException {
        return paymentDao.selectByReservationId(reservationId);
    }

    @Override
    public List<Payment> getPaymentsByUserId(Long userId) throws SQLException {
        return paymentDao.selectByUserId(userId);
    }

    @Override
    @Transactional
    public Payment processPayment(Payment payment) throws SQLException {
        Long paymentId = paymentDao.insert(payment);
        payment.setPaymentId(paymentId);
        return payment;
    }

    @Override
    @Transactional
    public Payment processCardPayment(Long reservationId, Long userId, BigDecimal amount, String cardInfo) throws SQLException {
        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentMethod("CARD")
                .amount(amount)
                .cardInfo(cardInfo)
                .status("COMPLETED")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Long paymentId = paymentDao.insert(payment);
        payment.setPaymentId(paymentId);
        return payment;
    }

    @Override
    @Transactional
    public Payment processBankTransferPayment(Long reservationId, Long userId, BigDecimal amount, String bankInfo) throws SQLException {
        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentMethod("BANK_TRANSFER")
                .amount(amount)
                .bankInfo(bankInfo)
                .status("COMPLETED")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Long paymentId = paymentDao.insert(payment);
        payment.setPaymentId(paymentId);
        return payment;
    }

    @Override
    @Transactional
    public Payment processPhonePayment(Long reservationId, Long userId, BigDecimal amount, String phoneInfo) throws SQLException {
        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentMethod("PHONE")
                .amount(amount)
                .phoneInfo(phoneInfo)
                .status("COMPLETED")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Long paymentId = paymentDao.insert(payment);
        payment.setPaymentId(paymentId);
        return payment;
    }

    @Override
    @Transactional
    public boolean updatePaymentStatus(Long paymentId, String status) throws SQLException {
        int updated = paymentDao.updateStatus(paymentId, status, LocalDateTime.now());
        return updated > 0;
    }

    @Override
    @Transactional
    public Payment cancelPayment(Long paymentId, String cancelReason) throws SQLException {
        int updated = paymentDao.cancelPayment(paymentId, cancelReason, LocalDateTime.now());
        if (updated > 0) {
            return paymentDao.selectById(paymentId);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean updatePaymentFailInfo(Long paymentId, String failReason) throws SQLException {
        int updated = paymentDao.updateFailInfo(paymentId, failReason, LocalDateTime.now());
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deletePayment(Long paymentId) throws SQLException {
        int deleted = paymentDao.delete(paymentId);
        return deleted > 0;
    }

    @Override
    public List<Payment> getFilteredPayments(Map<String, Object> filters) throws SQLException {
        return paymentDao.selectFiltered(filters);
    }

    @Override
    public Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return paymentDao.selectStatistics(startDate, endDate);
    }

    @Override
    public String maskPaymentInfo(String info, String type) {
        if (info == null) return null;
        switch (type) {
            case "CARD":
                if (info.length() > 4)
                    return "****-****-" + info.substring(info.length() - 4);
                return "****-****-" + info;
            case "BANK":
                if (info.length() > 4)
                    return info.substring(0, 2) + "***" + info.substring(info.length() - 2);
                return "***" + info;
            case "PHONE":
                if (info.length() >= 4)
                    return "***" + info.substring(info.length() - 4);
                return "***" + info;
            default:
                return info;
        }
    }

    @Override
    public Payment getPaymentByImpUid(String impUid) throws SQLException, ResourceNotFoundException {
        Payment payment = paymentDao.selectByPaymentKey(impUid); // paymentKey가 imp_uid
        if (payment == null) {
            throw new ResourceNotFoundException("imp_uid '" + impUid + "'에 해당하는 결제 정보를 찾을 수 없습니다.");
        }
        return payment;
    }
}

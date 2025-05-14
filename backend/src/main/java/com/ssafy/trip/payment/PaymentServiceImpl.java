package com.ssafy.trip.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 결제 서비스 구현 클래스
 * 결제 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;

    /**
     * 생성자 주입을 통한 의존성 주입
     * 
     * @param paymentDao 결제 DAO
     */
    @Autowired
    public PaymentServiceImpl(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    /**
     * 새 결제를 생성합니다.
     */
    @Override
    @Transactional
    public Long createPayment(Payment payment) throws SQLException {
        // 생성 시간이 설정되지 않은 경우 현재 시간으로 설정
        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(LocalDateTime.now());
        }
        
        // 수정 시간이 설정되지 않은 경우 현재 시간으로 설정
        if (payment.getUpdatedAt() == null) {
            payment.setUpdatedAt(LocalDateTime.now());
        }
        
        return paymentDao.insert(payment);
    }

    /**
     * 결제 ID로 결제 정보를 조회합니다.
     */
    @Override
    public Payment getPaymentById(Long paymentId) throws SQLException {
        return paymentDao.selectById(paymentId);
    }

    /**
     * 예약 ID로 결제 정보를 조회합니다.
     */
    @Override
    public Payment getPaymentByReservationId(Long reservationId) throws SQLException {
        return paymentDao.selectByReservationId(reservationId);
    }

    /**
     * 사용자 ID로 결제 목록을 조회합니다.
     */
    @Override
    public List<Payment> getPaymentsByUserId(Long userId) throws SQLException {
        return paymentDao.selectByUserId(userId);
    }

    /**
     * 결제를 처리합니다.
     * 실제 결제 게이트웨이와 연동하여 결제를 처리하는 로직이 구현되어야 합니다.
     */
    @Override
    @Transactional
    public Payment processPayment(Payment payment) throws SQLException {
        // 결제 방법에 따라 다른 처리 로직 수행
        switch (payment.getPaymentMethod()) {
            case "CARD":
                return processCardPayment(payment.getReservationId(), payment.getUserId(), payment.getAmount(), payment.getCardInfo());
            case "BANK_TRANSFER":
                return processBankTransferPayment(payment.getReservationId(), payment.getUserId(), payment.getAmount(), payment.getBankInfo());
            case "PHONE":
                return processPhonePayment(payment.getReservationId(), payment.getUserId(), payment.getAmount(), payment.getPhoneInfo());
            default:
                throw new IllegalArgumentException("지원하지 않는 결제 방법입니다: " + payment.getPaymentMethod());
        }
    }

    /**
     * 카드 결제를 처리합니다.
     */
    @Override
    @Transactional
    public Payment processCardPayment(Long reservationId, Long userId, BigDecimal amount, String cardInfo) throws SQLException {
        // 카드 정보 마스킹 처리
        String maskedCardInfo = maskPaymentInfo(cardInfo, "CARD");
        
        // 결제 키 생성 (실제로는 결제 게이트웨이에서 제공)
        String paymentKey = generatePaymentKey("CARD");
        
        // 결제 정보 생성
        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentMethod("CARD")
                .paymentKey(paymentKey)
                .amount(amount)
                .currency("KRW")
                .status("COMPLETED") // 실제로는 결제 게이트웨이의 응답에 따라 설정
                .cardInfo(maskedCardInfo)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 결제 정보 저장
        Long paymentId = createPayment(payment);
        payment.setPaymentId(paymentId);
        
        return payment;
    }

    /**
     * 계좌이체 결제를 처리합니다.
     */
    @Override
    @Transactional
    public Payment processBankTransferPayment(Long reservationId, Long userId, BigDecimal amount, String bankInfo) throws SQLException {
        // 은행 정보 마스킹 처리
        String maskedBankInfo = maskPaymentInfo(bankInfo, "BANK");
        
        // 결제 키 생성 (실제로는 결제 게이트웨이에서 제공)
        String paymentKey = generatePaymentKey("BANK");
        
        // 결제 정보 생성
        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentMethod("BANK_TRANSFER")
                .paymentKey(paymentKey)
                .amount(amount)
                .currency("KRW")
                .status("COMPLETED") // 실제로는 결제 게이트웨이의 응답에 따라 설정
                .bankInfo(maskedBankInfo)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 결제 정보 저장
        Long paymentId = createPayment(payment);
        payment.setPaymentId(paymentId);
        
        return payment;
    }

    /**
     * 휴대폰 결제를 처리합니다.
     */
    @Override
    @Transactional
    public Payment processPhonePayment(Long reservationId, Long userId, BigDecimal amount, String phoneInfo) throws SQLException {
        // 휴대폰 정보 마스킹 처리
        String maskedPhoneInfo = maskPaymentInfo(phoneInfo, "PHONE");
        
        // 결제 키 생성 (실제로는 결제 게이트웨이에서 제공)
        String paymentKey = generatePaymentKey("PHONE");
        
        // 결제 정보 생성
        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentMethod("PHONE")
                .paymentKey(paymentKey)
                .amount(amount)
                .currency("KRW")
                .status("COMPLETED") // 실제로는 결제 게이트웨이의 응답에 따라 설정
                .phoneInfo(maskedPhoneInfo)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 결제 정보 저장
        Long paymentId = createPayment(payment);
        payment.setPaymentId(paymentId);
        
        return payment;
    }

    /**
     * 결제 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public boolean updatePaymentStatus(Long paymentId, String status) throws SQLException {
        return paymentDao.updateStatus(paymentId, status, LocalDateTime.now()) > 0;
    }

    /**
     * 결제를 취소합니다.
     */
    @Override
    @Transactional
    public Payment cancelPayment(Long paymentId, String cancelReason) throws SQLException {
        // 결제 취소 처리
        LocalDateTime cancelledAt = LocalDateTime.now();
        int result = paymentDao.cancelPayment(paymentId, cancelReason, cancelledAt);
        
        if (result > 0) {
            // 취소된 결제 정보 조회
            return paymentDao.selectById(paymentId);
        }
        
        return null;
    }

    /**
     * 결제 실패 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public boolean updatePaymentFailInfo(Long paymentId, String failReason) throws SQLException {
        return paymentDao.updateFailInfo(paymentId, failReason, LocalDateTime.now()) > 0;
    }

    /**
     * 결제를 삭제합니다.
     */
    @Override
    @Transactional
    public boolean deletePayment(Long paymentId) throws SQLException {
        return paymentDao.delete(paymentId) > 0;
    }

    /**
     * 필터링된 결제 목록을 조회합니다.
     */
    @Override
    public List<Payment> getFilteredPayments(Map<String, Object> filters) throws SQLException {
        return paymentDao.selectFiltered(filters);
    }

    /**
     * 결제 통계를 조회합니다.
     */
    @Override
    public Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return paymentDao.selectStatistics(startDate, endDate);
    }

    /**
     * 결제 정보를 마스킹 처리합니다.
     */
    @Override
    public String maskPaymentInfo(String info, String type) {
        if (info == null || info.isEmpty()) {
            return "";
        }
        
        switch (type) {
            case "CARD":
                // 카드 번호 마스킹 (예: 1234-5678-9012-3456 -> 1234-56**-****-3456)
                if (info.length() >= 16) {
                    return info.substring(0, 6) + "******" + info.substring(info.length() - 4);
                }
                break;
            case "BANK":
                // 계좌번호 마스킹 (예: 123456-78-901234 -> 123456-**-**1234)
                if (info.contains("-")) {
                    String[] parts = info.split("-");
                    if (parts.length >= 3) {
                        return parts[0] + "-**-**" + parts[2].substring(Math.max(0, parts[2].length() - 4));
                    }
                }
                break;
            case "PHONE":
                // 휴대폰 번호 마스킹 (예: 010-1234-5678 -> 010-****-5678)
                if (info.contains("-")) {
                    String[] parts = info.split("-");
                    if (parts.length >= 3) {
                        return parts[0] + "-****-" + parts[2];
                    }
                }
                break;
        }
        
        // 기본 마스킹 처리
        if (info.length() > 8) {
            return info.substring(0, 4) + "****" + info.substring(info.length() - 4);
        } else if (info.length() > 4) {
            return info.substring(0, 2) + "****" + info.substring(info.length() - 2);
        } else {
            return "****";
        }
    }
    
    /**
     * 결제 키를 생성합니다.
     * 실제로는 결제 게이트웨이에서 제공하는 키를 사용해야 합니다.
     * 
     * @param prefix 결제 방법 접두사
     * @return 생성된 결제 키
     */
    private String generatePaymentKey(String prefix) {
        return prefix.toLowerCase() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
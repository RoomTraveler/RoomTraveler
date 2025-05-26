package com.ssafy.trip.payment.service;

import com.ssafy.trip.payment.model.OrderDto;
import com.ssafy.trip.payment.model.Payment;
import com.ssafy.trip.payment.dto.PaymentCompleteRequestDto;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.ssafy.trip.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.io.IOException;

public interface PaymentService {
    Long createPayment(Payment payment) throws SQLException;
    Payment getPaymentById(Long paymentId) throws SQLException;
    Payment getPaymentByReservationId(Long reservationId) throws SQLException;
    List<Payment> getPaymentsByUserId(Long userId) throws SQLException;
    Payment processPayment(Payment payment) throws SQLException;
    Payment processCardPayment(Long reservationId, Long userId, java.math.BigDecimal amount, String cardInfo) throws SQLException;
    Payment processBankTransferPayment(Long reservationId, Long userId, java.math.BigDecimal amount, String bankInfo) throws SQLException;
    Payment processPhonePayment(Long reservationId, Long userId, java.math.BigDecimal amount, String phoneInfo) throws SQLException;
    boolean updatePaymentStatus(Long paymentId, String status) throws SQLException;
    Payment cancelPayment(Long paymentId, String cancelReason) throws SQLException;
    boolean updatePaymentFailInfo(Long paymentId, String failReason) throws SQLException;
    boolean deletePayment(Long paymentId) throws SQLException;
    List<Payment> getFilteredPayments(Map<String, Object> filters) throws SQLException;
    Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;
    String maskPaymentInfo(String info, String type);

    // 아임포트 연동 추가
    String saveOrder(OrderDto orderDto);

    /**
     * 결제 정보를 검증하고 최종적으로 확정합니다.
     * 아임포트 서버로부터 결제 정보를 조회하여 실제 결제 금액과 요청된 금액을 비교합니다.
     * 성공 시 예약 상태를 업데이트하고 결제 정보를 저장합니다.
     *
     * @param paymentCompleteDto 결제 완료 요청 DTO (imp_uid, merchant_uid 등 포함)
     * @param userId 현재 사용자 ID
     * @return 저장된 Payment 객체 또는 결제 처리 결과 DTO
     * @throws IamportResponseException 아임포트 API 통신 오류 시
     * @throws IOException 네트워크 오류 시
     * @throws SQLException DB 오류 시
     * @throws IllegalStateException 결제 검증 실패 시 (예: 금액 불일치, 이미 처리된 주문)
     * @throws ResourceNotFoundException 리소스 조회 실패 시
     */
    Payment validateAndFinalizePayment(PaymentCompleteRequestDto paymentCompleteDto, Long userId)
            throws IamportResponseException, IOException, SQLException, IllegalStateException, ResourceNotFoundException;

    /**
     * 결제를 취소하고 관련 예약 상태를 업데이트합니다. (아임포트 연동)
     *
     * @param paymentId 취소할 결제의 ID (우리 시스템의 paymentId)
     * @param reason 취소 사유
     * @param userId 현재 사용자 ID (권한 확인용)
     * @return 아임포트 결제 취소 응답 객체
     * @throws IamportResponseException 아임포트 API 통신 오류 또는 취소 실패 시
     * @throws IOException 네트워크 오류 시
     * @throws SQLException DB 오류 시
     * @throws IllegalStateException 이미 취소된 결제거나, 취소 권한이 없는 경우 등
     * @throws ResourceNotFoundException 리소스 조회 실패 시
     */
    IamportResponse<com.siot.IamportRestClient.response.Payment> cancelPayment(Long paymentId, String reason, Long userId)
            throws IamportResponseException, IOException, SQLException, IllegalStateException, ResourceNotFoundException;

    /**
     * 내부 결제를 취소하고 관련 예약 상태를 업데이트합니다. (DB 직접 수정)
     *
     * @param paymentId 취소할 결제의 ID (우리 시스템의 paymentId)
     * @param reason 취소 사유
     * @param userId 현재 사용자 ID (권한 확인용)
     * @return 취소 처리된 Payment 객체
     * @throws SQLException DB 오류 시
     * @throws ResourceNotFoundException 리소스 조회 실패 시
     * @throws IllegalStateException 이미 취소된 결제거나, 취소 권한이 없는 경우 등
     */
    Payment cancelInternalPayment(Long paymentId, String reason, Long userId)
            throws SQLException, ResourceNotFoundException, IllegalStateException;

    /**
     * 특정 결제 정보를 조회합니다. 사용자 본인의 결제만 조회 가능합니다.
     *
     * @param paymentId 조회할 결제 ID
     * @param userId 현재 사용자 ID
     * @return Payment 객체
     * @throws SQLException DB 오류 시
     * @throws ResourceNotFoundException 해당 결제가 없거나 조회 권한이 없을 때 (커스텀 예외 사용 가능)
     */
    Payment getPaymentByIdAndUserId(Long paymentId, Long userId) throws SQLException /*, ResourceNotFoundException */;

    /**
     * 특정 사용자의 모든 결제 내역을 페이징하여 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 Payment 목록
     * @throws SQLException DB 오류 시
     */
    Page<Payment> getPaymentsByUserId(Long userId, Pageable pageable) throws SQLException;
    
    /**
     * merchantUid로 결제 정보를 조회합니다.
     *
     * @param merchantUid 주문 ID
     * @return Payment 객체 또는 null
     * @throws SQLException DB 오류 시
     */
    Payment getPaymentByMerchantUid(String merchantUid) throws SQLException;

    /**
     * impUid (paymentKey)로 결제 정보를 조회합니다.
     *
     * @param impUid 아임포트 거래 ID
     * @return Payment 객체 또는 null
     * @throws SQLException DB 오류 시
     * @throws ResourceNotFoundException 해당 impUid로 결제 정보를 찾을 수 없는 경우
     */
    Payment getPaymentByImpUid(String impUid) throws SQLException, ResourceNotFoundException;
}

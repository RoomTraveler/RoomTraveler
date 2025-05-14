package com.ssafy.trip.payment;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 결제 서비스 인터페이스
 * 결제 관련 비즈니스 로직을 처리합니다.
 */
public interface PaymentService {
    /**
     * 새 결제를 생성합니다.
     * 
     * @param payment 결제 정보
     * @return 생성된 결제의 ID
     * @throws SQLException SQL 예외 발생 시
     */
    Long createPayment(Payment payment) throws SQLException;
    
    /**
     * 결제 ID로 결제 정보를 조회합니다.
     * 
     * @param paymentId 결제 ID
     * @return 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment getPaymentById(Long paymentId) throws SQLException;
    
    /**
     * 예약 ID로 결제 정보를 조회합니다.
     * 
     * @param reservationId 예약 ID
     * @return 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment getPaymentByReservationId(Long reservationId) throws SQLException;
    
    /**
     * 사용자 ID로 결제 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 결제 목록
     * @throws SQLException SQL 예외 발생 시
     */
    List<Payment> getPaymentsByUserId(Long userId) throws SQLException;
    
    /**
     * 결제를 처리합니다.
     * 
     * @param payment 결제 정보
     * @return 처리된 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment processPayment(Payment payment) throws SQLException;
    
    /**
     * 카드 결제를 처리합니다.
     * 
     * @param reservationId 예약 ID
     * @param userId 사용자 ID
     * @param amount 결제 금액
     * @param cardInfo 카드 정보
     * @return 처리된 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment processCardPayment(Long reservationId, Long userId, java.math.BigDecimal amount, String cardInfo) throws SQLException;
    
    /**
     * 계좌이체 결제를 처리합니다.
     * 
     * @param reservationId 예약 ID
     * @param userId 사용자 ID
     * @param amount 결제 금액
     * @param bankInfo 은행 정보
     * @return 처리된 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment processBankTransferPayment(Long reservationId, Long userId, java.math.BigDecimal amount, String bankInfo) throws SQLException;
    
    /**
     * 휴대폰 결제를 처리합니다.
     * 
     * @param reservationId 예약 ID
     * @param userId 사용자 ID
     * @param amount 결제 금액
     * @param phoneInfo 휴대폰 정보
     * @return 처리된 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment processPhonePayment(Long reservationId, Long userId, java.math.BigDecimal amount, String phoneInfo) throws SQLException;
    
    /**
     * 결제 상태를 업데이트합니다.
     * 
     * @param paymentId 결제 ID
     * @param status 새 상태
     * @return 업데이트 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    boolean updatePaymentStatus(Long paymentId, String status) throws SQLException;
    
    /**
     * 결제를 취소합니다.
     * 
     * @param paymentId 결제 ID
     * @param cancelReason 취소 사유
     * @return 취소된 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment cancelPayment(Long paymentId, String cancelReason) throws SQLException;
    
    /**
     * 결제 실패 정보를 업데이트합니다.
     * 
     * @param paymentId 결제 ID
     * @param failReason 실패 사유
     * @return 업데이트 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    boolean updatePaymentFailInfo(Long paymentId, String failReason) throws SQLException;
    
    /**
     * 결제를 삭제합니다.
     * 
     * @param paymentId 결제 ID
     * @return 삭제 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    boolean deletePayment(Long paymentId) throws SQLException;
    
    /**
     * 필터링된 결제 목록을 조회합니다.
     * 
     * @param filters 필터 조건
     * @return 필터링된 결제 목록
     * @throws SQLException SQL 예외 발생 시
     */
    List<Payment> getFilteredPayments(Map<String, Object> filters) throws SQLException;
    
    /**
     * 결제 통계를 조회합니다.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 결제 통계
     * @throws SQLException SQL 예외 발생 시
     */
    Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;
    
    /**
     * 결제 정보를 마스킹 처리합니다.
     * 
     * @param info 마스킹할 정보
     * @param type 정보 유형 (CARD, BANK, PHONE)
     * @return 마스킹된 정보
     */
    String maskPaymentInfo(String info, String type);
}
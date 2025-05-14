package com.ssafy.trip.payment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 결제 데이터 액세스 인터페이스
 */
@Mapper
public interface PaymentDao {
    /**
     * 새 결제 정보를 등록합니다.
     * 
     * @param payment 등록할 결제 정보
     * @return 생성된 결제의 ID
     * @throws SQLException SQL 예외 발생 시
     */
    Long insert(Payment payment) throws SQLException;
    
    /**
     * 결제 ID로 결제 정보를 조회합니다.
     * 
     * @param paymentId 결제 ID
     * @return 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment selectById(Long paymentId) throws SQLException;
    
    /**
     * 예약 ID로 결제 정보를 조회합니다.
     * 
     * @param reservationId 예약 ID
     * @return 결제 정보
     * @throws SQLException SQL 예외 발생 시
     */
    Payment selectByReservationId(Long reservationId) throws SQLException;
    
    /**
     * 사용자 ID로 결제 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 결제 목록
     * @throws SQLException SQL 예외 발생 시
     */
    List<Payment> selectByUserId(Long userId) throws SQLException;
    
    /**
     * 결제 상태를 업데이트합니다.
     * 
     * @param paymentId 결제 ID
     * @param status 새 상태
     * @param updatedAt 업데이트 시간
     * @return 업데이트된 행 수
     * @throws SQLException SQL 예외 발생 시
     */
    int updateStatus(@Param("paymentId") Long paymentId, 
                     @Param("status") String status, 
                     @Param("updatedAt") LocalDateTime updatedAt) throws SQLException;
    
    /**
     * 결제를 취소합니다.
     * 
     * @param paymentId 결제 ID
     * @param cancelReason 취소 사유
     * @param cancelledAt 취소 시간
     * @return 업데이트된 행 수
     * @throws SQLException SQL 예외 발생 시
     */
    int cancelPayment(@Param("paymentId") Long paymentId, 
                      @Param("cancelReason") String cancelReason, 
                      @Param("cancelledAt") LocalDateTime cancelledAt) throws SQLException;
    
    /**
     * 결제 실패 정보를 업데이트합니다.
     * 
     * @param paymentId 결제 ID
     * @param failReason 실패 사유
     * @param updatedAt 업데이트 시간
     * @return 업데이트된 행 수
     * @throws SQLException SQL 예외 발생 시
     */
    int updateFailInfo(@Param("paymentId") Long paymentId, 
                       @Param("failReason") String failReason, 
                       @Param("updatedAt") LocalDateTime updatedAt) throws SQLException;
    
    /**
     * 결제 정보를 삭제합니다.
     * 
     * @param paymentId 결제 ID
     * @return 삭제된 행 수
     * @throws SQLException SQL 예외 발생 시
     */
    int delete(Long paymentId) throws SQLException;
    
    /**
     * 필터링된 결제 목록을 조회합니다.
     * 
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 결제 목록
     * @throws SQLException SQL 예외 발생 시
     */
    List<Payment> selectFiltered(Map<String, Object> filters) throws SQLException;
    
    /**
     * 결제 통계를 조회합니다.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 결제 통계 (키: 통계 이름, 값: 통계 값)
     * @throws SQLException SQL 예외 발생 시
     */
    Map<String, Object> selectStatistics(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate) throws SQLException;
}
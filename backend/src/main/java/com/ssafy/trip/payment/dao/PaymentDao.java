package com.ssafy.trip.payment.dao;

import com.ssafy.trip.payment.model.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentDao {
    Long insert(Payment payment) throws SQLException;

    /**
     * 결제 정보와 함께 merchant_uid를 저장합니다.
     * @param payment 저장할 결제 정보 (merchantUid 포함)
     * @return 영향을 받은 행 수 (MyBatis Long id 반환 설정시 ID 반환 가능)
     */
    int insertPaymentWithMerchantUid(Payment payment) throws SQLException;

    Payment selectById(Long paymentId) throws SQLException;
    Payment selectByReservationId(Long reservationId) throws SQLException;

    /**
     * merchant_uid로 결제 정보를 조회합니다.
     * @param merchantUid 주문 ID
     * @return 결제 정보 (LIMIT 1 적용됨)
     */
    Payment selectByMerchantUid(String merchantUid) throws SQLException;

    /**
     * paymentKey (imp_uid)로 결제 정보를 조회합니다.
     * @param paymentKey 아임포트 거래 ID (imp_uid)
     * @return 결제 정보
     */
    Payment selectByPaymentKey(String paymentKey) throws SQLException;

    Payment selectByIdAndUserId(@Param("paymentId") Long paymentId, @Param("userId") Long userId) throws SQLException;
    
    List<Payment> selectByUserId(Long userId) throws SQLException;
    List<Payment> selectPageByUserId(@Param("userId") Long userId, @Param("offset") long offset, @Param("limit") int limit) throws SQLException;
    long countByUserId(@Param("userId") Long userId) throws SQLException;

    int updateStatus(@Param("paymentId") Long paymentId, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt) throws SQLException;
    int cancelPayment(@Param("paymentId") Long paymentId, @Param("cancelReason") String cancelReason, @Param("cancelledAt") LocalDateTime cancelledAt) throws SQLException;
    int updatePaymentAfterCancel(Payment payment) throws SQLException;
    int updateFailInfo(@Param("paymentId") Long paymentId, @Param("failReason") String failReason, @Param("updatedAt") LocalDateTime updatedAt) throws SQLException;
    int delete(Long paymentId) throws SQLException;
    List<Payment> selectFiltered(Map<String, Object> filters) throws SQLException;
    Map<String, Object> selectStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate) throws SQLException;
}

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
    Payment selectById(Long paymentId) throws SQLException;
    Payment selectByReservationId(Long reservationId) throws SQLException;
    List<Payment> selectByUserId(Long userId) throws SQLException;
    int updateStatus(@Param("paymentId") Long paymentId, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt) throws SQLException;
    int cancelPayment(@Param("paymentId") Long paymentId, @Param("cancelReason") String cancelReason, @Param("cancelledAt") LocalDateTime cancelledAt) throws SQLException;
    int updateFailInfo(@Param("paymentId") Long paymentId, @Param("failReason") String failReason, @Param("updatedAt") LocalDateTime updatedAt) throws SQLException;
    int delete(Long paymentId) throws SQLException;
    List<Payment> selectFiltered(Map<String, Object> filters) throws SQLException;
    Map<String, Object> selectStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate) throws SQLException;
}

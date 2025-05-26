package com.ssafy.trip.payment.service;

import com.ssafy.trip.payment.model.Payment;
import com.ssafy.trip.payment.dto.OrderDto;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    Payment validateAndSaveIamport(String impUid) throws Exception;
    Payment cancelIamportAndUpdate(String impUid) throws Exception;
}

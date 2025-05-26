package com.ssafy.trip.payment.dto;

import com.ssafy.trip.payment.model.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDto {
    private Long reservationId;
    private Long userId;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private String cardInfo;
    private String bankInfo;
    private String phoneInfo;
    private String impUid;

    public Payment toEntity() {
        return Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentMethod(paymentMethod)
                .paymentKey(impUid)
                .amount(amount)
                .currency(currency == null ? "KRW" : currency)
                .status("COMPLETED")
                .cardInfo(cardInfo)
                .bankInfo(bankInfo)
                .phoneInfo(phoneInfo)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

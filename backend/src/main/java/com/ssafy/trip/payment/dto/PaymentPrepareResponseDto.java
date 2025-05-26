package com.ssafy.trip.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPrepareResponseDto {
    private String merchantUid;      // 가맹점 주문번호
    private BigDecimal amount;         // 결제 요청 금액
    private String paymentName;      // 주문명 (예: "객실 A 외 2건")
    private String buyerEmail;       // 구매자 이메일 (선택적, 클라이언트에서 채울 수도 있음)
    private String buyerName;        // 구매자 이름 (선택적)
    private String buyerTel;         // 구매자 연락처 (선택적)
    // 필요에 따라 PG사 관련 정보나 리다이렉트 URL 등 추가 가능
} 
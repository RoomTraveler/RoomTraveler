package com.ssafy.trip.payment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentCompleteRequestDto {
    private String impUid;      // 아임포트 결제 고유 ID (rsp.imp_uid)
    private String merchantUid; // 주문 ID (rsp.merchant_uid)
    // 필요시 결제 성공 여부 (rsp.success) 등 추가 정보 받을 수 있으나, 백엔드에서 직접 검증하므로 필수 아님
} 
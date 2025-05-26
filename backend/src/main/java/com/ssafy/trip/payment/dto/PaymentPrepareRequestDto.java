package com.ssafy.trip.payment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class PaymentPrepareRequestDto {
    // 사용자가 결제하고자 하는 예약 건들의 상세 정보 리스트
    private List<ReservationCreationDto> reservationsToCreate;
    private String specialRequests; // 모든 예약에 공통으로 적용될 특별 요청 사항 (선택적)
} 
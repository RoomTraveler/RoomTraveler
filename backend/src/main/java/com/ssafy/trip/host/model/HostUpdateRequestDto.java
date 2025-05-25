package com.ssafy.trip.host.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class HostUpdateRequestDto {

    // 사업자 번호는 일반적으로 수정 불가 항목으로 간주하여 제외
    // private String businessNumber;

    private String businessName;       // 상호명
    private String ceoName;            // 대표자명
    private String businessAddress;    // 사업장 주소
    private String businessPhone;      // 사업장 전화번호
    private LocalDate businessLicenseExpire; // 사업자 등록증 만료일

    private String bankName;           // 정산 은행명
    private String bankAccount;        // 정산 계좌번호
    private String bankOwner;          // 정산 예금주명

    private String description;        // 호스트 자기소개

    private BigDecimal latitude;           // 위도
    private BigDecimal longitude;          // 경도

    private String businessType;       // 사업자 업종(호텔, 펜션 등)

    // 사업자 등록증 이미지(businessLicense)는 별도의 API (예: 재제출) 또는
    // HostService의 updateHostInfo 메소드에서 MultipartFile로 직접 처리하므로 DTO에서는 제외합니다.
    // 또는, 이미지 URL을 직접 업데이트하도록 할 수도 있지만, 파일 업로드가 일반적입니다.
} 
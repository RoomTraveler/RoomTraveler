package com.ssafy.trip.host.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.math.BigDecimal;
// javax.validation.constraints.* 또는 jakarta.validation.constraints.* 는 필요에 따라 추가

@Getter
@Setter
@NoArgsConstructor
@ToString
public class HostRegistrationRequestDto {

    // @NotBlank(message = "사업자 등록번호는 필수입니다.")
    private String businessNumber;     // 사업자 등록번호

    // @NotBlank(message = "상호명은 필수입니다.")
    private String businessName;       // 상호명

    // @NotBlank(message = "대표자명은 필수입니다.")
    private String ceoName;            // 대표자명

    // @NotBlank(message = "사업장 주소는 필수입니다.")
    private String businessAddress;    // 사업장 주소

    private String businessPhone;      // 사업장 전화번호 (선택적)

    private LocalDate businessLicenseExpire; // 사업자 등록증 만료일 (선택적)

    // @NotBlank(message = "정산 은행명은 필수입니다.")
    private String bankName;           // 정산 은행명

    // @NotBlank(message = "정산 계좌번호는 필수입니다.")
    private String bankAccount;        // 정산 계좌번호

    // @NotBlank(message = "정산 예금주명은 필수입니다.")
    private String bankOwner;          // 정산 예금주명

    private String description;        // 호스트 자기소개 (선택적)

    private BigDecimal latitude;           // 위도 (선택적)

    private BigDecimal longitude;          // 경도 (선택적)

    private String businessType;       // 사업자 업종 (선택적, 예: 호텔, 펜션 등)

    // user_id는 컨트롤러 레벨에서 @AuthenticationPrincipal 등을 통해 주입받아 서비스로 전달
    // business_license (사업자 등록증 이미지 파일)는 컨트롤러에서 MultipartFile로 직접 받아 서비스로 전달
} 
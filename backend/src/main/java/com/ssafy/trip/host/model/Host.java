package com.ssafy.trip.host.model;

import com.ssafy.trip.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 호스트 정보를 담는 클래스
 */
@Getter
@Setter
@ToString
public class Host {
    private Long hostId;                 // 호스트 PK
    private Long userId;                 // 회원 PK (users.user_id)
    private String businessNumber;       // 사업자 등록번호
    private String businessName;         // 상호명
    private String ceoName;              // 대표자명
    private String businessAddress;      // 사업장 주소
    private String businessPhone;        // 사업장 전화번호
    private String businessLicense;      // 사업자 등록증 이미지 URL

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate businessLicenseExpire; // 사업자 등록증 만료일

    private String licenseResubmitUrl;   // 사업자 등록증 재제출 이미지 URL

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime licenseResubmittedAt; // 사업자 등록증 재제출 일시

    private String bankName;             // 정산 은행명
    private String bankAccount;          // 정산 계좌번호
    private String bankOwner;            // 정산 예금주명
    private String description;          // 호스트 자기소개
    private BigDecimal latitude;           // 위도
    private BigDecimal longitude;          // 경도
    private String status;               // 상태(WAIT: 심사중, ACTIVE: 승인, REJECT: 반려)
    private String adminComment;         // 관리자 심사 코멘트

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;        // 최초 승인일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime rejectedAt;        // 최초 반려일

    private String businessType;         // 사업자 업종(호텔, 펜션 등)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // 연관된 User 객체 (MyBatis resultMap에서 사용)
    private User user;

    // 생성자, getter, setter, toString 등은 Lombok이 자동으로 생성
}
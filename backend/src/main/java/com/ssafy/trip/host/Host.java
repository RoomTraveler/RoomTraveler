package com.ssafy.trip.host;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 호스트 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Host {
    private Long hostId;             // 호스트 ID (users.user_id와 1:1 매핑)
    private String businessName;     // 업체명
    private String businessRegNo;    // 사업자 등록번호
    private String bankAccount;      // 정산 계좌 정보
    private String profileText;      // 호스트 소개글
    private String hostStatus;       // 심사 상태 (PENDING, APPROVED, REJECTED)
    private String createdAt;        // 생성 시간
    private String updatedAt;        // 수정 시간
    
    // 추가 필드 - User 정보와 함께 조회할 때 사용
    private String username;         // 사용자 닉네임
    private String email;            // 사용자 이메일
    private String role;             // 사용자 권한
}
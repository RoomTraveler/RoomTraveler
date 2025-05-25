package com.ssafy.trip.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long userId;         // 사용자 ID
    private String username;     // 닉네임
    private String email;        // 로그인용 이메일
    private String password;     // 해시 처리된 비밀번호
    private String phone;        // 휴대폰 번호
    private String profileImage; // 프로필 이미지 URL
    private String role;         // 권한 구분 (USER, HOST, ADMIN)
    private String userAccountStatus; // 사용자 계정 상태 (users.status)
    private String refreshToken;
    private String createdAt;    // 생성 시간
    private String updatedAt;    // 수정 시간

    // Host specific fields
    private Long hostId; // 호스트 ID 추가
    private String businessName; 
    private String businessNumber; 
    private String hostRegistrationStatus; // 호스트 등록 상태 (hosts.status)
    // Optional: Add other fields from hosts table if needed, e.g.:
    // private String ceoName;
}
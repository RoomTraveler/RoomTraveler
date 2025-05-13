package com.ssafy.trip.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 인증 응답 DTO
 * 로그인 또는 회원가입 성공 시 클라이언트에게 반환되는 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private UserDto user;
    
    /**
     * 사용자 정보 DTO (중첩 클래스)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String email;
        private String name;
        private String role;
        private String avatar;
        private String createdAt;
    }
}
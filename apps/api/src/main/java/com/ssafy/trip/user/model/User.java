package com.ssafy.trip.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 담는 모델 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String address;
    private String role;
    private String avatar;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
}
package com.ssafy.trip.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String confirmPassword;
    private String name;
    private String phone;
    private String address;
    private boolean termsAgreement;
    private boolean privacyAgreement;
    private boolean marketingAgreement;
}
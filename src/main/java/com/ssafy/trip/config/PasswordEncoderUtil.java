package com.ssafy.trip.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 인코딩 유틸리티 클래스
 * 비밀번호 인코딩 및 검증 기능을 제공합니다.
 */
@Component
public class PasswordEncoderUtil {

    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordEncoderUtil(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 비밀번호를 인코딩합니다.
     * 
     * @param rawPassword 인코딩할 원본 비밀번호
     * @return 인코딩된 비밀번호
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 원본 비밀번호와 인코딩된 비밀번호가 일치하는지 확인합니다.
     * 
     * @param rawPassword 원본 비밀번호
     * @param encodedPassword 인코딩된 비밀번호
     * @return 일치 여부
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 평문 비밀번호와 저장된 비밀번호를 비교합니다.
     * 저장된 비밀번호가 인코딩되지 않은 경우 평문 비교를 수행합니다.
     * 
     * @param rawPassword 원본 비밀번호
     * @param storedPassword 저장된 비밀번호 (인코딩 또는 평문)
     * @return 일치 여부
     */
    public boolean matchesWithPlainTextFallback(String rawPassword, String storedPassword) {
        // BCrypt로 인코딩된 비밀번호는 $2a$, $2b$, $2y$ 등으로 시작
        if (storedPassword != null && (storedPassword.startsWith("$2a$") || 
                                      storedPassword.startsWith("$2b$") || 
                                      storedPassword.startsWith("$2y$"))) {
            // 인코딩된 비밀번호인 경우 BCrypt 매칭 사용
            return passwordEncoder.matches(rawPassword, storedPassword);
        } else {
            // 평문 비밀번호인 경우 직접 비교
            return rawPassword.equals(storedPassword);
        }
    }
}
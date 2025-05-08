package com.ssafy.trip.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * 비밀번호 인코딩 유틸리티 클래스
 * 비밀번호 인코딩 및 검증 기능을 제공합니다.
 * Spring Security 의존성 제거로 인해 단순화된 버전입니다.
 */
@Component
public class PasswordEncoderUtil {

    /**
     * 비밀번호를 인코딩합니다.
     * 현재는 평문으로 반환하지만, 필요시 해시 함수를 구현할 수 있습니다.
     * 
     * @param rawPassword 인코딩할 원본 비밀번호
     * @return 인코딩된 비밀번호
     */
    public String encode(String rawPassword) {
        // 현재 애플리케이션은 평문 비밀번호를 사용하므로 그대로 반환
        // 필요시 아래 주석 처리된 해시 함수를 활성화할 수 있습니다.
        return rawPassword;

        // SHA-256 해시 함수 구현 예시
        // try {
        //     MessageDigest digest = MessageDigest.getInstance("SHA-256");
        //     byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        //     return Base64.getEncoder().encodeToString(hash);
        // } catch (NoSuchAlgorithmException e) {
        //     throw new RuntimeException("비밀번호 인코딩 중 오류가 발생했습니다.", e);
        // }
    }

    /**
     * 원본 비밀번호와 저장된 비밀번호가 일치하는지 확인합니다.
     * 
     * @param rawPassword 원본 비밀번호
     * @param storedPassword 저장된 비밀번호
     * @return 일치 여부
     */
    public boolean matches(String rawPassword, String storedPassword) {
        // 현재 애플리케이션은 평문 비밀번호를 사용하므로 직접 비교
        return rawPassword.equals(storedPassword);
    }

    /**
     * 평문 비밀번호와 저장된 비밀번호를 비교합니다.
     * Spring Security 제거로 인해 단순 비교만 수행합니다.
     * 
     * @param rawPassword 원본 비밀번호
     * @param storedPassword 저장된 비밀번호
     * @return 일치 여부
     */
    public boolean matchesWithPlainTextFallback(String rawPassword, String storedPassword) {
        // 평문 비밀번호 직접 비교
        return rawPassword.equals(storedPassword);
    }
}

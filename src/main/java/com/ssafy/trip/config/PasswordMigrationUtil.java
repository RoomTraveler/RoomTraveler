package com.ssafy.trip.config;

import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 비밀번호 마이그레이션 유틸리티 클래스
 * 평문 비밀번호를 인코딩된 비밀번호로 마이그레이션하는 기능을 제공합니다.
 */
@Component
public class PasswordMigrationUtil {

    private final PasswordEncoderUtil passwordEncoderUtil;
    private final UserService userService;

    public PasswordMigrationUtil(PasswordEncoderUtil passwordEncoderUtil, UserService userService) {
        this.passwordEncoderUtil = passwordEncoderUtil;
        this.userService = userService;
    }

    /**
     * 비밀번호가 마이그레이션이 필요한지 확인합니다.
     * 
     * @param password 저장된 비밀번호
     * @return 마이그레이션 필요 여부
     */
    public boolean needsMigration(String password) {
        // BCrypt로 인코딩된 비밀번호는 $2a$, $2b$, $2y$ 등으로 시작
        return password == null || !(password.startsWith("$2a$") || 
                                    password.startsWith("$2b$") || 
                                    password.startsWith("$2y$"));
    }

    /**
     * 사용자 로그인 시 비밀번호를 마이그레이션합니다.
     * 
     * @param email 사용자 이메일
     * @param rawPassword 원본 비밀번호
     * @return 마이그레이션 성공 여부
     */
    public boolean migratePasswordOnLogin(String email, String rawPassword) {
        try {
            // 사용자 조회
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return false;
            }
            
            // 비밀번호가 마이그레이션이 필요한지 확인
            if (needsMigration(user.getPassword())) {
                // 평문 비밀번호가 일치하는지 확인
                if (rawPassword.equals(user.getPassword())) {
                    // 비밀번호 인코딩 및 업데이트
                    String encodedPassword = passwordEncoderUtil.encode(rawPassword);
                    userService.updateUser(email, user.getUsername(), encodedPassword);
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            // 마이그레이션 실패 시 로그 기록 (실제 구현에서는 로깅 추가)
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 모든 사용자의 비밀번호를 마이그레이션합니다.
     * 주의: 이 메서드는 관리자만 실행해야 합니다.
     * 
     * @return 마이그레이션된 사용자 수
     */
    public int migrateAllPasswords() {
        int count = 0;
        try {
            // 모든 사용자 조회
            for (User user : userService.getUserList()) {
                // 비밀번호가 마이그레이션이 필요한지 확인
                if (needsMigration(user.getPassword())) {
                    // 비밀번호 인코딩 및 업데이트
                    String encodedPassword = passwordEncoderUtil.encode(user.getPassword());
                    userService.updateUser(user.getEmail(), user.getUsername(), encodedPassword);
                    count++;
                }
            }
        } catch (SQLException e) {
            // 마이그레이션 실패 시 로그 기록 (실제 구현에서는 로깅 추가)
            e.printStackTrace();
        }
        return count;
    }
}
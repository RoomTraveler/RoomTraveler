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
    private String password; // 비밀번호
    private String phone;        // 휴대폰 번호
    private String role;         // 권한 구분 (USER, HOST, ADMIN)
    private String status;       // 계정 상태 (ACTIVE, INACTIVE, SUSPENDED)
    private String createdAt;    // 생성 시간
    private String updatedAt;    // 수정 시간

    // JSP 뷰와 일치시키기 위한 name 게터
    public String getName() {
        return username;
    }

    // JSP 뷰와 일치시키기 위한 name 세터
    public void setName(String name) {
        this.username = name;
    }
}

package com.ssafy.trip.config;

import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 커스텀 사용자 상세 서비스
 * 스프링 시큐리티의 UserDetailsService 인터페이스 구현
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 이메일로 사용자 정보를 로드합니다.
     * 
     * @param email 사용자 이메일
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            // 이메일로 사용자 조회
            User user = userService.getUserByEmail(email);
            
            if (user == null) {
                throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
            }
            
            // 계정이 활성 상태인지 확인
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new UsernameNotFoundException("비활성화된 계정입니다: " + email);
            }
            
            // 권한 설정
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
            
            // Spring Security의 User 객체 반환
            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
            );
        } catch (SQLException e) {
            throw new UsernameNotFoundException("사용자 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
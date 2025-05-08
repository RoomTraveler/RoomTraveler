package com.ssafy.trip.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * 커스텀 인증 제공자
 * 평문 비밀번호와 인코딩된 비밀번호를 모두 지원합니다.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoderUtil passwordEncoderUtil;

    public CustomAuthenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoderUtil passwordEncoderUtil) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoderUtil = passwordEncoderUtil;
    }

    /**
     * 사용자 인증을 처리합니다.
     * 
     * @param authentication 인증 객체
     * @return 인증된 인증 객체
     * @throws AuthenticationException 인증 실패 시
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        try {
            // 사용자 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            // 비밀번호 검증 (평문 또는 인코딩된 비밀번호 모두 지원)
            if (passwordEncoderUtil.matchesWithPlainTextFallback(password, userDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(
                    userDetails, password, userDetails.getAuthorities());
            } else {
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("사용자를 찾을 수 없습니다: " + email);
        }
    }

    /**
     * 이 인증 제공자가 지원하는 인증 유형을 확인합니다.
     * 
     * @param authentication 인증 클래스
     * @return 지원 여부
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
package com.ssafy.trip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 스프링 시큐리티 설정 클래스
 * 애플리케이션의 보안 설정을 정의합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomAuthenticationProvider authenticationProvider;

    public SecurityConfig(BCryptPasswordEncoder passwordEncoder, CustomAuthenticationProvider authenticationProvider) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * 인증 관리자 설정
     * 
     * @param authConfig 인증 설정 객체
     * @return AuthenticationManager 객체
     * @throws Exception 예외 발생 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 설정
     * 
     * @param http HTTP 보안 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 인증 제공자 설정
            .authenticationProvider(authenticationProvider)

            // CSRF 보호 활성화
            .csrf(csrf -> csrf.disable())  // 개발 중에는 비활성화, 실제 운영 시 활성화 권장

            // 요청에 대한 권한 설정
            .authorizeHttpRequests(authorize -> authorize
                // 정적 리소스에 대한 접근 허용
                .requestMatchers("/css/**", "/js/**", "/img/**", "/resources/**").permitAll()
                // 메인 페이지, 로그인, 회원가입 페이지 접근 허용
                .requestMatchers("/", "/index", "/user/login-form", "/user/regist-user-form", "/user/regist-user", "/user/login").permitAll()
                // 관리자 페이지는 ADMIN 권한 필요
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 호스트 페이지는 HOST 또는 ADMIN 권한 필요
                .requestMatchers("/host/**").hasAnyRole("HOST", "ADMIN")
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // 폼 로그인 설정
            .formLogin(form -> form
                .loginPage("/user/login-form")  // 커스텀 로그인 페이지
                .loginProcessingUrl("/user/login")  // 로그인 처리 URL
                .defaultSuccessUrl("/", true)  // 로그인 성공 시 리다이렉트 URL
                .failureUrl("/user/login-form?error=true")  // 로그인 실패 시 리다이렉트 URL
                .usernameParameter("email")  // 사용자 이름 파라미터 (이메일 사용)
                .passwordParameter("password")  // 비밀번호 파라미터
                .permitAll()
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))  // 로그아웃 URL
                .logoutSuccessUrl("/")  // 로그아웃 성공 시 리다이렉트 URL
                .invalidateHttpSession(true)  // 세션 무효화
                .deleteCookies("JSESSIONID")  // 쿠키 삭제
                .permitAll()
            )

            // 접근 거부 처리
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")  // 접근 거부 시 리다이렉트 URL
            );

        return http.build();
    }
}

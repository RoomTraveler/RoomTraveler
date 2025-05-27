package com.ssafy.trip.security;

import com.ssafy.trip.security.jwt.JwtAuthenticationFilter;
import com.ssafy.trip.security.jwt.JwtUtil;
import com.ssafy.trip.security.jwt.JwtVerificationFilter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix() // role의 기본 prefix 설정: ROLE_
                .role("ADMIN").implies("HOST")
                .role("HOST").implies("USER")
                .build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                               @Qualifier("corsConfigurationSource")CorsConfigurationSource corsConfig,
                                               CustomUserDetailsService userDetailsService,
                                               JwtAuthenticationFilter authFilter,
                                               JwtVerificationFilter jwtVerificationFilter,
                                               SecurityExceptionHandlingFilter exceptionFilter) throws Exception {
        http.securityMatcher("/api/**")
                .cors(cors -> cors.configurationSource(corsConfig))
                .userDetailsService(userDetailsService)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(authorize ->
                authorize.requestMatchers(
                        "/api/user/auth/**",
                                "/api/user/refresh",
                                "/api/map/**",
                                "/api/attractions/**",
                                "/api/plans/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll() // 여기까지 permitAll 적용하고 체인 분리
                        .requestMatchers(HttpMethod.GET, "/api/accommodations/**").permitAll() // 숙소 정보 조회는 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll() // 리뷰 조회는 모두 허용
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasAnyRole("USER", "HOST", "ADMIN") // 리뷰 작성
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasAnyRole("USER", "HOST", "ADMIN") // 리뷰 수정
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasAnyRole("USER", "HOST", "ADMIN") // 리뷰 삭제
                        .requestMatchers(HttpMethod.GET,
                                "/api/events/**",
                                "/api/stats/**",
                                "/api/region/**").permitAll()
                        .requestMatchers("/api/host/register").hasRole("USER")
                        .requestMatchers("/api/admin/**", "/api/events/**","/api/stats/**","/api/region/**").hasRole("ADMIN")
                        .requestMatchers("/api/host/**").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers("/api/notifications/**", "/api/cart","/api/cart/**", "/api/v1/payments/**").hasAnyRole("USER", "HOST", "ADMIN")
                        .requestMatchers("/api/favorites/**").hasAnyRole("USER", "HOST", "ADMIN")
                        .requestMatchers("/api/**").authenticated() // 나머지 /api/** 경로는 인증 필요
                        .anyRequest().permitAll());

        http.addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(authFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionFilter, JwtVerificationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

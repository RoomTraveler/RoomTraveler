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
                authorize.requestMatchers("/api/user/auth/**", "/api/map/plans").permitAll()); // 바로 통과 시킬 경로

        http.addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(authFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionFilter, JwtVerificationFilter.class);

        return http.build();
    }

//    @Bean
//    @Order(2)
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(authorize -> authorize
//                                .anyRequest().permitAll()
//                .requestMatchers("/", "/login", "/accommodation", "/plan", "/api/user/register", "/api/user/login", "/user/login-form", "/swagger-ui/*").permitAll()
//                                // 2) Swagger UI & OpenAPI spec
//                                .requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
//                                .requestMatchers("/v3/api-docs/**").permitAll()
//                                .requestMatchers("/webjars/**").permitAll()
//
//                                // 3) 기타 public API
//                                .requestMatchers("/api/logistics").permitAll()
//                .requestMatchers("/accommodation/host/**").hasRole("HOST")
//                .requestMatchers("/accommodation/admin/**").hasRole("ADMIN")
//                .anyRequest().hasRole("USER")
//                )
//
//                .logout(logout -> logout
//                        .logoutUrl("/user/logout")
//                        .deleteCookies("JWT")
//                        .logoutSuccessHandler((request, response, authentication) -> {
//                            response.setStatus(HttpServletResponse.SC_OK);
//                            response.setContentType("application/json;charset=UTF-8");
//                            log.info("Logged out successfully????");
//                            String json = "{\"message\": \"로그아웃 성공했습니다!\"}";
//                            response.getWriter().write(json);
//                        })
//                )
//
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                );
//
//        http.addFilterBefore(jwtAuthenticationFilter(),
//                UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/member/checkEmail", configuration);

        return source;
    }
}

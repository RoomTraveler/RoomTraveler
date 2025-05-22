package com.ssafy.trip.security.jwt;

import com.ssafy.trip.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("[JwtVerificationFilter] Request URI: {} {}", request.getMethod(), request.getRequestURI());
        String authorizationHeader = request.getHeader("Authorization");
        log.debug("[JwtVerificationFilter] Authorization Header: {}", authorizationHeader);

        if ("/api/user/auth/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            log.debug("[JwtVerificationFilter] Skipping filter for /api/user/auth/login\"");
            filterChain.doFilter(request, response);
            return;
        }

        if ("/api/user/refresh".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            log.debug("[JwtVerificationFilter] Skipping filter for /api/user/refresh");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtUtil.resolveToken(request);
            log.debug("[JwtVerificationFilter] Resolved Token: {}", token);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                log.debug("[JwtVerificationFilter] Token is valid");
                Authentication authentication = jwtUtil.getAuthentication(token, userDetailsService);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("[JwtVerificationFilter] Set Authentication in SecurityContextHolder: {}", authentication);
            } else {
                log.debug("[JwtVerificationFilter] Token is invalid or not present");
            }
        } catch (JwtException e) {
            log.error("[JwtVerificationFilter] JWT Error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("[JwtVerificationFilter] General Error: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


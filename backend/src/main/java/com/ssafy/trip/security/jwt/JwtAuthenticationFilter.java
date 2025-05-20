package com.ssafy.trip.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.trip.security.CustomUserDetails;
import com.ssafy.trip.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.setFilterProcessesUrl("/api/auth/login");
        this.setUsernameParameter("email");
        this.setPasswordParameter("password");
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails.getEmail(), userDetails.getRole());
        Map<String, String> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("access_token", accessToken);
        handleResult(response, result, HttpStatus.OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        throw failed;
    }

    private void handleResult(HttpServletResponse response, Map<String,?> data, HttpStatus status) {
        response.setContentType("application/json");
        try {
            String jsonResponse = new ObjectMapper().writeValueAsString(data);
            response.setStatus(status.value());
            response.getWriter().write(jsonResponse);
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}

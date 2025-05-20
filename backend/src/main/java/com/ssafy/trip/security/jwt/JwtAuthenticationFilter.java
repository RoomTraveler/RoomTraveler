package com.ssafy.trip.security.jwt;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.trip.security.CustomUserDetails;
import com.ssafy.trip.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
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

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.setFilterProcessesUrl("/api/user/login");
        this.setUsernameParameter("email");
        this.setPasswordParameter("password");

//        import axios from 'axios';
//
//// 로그인 데이터
//const loginData = {
//                email: 'test',
//                password: '1234'
//};
//
//// URLSearchParams를 이용해 form-urlencoded 형식으로 변환
//const formBody = new URLSearchParams();
//        formBody.append('email', loginData.email);
//        formBody.append('password', loginData.password);
//
//// axios 요청
//        axios.post('/api/user/login', formBody, {
//                headers: {
//            'Content-Type': 'application/x-www-form-urlencoded'
//        }
//})
//.then(response => {
//                console.log('로그인 성공:', response.data);
//})
//.catch(error => {
//                console.error('로그인 실패:', error.response?.data || error.message);
//});
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails.getUser());
        Map<String, String> result = Map.of("status", "SUCCESS","access_token", accessToken);
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

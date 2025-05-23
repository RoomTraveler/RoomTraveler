package com.ssafy.trip.security.jwt;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.trip.security.CustomUserDetails;
import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
        this.setFilterProcessesUrl("/api/user/auth/login");
        this.setUsernameParameter("email");
        this.setPasswordParameter("password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (request.getContentType() != null && request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);
                String email = credentials.get(getUsernameParameter());
                String password = credentials.get(getPasswordParameter());

                if (email == null) email = "";
                if (password == null) password = "";

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email.trim(), password);
                setDetails(request, authRequest);
                return this.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException e) {
                throw new RuntimeException("Error processing JSON request: " + e.getMessage(), e);
            }
        }
        return super.attemptAuthentication(request, response);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        User user = userDetails.getUser();
        String accessToken = jwtUtil.generateAccessToken(user);

        String refreshToken = jwtUtil.generateRefreshToken(user);
        userService.updateUserRefresh(user.getUserId(), refreshToken);

        Map<String, String> result = Map.of("status", "SUCCESS","access_token", accessToken, "refreshToken", refreshToken);
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

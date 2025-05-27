package com.ssafy.trip.security.jwt;

import com.ssafy.trip.user.User;
import io.jsonwebtoken.*;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import com.ssafy.trip.security.CustomUserDetailsService;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;

    // @Value("${jwt.secret}") // 주석 처리 -> 주석 처리 유지 또는 제거
    // private String secretString; // 주석 처리 -> 주석 처리 유지 또는 제거

    public JwtUtil(/*@Value("${jwt.secret}") String secretString*/) { // 생성자 파라미터 제거 또는 주석 처리
        // this.key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8)); // 고정 시크릿 키 사용 로직 제거 또는 주석 처리
        this.key = Jwts.SIG.HS256.key().build(); // 동적 생성 로직으로 복원
    }

    @Value("${ssafy.jwt.access-expmin}")
    private int accessExpire;
    @Value("${ssafy.jwt.refresh-expmin}")
    private int refreshExpire;

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("name", user.getUsername());
        claims.put("role", user.getRole());
        if (user.getHostId() != null) {
            claims.put("hostId", user.getHostId());
        }
        log.debug("[JwtUtil] Access Token 생성. User ID: {}, Email: {}, Role: {}, Host ID: {}", user.getUserId(), user.getEmail(), user.getRole(), user.getHostId());
        return create("accessToken", accessExpire, claims);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("name", user.getUsername());
        claims.put("role", user.getRole());
        if (user.getHostId() != null) {
            claims.put("hostId", user.getHostId());
        }
        log.debug("[JwtUtil] Refresh Token 생성. User ID: {}, Email: {}, Role: {}, Host ID: {}", user.getUserId(), user.getEmail(), user.getRole(), user.getHostId());
        return create("refreshToken", refreshExpire, claims);
    }

    private String create(String subject, long expiration, Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + 1000 * 60 * expiration);
        String jwt = Jwts.builder().subject(subject).claims(claims).expiration(expirationDate).signWith(key).compact();
        log.debug("token 생성: {}", jwt);
        return jwt;
    }

    public boolean isTokenValid(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(key)
                    .build();

            parser.parse(token); // 예외 없으면 유효
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    public Claims getClaims(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();

        Jws<Claims> jws = parser.parseSignedClaims(token);

        return jws.getPayload();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        return isTokenValid(token);
    }

    public Authentication getAuthentication(String token, CustomUserDetailsService userDetailsService) {
        Claims claims = getClaims(token);
        String email = claims.get("email", String.class);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (userDetails != null) {
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        }
        return null;
    }
}

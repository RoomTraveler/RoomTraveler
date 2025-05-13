package com.ssafy.trip.user.controller;

import com.ssafy.trip.user.dto.AuthResponse;
import com.ssafy.trip.user.dto.LoginRequest;
import com.ssafy.trip.user.dto.RegisterRequest;
import com.ssafy.trip.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 API 컨트롤러
 * 사용자 인증 및 정보 관리를 위한 RESTful API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/users")
public class ApiUserController {

    // 임시 사용자 저장소 (실제 구현에서는 데이터베이스 사용)
    private final Map<String, User> userStore = new HashMap<>();
    
    /**
     * 컨트롤러 초기화 시 샘플 사용자 추가
     */
    public ApiUserController() {
        // 샘플 사용자 추가
        User sampleUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("password") // 실제 구현에서는 암호화 필요
                .name("홍길동")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .role("USER")
                .isActive(true)
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .build();
        
        userStore.put(sampleUser.getEmail(), sampleUser);
    }

    /**
     * 로그인 API
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 사용자 검증 (실제 구현에서는 데이터베이스 조회 및 비밀번호 검증)
        User user = userStore.get(request.getEmail());
        
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(false)
                    .message("이메일 또는 비밀번호가 올바르지 않습니다.")
                    .build());
        }
        
        // 인증 성공 응답
        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("로그인 성공")
                .token("sample-jwt-token") // 실제 구현에서는 JWT 토큰 생성
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole())
                        .avatar(user.getAvatar())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build());
    }

    /**
     * 회원가입 API
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // 이메일 중복 확인
        if (userStore.containsKey(request.getEmail())) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(false)
                    .message("이미 사용 중인 이메일 주소입니다.")
                    .build());
        }
        
        // 비밀번호 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(false)
                    .message("비밀번호와 비밀번호 확인이 일치하지 않습니다.")
                    .build());
        }
        
        // 약관 동의 확인
        if (!request.isTermsAgreement() || !request.isPrivacyAgreement()) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(false)
                    .message("필수 약관에 동의해야 합니다.")
                    .build());
        }
        
        // 사용자 생성 (실제 구현에서는 데이터베이스에 저장)
        User newUser = User.builder()
                .id(System.currentTimeMillis())
                .email(request.getEmail())
                .password(request.getPassword()) // 실제 구현에서는 암호화 필요
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role("USER")
                .isActive(true)
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .build();
        
        userStore.put(newUser.getEmail(), newUser);
        
        // 회원가입 성공 응답
        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("회원가입 성공")
                .user(AuthResponse.UserDto.builder()
                        .id(newUser.getId())
                        .email(newUser.getEmail())
                        .name(newUser.getName())
                        .role(newUser.getRole())
                        .createdAt(newUser.getCreatedAt())
                        .build())
                .build());
    }

    /**
     * 사용자 정보 조회 API
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // 실제 구현에서는 데이터베이스에서 ID로 조회
        User user = userStore.values().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 비밀번호는 응답에서 제외
        user.setPassword(null);
        
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 정보 업데이트 API
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        // 실제 구현에서는 데이터베이스에서 ID로 조회 후 업데이트
        User user = userStore.values().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 업데이트 가능한 필드만 업데이트
        if (userUpdate.getName() != null) {
            user.setName(userUpdate.getName());
        }
        if (userUpdate.getPhone() != null) {
            user.setPhone(userUpdate.getPhone());
        }
        if (userUpdate.getAddress() != null) {
            user.setAddress(userUpdate.getAddress());
        }
        if (userUpdate.getAvatar() != null) {
            user.setAvatar(userUpdate.getAvatar());
        }
        
        user.setUpdatedAt(LocalDateTime.now().toString());
        
        // 비밀번호는 응답에서 제외
        User responseUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        
        return ResponseEntity.ok(responseUser);
    }
}
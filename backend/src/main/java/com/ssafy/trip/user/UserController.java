package com.ssafy.trip.user;

import com.ssafy.trip.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 RESTful API 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "User Management", description = "API endpoints for user management including registration, login, profile management, etc.")
public class UserController {
	private final UserService userService;
	private final JwtUtil jwtUtil;

	/**
	 * 새 사용자를 등록합니다.
	 *
	 * @param user 등록할 사용자 정보
	 * @return 등록 결과 및 상태 코드
	 */
	@Operation(summary = "Register a new user", description = "Creates a new user account with the provided information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User successfully registered",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "Invalid input or registration failed",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Internal server error",
					content = @Content(mediaType = "application/json"))
	})
	@PostMapping("/auth/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		Map<String, Object> response = new HashMap<>();

		try {
			int result = userService.registUser(user);
			if (result > 0) {
				response.put("success", true);
				response.put("message", "사용자가 성공적으로 등록되었습니다.");
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				response.put("success", false);
				response.put("message", "사용자 등록에 실패했습니다.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		} catch (SQLException e) {
			response.put("success", false);
			response.put("message", "사용자 등록 중 오류가 발생했습니다: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 로그인 처리를 합니다.
	 *
	 * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
	 * @return 로그인 결과 및 상태 코드
	 */
	@Operation(summary = "User login", description = "Authenticates a user and creates a session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Login successful",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "401", description = "Invalid credentials",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Internal server error",
					content = @Content(mediaType = "application/json"))
	})
	@PostMapping("/auth/login")
	public ResponseEntity<?> login(@RequestBody User loginRequest) {
		Map<String, Object> res = new HashMap<>();

		try {
			User loginUser = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

			if (loginUser == null) {
				res.put("success", false);
				res.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
				return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
			}

			res.put("success", true);
			res.put("message", "로그인에 성공했습니다.");

			return new ResponseEntity<>(res, HttpStatus.OK);
		} catch (SQLException e) {
			res.put("success", false);
			res.put("message", "로그인 중 오류가 발생했습니다: " + e.getMessage());
			return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	/**
	 * 현재 로그인한 사용자 정보를 조회합니다.
	 * @return 사용자 정보 및 상태 코드
	 */
    @Operation(summary = "Get current user profile", description = "Retrieves the profile information of the currently logged-in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Not logged in",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/me")
	public ResponseEntity<?> getCurrentUser() {
		Map<String, Object> response = new HashMap<>();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			response.put("success", false);
			response.put("message", "로그인이 필요합니다.");
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		String email = ((UserDetails) authentication.getPrincipal()).getUsername();

		try {
			Optional<User> OptionalUser = userService.getUserByEmail(email);
			if (OptionalUser.isEmpty()) {
				response.put("success", false);
				response.put("message", "사용자 정보를 찾을 수 없습니다.");
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}

			// 비밀번호는 응답에서 제외
			User user = OptionalUser.get();
			user.setPassword(null);

			response.put("success", true);
			response.put("user", user);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (SQLException e) {
			response.put("success", false);
			response.put("message", "사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	/**
	 * 사용자 정보를 수정합니다.
	 *
	 * @param userUpdateRequest 수정할 사용자 정보
	 * @return 수정 결과 및 상태 코드
	 */
	@Operation(summary = "Update user profile", description = "Updates the profile information of the currently logged-in user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User profile updated successfully",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "Invalid input or update failed",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "401", description = "Not logged in",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "403", description = "Forbidden - cannot update other user's profile",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Internal server error",
					content = @Content(mediaType = "application/json"))
	})
	@PutMapping("/me")
	public ResponseEntity<?> updateUser(@RequestBody User userUpdateRequest) {
		Map<String, Object> response = new HashMap<>();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			response.put("success", false);
			response.put("message", "로그인이 필요합니다.");
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		String authenticatedEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

		// 이메일 위조 방지: 요청의 이메일과 인증된 이메일이 다르면 거부
		if (!authenticatedEmail.equals(userUpdateRequest.getEmail())) {
			response.put("success", false);
			response.put("message", "자신의 정보만 수정할 수 있습니다.");
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		}

		try {
			String rawPassword = userUpdateRequest.getPassword();

			int result = userService.updateUser(
					authenticatedEmail,
					userUpdateRequest.getUsername(),
					rawPassword
			);

			if (result > 0) {
				response.put("success", true);
				response.put("message", "사용자 정보가 성공적으로 수정되었습니다.");
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				response.put("success", false);
				response.put("message", "사용자 정보 수정에 실패했습니다.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		} catch (SQLException e) {
			response.put("success", false);
			response.put("message", "사용자 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 비밀번호를 변경합니다.
	 *
	 * @param passwordRequest 비밀번호 변경 요청 정보
	 * @return 변경 결과 및 상태 코드
	 */
    @Operation(summary = "Change password", description = "Changes the password of the currently logged-in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid input or password change failed",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Not logged in",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
	@PutMapping("/password")
	public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordRequest) {
		Map<String, Object> response = new HashMap<>();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			response.put("success", false);
			response.put("message", "로그인이 필요합니다.");
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		String email = ((UserDetails) authentication.getPrincipal()).getUsername(); // 사용자 이메일 꺼냄
		String newPassword = passwordRequest.get("password");

		if (newPassword == null || newPassword.trim().isEmpty()) {
			response.put("success", false);
			response.put("message", "새 비밀번호를 입력해주세요.");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			Optional<User> user = userService.getUserByEmail(email);
			if (user.isEmpty()) {
				response.put("success", false);
				response.put("message", "사용자 정보를 찾을 수 없습니다.");
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}

			int result = userService.updateUser(email, user.get().getUsername(), newPassword);
			if (result > 0) {
				response.put("success", true);
				response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				response.put("success", false);
				response.put("message", "비밀번호 변경에 실패했습니다.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		} catch (SQLException e) {
			response.put("success", false);
			response.put("message", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

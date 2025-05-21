package com.ssafy.trip.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ssafy.trip.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 알림 컨트롤러
 * 알림 관련 HTTP 요청을 처리합니다.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Management", description = "API endpoints for managing notifications including creating, reading, updating, and deleting notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 생성자 주입을 통한 의존성 주입
     * 
     * @param notificationService 알림 서비스
     */
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 사용자의 모든 알림을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 알림 목록
     */
    @Operation(summary = "Get all notifications for a user", description = "Retrieves all notifications for the specified user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getNotificationsByUserId(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자의 읽지 않은 알림을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 목록
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<?> getUnreadNotificationsByUserId(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("읽지 않은 알림 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 알림 ID로 알림을 조회합니다.
     * 
     * @param notificationId 알림 ID
     * @return 알림 정보
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotificationById(@PathVariable Long notificationId) {
        try {
            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("알림을 찾을 수 없습니다: " + notificationId);
            }
            return ResponseEntity.ok(notification);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 새 알림을 생성합니다.
     * 
     * @param notification 생성할 알림 정보
     * @return 생성된 알림 ID
     */
    @Operation(summary = "Create a new notification", description = "Creates a new notification with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notification successfully created", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<?> createNotification(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Notification notification) {
        if (userDetails == null && notification.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 ID가 필요합니다.");
        }
        // 현재 로그인한 사용자의 ID를 우선 사용, 없으면 요청 바디의 userId 사용
        Long currentUserId = (userDetails != null) ? userDetails.getUserId() : notification.getUserId();
        notification.setUserId(currentUserId);

        try {
            Long notificationId = notificationService.createNotification(notification);
            Map<String, Object> response = new HashMap<>();
            response.put("notificationId", notificationId);
            response.put("message", "알림이 성공적으로 생성되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 예약 관련 알림을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param content 알림 내용
     * @param reservationId 예약 ID
     * @return 생성된 알림 ID
     */
    @PostMapping("/booking")
    public ResponseEntity<?> createBookingNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long reservationId) {
        try {
            Long notificationId = notificationService.createBookingNotification(userId, title, content, reservationId);
            Map<String, Object> response = new HashMap<>();
            response.put("notificationId", notificationId);
            response.put("message", "예약 알림이 성공적으로 생성되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예약 알림 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 시스템 알림을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param content 알림 내용
     * @return 생성된 알림 ID
     */
    @PostMapping("/system")
    public ResponseEntity<?> createSystemNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content) {
        try {
            Long notificationId = notificationService.createSystemNotification(userId, title, content);
            Map<String, Object> response = new HashMap<>();
            response.put("notificationId", notificationId);
            response.put("message", "시스템 알림이 성공적으로 생성되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("시스템 알림 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 알림을 읽음 상태로 표시합니다.
     * 
     * @param notificationId 알림 ID
     * @return 업데이트 결과
     */
    @Operation(summary = "Mark notification as read", description = "Marks the specified notification as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification successfully marked as read", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Notification not found", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long notificationId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        // TODO: 해당 알림이 현재 로그인한 사용자의 것인지 확인하는 로직 추가 권장
        try {
            boolean updated = notificationService.markAsRead(notificationId);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("알림을 찾을 수 없습니다: " + notificationId);
            }
            return ResponseEntity.ok("알림이 읽음 상태로 표시되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 알림을 읽지 않음 상태로 표시합니다.
     * 
     * @param notificationId 알림 ID
     * @return 업데이트 결과
     */
    @PutMapping("/{notificationId}/unread")
    public ResponseEntity<?> markAsUnread(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long notificationId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        // TODO: 해당 알림이 현재 로그인한 사용자의 것인지 확인하는 로직 추가 권장
        try {
            boolean updated = notificationService.markAsUnread(notificationId);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("알림을 찾을 수 없습니다: " + notificationId);
            }
            return ResponseEntity.ok("알림이 읽지 않음 상태로 표시되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자의 모든 알림을 읽음 상태로 표시합니다.
     * 
     * @param userId 사용자 ID
     * @return 업데이트 결과
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        try {
            int count = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(count + "개의 알림이 읽음 상태로 표시되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 알림을 삭제합니다.
     * 
     * @param notificationId 알림 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long notificationId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        // TODO: 해당 알림이 현재 로그인한 사용자의 것인지 확인하는 로직 추가 권장
        try {
            boolean deleted = notificationService.deleteNotification(notificationId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("알림을 찾을 수 없습니다: " + notificationId);
            }
            return ResponseEntity.ok("알림이 성공적으로 삭제되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자의 모든 알림을 삭제합니다.
     * 
     * @param userId 사용자 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteAllNotificationsByUserId(@PathVariable Long userId) {
        try {
            int count = notificationService.deleteAllNotificationsByUserId(userId);
            return ResponseEntity.ok(count + "개의 알림이 삭제되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 현재 로그인한 사용자의 알림 목록을 페이지네이션 및 필터링하여 조회합니다.
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @param type 알림 유형 필터
     * @param readStatus 읽음 상태 필터 (UNREAD, READ, null)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 당 항목 수
     * @param sortBy 정렬 기준 필드
     * @param sortDir 정렬 방향 (ASC, DESC)
     * @return 페이지네이션된 알림 목록
     */
    @Operation(summary = "Get notifications for the current user with pagination and filters", description = "Retrieves a paginated list of notifications for the currently authenticated user, with optional filters for type and read status, and sorting.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized if user is not logged in"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getNotificationsForCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String readStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = userDetails.getUserId();

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDir.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // String으로 받은 readStatus를 Boolean으로 변환 (null, true, false)
            Boolean isRead = null;
            if ("READ".equalsIgnoreCase(readStatus)) {
                isRead = true;
            } else if ("UNREAD".equalsIgnoreCase(readStatus)) {
                isRead = false;
            }

            // 서비스 메소드 호출 (새로운 서비스 메소드 필요)
            Page<Notification> notificationPage = notificationService.getNotificationsByUserIdWithFilter(userId, type, isRead, pageable);
            return ResponseEntity.ok(notificationPage);
        } catch (IllegalArgumentException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 정렬 파라미터입니다: " + e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 현재 로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 읽지 않은 알림 개수
     */
    @Operation(summary = "Get unread notification count for the current user", description = "Retrieves the count of unread notifications for the currently authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved unread notification count",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "integer"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized if user is not logged in"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/count/unread")
    public ResponseEntity<?> getUnreadNotificationCountForCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = userDetails.getUserId();

        try {
            long count = notificationService.getUnreadNotificationCountByUserId(userId); // 새로운 서비스 메소드 필요
            return ResponseEntity.ok(count);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("읽지 않은 알림 개수 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자의 모든 알림을 읽음 상태로 표시합니다. (현재 사용자 기준으로 변경)
     * 
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 업데이트 결과
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsReadForCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = userDetails.getUserId();
        try {
            int count = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(count + "개의 알림이 읽음 상태로 표시되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자의 모든 알림을 삭제합니다. (현재 사용자 기준으로 변경)
     * 
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 삭제 결과
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllNotificationsForCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = userDetails.getUserId();
        try {
            int count = notificationService.deleteAllNotificationsByUserId(userId);
            return ResponseEntity.ok(count + "개의 알림이 삭제되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("알림 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

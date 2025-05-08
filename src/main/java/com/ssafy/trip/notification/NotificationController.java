package com.ssafy.trip.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 컨트롤러
 * 알림 관련 HTTP 요청을 처리합니다.
 */
@RestController
@RequestMapping("/api/notifications")
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
    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Notification notification) {
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
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
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
    public ResponseEntity<?> markAsUnread(@PathVariable Long notificationId) {
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
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
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
}
package com.ssafy.trip.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    private Long notificationId;    // 알림 ID
    private Long userId;            // 사용자 ID
    private String title;           // 알림 제목
    private String content;         // 알림 내용
    private String notificationType; // 알림 유형 (BOOKING, CANCELLATION, REVIEW, SYSTEM)
    private Long referenceId;       // 참조 ID (예약 ID, 리뷰 ID 등)
    private Boolean isRead;         // 읽음 여부
    private LocalDateTime createdAt; // 생성 시간
    
    // 추가 필드 - 조인 시 사용
    private String username;        // 사용자 이름
}
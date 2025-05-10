package com.ssafy.trip.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.sql.SQLException;
import java.util.List;

/**
 * 알림 뷰 컨트롤러
 * 알림 관련 웹 페이지 요청을 처리합니다.
 */
@Controller
@RequestMapping("/notification")
public class NotificationViewController {

    private final NotificationService notificationService;

    /**
     * 생성자 주입을 통한 의존성 주입
     * 
     * @param notificationService 알림 서비스
     */
    @Autowired
    public NotificationViewController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 알림 목록 페이지를 표시합니다.
     * 
     * @param model 모델
     * @param userId 세션에서 가져온 사용자 ID
     * @param filter 필터 (unread: 읽지 않은 알림만 표시)
     * @return 알림 목록 뷰
     */
    @GetMapping
    public String getNotificationList(
            Model model,
            @SessionAttribute(name = "userId", required = false) Long userId,
            @RequestParam(required = false) String filter) {
        
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            List<Notification> notifications;
            
            // 필터에 따라 알림 목록 조회
            if ("unread".equals(filter)) {
                notifications = notificationService.getUnreadNotificationsByUserId(userId);
                model.addAttribute("filterType", "unread");
            } else {
                notifications = notificationService.getNotificationsByUserId(userId);
                model.addAttribute("filterType", "all");
            }
            
            // 읽지 않은 알림 개수 계산
            long unreadCount = notifications.stream()
                    .filter(n -> !n.getIsRead())
                    .count();
            
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("userId", userId);
            
            return "notification/notification-list";
            
        } catch (SQLException e) {
            // 오류 발생 시 메인 페이지로 리다이렉트
            return "redirect:/?alertMsg=알림 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
    
    /**
     * 헤더에 표시할 읽지 않은 알림 개수를 조회하는 API
     * 
     * @param userId 세션에서 가져온 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    @GetMapping("/count/unread")
    public String getUnreadNotificationCount(
            Model model,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        
        if (userId == null) {
            model.addAttribute("unreadCount", 0);
        } else {
            try {
                List<Notification> unreadNotifications = notificationService.getUnreadNotificationsByUserId(userId);
                model.addAttribute("unreadCount", unreadNotifications.size());
            } catch (SQLException e) {
                model.addAttribute("unreadCount", 0);
            }
        }
        
        return "fragments/notification-count";
    }
}
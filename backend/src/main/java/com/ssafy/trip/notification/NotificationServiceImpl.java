package com.ssafy.trip.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 서비스 구현 클래스
 * 알림 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;

    /**
     * 생성자 주입을 통한 의존성 주입
     * 
     * @param notificationDao 알림 DAO
     */
    @Autowired
    public NotificationServiceImpl(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    /**
     * 새 알림을 생성합니다.
     */
    @Override
    @Transactional
    public Long createNotification(Notification notification) throws SQLException {
        // 생성 시간이 설정되지 않은 경우 현재 시간으로 설정
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        
        // 읽음 상태가 설정되지 않은 경우 기본값으로 false(읽지 않음) 설정
        if (notification.getIsRead() == null) {
            notification.setIsRead(false);
        }
        
        notificationDao.insert(notification);
        return notification.getNotificationId();
    }

    /**
     * 알림 ID로 알림을 조회합니다.
     */
    @Override
    public Notification getNotificationById(Long notificationId) throws SQLException {
        return notificationDao.selectById(notificationId);
    }

    /**
     * 사용자 ID로 모든 알림을 조회합니다.
     */
    @Override
    public List<Notification> getNotificationsByUserId(Long userId) throws SQLException {
        return notificationDao.selectByUserId(userId);
    }

    /**
     * 사용자 ID로 읽지 않은 알림을 조회합니다.
     */
    @Override
    public List<Notification> getUnreadNotificationsByUserId(Long userId) throws SQLException {
        return notificationDao.selectUnreadByUserId(userId);
    }

    /**
     * 알림을 읽음 상태로 표시합니다.
     */
    @Override
    @Transactional
    public boolean markAsRead(Long notificationId) throws SQLException {
        return notificationDao.updateReadStatus(notificationId, true) > 0;
    }

    /**
     * 알림을 읽지 않음 상태로 표시합니다.
     */
    @Override
    @Transactional
    public boolean markAsUnread(Long notificationId) throws SQLException {
        return notificationDao.updateReadStatus(notificationId, false) > 0;
    }

    /**
     * 사용자의 모든 알림을 읽음 상태로 표시합니다.
     */
    @Override
    @Transactional
    public int markAllAsRead(Long userId) throws SQLException {
        return notificationDao.markAllAsRead(userId);
    }

    /**
     * 알림을 삭제합니다.
     */
    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId) throws SQLException {
        return notificationDao.delete(notificationId) > 0;
    }

    /**
     * 사용자의 모든 알림을 삭제합니다.
     */
    @Override
    @Transactional
    public int deleteAllNotificationsByUserId(Long userId) throws SQLException {
        return notificationDao.deleteAllByUserId(userId);
    }

    /**
     * 예약 관련 알림을 생성합니다.
     */
    @Override
    @Transactional
    public Long createBookingNotification(Long userId, String title, String content, Long reservationId) throws SQLException {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .notificationType("BOOKING")
                .referenceId(reservationId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        return createNotification(notification);
    }

    /**
     * 시스템 알림을 생성합니다.
     */
    @Override
    @Transactional
    public Long createSystemNotification(Long userId, String title, String content) throws SQLException {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        return createNotification(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByUserIdWithFilter(Long userId, String notificationType, Boolean isRead, Pageable pageable) throws SQLException {
        String sortBy = "createdAt"; // 기본 정렬 필드
        String sortDir = "DESC";   // 기본 정렬 방향

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next(); // 첫 번째 정렬 조건 사용
            sortBy = order.getProperty();
            sortDir = order.getDirection().name();
        }
        
        List<Notification> notifications = notificationDao.selectByUserIdWithFilter(
                userId,
                notificationType,
                isRead,
                pageable.getOffset(),
                pageable.getPageSize(),
                sortBy,
                sortDir
        );
        long totalCount = notificationDao.countByUserIdWithFilter(userId, notificationType, isRead);
        return new PageImpl<>(notifications, pageable, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCountByUserId(Long userId) throws SQLException {
        return notificationDao.countUnreadByUserId(userId);
    }
}
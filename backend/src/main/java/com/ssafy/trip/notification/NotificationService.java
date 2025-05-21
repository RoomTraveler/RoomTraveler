package com.ssafy.trip.notification;

import java.sql.SQLException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 알림 서비스 인터페이스
 * 알림 관련 비즈니스 로직을 처리합니다.
 */
public interface NotificationService {
    /**
     * 새 알림을 생성합니다.
     * 
     * @param notification 생성할 알림 객체
     * @return 생성된 알림의 ID
     * @throws SQLException SQL 예외 발생 시
     */
    Long createNotification(Notification notification) throws SQLException;
    
    /**
     * 알림 ID로 알림을 조회합니다.
     * 
     * @param notificationId 조회할 알림 ID
     * @return 조회된 알림 객체
     * @throws SQLException SQL 예외 발생 시
     */
    Notification getNotificationById(Long notificationId) throws SQLException;
    
    /**
     * 사용자 ID로 모든 알림을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 모든 알림 목록
     * @throws SQLException SQL 예외 발생 시
     */
    List<Notification> getNotificationsByUserId(Long userId) throws SQLException;
    
    /**
     * 사용자 ID로 읽지 않은 알림을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 읽지 않은 알림 목록
     * @throws SQLException SQL 예외 발생 시
     */
    List<Notification> getUnreadNotificationsByUserId(Long userId) throws SQLException;
    
    /**
     * 알림을 읽음 상태로 표시합니다.
     * 
     * @param notificationId 알림 ID
     * @return 업데이트 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    boolean markAsRead(Long notificationId) throws SQLException;
    
    /**
     * 알림을 읽지 않음 상태로 표시합니다.
     * 
     * @param notificationId 알림 ID
     * @return 업데이트 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    boolean markAsUnread(Long notificationId) throws SQLException;
    
    /**
     * 사용자의 모든 알림을 읽음 상태로 표시합니다.
     * 
     * @param userId 사용자 ID
     * @return 업데이트된 알림 수
     * @throws SQLException SQL 예외 발생 시
     */
    int markAllAsRead(Long userId) throws SQLException;
    
    /**
     * 알림을 삭제합니다.
     * 
     * @param notificationId 삭제할 알림 ID
     * @return 삭제 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    boolean deleteNotification(Long notificationId) throws SQLException;
    
    /**
     * 사용자의 모든 알림을 삭제합니다.
     * 
     * @param userId 사용자 ID
     * @return 삭제된 알림 수
     * @throws SQLException SQL 예외 발생 시
     */
    int deleteAllNotificationsByUserId(Long userId) throws SQLException;
    
    /**
     * 예약 관련 알림을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param content 알림 내용
     * @param reservationId 예약 ID
     * @return 생성된 알림의 ID
     * @throws SQLException SQL 예외 발생 시
     */
    Long createBookingNotification(Long userId, String title, String content, Long reservationId) throws SQLException;
    
    /**
     * 시스템 알림을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param content 알림 내용
     * @return 생성된 알림의 ID
     * @throws SQLException SQL 예외 발생 시
     */
    Long createSystemNotification(Long userId, String title, String content) throws SQLException;

    /**
     * 사용자 ID와 필터 조건에 따라 알림 목록을 페이지네이션하여 조회합니다.
     *
     * @param userId 사용자 ID
     * @param notificationType 알림 유형 필터 (null이면 전체)
     * @param isRead 읽음 상태 필터 (null이면 전체, true면 읽음, false면 읽지 않음)
     * @param pageable 페이지네이션 정보 (페이지 번호, 크기, 정렬)
     * @return 페이지네이션된 알림 목록
     * @throws SQLException SQL 예외 발생 시
     */
    Page<Notification> getNotificationsByUserIdWithFilter(Long userId, String notificationType, Boolean isRead, Pageable pageable) throws SQLException;

    /**
     * 사용자 ID로 읽지 않은 알림 개수를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     * @throws SQLException SQL 예외 발생 시
     */
    long getUnreadNotificationCountByUserId(Long userId) throws SQLException;
}
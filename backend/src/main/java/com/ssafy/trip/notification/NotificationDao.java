package com.ssafy.trip.notification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

/**
 * 알림 데이터 접근 객체 인터페이스
 */
@Mapper
public interface NotificationDao {
    /**
     * 새 알림을 추가합니다.
     */
    int insert(Notification notification) throws SQLException;

    /**
     * 알림 ID로 알림을 조회합니다.
     */
    Notification selectById(Long notificationId) throws SQLException;

    /**
     * 사용자 ID로 알림 목록을 조회합니다.
     */
    List<Notification> selectByUserId(Long userId) throws SQLException;

    /**
     * 사용자 ID로 읽지 않은 알림 목록을 조회합니다.
     */
    List<Notification> selectUnreadByUserId(Long userId) throws SQLException;

    /**
     * 알림을 읽음 상태로 업데이트합니다.
     */
    int updateReadStatus(@Param("notificationId") Long notificationId, @Param("isRead") Boolean isRead) throws SQLException;

    /**
     * 사용자의 모든 알림을 읽음 상태로 업데이트합니다.
     */
    int markAllAsRead(Long userId) throws SQLException;

    /**
     * 알림을 삭제합니다.
     */
    int delete(Long notificationId) throws SQLException;

    /**
     * 사용자의 모든 알림을 삭제합니다.
     */
    int deleteAllByUserId(Long userId) throws SQLException;
}
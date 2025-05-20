package com.ssafy.trip.notification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
// import org.springframework.data.domain.Sort; // MyBatis에서는 직접 사용보다 String으로 sortBy, sortDir 전달이 일반적

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

    /**
     * 사용자 ID와 필터 조건에 따라 알림 목록을 조회합니다. (페이지네이션 및 정렬 포함)
     */
    List<Notification> selectByUserIdWithFilter(@Param("userId") Long userId,
                                                @Param("notificationType") String notificationType,
                                                @Param("isRead") Boolean isRead,
                                                @Param("offset") long offset,
                                                @Param("limit") int limit,
                                                @Param("sortBy") String sortBy,      // 정렬 기준 필드명
                                                @Param("sortDir") String sortDir);   // 정렬 방향 (ASC, DESC)

    /**
     * 사용자 ID와 필터 조건에 따른 알림 총 개수를 조회합니다.
     */
    long countByUserIdWithFilter(@Param("userId") Long userId,
                                 @Param("notificationType") String notificationType,
                                 @Param("isRead") Boolean isRead);

    /**
     * 사용자 ID로 읽지 않은 알림 개수를 조회합니다.
     */
    long countUnreadByUserId(Long userId) throws SQLException;
}
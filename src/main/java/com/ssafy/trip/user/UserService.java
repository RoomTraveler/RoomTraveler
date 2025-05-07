package com.ssafy.trip.user;

import java.sql.SQLException;
import java.util.List;

/**
 * 사용자 서비스 인터페이스
 */
public interface UserService {

    /**
     * 새 사용자를 등록합니다.
     */
    int registUser(User user) throws SQLException;

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    User login(String email, String password) throws SQLException;

    /**
     * 모든 사용자 목록을 조회합니다.
     */
    List<User> getUserList() throws SQLException;

    /**
     * 모든 사용자 목록을 조회합니다. (AdminController용 별칭 메서드)
     */
    default List<User> getAllUsers() throws SQLException {
        return getUserList();
    }

    /**
     * 사용자 정보를 업데이트합니다.
     */
    int updateUser(String email, String username, String password) throws SQLException;

    /**
     * 사용자 상태를 업데이트합니다.
     */
    int updateUserStatus(Long userId, String status) throws SQLException;

    /**
     * 사용자를 삭제합니다.
     */
    int deleteUser(String email) throws SQLException;

    /**
     * 이름과 이메일로 비밀번호를 찾습니다.
     */
    String findPassword(String username, String email) throws SQLException;
}

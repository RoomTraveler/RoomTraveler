package com.ssafy.trip.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;

/**
 * 사용자 데이터 접근 객체 인터페이스
 */
@Mapper
public interface UserDao {
    /**
     * 새 사용자를 등록합니다.
     */
    int insert(User user) throws SQLException;

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    User login(String email, String passwordHash) throws SQLException;

    /**
     * 모든 사용자 목록을 조회합니다.
     */
    List<User> getUsers() throws SQLException;

    /**
     * 사용자 정보를 업데이트합니다.
     */
    int updateUser(String email, String username, String passwordHash) throws SQLException;

    /**
     * 사용자를 삭제합니다.
     */
    int deleteUser(String email) throws SQLException;

    /**
     * 이름과 이메일로 비밀번호를 찾습니다.
     */
    String findPassword(String username, String email) throws SQLException;

    // 6) 이메일로 사용자 ID 찾기
    Long findUserIdByEmail(String email);
}

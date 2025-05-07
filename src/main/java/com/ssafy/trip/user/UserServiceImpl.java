package com.ssafy.trip.user;

import java.sql.SQLException;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 서비스 구현 클래스
 */
@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao dao;

    /**
     * 새 사용자를 등록합니다.
     */
    @Override
    public int registUser(User user) throws SQLException {
    	return dao.insert(user);
    }

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    @Override
    public User login(String email, String password) throws SQLException {
    	return dao.login(email, password);
    }

    /**
     * 모든 사용자 목록을 조회합니다.
     */
    @Override
    public List<User> getUserList() throws SQLException {
    	return dao.getUsers();
    }

    /**
     * 사용자 정보를 업데이트합니다.
     */
    @Override
    public int updateUser(String email, String username, String password) throws SQLException {
    	return dao.updateUser(email, username, password);
    }

    /**
     * 사용자 상태를 업데이트합니다.
     */
    @Override
    public int updateUserStatus(Long userId, String status) throws SQLException {
        return dao.updateUserStatus(userId, status);
    }

    /**
     * 사용자를 삭제합니다.
     */
    @Override
    public int deleteUser(String email) throws SQLException {
    	return dao.deleteUser(email);
    }

    /**
     * 이름과 이메일로 비밀번호를 찾습니다.
     */
    @Override
    public String findPassword(String username, String email) throws SQLException {
    	return dao.findPassword(username, email);
    }
}

package com.ssafy.trip.user;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 서비스 구현 클래스
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao dao;
    private final PasswordEncoder passwordEncoder;

    /**
     * 새 사용자를 등록합니다.
     */
    @Override
    public int registUser(User user) throws SQLException {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return dao.insert(user);
    }

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    @Override
    public User login(String email, String password) throws SQLException {
        User loginUser = dao.login(email);
        if (loginUser != null) {
            log.info("loginUser {}", loginUser);
            if (passwordEncoder.matches(password, loginUser.getPassword())) {
                return loginUser;
            }
        }
        return null;
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
        password = (password != null && !password.isBlank())
                ? passwordEncoder.encode(password)
                : null;
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
     * 사용자 역할을 업데이트합니다.
     */
    @Override
    public int updateUserRole(Long userId, String role) throws SQLException {
        return dao.updateUserRole(userId, role);
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

    /**
     * 이메일로 사용자를 조회합니다.
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        return dao.getUserByEmail(email);
    }

    @Override
    public int updateUserRefresh(Long userId, String refreshToken) {
        return dao.updateUserRefreshToken(userId, refreshToken);
    }
}

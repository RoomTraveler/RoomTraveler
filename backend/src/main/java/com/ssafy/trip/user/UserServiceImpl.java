package com.ssafy.trip.user;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import com.ssafy.trip.s3.AWSS3Service;

/**
 * 사용자 서비스 구현 클래스
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AWSS3Service s3Service;

    /**
     * 새 사용자를 등록합니다.
     */
    @Override
    @Transactional
    public int registUser(User user) throws SQLException {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다.");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (userDao.getUserByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        userDao.insert(user);
        log.info("New user registered: {}", user.getEmail());
        return 1;
    }

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    @Override
    public User login(String email, String password) throws SQLException {
        User user = userDao.getUserByEmail(email);
        if (user != null && "ACTIVE".equals(user.getStatus()) && passwordEncoder.matches(password, user.getPassword())) {
            log.info("User login successful: {}", email);
            return user;
        }
        log.warn("User login failed: {}", email);
        return null;
    }

    /**
     * 모든 사용자 목록을 조회합니다.
     */
    @Override
    public List<User> getUserList() throws SQLException {
        return userDao.getUsers();
    }

    /**
     * 사용자 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateUser(String authenticatedUserEmail, String newUsername, String newPhone, String newRawPassword) throws SQLException {
        User existingUser = userDao.getUserByEmail(authenticatedUserEmail);
        if (existingUser == null) {
            log.warn("업데이트할 사용자를 찾을 수 없습니다. Email: {}", authenticatedUserEmail);
            return -1;
        }

        User userToUpdate = new User();
        userToUpdate.setUserId(existingUser.getUserId());
        boolean hasChanges = false;

        if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(existingUser.getUsername())) {
            userToUpdate.setUsername(newUsername);
            hasChanges = true;
        }

        if (newPhone != null && !newPhone.isEmpty() && !newPhone.equals(existingUser.getPhone())) {
            userToUpdate.setPhone(newPhone);
            hasChanges = true;
        }

        if (newRawPassword != null && !newRawPassword.isEmpty()) {
            userToUpdate.setPassword(passwordEncoder.encode(newRawPassword));
            hasChanges = true;
        }

        if (!hasChanges) {
            log.info("요청된 사용자 정보 변경 사항이 없습니다. Email: {}", authenticatedUserEmail);
            return 0;
        }
        
        int result = userDao.updateUserGeneral(userToUpdate);

        if (result > 0) {
            log.info("사용자 정보 업데이트 성공. UserId: {}", existingUser.getUserId());
        } else if (hasChanges) {
            log.warn("사용자 정보 업데이트 시도했으나 DB에 반영되지 않음. UserId: {}", existingUser.getUserId());
        }
        return result;
    }

    /**
     * 사용자 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateUserStatus(Long userId, String status) throws SQLException {
        User user = userDao.selectUserById(userId);
        if (user == null) throw new RuntimeException("User not found with id: " + userId);
        if (!("ACTIVE".equals(status) || "INACTIVE".equals(status) || "SUSPENDED".equals(status))) {
            throw new IllegalArgumentException("유효하지 않은 상태값입니다: " + status);
        }
        userDao.updateUserStatus(userId, status);
        log.info("User status updated. ID: {}, New Status: {}", userId, status);
        return 1;
    }

    /**
     * 사용자 역할을 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateUserRole(Long userId, String role) throws SQLException {
        User user = userDao.selectUserById(userId);
        if (user == null) throw new RuntimeException("User not found with id: " + userId);
        if (!("USER".equals(role) || "HOST".equals(role) || "ADMIN".equals(role))) {
            throw new IllegalArgumentException("유효하지 않은 역할값입니다: " + role);
        }
        userDao.updateUserRole(userId, role);
        log.info("User role updated. ID: {}, New Role: {}", userId, role);
        return 1;
    }

    /**
     * 사용자를 삭제합니다.
     */
    @Override
    @Transactional
    public int deleteUser(String email) throws SQLException {
        User user = userDao.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("삭제할 사용자를 찾을 수 없습니다. Email: " + email);
        }
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                s3Service.deleteImage(user.getProfileImage());
                log.info("사용자 삭제 중 프로필 이미지 삭제 완료 (URL: {}), Email: {}", user.getProfileImage(), email);
            } catch (Exception e) {
                log.error("사용자 삭제 중 S3 프로필 이미지 삭제 실패 (URL: {}), Email: {}, 오류: {}", user.getProfileImage(), email, e.getMessage());
            }
        }
        userDao.deleteUser(email);
        log.info("User deleted: {}", email);
        return 1;
    }

    /**
     * 이름과 이메일로 비밀번호를 찾습니다.
     */
    @Override
    public String findPassword(String username, String email) throws SQLException {
        log.warn("findPassword (보안 주의!): 사용자={}, 이메일={}. 실제 비밀번호 반환 로직은 사용하지 않습니다.", username, email);
        User user = userDao.getUserByEmail(email);
        if (user != null && user.getUsername().equals(username)) {
            throw new UnsupportedOperationException("실제 비밀번호를 반환하는 대신 임시 비밀번호 발급 및 이메일 전송 로직으로 대체해야 합니다.");
        }
        return null;
    }

    /**
     * 이메일로 사용자를 조회합니다.
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(userDao.getUserByEmail(email));
    }

    @Override
    @Transactional
    public int updateUserRefresh(Long userId, String refreshToken) {
        return userDao.updateUserRefreshToken(userId, refreshToken);
    }

    @Override
    @Transactional
    public void registerUser(User user) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                throw new IllegalArgumentException("이메일은 필수입니다.");
            }
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                throw new IllegalArgumentException("사용자 이름은 필수입니다.");
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다.");
            }
            if (userDao.getUserByEmail(user.getEmail()) != null) {
                throw new IllegalArgumentException("이미 사용중인 이메일입니다: " + user.getEmail());
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRole() == null) user.setRole("USER");
            if (user.getStatus() == null) user.setStatus("ACTIVE");
            userDao.insert(user);
            log.info("New user registered (void return): {}", user.getEmail());
        } catch (IllegalArgumentException e) {
            log.error("void registerUser Argument Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public User getUserById(Long userId) {
        return userDao.selectUserById(userId);
    }

    @Override
    @Transactional
    public void updateUserInfo(User userUpdates) {
        User existingUser = userDao.selectUserById(userUpdates.getUserId());
        if (existingUser == null) {
            throw new RuntimeException("업데이트할 사용자를 찾을 수 없습니다. ID: " + userUpdates.getUserId());
        }
        boolean changed = false;
        if (userUpdates.getUsername() != null && !userUpdates.getUsername().isEmpty() && !userUpdates.getUsername().equals(existingUser.getUsername())) {
            existingUser.setUsername(userUpdates.getUsername());
            changed = true;
        }
        if (userUpdates.getPhone() != null && !userUpdates.getPhone().equals(existingUser.getPhone())) {
            existingUser.setPhone(userUpdates.getPhone());
            changed = true;
        }
        if (changed) {
            userDao.updateUserInfo(existingUser);
            log.info("User info updated (username, phone only) for ID: {}", existingUser.getUserId());
        } else {
            log.info("요청된 사용자 정보(이름, 전화번호) 변경 사항이 없습니다. ID: {}", userUpdates.getUserId());
        }
    }

    @Override
    @Transactional
    public String updateUserProfileImage(Long userId, MultipartFile profileImageFile) throws SQLException, IOException {
        User user = userDao.selectUserById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId);
        }
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                s3Service.deleteImage(user.getProfileImage());
                log.info("S3에서 기존 프로필 이미지 삭제 완료: {}", user.getProfileImage());
            } catch (Exception e) {
                log.error("S3 이미지 삭제 중 오류 발생. Image URL: {}", user.getProfileImage(), e);
            }
        }
        String newImageUrl = null;
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            newImageUrl = s3Service.uploadFile(profileImageFile);
        } else {
            newImageUrl = null;
        }
        
        User imageUpdatePayload = new User();
        imageUpdatePayload.setUserId(userId);
        imageUpdatePayload.setProfileImage(newImageUrl);
        userDao.updateUserProfileImage(imageUpdatePayload);
        log.info("사용자 프로필 이미지 업데이트 완료. ID: {}, New Image URL: {}", userId, newImageUrl);
        return newImageUrl;
    }

    @Override
    public boolean checkPassword(Long userId, String password) {
        User user = userDao.selectUserById(userId);
        return user != null && password != null && passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public String findUserPassword(String username, String email) {
        log.warn("findUserPassword (보안 주의!): 사용자={}, 이메일={}. 실제 비밀번호 반환 로직은 사용하지 않습니다.", username, email);
        User user = userDao.getUserByEmail(email);
        if (user != null && user.getUsername().equals(username)) {
            throw new UnsupportedOperationException("실제 비밀번호를 반환하는 대신 임시 비밀번호 발급 및 이메일 전송 로직으로 대체해야 합니다.");
        }
        return null;
    }

    @Override
    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken) {
        User user = userDao.selectUserById(userId);
        if (user == null) throw new RuntimeException("User not found with id: " + userId);
        userDao.updateUserRefreshToken(userId, refreshToken);
        log.info("Refresh token saved for user ID: {}", userId);
    }

    @Override
    public String getRefreshToken(Long userId) {
        User user = userDao.selectUserById(userId);
        if (user == null) {
            log.warn("getRefreshToken: User not found with id: {}", userId);
            return null;
        }
        return user.getRefreshToken();
    }

    @Transactional
    public int updateUserPassword(String authenticatedUserEmail, String newRawPassword) throws SQLException {
        User existingUser = userDao.getUserByEmail(authenticatedUserEmail);
        if (existingUser == null) {
            log.warn("비밀번호를 변경할 사용자를 찾을 수 없습니다. Email: {}", authenticatedUserEmail);
            return -1;
        }
        if (newRawPassword == null || newRawPassword.isEmpty()) {
            log.warn("새 비밀번호가 비어있습니다. Email: {}", authenticatedUserEmail);
            return 0;
        }
        User passwordUpdatePayload = new User();
        passwordUpdatePayload.setUserId(existingUser.getUserId());
        passwordUpdatePayload.setPassword(passwordEncoder.encode(newRawPassword));
        userDao.updateUserPassword(passwordUpdatePayload);
        log.info("사용자 (이메일 기반) 비밀번호 업데이트 완료. Email: {}", authenticatedUserEmail);
        return 1;
    }

    @Override
    @Transactional
    public void updateUserPassword(Long userId, String currentPassword, String newPassword) {
        User user = userDao.selectUserById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId);
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        User passwordUpdatePayload = new User();
        passwordUpdatePayload.setUserId(userId);
        passwordUpdatePayload.setPassword(passwordEncoder.encode(newPassword));
        userDao.updateUserPassword(passwordUpdatePayload);
        log.info("사용자 비밀번호 업데이트 완료. ID: {}", userId);
    }
}

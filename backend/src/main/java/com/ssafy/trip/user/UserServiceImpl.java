package com.ssafy.trip.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import com.ssafy.trip.s3.AWSS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 서비스 구현 클래스
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String ROLE_USER = "USER";
    private static final String ROLE_HOST = "HOST";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    private static final String ACCOUNT_STATUS_INACTIVE = "INACTIVE";
    private static final String ACCOUNT_STATUS_SUSPENDED = "SUSPENDED";

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AWSS3Service s3Service;

    /**
     * 새 사용자를 등록합니다.
     */
    @Override
    @Transactional
    public void registUser(User user) throws SQLException {
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
            user.setRole(ROLE_USER);
        }
        if (user.getUserAccountStatus() == null) {
            user.setUserAccountStatus(ACCOUNT_STATUS_ACTIVE);
        }
        userDao.insert(user);
        log.info("New user registered: {}", user.getEmail());
    }

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public User login(String email, String password) throws SQLException {
        User user = userDao.getUserByEmail(email);
        if (user != null && ACCOUNT_STATUS_ACTIVE.equals(user.getUserAccountStatus()) && passwordEncoder.matches(password, user.getPassword())) {
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
    @Transactional(readOnly = true)
    public List<User> getUserList() throws SQLException {
        return userDao.getUsers();
    }

    /**
     * 사용자 정보를 업데이트합니다. (이름, 전화번호만 해당)
     * 비밀번호 변경은 updateUserPassword(Long userId, String currentPassword, String newPassword) 또는 관리자용 updateUserPassword(String authenticatedUserEmail, String newRawPassword) 메서드를 사용하세요.
     *
     * @param authenticatedUserEmail 인증된 사용자의 이메일
     * @param newUsername 새 사용자 이름 (변경 없으면 null 또는 기존값)
     * @param newPhone 새 전화번호 (변경 없으면 null 또는 기존값)
     * @param newRawPassword 이 파라미터는 더 이상 사용되지 않습니다. 비밀번호 변경은 전용 메서드를 이용하세요.
     * @return 업데이트 성공 시 1, 변경 사항 없을 시 0, 사용자 찾을 수 없을 시 -1
     * @throws SQLException SQL 예외 발생 시
     */
    @Override
    @Transactional
    public int updateUser(String authenticatedUserEmail, String newUsername, String newPhone, String newRawPassword) throws SQLException {
        User existingUser = userDao.getUserByEmail(authenticatedUserEmail);
        if (existingUser == null) {
            log.warn("업데이트할 사용자를 찾을 수 없습니다. Email: {}", authenticatedUserEmail);
            return -1;
        }

        User userToUpdate = User.builder().userId(existingUser.getUserId()).build();
        boolean hasChanges = false;

        if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(existingUser.getUsername())) {
            userToUpdate.setUsername(newUsername);
            hasChanges = true;
        }

        if (newPhone != null && !newPhone.isEmpty() && !newPhone.equals(existingUser.getPhone())) {
            userToUpdate.setPhone(newPhone);
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
        if (!(ACCOUNT_STATUS_ACTIVE.equals(status) || ACCOUNT_STATUS_INACTIVE.equals(status) || ACCOUNT_STATUS_SUSPENDED.equals(status))) {
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
        if (!(ROLE_USER.equals(role) || ROLE_HOST.equals(role) || ROLE_ADMIN.equals(role))) {
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
     * 이름과 이메일로 비밀번호를 찾습니다. (보안 주의: 실제 비밀번호 반환 로직 아님)
     * 중요: 이 메서드는 실제 비밀번호를 반환해서는 안 됩니다.
     * 대신, 임시 비밀번호를 안전하게 생성하여 사용자 이메일로 발송하는 로직으로 반드시 대체되어야 합니다.
     * 현재 구현은 보안상 취약하며, `UnsupportedOperationException`을 발생시켜 실제 사용을 방지합니다.
     */
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
            if (user.getRole() == null) user.setRole(ROLE_USER);
            if (user.getUserAccountStatus() == null) user.setUserAccountStatus(ACCOUNT_STATUS_ACTIVE);
            userDao.insert(user);
            log.info("New user registered (void return): {}", user.getEmail());
        } catch (IllegalArgumentException e) {
            log.error("void registerUser Argument Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
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

        if (userUpdates.getUsername() != null &&
            !userUpdates.getUsername().isEmpty() &&
            !userUpdates.getUsername().equals(existingUser.getUsername())) {
            existingUser.setUsername(userUpdates.getUsername());
            changed = true;
        }

        if (userUpdates.getPhone() != null &&
            !userUpdates.getPhone().isEmpty() &&
            !userUpdates.getPhone().equals(existingUser.getPhone())) {
            existingUser.setPhone(userUpdates.getPhone());
            changed = true;
        }

        if (userUpdates.getUserAccountStatus() != null &&
            !userUpdates.getUserAccountStatus().isEmpty() &&
            !userUpdates.getUserAccountStatus().equals(existingUser.getUserAccountStatus())) {
            String newStatus = userUpdates.getUserAccountStatus();
            if (!(ACCOUNT_STATUS_ACTIVE.equals(newStatus) || ACCOUNT_STATUS_INACTIVE.equals(newStatus) || ACCOUNT_STATUS_SUSPENDED.equals(newStatus))) {
                throw new IllegalArgumentException("유효하지 않은 상태값입니다: " + newStatus);
            }
            existingUser.setUserAccountStatus(newStatus);
            changed = true;
        }

        if (changed) {
            userDao.updateUserGeneral(existingUser);
            log.info("사용자 정보 업데이트 성공 (updateUserInfo 호출 -> updateUserGeneral DAO 사용). UserId: {}", existingUser.getUserId());
        } else {
            log.info("요청된 사용자 정보 변경 사항이 없습니다 (updateUserInfo). UserId: {}", existingUser.getUserId());
        }
    }

    @Override
    @Transactional
    public String updateUserProfileImage(Long userId, MultipartFile profileImageFile) throws SQLException , IOException {
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
    @Transactional(readOnly = true)
    public boolean checkPassword(Long userId, String password) {
        User user = userDao.selectUserById(userId);
        return user != null && password != null && passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 이름과 이메일로 사용자의 비밀번호를 찾습니다. (보안 주의: 실제 비밀번호 반환 로직 아님)
     * 중요: 이 메서드는 실제 비밀번호를 반환해서는 안 됩니다.
     * 대신, 임시 비밀번호를 안전하게 생성하여 사용자 이메일로 발송하는 로직으로 반드시 대체되어야 합니다.
     * 현재 구현은 보안상 취약하며, `UnsupportedOperationException`을 발생시켜 실제 사용을 방지합니다.
     */
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public String getRefreshToken(Long userId) {
        User user = userDao.selectUserById(userId);
        if (user == null) {
            log.warn("getRefreshToken: User not found with id: {}", userId);
            return null;
        }
        return user.getRefreshToken();
    }

    /**
     * 관리자가 사용자의 비밀번호를 강제로 재설정합니다.
     * 이 메서드는 사용자의 현재 비밀번호를 알 필요 없이 관리자 권한으로 비밀번호를 변경할 때 사용됩니다.
     *
     * @param authenticatedUserEmail 비밀번호를 변경할 대상 사용자의 이메일 (관리자가 특정 사용자를 지정)
     * @param newRawPassword 설정할 새 비밀번호 (평문)
     * @return 업데이트 성공 시 1, 사용자 찾을 수 없거나 새 비밀번호가 없을 시 각각 -1, 0 반환
     * @throws SQLException SQL 예외 발생 시
     */
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
        User userToUpdate = User.builder()
                                .userId(existingUser.getUserId())
                                .password(passwordEncoder.encode(newRawPassword))
                                .build();
        try {
            userDao.updateUserPassword(userToUpdate);
            log.info("사용자 비밀번호 업데이트 성공 (관리자에 의해 강제 변경). UserId: {}", existingUser.getUserId());
            return 1;
        } catch (Exception e) {
            log.error("사용자 비밀번호 업데이트 중 DB 오류 발생. UserId: {}", existingUser.getUserId(), e);
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new RuntimeException("사용자 비밀번호 업데이트 중 오류 발생", e);
        }
    }

    /**
     * 사용자가 직접 자신의 비밀번호를 변경합니다.
     * 현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다.
     *
     * @param userId        비밀번호를 변경할 사용자의 ID
     * @param currentPassword 현재 사용 중인 비밀번호 (평문)
     * @param newPassword   새로 설정할 비밀번호 (평문)
     * @throws RuntimeException 사용자를 찾을 수 없거나, 현재 비밀번호가 일치하지 않거나, 새 비밀번호가 유효하지 않을 때 발생
     */
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
        if (newPassword == null || newPassword.isEmpty()) {
             throw new IllegalArgumentException("새 비밀번호는 비어 있을 수 없습니다.");
        }
        User userToUpdate = User.builder()
                                .userId(userId)
                                .password(passwordEncoder.encode(newPassword))
                                .build();
        userDao.updateUserPassword(userToUpdate);
        log.info("User password updated by user. ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAdminUsers() {
        return userDao.findByRole(ROLE_ADMIN);
    }

    @Override
    public List<Squad> getSquadsByUserId(Long userId) {
        return userDao.getSquadsByUserId(userId);
    }

    @Override
    public List<SquadUser> findUsersBySquadId(Integer squadId) {
        return userDao.findUsersBySquadId(squadId);
    }

    @Override
    @Transactional
    public int insertSquad(Long userId, SquadCreateRequest squadCreateRequest) {
        log.info("insertSquad {}", squadCreateRequest);
        SquadInsertParam squadInsertParam = new SquadInsertParam();
        squadInsertParam.setUserId(userId);
        squadInsertParam.setSquadName(squadCreateRequest.getSquadName());
        userDao.insertSquad(squadInsertParam);
        int squadId = squadInsertParam.getSquadId();
        squadCreateRequest.getInvitedUserIds().add(userId);
        List<Long> invitedUserIds = squadCreateRequest.getInvitedUserIds();
        log.info("invitedUserIds {}", invitedUserIds);

        userDao.insertSquadMember(invitedUserIds, squadId);

        return squadId;
    }

    @Override
    public List<User> getUsersByKeyword(String keyword) {
        return userDao.getUsersByKeyword(keyword);
    }
}

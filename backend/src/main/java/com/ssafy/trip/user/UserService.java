package com.ssafy.trip.user;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 서비스 인터페이스
 */
public interface UserService {

    /**
     * 새 사용자를 등록합니다.
     *
     * @param user 등록할 사용자 정보
     * @throws SQLException SQL 예외 발생 시
     */
    void registUser(User user) throws SQLException;

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
     * 사용자 정보(이름, 전화번호, 비밀번호)를 수정합니다.
     *
     * @param authenticatedUserEmail 인증된 사용자의 이메일 (또는 ID로 변경 가능)
     * @param newUsername 새 사용자 이름 (변경 없으면 null 또는 기존 값)
     * @param newPhone 새 전화번호 (변경 없으면 null 또는 기존 값)
     * @param newRawPassword 새 비밀번호 (raw, 암호화 전) (변경 없으면 null)
     * @return 업데이트된 행의 수 (0이면 변경 사항 없음 또는 사용자 없음, -1이면 에러)
     * @throws SQLException SQL 처리 중 예외 발생 시
     */
    int updateUser(String authenticatedUserEmail, String newUsername, String newPhone, String newRawPassword) throws SQLException;

    /**
     * 사용자 상태를 업데이트합니다.
     */
    int updateUserStatus(Long userId, String status) throws SQLException;

    /**
     * 사용자 역할을 업데이트합니다.
     */
    int updateUserRole(Long userId, String role) throws SQLException;

    /**
     * 사용자를 삭제합니다.
     */
    int deleteUser(String email) throws SQLException;

    /**
     * 이름과 이메일로 비밀번호를 찾습니다.
     */
    String findPassword(String username, String email) throws SQLException;

    /**
     * 이메일로 사용자를 조회합니다.
     */
    Optional<User> getUserByEmail(String email);

    int updateUserRefresh(Long userId, String refreshToken);

    List<Squad> getSquadsByUserId(Long userId);
    List<SquadUser> findUsersBySquadId(Integer squadId);
    int insertSquad(Long userId, SquadCreateRequest squadCreateRequest);
    List<User> getUsersByKeyword(String keyword);


    void registerUser(User user);

    User getUserById(Long userId);

    void updateUserInfo(User user);

    String updateUserProfileImage(Long userId, MultipartFile profileImageFile) throws Exception;

    void updateUserPassword(Long userId, String currentPassword, String newPassword);

    boolean checkPassword(Long userId, String password);

    String findUserPassword(String username, String email);

    void saveRefreshToken(Long userId, String refreshToken);

    String getRefreshToken(Long userId);

    List<User> getAdminUsers();
}

package com.ssafy.trip.user;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 사용자 데이터 접근 객체 인터페이스
 */
@Mapper
public interface UserDao {
    /**
     * 새 사용자를 등록합니다.
     */
    void insert(User user);

    /**
     * 이메일과 비밀번호로 로그인합니다.
     */
    User login(String email, String password);

    /**
     * 모든 사용자 목록을 조회합니다.
     */
    List<User> getUsers();

    /**
     * 사용자 정보를 업데이트합니다.
     */
    void updateUserInfo(User user);

    /**
     * 사용자 상태를 업데이트합니다.
     */
    void updateUserStatus(Long userId, String status);

    /**
     * 사용자 역할을 업데이트합니다.
     */
    void updateUserRole(Long userId, String role);

    /**
     * 사용자를 삭제합니다.
     */
    void deleteUser(String email);

    /**
     * 이름과 이메일로 비밀번호를 찾습니다.
     */
    String findPassword(String username, String email);

    /**
     * 이메일로 사용자를 조회합니다.
     */
    User getUserByEmail(String email);

    Long findUserIdByEmail(String email);

    User selectUserById(Long userId);

    void updateUserProfileImage(User user);

    void updateUserPassword(User user);

    int updateUserRefreshToken(Long userId, String refreshToken);

    List<Squad> getSquadsByUserId(Long userId);
    List<SquadUser> findUsersBySquadId(Integer squadId);
    int insertSquad(SquadInsertParam squadInsertParam);
    int insertSquadMember(List<Long> invitedUserId, int squadId);
    List<User> getUsersByKeyword(String keyword);

    int updateUserGeneral(User user);

    List<User> findByRole(String role);
    Optional<User> findByUsernameAndEmail(String username, String email);
}

package com.ssafy.trip.admin;

import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.model.Gugun;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 관리자 기능을 위한 서비스 인터페이스
 */
public interface AdminService {

    /**
     * 사용자 통계 정보를 조회합니다.
     * @return 사용자 통계 정보 맵 (총 사용자 수, 활성 사용자 수, 호스트 수, 관리자 수 등)
     */
    Map<String, Long> getUserStatistics() throws Exception;

    /**
     * 숙소 통계 정보를 조회합니다.
     * @return 숙소 통계 정보 맵 (총 숙소 수, 활성 숙소 수, 객실 수 등)
     */
    Map<String, Long> getAccommodationStatistics() throws Exception;

    /**
     * 예약 통계 정보를 조회합니다.
     * @return 예약 통계 정보 맵 (총 예약 수, 완료된 예약 수, 취소된 예약 수 등)
     */
    Map<String, Long> getReservationStatistics() throws Exception;

    /**
     * 모든 시도 정보를 조회합니다.
     * @return 시도 목록
     */
    List<Sido> getAllSidos() throws Exception;

    /**
     * 시도 코드로 구군 목록을 조회합니다.
     * @param sidoCode 시도 코드
     * @return 구군 목록
     */
    List<Gugun> getGugunsBySido(int sidoCode) throws Exception;

    /**
     * 모든 시도 데이터를 삭제합니다.
     * @return 삭제된 시도 수
     */
    int clearAllSidos() throws Exception;

    /**
     * 특정 시도에 속한 모든 구군 데이터를 삭제합니다.
     * @param sidoCode 시도 코드
     * @return 삭제된 구군 수
     */
    int clearGugunsBySido(int sidoCode) throws Exception;

    /**
     * TourAPI에서 가져온 시도 데이터를 DB에 저장합니다.
     * @param sidos 시도 목록
     * @return 저장된 시도 수
     */
    int importSidos(List<Sido> sidos) throws Exception;

    /**
     * TourAPI에서 가져온 구군 데이터를 DB에 저장합니다.
     * @param sidoCode 시도 코드
     * @param guguns 구군 목록
     * @return 저장된 구군 수
     */
    int importGuguns(int sidoCode, List<Gugun> guguns) throws Exception;

    // 관리자용 필터링 및 페이지네이션된 숙소 목록 조회
    Page<Accommodation> getAdminAccommodations(Map<String, Object> params, Pageable pageable);

    // 관리자용 사용자 목록 조회 (페이지네이션)
    Page<User> getAdminUsers(String role, String status, String keyword, Pageable pageable);

    /**
     * 관리자가 사용자의 역할을 변경합니다.
     * @param userId 대상 사용자의 ID
     * @param newRole 새로운 역할 (USER, HOST, ADMIN 중 하나)
     * @return 역할 변경 성공 여부
     */
    boolean updateUserRoleByAdmin(Long userId, String newRole);

    // 여기부터 추가된 메서드들 (이전 코드에서 발견, 중복 제거됨)
    Map<String, Object> getAccommodationTypeChartData(); // 실제 반환 타입은 Long으로 되어있을 수 있음, AdminController와 통일 필요
    List<Map<String, Object>> getMonthlyChartData();
    List<Accommodation> getRecentAccommodations(int limit);
    List<User> getRecentUsers(int limit);
    Map<String, Object> getSystemStatus();

    /**
     * 필터링 및 페이징 조건에 맞는 전체 호스트 목록을 조회합니다.
     * @param status 호스트 상태 (옵션)
     * @param keyword 검색어 (옵션, 사용자명, 이메일 등)
     * @param pageable 페이징 정보
     * @return 페이징된 호스트(User) 목록
     */
    Page<User> getAllHostsPage(String status, String keyword, Pageable pageable);

    /**
     * 호스트 상태와 사유를 업데이트합니다. (AdminController에서 호출)
     * @param hostId 호스트 ID (PK)
     * @param status 새로운 상태 (ACTIVE, REJECT 등)
     * @param reason 사유 (주로 거절 사유)
     * @return 업데이트 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    boolean updateHostStatusAndReason(Long hostId, String status, String reason) throws SQLException;
}

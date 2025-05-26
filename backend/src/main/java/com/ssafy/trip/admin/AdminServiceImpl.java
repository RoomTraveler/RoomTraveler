package com.ssafy.trip.admin;

import com.ssafy.trip.accommodation.dao.AccommodationDao;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.host.service.HostService;
import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.model.Gugun;
import com.ssafy.trip.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 기능을 위한 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final AdminDao adminDao;
    private final AccommodationDao accommodationDao;
    private final HostService hostService;

    /**
     * 사용자 통계 정보를 조회합니다.
     */
    @Override
    public Map<String, Long> getUserStatistics() throws Exception {
        return adminDao.getUserStatistics();
    }

    /**
     * 숙소 통계 정보를 조회합니다.
     */
    @Override
    public Map<String, Long> getAccommodationStatistics() throws Exception {
        return adminDao.getAccommodationStatistics();
    }

    /**
     * 예약 통계 정보를 조회합니다.
     */
    @Override
    public Map<String, Long> getReservationStatistics() throws Exception {
        return adminDao.getReservationStatistics();
    }

    /**
     * 모든 시도 정보를 조회합니다.
     */
    @Override
    public List<Sido> getAllSidos() throws Exception {
        return adminDao.getAllSidos();
    }

    /**
     * 시도 코드로 구군 목록을 조회합니다.
     */
    @Override
    public List<Gugun> getGugunsBySido(int sidoCode) throws Exception {
        return adminDao.getGugunsBySido(sidoCode);
    }

    /**
     * 모든 시도 데이터를 삭제합니다.
     */
    @Override
    public int clearAllSidos() throws Exception {
        try {
            return adminDao.clearAllSidos();
        } catch (SQLException e) {
            logger.error("모든 시도 데이터 삭제 중 SQL 오류 발생", e);
            throw new RuntimeException("모든 시도 데이터 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 특정 시도에 속한 모든 구군 데이터를 삭제합니다.
     */
    @Override
    public int clearGugunsBySido(int sidoCode) throws Exception {
        try {
            return adminDao.clearGugunsBySido(sidoCode);
        } catch (SQLException e) {
            logger.error("시도별 구군 데이터 삭제 중 SQL 오류 발생 - sidoCode: {}", sidoCode, e);
            throw new RuntimeException("시도별 구군 데이터 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * TourAPI에서 가져온 시도 데이터를 DB에 저장합니다.
     * 외래키 제약조건을 고려하여 구군 데이터를 먼저 삭제한 후 시도 데이터를 삭제합니다.
     */
    @Override
    public int importSidos(List<Sido> sidos) throws Exception {
        if (sidos == null || sidos.isEmpty()) {
            return 0;
        }
        try {
            return adminDao.importSidos(sidos);
        } catch (SQLException e) {
            logger.error("시도 데이터 가져오기(import) 중 SQL 오류 발생", e);
            throw new RuntimeException("시도 데이터 가져오기 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * TourAPI에서 가져온 구군 데이터를 DB에 저장합니다.
     */
    @Override
    public int importGuguns(int sidoCode, List<Gugun> guguns) throws Exception {
        if (guguns == null || guguns.isEmpty()) {
            return 0;
        }
        try {
            return adminDao.importGuguns(sidoCode, guguns);
        } catch (SQLException e) {
            logger.error("구군 데이터 가져오기(import) 중 SQL 오류 발생 - sidoCode: {}", sidoCode, e);
            throw new RuntimeException("구군 데이터 가져오기 중 오류가 발생했습니다.", e);
        }
    }
    @Override
    public Page<Accommodation> getAdminAccommodations(Map<String, Object> params, Pageable pageable) {
        // ★ 이미 Controller에서 "필터될 값만" 넣어서 옴 (빈/ALL/null 없음)
        params.put("offset", pageable.getOffset());
        params.put("limit", pageable.getPageSize());

        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                params.put("sortBy", order.getProperty());
                params.put("sortDirection", order.getDirection().name());
            });
        } else {
            params.put("sortBy", "createdAt");
            params.put("sortDirection", "DESC");
        }

        logger.debug("숙소 목록 조회 파라미터 (서비스): {}", params);

        List<Accommodation> accommodations;
        long total;
        try {
            accommodations = accommodationDao.getFilteredAccommodations(params);
            total = accommodationDao.countFilteredAccommodations(params);
        } catch (java.sql.SQLException e) {
            logger.error("DB 조회 중 SQL 오류 발생", e);
            throw new RuntimeException("숙소 목록 조회 중 데이터베이스 오류가 발생했습니다.", e);
        }

        return new PageImpl<>(accommodations, pageable, total);
    }


    @Override
    public Map<String, Object> getAccommodationTypeChartData() {
        List<Map<String, Object>> rawCounts;
        try {
            rawCounts = accommodationDao.selectAccommodationTypeCounts();
        } catch (java.sql.SQLException e) {
            logger.error("DB 조회 중 SQL 오류 발생 (숙소 유형 카운트)", e);
            throw new RuntimeException("숙소 유형별 통계 조회 중 데이터베이스 오류가 발생했습니다.", e);
        }

        if (rawCounts == null) {
            return Map.of();
        }
        return rawCounts.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("accommodation_type"),
                        map -> ((Number) map.get("value")).longValue()
                ));
    }

    @Override
    public List<Map<String, Object>> getMonthlyChartData() {
        return List.of();
    }

    @Override
    public List<Accommodation> getRecentAccommodations(int limit) {
        return List.of();
    }

    @Override
    public List<User> getRecentUsers(int limit) {
        return List.of();
    }

    @Override
    public Map<String, Object> getSystemStatus() {
        return Map.of();
    }

    @Override
    public Page<User> getAllHostsPage(String status, String keyword, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        if (status != null && !status.isEmpty()) {
            params.put("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            params.put("keyword", keyword);
        }
        params.put("offset", pageable.getOffset());
        params.put("limit", pageable.getPageSize());

        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                params.put("sortBy", order.getProperty());
                params.put("sortDirection", order.getDirection().name());
            });
        } else {
            // 기본 정렬 조건 (필요에 따라 수정)
            params.put("sortBy", "userId"); // 또는 호스트 테이블의 기본 정렬 컬럼
            params.put("sortDirection", "DESC");
        }

        logger.debug("전체 호스트 목록 조회 파라미터 (서비스): {}", params);

        List<User> hosts;
        long total;
        try {
            hosts = adminDao.findAllHostsFiltered(params);
            total = adminDao.countAllHostsFiltered(params);
        } catch (SQLException e) {
            logger.error("DB 조회 중 오류 발생 (전체 호스트 목록)", e);
            throw new RuntimeException("전체 호스트 목록 조회 중 데이터베이스 오류가 발생했습니다.", e);
        }

        return new PageImpl<>(hosts, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAdminUsers(String role, String status, String keyword, Pageable pageable) {
        logger.info("AdminService: getAdminUsers 호출됨 - role: {}, status: {}, keyword: {}, page: {}, size: {}",
                role, status, keyword, pageable.getPageNumber(), pageable.getPageSize());

        Map<String, Object> params = new HashMap<>();
        params.put("role", role);
        params.put("status", status);
        params.put("keyword", keyword);
        params.put("offset", pageable.getOffset());
        params.put("size", pageable.getPageSize());

        List<User> users = adminDao.findUsersByAdmin(params);
        int totalElements = adminDao.countUsersByAdmin(params);
        
        logger.debug("AdminService: DAO로부터 조회된 사용자 수: {}, 총 사용자 수: {}", users.size(), totalElements);

        return new PageImpl<>(users, pageable, totalElements);
    }

    @Override
    @Transactional
    public boolean updateUserRoleByAdmin(Long userId, String newRole) {
        try {
        int updatedRows = adminDao.updateUserRole(userId, newRole);
        if (updatedRows > 0) {
                logger.info("사용자 ID {}의 역할이 {}로 성공적으로 변경되었습니다.", userId, newRole);
            return true;
        } else {
                logger.warn("사용자 ID {}의 역할 변경 실패. 사용자를 찾을 수 없거나 변경 사항이 없습니다.", userId);
                return false;
            }
        } catch (Exception e) {
            logger.error("사용자 역할 변경 중 오류 발생 (userId: {}, newRole: {}): {}", userId, newRole, e.getMessage(), e);
            // 필요에 따라 사용자 정의 예외를 던지거나, false를 반환하여 컨트롤러에서 처리하도록 할 수 있습니다.
            // 여기서는 일단 false를 반환합니다.
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateHostStatusAndReason(Long hostId, String status, String reason) throws SQLException {
        // AdminController에서 넘어온 파라미터를 HostService의 메서드로 전달
        // HostService는 hosts 테이블의 PK인 host_id를 기준으로 업데이트 수행
        return hostService.updateHostStatusAndReason(hostId, status, reason);
    }
}

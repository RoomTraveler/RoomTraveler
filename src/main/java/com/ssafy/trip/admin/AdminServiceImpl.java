package com.ssafy.trip.admin;

import com.ssafy.trip.map.MapDTO.Sido;
import com.ssafy.trip.map.MapDTO.Gugun;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 기능을 위한 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminDao adminDao;

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
        return adminDao.clearAllSidos();
    }

    /**
     * 특정 시도에 속한 모든 구군 데이터를 삭제합니다.
     */
    @Override
    public int clearGugunsBySido(int sidoCode) throws Exception {
        return adminDao.clearGugunsBySido(sidoCode);
    }

    /**
     * TourAPI에서 가져온 시도 데이터를 DB에 저장합니다.
     * 외래키 제약조건을 고려하여 구군 데이터를 먼저 삭제한 후 시도 데이터를 삭제합니다.
     */
    @Override
    public int importSidos(List<Sido> sidos) throws Exception {
        // 모든 시도에 대한 구군 데이터를 먼저 삭제
        List<Sido> existingSidos = adminDao.getAllSidos();
        for (Sido sido : existingSidos) {
            adminDao.clearGugunsBySido(sido.getCode());
        }

        // 기존 시도 데이터를 모두 삭제한 후 새로운 데이터를 저장
        adminDao.clearAllSidos();
        return adminDao.importSidos(sidos);
    }

    /**
     * TourAPI에서 가져온 구군 데이터를 DB에 저장합니다.
     */
    @Override
    public int importGuguns(int sidoCode, List<Gugun> guguns) throws Exception {
        // 해당 시도에 속한 구군 데이터를 모두 삭제한 후 새로운 데이터를 저장
        adminDao.clearGugunsBySido(sidoCode);
        return adminDao.importGuguns(sidoCode, guguns);
    }
}

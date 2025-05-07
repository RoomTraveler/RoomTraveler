package com.ssafy.trip.admin;

import com.ssafy.trip.map.MapDTO.Sido;
import com.ssafy.trip.map.MapDTO.Gugun;

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
}

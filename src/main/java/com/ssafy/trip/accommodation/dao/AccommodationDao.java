package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Accommodation;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 숙소 데이터 액세스 인터페이스
 */
@Mapper
public interface AccommodationDao {

    /**
     * 새 숙소를 등록합니다.
     * @param accommodation 등록할 숙소 정보
     * @return 생성된 숙소의 ID
     */
    Long insert(Accommodation accommodation) throws SQLException;

    /**
     * 숙소 ID로 숙소를 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 숙소 정보
     */
    Accommodation getAccommodationById(Long accommodationId) throws SQLException;

    /**
     * 호스트 ID로 숙소 목록을 조회합니다.
     * @param hostId 호스트 ID
     * @return 숙소 목록
     */
    List<Accommodation> getAccommodationsByHostId(Long hostId) throws SQLException;

    /**
     * 지역 코드로 숙소 목록을 조회합니다.
     * @param sidoCode 시도 코드
     * @param gugunCode 구군 코드 (선택적)
     * @return 숙소 목록
     */
    List<Accommodation> getAccommodationsByRegion(Integer sidoCode, Integer gugunCode) throws SQLException;

    /**
     * 키워드로 숙소를 검색합니다.
     * @param keyword 검색 키워드
     * @return 숙소 목록
     */
    List<Accommodation> searchAccommodations(String keyword) throws SQLException;

    /**
     * 숙소 정보를 업데이트합니다.
     * @param accommodation 업데이트할 숙소 정보
     * @return 업데이트된 행 수
     */
    int updateAccommodation(Accommodation accommodation) throws SQLException;

    /**
     * 숙소 상태를 업데이트합니다.
     * @param accommodationId 숙소 ID
     * @param status 새 상태
     * @return 업데이트된 행 수
     */
    int updateAccommodationStatus(Long accommodationId, String status) throws SQLException;

    /**
     * 숙소를 삭제합니다.
     * @param accommodationId 삭제할 숙소 ID
     * @return 삭제된 행 수
     */
    int deleteAccommodation(Long accommodationId) throws SQLException;

    /**
     * 모든 숙소를 조회합니다.
     * @return 모든 숙소 목록
     */
    List<Accommodation> getAllAccommodations() throws SQLException;

    /**
     * 필터링된 숙소 목록을 조회합니다.
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 숙소 목록
     */
    List<Accommodation> getFilteredAccommodations(Map<String, Object> filters) throws SQLException;

    /**
     * 외부 API에서 가져온 숙소 정보를 저장합니다.
     * @param accommodation 저장할 숙소 정보
     * @return 생성된 숙소의 ID
     */
    Long insertFromApi(Accommodation accommodation) throws SQLException;

    /**
     * 유사한 숙소 목록을 조회합니다.
     * @param accommodationId 기준 숙소 ID
     * @param limit 조회할 최대 숙소 수
     * @return 유사한 숙소 목록
     */
    List<Accommodation> getSimilarAccommodations(Long accommodationId, int limit) throws SQLException;
}

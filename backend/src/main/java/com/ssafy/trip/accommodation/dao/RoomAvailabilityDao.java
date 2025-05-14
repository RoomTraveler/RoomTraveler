package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.RoomAvailability;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 객실 가용성 데이터 액세스 인터페이스
 */
@Mapper
public interface RoomAvailabilityDao {

    /**
     * 새 객실 가용성 정보를 등록합니다.
     * @param roomAvailability 등록할 객실 가용성 정보
     * @return 생성된 가용성 정보의 ID
     */
    Long insert(RoomAvailability roomAvailability) throws SQLException;

    /**
     * 가용성 ID로 객실 가용성 정보를 조회합니다.
     * @param availabilityId 가용성 ID
     * @return 객실 가용성 정보
     */
    RoomAvailability getAvailabilityById(Long availabilityId) throws SQLException;

    /**
     * 객실 ID와 날짜로 객실 가용성 정보를 조회합니다.
     * @param roomId 객실 ID
     * @param date 날짜
     * @return 객실 가용성 정보
     */
    RoomAvailability getAvailabilityByRoomIdAndDate(Long roomId, LocalDate date) throws SQLException;

    /**
     * 객실 ID로 객실 가용성 정보 목록을 조회합니다.
     * @param roomId 객실 ID
     * @return 객실 가용성 정보 목록
     */
    List<RoomAvailability> getAvailabilitiesByRoomId(Long roomId) throws SQLException;

    /**
     * 객실 ID와 날짜 범위로 객실 가용성 정보 목록을 조회합니다.
     * @param roomId 객실 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 객실 가용성 정보 목록
     */
    List<RoomAvailability> getAvailabilitiesByRoomIdAndDateRange(Long roomId, LocalDate startDate, LocalDate endDate) throws SQLException;

    /**
     * 숙소 ID와 날짜 범위로 객실 가용성 정보 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 객실 가용성 정보 목록
     */
    List<RoomAvailability> getAvailabilitiesByAccommodationIdAndDateRange(Long accommodationId, LocalDate startDate, LocalDate endDate) throws SQLException;

    /**
     * 객실 가용성 정보를 업데이트합니다.
     * @param roomAvailability 업데이트할 객실 가용성 정보
     * @return 업데이트된 행 수
     */
    int updateAvailability(RoomAvailability roomAvailability) throws SQLException;

    /**
     * 객실 ID와 날짜로 가용 객실 수를 업데이트합니다.
     * @param roomId 객실 ID
     * @param date 날짜
     * @param availableCount 가용 객실 수
     * @return 업데이트된 행 수
     */
    int updateAvailableCount(Long roomId, LocalDate date, Integer availableCount) throws SQLException;

    /**
     * 객실 ID와 날짜로 특별 가격을 업데이트합니다.
     * @param roomId 객실 ID
     * @param date 날짜
     * @param price 특별 가격
     * @return 업데이트된 행 수
     */
    int updatePrice(Long roomId, LocalDate date, Double price) throws SQLException;

    /**
     * 객실 가용성 정보를 삭제합니다.
     * @param availabilityId 삭제할 가용성 ID
     * @return 삭제된 행 수
     */
    int deleteAvailability(Long availabilityId) throws SQLException;

    /**
     * 객실 ID와 날짜로 객실 가용성 정보를 삭제합니다.
     * @param roomId 객실 ID
     * @param date 날짜
     * @return 삭제된 행 수
     */
    int deleteAvailabilityByRoomIdAndDate(Long roomId, LocalDate date) throws SQLException;

    /**
     * 객실 ID로 모든 객실 가용성 정보를 삭제합니다.
     * @param roomId 객실 ID
     * @return 삭제된 행 수
     */
    int deleteAvailabilitiesByRoomId(Long roomId) throws SQLException;

    /**
     * 필터링된 객실 가용성 정보 목록을 조회합니다.
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 객실 가용성 정보 목록
     */
    List<RoomAvailability> getFilteredAvailabilities(Map<String, Object> filters) throws SQLException;
}

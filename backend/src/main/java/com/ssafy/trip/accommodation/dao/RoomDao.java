package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Room;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 객실 데이터 액세스 인터페이스
 */
@Mapper
public interface RoomDao {

    /**
     * 새 객실을 등록합니다.
     * @param room 등록할 객실 정보
     * @return 생성된 객실의 ID
     */
    Long insert(Room room) throws SQLException;

    /**
     * 객실 ID로 객실을 조회합니다.
     * @param roomId 객실 ID
     * @return 객실 정보
     */
    Room getRoomById(Long roomId) throws SQLException;

    /**
     * 숙소 ID로 객실 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 객실 목록
     */
    List<Room> getRoomsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 숙소 ID와 날짜로 가용한 객실 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @return 가용한 객실 목록
     */
    List<Room> getAvailableRooms(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate) throws SQLException;

    /**
     * 객실 정보를 업데이트합니다.
     * @param room 업데이트할 객실 정보
     * @return 업데이트된 행 수
     */
    int updateRoom(Room room) throws SQLException;

    /**
     * 객실 상태를 업데이트합니다.
     * @param roomId 객실 ID
     * @param status 새 상태
     * @return 업데이트된 행 수
     */
    int updateRoomStatus(Long roomId, String status) throws SQLException;

    /**
     * 객실을 삭제합니다.
     * @param roomId 삭제할 객실 ID
     * @return 삭제된 행 수
     */
    int deleteRoom(Long roomId) throws SQLException;

    /**
     * 필터링된 객실 목록을 조회합니다.
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 객실 목록
     */
    List<Room> getFilteredRooms(Map<String, Object> filters) throws SQLException;

    /**
     * 외부 API에서 가져온 객실 정보를 저장합니다.
     * @param room 저장할 객실 정보
     * @return 생성된 객실의 ID
     */
    Long insertFromApi(Room room) throws SQLException;

    /**
     * 호스트 ID로 객실 목록을 조회합니다.
     * @param hostId 호스트 ID
     * @return 객실 목록
     */
    List<Room> getRoomsByHostId(Long hostId) throws SQLException;
}

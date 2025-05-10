package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.Image;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 숙소 서비스 인터페이스
 */
public interface AccommodationService {

    /**
     * 새 숙소를 등록합니다.
     * @param accommodation 등록할 숙소 정보
     * @param images 숙소 이미지 목록
     * @return 생성된 숙소의 ID
     */
    Long registerAccommodation(Accommodation accommodation, List<Image> images) throws SQLException;

    /**
     * 새 객실을 등록합니다.
     * @param room 등록할 객실 정보
     * @param images 객실 이미지 목록
     * @return 생성된 객실의 ID
     */
    Long registerRoom(Room room, List<Image> images) throws SQLException;

    /**
     * 숙소 ID로 숙소를 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 숙소 정보
     */
    Accommodation getAccommodationById(Long accommodationId) throws SQLException;

    /**
     * 객실 ID로 객실을 조회합니다.
     * @param roomId 객실 ID
     * @return 객실 정보
     */
    Room getRoomById(Long roomId) throws SQLException;

    /**
     * 호스트 ID로 숙소 목록을 조회합니다.
     * @param hostId 호스트 ID
     * @return 숙소 목록
     */
    List<Accommodation> getAccommodationsByHostId(Long hostId) throws SQLException;

    /**
     * 숙소 ID로 객실 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 객실 목록
     */
    List<Room> getRoomsByAccommodationId(Long accommodationId) throws SQLException;

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
     * @param images 업데이트할 이미지 목록 (null인 경우 이미지 업데이트 안함)
     * @return 업데이트된 행 수
     */
    int updateAccommodation(Accommodation accommodation, List<Image> images) throws SQLException;

    /**
     * 객실 정보를 업데이트합니다.
     * @param room 업데이트할 객실 정보
     * @param images 업데이트할 이미지 목록 (null인 경우 이미지 업데이트 안함)
     * @return 업데이트된 행 수
     */
    int updateRoom(Room room, List<Image> images) throws SQLException;

    /**
     * 숙소 상태를 업데이트합니다.
     * @param accommodationId 숙소 ID
     * @param status 새 상태
     * @return 업데이트된 행 수
     */
    int updateAccommodationStatus(Long accommodationId, String status) throws SQLException;

    /**
     * 객실 상태를 업데이트합니다.
     * @param roomId 객실 ID
     * @param status 새 상태
     * @return 업데이트된 행 수
     */
    int updateRoomStatus(Long roomId, String status) throws SQLException;

    /**
     * 숙소를 삭제합니다.
     * @param accommodationId 삭제할 숙소 ID
     * @return 삭제된 행 수
     */
    int deleteAccommodation(Long accommodationId) throws SQLException;


    /**
     * 객실을 삭제합니다.
     * @param roomId 삭제할 객실 ID
     * @return 삭제된 행 수
     */
    int deleteRoom(Long roomId) throws SQLException;

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
     * 필터링된 객실 목록을 조회합니다.
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 객실 목록
     */
    List<Room> getFilteredRooms(Map<String, Object> filters) throws SQLException;

    /**
     * 외부 API에서 가져온 숙소 정보를 저장합니다.
     * @param accommodation 저장할 숙소 정보
     * @param rooms 저장할 객실 정보 목록
     * @param images 저장할 이미지 정보 목록
     * @return 생성된 숙소의 ID
     */
    Long importFromApi(Accommodation accommodation, List<Room> rooms, List<Image> images) throws SQLException;

    /**
     * 유사한 숙소 목록을 조회합니다.
     * @param accommodationId 기준 숙소 ID
     * @param limit 조회할 최대 숙소 수
     * @return 유사한 숙소 목록
     */
    List<Accommodation> getSimilarAccommodations(Long accommodationId, int limit) throws SQLException;

    /**
     * 모든 숙소를 삭제합니다.
     * @return 삭제된 행 수
     */
    int deleteAllAccommodations() throws SQLException;
}

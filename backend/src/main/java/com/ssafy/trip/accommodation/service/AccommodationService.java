package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.dto.request.AccommodationRequestDto;
import com.ssafy.trip.dto.response.AccommodationResponseDto;
import com.ssafy.trip.dto.request.RoomRequestDto;
import com.ssafy.trip.dto.response.RoomResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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


    //타입별 카운트
    Map<String, Long> getAccommodationTypeCounts() throws SQLException;

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
     * 숙소 ID와 선택적 날짜 범위 및 인원수로 객실 목록을 조회합니다.
     * 날짜 범위와 인원수가 제공되면, 해당 기간 동안의 최소 가용 객실 수도 함께 조회합니다.
     * @param accommodationId 숙소 ID
     * @param startDate 조회 시작 날짜 (YYYY-MM-DD 형식, 옵셔널)
     * @param endDate 조회 종료 날짜 (YYYY-MM-DD 형식, 옵셔널)
     * @param guests 총 인원수 (옵셔널)
     * @return 객실 목록 (minAvailableCount 포함 가능)
     */
    List<Room> getRoomsByAccommodationId(Long accommodationId, String startDate, String endDate, Integer guests) throws SQLException;

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
    int updateAccommodationStatus(Long accommodationId, Accommodation.AccommodationStatus status) throws SQLException;

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
     * 필터링된 숙소 목록을 조회합니다. (페이징 적용)
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     *              페이징을 위한 offset과 limit 값이 포함되어야 함
     * @return 페이징된 숙소 목록 및 전체 아이템 수, 전체 페이지 수 등을 포함하는 Map
     *         예: {"content": List<Accommodation>, "currentPage": int, "totalItems": long, "totalPages": int}
     */
    Map<String, Object> getFilteredAccommodations(Map<String, Object> filters) throws SQLException;

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

    /**
     * 승인 대기 중인 숙소 목록을 페이징하여 조회합니다.
     * @param pageable 페이징 정보
     * @return 페이징된 승인 대기 숙소 목록
     */
    Page<Accommodation> getPendingReviewAccommodations(Pageable pageable) throws SQLException;

    /**
     * 숙소 등록 신청을 승인합니다.
     * @param accommodationId 승인할 숙소 ID
     */
    void approveAccommodation(Long accommodationId) throws SQLException;

    /**
     * 숙소 등록 신청을 거절합니다. (알림 발송 포함)
     * @param accommodationId 거절할 숙소 ID
     * @param reason 거절 사유
     */
    void rejectAccommodation(Long accommodationId, String reason) throws SQLException;

    /**
     * 새 숙소를 등록합니다. (파일 업로드 포함)
     * @param requestDto 숙소 등록 요청 정보 (이미지 파일 포함)
     * @param hostId 호스트 ID (인증된 사용자로부터 획득)
     * @return 생성된 숙소 정보 DTO
     * @throws Exception 예외 처리
     */
    AccommodationResponseDto createAccommodationAndImages(com.ssafy.trip.dto.request.AccommodationRequestDto requestDto, Long hostId) throws Exception;

    /**
     * 기존 숙소 정보를 수정합니다. (파일 업로드/삭제 포함)
     * @param accommodationId 수정할 숙소 ID
     * @param requestDto 숙소 수정 요청 정보 (이미지 파일 포함)
     * @param hostId 호스트 ID (인증된 사용자로부터 획득, 숙소 소유주 확인용)
     * @return 수정된 숙소 정보 DTO
     * @throws Exception 예외 처리
     */
    AccommodationResponseDto updateAccommodationAndImages(Long accommodationId, com.ssafy.trip.dto.request.AccommodationRequestDto requestDto, Long hostId) throws Exception;

    /**
     * 숙소 상세 정보를 조회합니다. (이미지 포함)
     * @param accommodationId 조회할 숙소 ID
     * @return 숙소 상세 정보 DTO
     * @throws Exception 예외 처리
     */
    AccommodationResponseDto getAccommodationDetails(Long accommodationId) throws Exception;

    // Room Management for Host
    RoomResponseDto createRoomAndImages(Long accommodationId, com.ssafy.trip.dto.request.RoomRequestDto requestDto, Long hostId) throws Exception;

    RoomResponseDto updateRoomAndImages(Long roomId, com.ssafy.trip.dto.request.RoomRequestDto requestDto, Long hostId) throws Exception;

    RoomResponseDto getRoomDetailsForHost(Long roomId, Long hostId) throws Exception;

    List<RoomResponseDto> getRoomsForHost(Long accommodationId, Long hostId) throws Exception;

    void deleteRoomAndImages(Long roomId, Long hostId) throws Exception;
}

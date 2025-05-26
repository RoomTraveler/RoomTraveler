package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.AccommodationDao;
import com.ssafy.trip.accommodation.dao.RoomDao;
import com.ssafy.trip.accommodation.dao.ImageDao;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.notification.Notification;
import com.ssafy.trip.notification.NotificationService;
import com.ssafy.trip.s3.AWSS3Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ssafy.trip.accommodation.model.AccommodationRequestDto;
import com.ssafy.trip.accommodation.model.AccommodationResponseDto;
import com.ssafy.trip.accommodation.model.RoomRequestDto;
import com.ssafy.trip.accommodation.model.RoomResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 숙소 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationDao accommodationDao;
    private final RoomDao roomDao;
    private final ImageDao imageDao;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final AWSS3Service awss3Service;
    private final ObjectMapper objectMapper;

    /**
     * 새 숙소를 등록합니다.
     */
    @Override
    @Transactional
    public Long registerAccommodation(Accommodation accommodation, List<Image> images) throws SQLException {
        // 숙소 등록
        accommodationDao.insert(accommodation);
        Long accommodationId = accommodation.getAccommodationId();

        // 이미지 등록
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                image.setReferenceId(accommodationId);
                image.setReferenceType("ACCOMMODATION");
                image.setAccommodationId(accommodationId);
                imageDao.insert(image);
            }
        }
        return accommodationId;
    }


    //타입별 카운트
    @Override
    public Map<String, Long> getAccommodationTypeCounts() throws SQLException {
        List<Map<String, Object>> rows = accommodationDao.selectAccommodationTypeCounts();
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String type = (row.get("accommodation_type") != null) ? row.get("accommodation_type").toString() : "기타";
            Long count = 0L;
            Object valueObj = row.get("value");
            if (valueObj instanceof Number) {
                count = ((Number) valueObj).longValue();
            } else if (valueObj != null) {
                count = Long.parseLong(valueObj.toString());
            }
            result.put(type, count);
        }
        return result;
    }


    /**
     * 새 객실을 등록합니다.
     */
    @Override
    @Transactional
    public Long registerRoom(Room room, List<Image> images) throws SQLException {
        // 객실 등록
        roomDao.insert(room);
        Long roomId = room.getRoomId();
        Long accommodationId = room.getAccommodationId(); // 숙소 ID 가져오기

        // 이미지 등록
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                image.setReferenceId(roomId);
                image.setReferenceType("ROOM");
                image.setRoomId(roomId);
                image.setAccommodationId(accommodationId);
                imageDao.insert(image);
            }
        }
        
        // 숙소의 객실 가격 통계 업데이트
        updateAccommodationRoomPriceStats(accommodationId);
        
        return roomId;
    }

    /**
     * 숙소 ID로 숙소를 조회합니다.
     */
    @Override
    public Accommodation getAccommodationById(Long accommodationId) throws SQLException {
        Accommodation accommodation = accommodationDao.getAccommodationById(accommodationId);
        if (accommodation != null) {
            setMainImageUrlForSingleAccommodation(accommodation);
        }
        return accommodation;
    }

    /**
     * 객실 ID로 객실을 조회합니다.
     */
    @Override
    public Room getRoomById(Long roomId) throws SQLException {
        Room room = roomDao.getRoomById(roomId);
        if (room != null) {
            setImagesForSingleRoom(room);
        }
        return room;
    }

    /**
     * 호스트 ID로 숙소 목록을 조회합니다.
     */
    @Override
    public List<Accommodation> getAccommodationsByHostId(Long hostId) throws SQLException {
        List<Accommodation> accommodations = accommodationDao.getAccommodationsByHostId(hostId);
        setMainImageUrlForAccommodations(accommodations);
        return accommodations;
    }

    /**
     * 숙소 ID와 선택적 날짜 범위 및 인원수로 객실 목록을 조회합니다.
     */
    @Override
    public List<Room> getRoomsByAccommodationId(Long accommodationId, String startDateStr, String endDateStr, Integer guests) throws SQLException {
        log.info("[AccommodationService] getRoomsByAccommodationId called - accommodationId: {}, startDate: {}, endDate: {}, guests: {}", 
                accommodationId, startDateStr, endDateStr, guests);
        
        Map<String, Object> params = new HashMap<>();
        params.put("accommodationId", accommodationId);

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (startDateStr != null && !startDateStr.isEmpty() && endDateStr != null && !endDateStr.isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateStr);
                endDate = LocalDate.parse(endDateStr);
                // DAO에는 문자열로 전달할 수도 있고, LocalDate로 변환해서 전달할 수도 있습니다.
                // 현재 RoomDao.getRoomsByAccommodationIdAndOptionalDateRange가 Map<String, Object>를 받으므로 문자열 유지 가능
                params.put("startDate", startDateStr);
                params.put("endDate", endDateStr);
            } catch (DateTimeParseException e) {
                log.warn("Invalid date format for startDate or endDate. Proceeding without date filter for availability calculation.");
            }
        }
        
        // RoomDao를 통해 객실 기본 정보를 가져옵니다.
        List<Room> rooms = roomDao.getRoomsByAccommodationIdAndOptionalDateRange(params); // 이 메소드가 날짜 필터링도 하는지 확인 필요
        log.info("[AccommodationService] Got {} rooms from RoomDao for accommodationId: {}", rooms.size(), accommodationId);

        // 날짜 정보와 인원수 정보가 유효한 경우에만 각 객실의 minAvailableCount를 계산하여 설정합니다.
        if (startDate != null && endDate != null && guests != null && guests > 0) {
            log.info("[AccommodationService] Calculating minAvailableCount for each room with dates and guests");
            for (Room room : rooms) {
                log.debug("[AccommodationService] Processing room ID: {}, capacity: {}", room.getRoomId(), room.getCapacity());
                // ReservationService를 사용하여 minAvailableCount 계산 (guests 파라미터 전달)
                int minAvailableCount = reservationService.calculateMinAvailableCountForRoom(room, startDate, endDate, guests);
                log.info("[AccommodationService] Room ID: {}, capacity: {}, calculated minAvailableCount: {}", 
                        room.getRoomId(), room.getCapacity(), minAvailableCount);
                room.setMinAvailableCount(minAvailableCount);
            }
        } else {
            log.info("[AccommodationService] Date or guests info insufficient, setting minAvailableCount to -1 for all rooms");
            for (Room room : rooms) {
                // 날짜나 인원 정보가 충분하지 않으면, 예약 가능 여부를 알 수 없으므로 minAvailableCount를 -1 (또는 다른 특정 값)로 설정
                room.setMinAvailableCount(-1); // 프론트에서 이 값을 보고 "날짜/인원 선택 시 확인 가능" 등으로 표시 가능
            }
        }

        for (Room room : rooms) {
            setImagesForSingleRoom(room);
        }
        return rooms;
    }

    /**
     * 지역 코드로 숙소 목록을 조회합니다.
     */
    @Override
    public List<Accommodation> getAccommodationsByRegion(Integer sidoCode, Integer gugunCode) throws SQLException {
        List<Accommodation> accommodations = accommodationDao.getAccommodationsByRegion(sidoCode, gugunCode);
        setMainImageUrlForAccommodations(accommodations);
        return accommodations;
    }

    /**
     * 키워드로 숙소를 검색합니다.
     */
    @Override
    public List<Accommodation> searchAccommodations(String keyword) throws SQLException {
        List<Accommodation> accommodations = accommodationDao.searchAccommodations(keyword);
        setMainImageUrlForAccommodations(accommodations);
        return accommodations;
    }
    
    private void setMainImageUrlForSingleAccommodation(Accommodation accommodation) throws SQLException {
        log.debug("setMainImageUrlForSingleAccommodation - START for accommodation ID: {}", accommodation != null ? accommodation.getAccommodationId() : "null accommodation object");
        if (accommodation == null) {
            log.warn("setMainImageUrlForSingleAccommodation - Received null accommodation object.");
            return;
        }

        Long currentAccommodationId = accommodation.getAccommodationId();
        log.debug("Accommodation ID for image fetching: {}", currentAccommodationId);
        if (currentAccommodationId == null) {
            log.error("Accommodation ID is NULL before calling imageDao.getImagesByReference for {}. Skipping image fetch.", accommodation);
            // 기본 이미지 또는 오류 처리
            accommodation.setMainImageUrl("https://via.placeholder.com/800x600?text=Error+ID+Null");
            accommodation.setThumbnailImageUrl("https://via.placeholder.com/800x600?text=Error+ID+Null");
            return;
        }

        // 1. DB에서 이미 thumbnailImageUrl이 로드되었는지 확인
        String thumbnailImageUrlFromDb = accommodation.getThumbnailImageUrl();
        log.debug("ThumbnailImageUrl from DB for accommodation ID {}: {}", currentAccommodationId, thumbnailImageUrlFromDb);

        // 2. mainImageUrl 설정 (기존 로직)
        log.debug("Calling imageDao.getImagesByReference with ID: {}, Type: ACCOMMODATION", currentAccommodationId);
        List<Image> images = null;
        try {
            images = imageDao.getImagesByReference(currentAccommodationId, "ACCOMMODATION");
        } catch (Exception e) {
            log.error("Error calling imageDao.getImagesByReference for accommodation ID {}: {}", currentAccommodationId, e.getMessage(), e);
            // 기본 이미지 또는 오류 처리
            accommodation.setMainImageUrl("https://via.placeholder.com/800x600?text=Image+Fetch+Error");
            accommodation.setThumbnailImageUrl("https://via.placeholder.com/800x600?text=Image+Fetch+Error");
            // SQLException을 다시 던지거나 상황에 맞게 처리
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            // 혹은 다른 RuntimeException으로 감싸서 던질 수 있습니다.
            // throw new RuntimeException("Failed to fetch images for accommodation " + currentAccommodationId, e);
            return; 
        }
        
        log.debug("Retrieved {} images for accommodation ID: {}", (images != null ? images.size() : "null list"), currentAccommodationId);

        if (images == null) { // Defensive check
            log.warn("imageDao.getImagesByReference returned null for accommodation ID: {}. Treating as no images.", currentAccommodationId);
            images = new ArrayList<>(); // Null 대신 빈 리스트로 처리
        }

        String mainImageUrl = null;
        if (!images.isEmpty()) {
            Image mainImage = images.stream().filter(img -> img.getIsMain() != null && img.getIsMain()).findFirst().orElse(images.get(0));
            mainImageUrl = mainImage.getImageUrl();
            if (mainImageUrl == null || mainImageUrl.isEmpty()) {
                mainImageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
            } else if (!mainImageUrl.startsWith("http://") && !mainImageUrl.startsWith("https://")) {
                mainImageUrl = "http://" + mainImageUrl;
            }
            accommodation.setMainImageUrl(mainImageUrl);
        } else {
            // 이미지가 없는 경우의 기본 mainImageUrl
            mainImageUrl = "https://via.placeholder.com/800x600?text=No+Images";
            accommodation.setMainImageUrl(mainImageUrl);
        }
        log.debug("Set mainImageUrl: {} for accommodation ID: {}", mainImageUrl, currentAccommodationId);

        // 3. thumbnailImageUrl 설정
        if (thumbnailImageUrlFromDb != null && !thumbnailImageUrlFromDb.isEmpty()) {
            // DB에서 가져온 값이 있으면 사용
            if (!thumbnailImageUrlFromDb.startsWith("http://") && !thumbnailImageUrlFromDb.startsWith("https://")) {
                accommodation.setThumbnailImageUrl("http://" + thumbnailImageUrlFromDb);
            } else {
                accommodation.setThumbnailImageUrl(thumbnailImageUrlFromDb);
            }
        } else if (mainImageUrl != null && !mainImageUrl.contains("placeholder.com")) {
            // DB에 썸네일 URL이 없고, mainImageUrl이 유효하면 파생 시도 (예: _thumb 접미사)
            // 실제 이 URL로 접근 가능한 썸네일 파일이 서버에 존재해야 함
            int dotIndex = mainImageUrl.lastIndexOf('.');
            if (dotIndex > 0) {
                String name = mainImageUrl.substring(0, dotIndex);
                String ext = mainImageUrl.substring(dotIndex);
                accommodation.setThumbnailImageUrl(name + "_thumb" + ext);
            } else {
                // 확장자가 없는 경우, mainImageUrl을 그대로 사용
                accommodation.setThumbnailImageUrl(mainImageUrl);
            }
        } else {
            // 위 조건 모두 해당 없으면, mainImageUrl을 썸네일로 사용 (플레이스홀더 포함)
            accommodation.setThumbnailImageUrl(mainImageUrl);
        }
        log.debug("Set thumbnailImageUrl: {} for accommodation ID: {}", accommodation.getThumbnailImageUrl(), currentAccommodationId);
        log.debug("setMainImageUrlForSingleAccommodation - END for accommodation ID: {}", currentAccommodationId);
    }

    private void setImagesForSingleRoom(Room room) throws SQLException {
        if (room == null) return;
        List<Image> images = imageDao.getImagesByReference(room.getRoomId(), "ROOM");
        List<String> imageUrls = new ArrayList<>();
        Image mainImageFromDb = null;

        for (Image image : images) {
            String imageUrl = image.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
            } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                imageUrl = "http://" + imageUrl;
            }
            imageUrls.add(imageUrl);
            if (image.getIsMain() != null && image.getIsMain()) {
                mainImageFromDb = image;
            }
        }
        room.setImageUrls(imageUrls);

        if (mainImageFromDb != null) {
            String mainUrl = mainImageFromDb.getImageUrl();
             if (mainUrl == null || mainUrl.isEmpty()) {
                mainUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
            } else if (!mainUrl.startsWith("http://") && !mainUrl.startsWith("https://")) {
                mainUrl = "http://" + mainUrl;
            }
            room.setMainImageUrl(mainUrl);
        } else if (!imageUrls.isEmpty()) {
            room.setMainImageUrl(imageUrls.get(0));
        }
    }

    /**
     * 숙소 목록에 대표 이미지 URL을 설정합니다.
     */
    private void setMainImageUrlForAccommodations(List<Accommodation> accommodations) throws SQLException {
        log.debug("setMainImageUrlForAccommodations - START. Number of accommodations: {}", accommodations != null ? accommodations.size() : "null list");
        if (accommodations == null) {
            log.error("setMainImageUrlForAccommodations - accommodations list is null!");
            // 예외를 던지거나, 빈 리스트로 처리하는 등의 방어 코드 추가 가능
            return; 
        }
        for (Accommodation accommodation : accommodations) {
            if (accommodation == null) {
                log.warn("setMainImageUrlForAccommodations - Found a null accommodation object in the list. Skipping.");
                continue;
            }
            log.debug("Processing images for accommodation ID: {}", accommodation.getAccommodationId());
            setMainImageUrlForSingleAccommodation(accommodation);
        }
        log.debug("setMainImageUrlForAccommodations - END");
    }

    /**
     * 숙소 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateAccommodation(Accommodation accommodation, List<Image> images) throws SQLException {
        if (images != null) {
            imageDao.deleteImagesByReference(accommodation.getAccommodationId(), "ACCOMMODATION");
            for (Image image : images) {
                image.setReferenceId(accommodation.getAccommodationId());
                image.setReferenceType("ACCOMMODATION");
                image.setAccommodationId(accommodation.getAccommodationId());
                imageDao.insert(image);
            }
        }
        int result = accommodationDao.updateAccommodation(accommodation);
        if (result > 0) {
            updateAccommodationRoomPriceStats(accommodation.getAccommodationId());
        }
        return result;
    }

    /**
     * 객실 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateRoom(Room room, List<Image> images) throws SQLException {
        if (images != null) {
            imageDao.deleteImagesByReference(room.getRoomId(), "ROOM");
            for (Image image : images) {
                image.setReferenceId(room.getRoomId());
                image.setReferenceType("ROOM");
                image.setRoomId(room.getRoomId());
                image.setAccommodationId(room.getAccommodationId());
                imageDao.insert(image);
            }
        }
        int result = roomDao.updateRoom(room);
        if (result > 0 && room.getAccommodationId() != null) {
            updateAccommodationRoomPriceStats(room.getAccommodationId());
        }
        return result;
    }

    /**
     * 숙소 상태를 업데이트합니다.
     */
    @Override
    public int updateAccommodationStatus(Long accommodationId, Accommodation.AccommodationStatus status) throws SQLException {
        return accommodationDao.updateAccommodationStatus(accommodationId, status.name());
    }

    /**
     * 객실 상태를 업데이트합니다.
     */
    @Override
    public int updateRoomStatus(Long roomId, String status) throws SQLException {
        return roomDao.updateRoomStatus(roomId, status);
    }

    /**
     * 숙소를 삭제합니다.
     */
    @Override
    @Transactional
    public int deleteAccommodation(Long accommodationId) throws SQLException {
        List<Room> rooms = roomDao.getRoomsByAccommodationId(accommodationId);
        for (Room room : rooms) {
            imageDao.deleteImagesByReference(room.getRoomId(), "ROOM");
            roomDao.deleteRoom(room.getRoomId());
        }
        imageDao.deleteImagesByReference(accommodationId, "ACCOMMODATION");
        return accommodationDao.deleteAccommodation(accommodationId);
    }


    /**
     * 객실을 삭제합니다.
     */
    @Override
    @Transactional
    public int deleteRoom(Long roomId) throws SQLException {
        // 객실 정보 조회 (숙소 ID를 얻기 위함)
        Room roomToDelete = roomDao.getRoomById(roomId);
        if (roomToDelete == null) {
            // 이미 삭제되었거나 없는 객실일 수 있으므로, 오류 대신 0 반환 또는 로깅 처리
            log.warn("삭제할 객실을 찾을 수 없습니다: {}", roomId);
            return 0; 
        }
        Long accommodationId = roomToDelete.getAccommodationId();

        // 연결된 이미지 삭제
        imageDao.deleteImagesByReference(roomId, "ROOM");
        // 객실 삭제
        int deletedRows = roomDao.deleteRoom(roomId);
        
        // 숙소의 객실 가격 통계 업데이트
        if (deletedRows > 0) {
            updateAccommodationRoomPriceStats(accommodationId);
        }
        
        return deletedRows;
    }

    /**
     * 모든 숙소를 조회합니다.
     */
    @Override
    public List<Accommodation> getAllAccommodations() throws SQLException {
        List<Accommodation> accommodations = accommodationDao.getAllAccommodations();
        setMainImageUrlForAccommodations(accommodations);
        return accommodations;
    }

    /**
     * 필터링된 숙소 목록을 조회합니다.
     */
    @Override
    public Map<String, Object> getFilteredAccommodations(Map<String, Object> filters) throws SQLException {
        log.info("Service: getFilteredAccommodations with filters: {}", filters);
        
        // DAO 호출 전에 필요한 경우 filters 맵의 guestCount, checkInDate, checkOutDate 등을 확인하고 로깅할 수 있습니다.
        // 예를 들어, checkInDate, checkOutDate 문자열을 LocalDate로 변환하여 ReservationService와 연동 준비
        String checkInDateStr = (String) filters.get("checkInDate");
        String checkOutDateStr = (String) filters.get("checkOutDate");
        Integer guestCount = (Integer) filters.get("guestCount");

        LocalDate checkIn = null;
        LocalDate checkOut = null;

        if (checkInDateStr != null && !checkInDateStr.isEmpty()){
            try {
                checkIn = LocalDate.parse(checkInDateStr);
            } catch (DateTimeParseException e){
                log.warn("Invalid checkInDate format: {}. Date filter for availability might not work as expected.", checkInDateStr);
            }
        }
        if (checkOutDateStr != null && !checkOutDateStr.isEmpty()){
            try {
                checkOut = LocalDate.parse(checkOutDateStr);
            } catch (DateTimeParseException e){
                log.warn("Invalid checkOutDate format: {}. Date filter for availability might not work as expected.", checkOutDateStr);
            }
        }

        // 1. DAO를 통해 기본 필터링된 숙소 목록과 전체 카운트 조회
        List<Accommodation> accommodations = accommodationDao.getFilteredAccommodations(filters);
        long totalCount = accommodationDao.countFilteredAccommodations(filters);

        log.debug("DAO returned {} accommodations, total count: {}", accommodations.size(), totalCount);

        // 2. (선택적이지만 권장) 날짜와 인원이 주어졌다면, 각 숙소의 실제 예약 가능 여부를 확인하여 추가 필터링
        // 이 로직은 성능에 영향을 줄 수 있으므로, 매우 많은 숙소가 반환될 경우 주의 필요
        List<Accommodation> availableAccommodations = new ArrayList<>();
        if (checkIn != null && checkOut != null && guestCount != null && guestCount > 0) {
            log.debug("Performing secondary availability check for {} accommodations with dates: {} - {} and guests: {}", accommodations.size(), checkIn, checkOut, guestCount);
            for (Accommodation acc : accommodations) {
                // 각 숙소의 객실 목록을 가져와 예약 가능 여부 확인
                List<Room> rooms = roomDao.getRoomsByAccommodationId(acc.getAccommodationId()); // RoomDao에 accommodationId로 객실 목록만 가져오는 메소드 필요
                boolean isBookableAccommodation = false;
                for (Room room : rooms) {
                    if (room.getCapacity() >= guestCount) { // 1차: 인원 수용 가능 여부
                        int availableCount = reservationService.calculateMinAvailableCountForRoom(room, checkIn, checkOut, guestCount);
                        if (availableCount > 0) {
                            isBookableAccommodation = true;
                            break; // 이 숙소는 예약 가능한 객실이 있음
                        }
                    }
                }
                if (isBookableAccommodation) {
                    availableAccommodations.add(acc);
                }
            }
            log.debug("After secondary availability check, {} accommodations are available.", availableAccommodations.size());
            // TODO: totalCount도 이 기준으로 다시 세어야 할 수 있으나, 복잡도를 높임.
            // 우선 프론트엔드에서는 필터링 된 목록을 보여주고, 페이지네이션은 초기 DB 카운트 기준으로 할 수 있음.
            // 또는, 여기서 필터링된 목록만으로 페이지네이션 정보를 재구성.
            // 현재는 DB 카운트를 그대로 사용하고, 필터링된 숙소 목록만 교체합니다.
        } else {
            availableAccommodations.addAll(accommodations); // 날짜/인원 필터 없으면 모두 추가
        }

        // 이미지 설정
        setMainImageUrlForAccommodations(availableAccommodations);

        Map<String, Object> response = new HashMap<>();
        response.put("content", availableAccommodations); // 필터링된 숙소 목록
        response.put("currentPage", filters.get("page"));
        response.put("totalItems", totalCount); // DB에서 가져온 전체 아이템 수 (2차 필터링 전 기준)
        response.put("totalPages", (int) Math.ceil((double) totalCount / (int) filters.get("size")));
        
        log.info("Service: getFilteredAccommodations response: {}", response);
        return response;
    }


    /**
     * 필터링된 객실 목록을 조회합니다.
     */
    @Override
    public List<Room> getFilteredRooms(Map<String, Object> filters) throws SQLException {
        List<Room> rooms = roomDao.getFilteredRooms(filters);
        for (Room room : rooms) {
            setImagesForSingleRoom(room);
        }
        return rooms;
    }

    /**
     * 외부 API에서 가져온 숙소 정보를 저장합니다.
     */
    @Override
    @Transactional
    public Long importFromApi(Accommodation accommodation, List<Room> rooms, List<Image> images) throws SQLException {
        accommodationDao.insertFromApi(accommodation);
        Long accommodationId = accommodation.getAccommodationId();
        log.debug("[DEBUG_LOG] Generated accommodation ID: {}", accommodationId);
        log.debug("[DEBUG_LOG] Accommodation object after insert: {}", accommodation);

        List<Image> accommodationImages = new ArrayList<>();
        for (Image image : images) {
            if ("ACCOMMODATION".equals(image.getReferenceType())) {
                image.setReferenceId(accommodationId);
                image.setAccommodationId(accommodationId);
                accommodationImages.add(image);
                log.debug("[DEBUG_LOG] Inserting accommodation image with accommodation_id: {}", image.getAccommodationId());
                imageDao.insertFromApi(image);
            }
        }

        Map<Long, Long> oldToNewRoomIds = new HashMap<>();
        for (Room room : rooms) {
            Long oldRoomId = room.getRoomId();
            room.setAccommodationId(accommodationId);
            log.debug("[DEBUG_LOG] Setting accommodation_id on room: {}", room.getAccommodationId());

            roomDao.insertFromApi(room);
            Long newRoomId = room.getRoomId();
            log.debug("[DEBUG_LOG] Generated room ID: {}", newRoomId);
            log.debug("[DEBUG_LOG] Room object after insert: {}", room);
            oldToNewRoomIds.put(oldRoomId, newRoomId);
        }

        for (Image image : images) {
            if ("ROOM".equals(image.getReferenceType())) {
                Long oldRoomId = image.getReferenceId();
                Long newRoomId = oldToNewRoomIds.get(oldRoomId);
                log.debug("[DEBUG_LOG] Room image - oldRoomId: {}, newRoomId: {}", oldRoomId, newRoomId);

                if (newRoomId != null) {
                    image.setReferenceId(newRoomId);
                    image.setRoomId(newRoomId);
                    image.setAccommodationId(accommodationId);
                    log.debug("[DEBUG_LOG] Inserting room image with accommodation_id: {}, room_id: {}", image.getAccommodationId(), image.getRoomId());
                    imageDao.insertFromApi(image);
                } else {
                    log.warn("[DEBUG_LOG] WARNING: newRoomId is null for oldRoomId: {}", oldRoomId);
                }
            }
        }
        return accommodationId;
    }

    /**
     * 유사한 숙소 목록을 조회합니다.
     */
    @Override
    public List<Accommodation> getSimilarAccommodations(Long accommodationId, int limit) throws SQLException {
        List<Accommodation> similarAccommodations = accommodationDao.getSimilarAccommodations(accommodationId, limit);
        setMainImageUrlForAccommodations(similarAccommodations);
        return similarAccommodations;
    }

    /**
     * 모든 숙소를 삭제합니다.
     * 관련된 모든 객실과 이미지도 함께 삭제됩니다.
     */
    @Override
    @Transactional
    public int deleteAllAccommodations() throws SQLException {
        int deletedCount = accommodationDao.deleteAllAccommodations();
        log.info("{}개의 모든 숙소가 삭제되었습니다.", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Accommodation> getPendingReviewAccommodations(Pageable pageable) throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("status", Accommodation.AccommodationStatus.PENDING_REVIEW.name());
        params.put("offset", pageable.getOffset());
        params.put("limit", pageable.getPageSize());
        
        if (pageable.getSort().isSorted()) {
            String sortBy = pageable.getSort().get().findFirst().map(Sort.Order::getProperty).orElse("createdAt");
            String sortDirection = pageable.getSort().get().findFirst().map(order -> order.getDirection().name()).orElse("DESC");
            params.put("sortBy", sortBy);
            params.put("sortDirection", sortDirection);
        } else {
            params.put("sortBy", "createdAt");
            params.put("sortDirection", "DESC");
        }

        List<Accommodation> content = accommodationDao.getFilteredAccommodations(params);
        setMainImageUrlForAccommodations(content);

        Map<String, Object> countParams = new HashMap<>(params);
        countParams.remove("offset");
        countParams.remove("limit");
        long total = accommodationDao.countFilteredAccommodations(countParams);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional
    public void approveAccommodation(Long accommodationId) throws SQLException {
        Accommodation accommodation = accommodationDao.getAccommodationById(accommodationId);
        if (accommodation == null) {
            throw new NoSuchElementException("ID " + accommodationId + "에 해당하는 숙소를 찾을 수 없습니다.");
        }
        if (accommodation.getStatus() != Accommodation.AccommodationStatus.PENDING_REVIEW) {
            throw new IllegalStateException("숙소 ID " + accommodationId + "는 현재 승인 대기 상태가 아닙니다. (현재 상태: " + accommodation.getStatus() + ")");
        }
        accommodationDao.updateAccommodationStatus(accommodationId, Accommodation.AccommodationStatus.ACTIVE.name());
        log.info("숙소 ID {} 가 승인되었습니다.", accommodationId);
        // 예: notificationService.sendAccommodationApprovedNotification(accommodation.getHostId(), accommodation.getTitle());
    }

    @Override
    @Transactional
    public void rejectAccommodation(Long accommodationId, String reason) throws SQLException {
        Accommodation accommodation = accommodationDao.getAccommodationById(accommodationId);
        if (accommodation == null) {
            throw new NoSuchElementException("ID " + accommodationId + "에 해당하는 숙소를 찾을 수 없습니다.");
        }
        if (accommodation.getStatus() != Accommodation.AccommodationStatus.PENDING_REVIEW) {
            throw new IllegalStateException("숙소 ID " + accommodationId + "는 현재 승인 대기 상태가 아닙니다. (현재 상태: " + accommodation.getStatus() + ")");
        }
        accommodationDao.updateAccommodationStatus(accommodationId, Accommodation.AccommodationStatus.REJECTED.name());
        log.info("숙소 ID {} 가 거절되었습니다. 사유: {}", accommodationId, reason);

        if (accommodation.getHostId() != null) {
            Notification notification = Notification.builder()
                .userId(accommodation.getHostId())
                .title("숙소 등록 신청 거절 알림")
                .content("회원님의 숙소 '" + accommodation.getTitle() + "' 등록 신청이 관리자에 의해 거절되었습니다. 사유: " + reason)
                .notificationType("ACCOMMODATION_APPLICATION_REJECTED")
                .referenceId(accommodationId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
            notificationService.createNotification(notification);
        } else {
            log.warn("숙소 ID {}에 호스트 ID가 없어 거절 알림을 발송할 수 없습니다.", accommodationId);
        }
    }

    // Helper method to update accommodation room price statistics
    private void updateAccommodationRoomPriceStats(Long accommodationId) throws SQLException {
        if (accommodationId == null) {
            log.warn("Accommodation ID is null. Cannot update room price stats.");
            return;
        }

        List<Double> prices = roomDao.selectActiveRoomPricesByAccommodationId(accommodationId);
        Double minPrice = null;
        Double maxPrice = null;

        if (prices != null && !prices.isEmpty()) {
            minPrice = Collections.min(prices);
            maxPrice = Collections.max(prices);
        } else {
            // 활성 객실이 없는 경우, 가격을 0 또는 null로 설정할 수 있습니다.
            // 여기서는 0.0으로 설정합니다.
            minPrice = 0.0;
            maxPrice = 0.0;
        }
        
        accommodationDao.updateRoomPriceStats(accommodationId, minPrice, maxPrice);
        log.info("Updated room price stats for accommodation {}: minPrice={}, maxPrice={}",
                accommodationId, minPrice, maxPrice);
    }

    @Override
    @Transactional
    public AccommodationResponseDto createAccommodationAndImages(AccommodationRequestDto requestDto, Long hostId) throws Exception {
        Accommodation accommodation = new Accommodation();
        accommodation.setHostId(hostId);
        accommodation.setTitle(requestDto.getTitle());
        accommodation.setAccommodationType(requestDto.getAccommodationType());
        accommodation.setDescription(requestDto.getDescription());
        accommodation.setAddress(requestDto.getAddress());
        if (requestDto.getSidoCode() != null && !requestDto.getSidoCode().isEmpty()) accommodation.setSidoCode(Integer.parseInt(requestDto.getSidoCode()));
        if (requestDto.getGugunCode() != null && !requestDto.getGugunCode().isEmpty()) accommodation.setGugunCode(Integer.parseInt(requestDto.getGugunCode()));
        accommodation.setLatitude(requestDto.getLatitude());
        accommodation.setLongitude(requestDto.getLongitude());
        try {
            if (requestDto.getCheckInTime() != null) accommodation.setCheckInTime(LocalTime.parse(requestDto.getCheckInTime()));
            if (requestDto.getCheckOutTime() != null) accommodation.setCheckOutTime(LocalTime.parse(requestDto.getCheckOutTime()));
        } catch (DateTimeParseException e) {
            log.error("시간 형식 파싱 오류: {}", e.getMessage());
            throw new IllegalArgumentException("체크인/아웃 시간 형식이 올바르지 않습니다 (HH:mm:ss).");
        }
        accommodation.setPhone(requestDto.getPhone());
        accommodation.setPhoneNumber(requestDto.getPhone());
        accommodation.setEmail(requestDto.getEmail());
        accommodation.setWebsite(requestDto.getWebsite());
        accommodation.setAmenities(requestDto.getAmenities());
        accommodation.setStatus(Accommodation.AccommodationStatus.PENDING_REVIEW);
        accommodation.setCreatedAt(LocalDateTime.now());
        accommodation.setUpdatedAt(LocalDateTime.now());
        
        accommodationDao.insert(accommodation);
        Long accommodationId = accommodation.getAccommodationId();
        if (accommodationId == null) {
            throw new SQLException("숙소 정보 저장 실패: ID를 반환받지 못했습니다.");
        }

        List<Image> savedImages = new ArrayList<>();
        MultipartFile mainImageFile = requestDto.getMainImageFile();
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String mainImageUrl = uploadImageToS3(mainImageFile);
            Image mainImage = Image.builder().referenceId(accommodationId).referenceType("ACCOMMODATION")
                    .accommodationId(accommodationId).imageUrl(mainImageUrl).isMain(true)
                    .caption(mainImageFile.getOriginalFilename()).createdAt(LocalDateTime.now()).build();
            imageDao.insert(mainImage);
            savedImages.add(mainImage);
        }

        List<MultipartFile> imageFiles = requestDto.getImageFiles();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = uploadImageToS3(file);
                    Image additionalImage = Image.builder().referenceId(accommodationId).referenceType("ACCOMMODATION")
                            .accommodationId(accommodationId).imageUrl(imageUrl).isMain(false)
                            .caption(file.getOriginalFilename()).createdAt(LocalDateTime.now()).build();
                    imageDao.insert(additionalImage);
                    savedImages.add(additionalImage);
        }
            }
        }
        Accommodation savedAccommodation = accommodationDao.getAccommodationById(accommodationId);
        return AccommodationResponseDto.fromEntity(savedAccommodation, savedImages, objectMapper);
    }

    @Override
    @Transactional
    public AccommodationResponseDto updateAccommodationAndImages(Long accommodationId, AccommodationRequestDto requestDto, Long hostId) throws Exception {
        Accommodation existingAccommodation = accommodationDao.getAccommodationById(accommodationId);
        if (existingAccommodation == null) {
            throw new NoSuchElementException("수정할 숙소를 찾을 수 없습니다. ID: " + accommodationId);
        }
        if (!existingAccommodation.getHostId().equals(hostId)) {
            throw new IllegalAccessException("해당 숙소에 대한 수정 권한이 없습니다.");
        }

        existingAccommodation.setTitle(requestDto.getTitle());
        existingAccommodation.setAccommodationType(requestDto.getAccommodationType());
        existingAccommodation.setDescription(requestDto.getDescription());
        existingAccommodation.setAddress(requestDto.getAddress());
        if (requestDto.getSidoCode() != null && !requestDto.getSidoCode().isEmpty()) existingAccommodation.setSidoCode(Integer.parseInt(requestDto.getSidoCode()));
        if (requestDto.getGugunCode() != null && !requestDto.getGugunCode().isEmpty()) existingAccommodation.setGugunCode(Integer.parseInt(requestDto.getGugunCode()));
        existingAccommodation.setLatitude(requestDto.getLatitude());
        existingAccommodation.setLongitude(requestDto.getLongitude());
        try {
            if (requestDto.getCheckInTime() != null) existingAccommodation.setCheckInTime(LocalTime.parse(requestDto.getCheckInTime()));
            if (requestDto.getCheckOutTime() != null) existingAccommodation.setCheckOutTime(LocalTime.parse(requestDto.getCheckOutTime()));
        } catch (DateTimeParseException e) {
             throw new IllegalArgumentException("체크인/아웃 시간 형식이 올바르지 않습니다 (HH:mm:ss).");
        }
        existingAccommodation.setPhone(requestDto.getPhone());
        existingAccommodation.setPhoneNumber(requestDto.getPhone());
        existingAccommodation.setEmail(requestDto.getEmail());
        existingAccommodation.setWebsite(requestDto.getWebsite());
        existingAccommodation.setAmenities(requestDto.getAmenities());
        existingAccommodation.setUpdatedAt(LocalDateTime.now());

        if (requestDto.getDeletedImageIds() != null && !requestDto.getDeletedImageIds().isEmpty()) {
            try {
                List<Long> deletedImageIds = objectMapper.readValue(requestDto.getDeletedImageIds(), new TypeReference<List<Long>>() {});
                for (Long imageIdToDelete : deletedImageIds) {
                    Image imageToDelete = imageDao.getImageById(imageIdToDelete);
                    if (imageToDelete != null && imageToDelete.getAccommodationId().equals(accommodationId)) {
                        deleteFileFromS3ByUrl(imageToDelete.getImageUrl());
                        imageDao.deleteImage(imageIdToDelete);
                    }
                }
            } catch (Exception e) {
                log.error("삭제할 이미지 ID 파싱 오류: {}", requestDto.getDeletedImageIds(), e);
            }
        }
        
        MultipartFile mainImageFile = requestDto.getMainImageFile();
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            List<Image> currentImages = imageDao.getImagesByReference(accommodationId, "ACCOMMODATION");
            currentImages.stream().filter(Image::getIsMain).findFirst().ifPresent(oldMainImage -> {
                try {
                    deleteFileFromS3ByUrl(oldMainImage.getImageUrl());
                    imageDao.deleteImage(oldMainImage.getImageId());
                } catch (Exception e) { log.error("기존 대표 이미지 DB/S3 삭제 실패", e);}
            });
            String mainImageUrl = uploadImageToS3(mainImageFile);
            Image newMainImage = Image.builder().referenceId(accommodationId).referenceType("ACCOMMODATION")
                .accommodationId(accommodationId).imageUrl(mainImageUrl).isMain(true)
                .caption(mainImageFile.getOriginalFilename()).createdAt(LocalDateTime.now()).build();
            imageDao.insert(newMainImage);
        } else if (requestDto.getMainImageId() == null && mainImageFile == null) { 
             List<Image> currentImages = imageDao.getImagesByReference(accommodationId, "ACCOMMODATION");
             currentImages.stream().filter(Image::getIsMain).findFirst().ifPresent(oldMainImage -> {
                try {
                    deleteFileFromS3ByUrl(oldMainImage.getImageUrl());
                    imageDao.deleteImage(oldMainImage.getImageId());
                } catch (Exception e) { log.error("대표 이미지 DB/S3 삭제 실패", e);}
            });
        }

        List<MultipartFile> imageFiles = requestDto.getImageFiles();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = uploadImageToS3(file);
                    Image additionalImage = Image.builder().referenceId(accommodationId).referenceType("ACCOMMODATION")
                        .accommodationId(accommodationId).imageUrl(imageUrl).isMain(false)
                        .caption(file.getOriginalFilename()).createdAt(LocalDateTime.now()).build();
                    imageDao.insert(additionalImage);
                }
            }
        }
        accommodationDao.updateAccommodation(existingAccommodation);
        Accommodation updatedAccommodation = accommodationDao.getAccommodationById(accommodationId);
        List<Image> updatedImages = imageDao.getImagesByReference(accommodationId, "ACCOMMODATION");
        return AccommodationResponseDto.fromEntity(updatedAccommodation, updatedImages, objectMapper);
    }

    @Override
    public AccommodationResponseDto getAccommodationDetails(Long accommodationId) throws Exception {
        Accommodation accommodation = accommodationDao.getAccommodationById(accommodationId);
        if (accommodation == null) {
            throw new NoSuchElementException("숙소를 찾을 수 없습니다. ID: " + accommodationId);
        }
        List<Image> images = imageDao.getImagesByReference(accommodationId, "ACCOMMODATION");
        return AccommodationResponseDto.fromEntity(accommodation, images, objectMapper);
    }

    private String uploadImageToS3(MultipartFile file) throws IOException {
        return awss3Service.uploadFile(file); 
    }

    private void deleteFileFromS3ByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try {
            awss3Service.deleteImage(fileUrl);
            log.info("S3 파일 삭제 시도 완료: {}", fileUrl);
        } catch (Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: {}", fileUrl, e);
        }
    }

    // --- Room Management for Host ---
    @Override
    @Transactional
    public RoomResponseDto createRoomAndImages(Long accommodationId, RoomRequestDto requestDto, Long hostId) throws Exception {
        Accommodation accommodation = accommodationDao.getAccommodationById(accommodationId);
        if (accommodation == null) {
            throw new NoSuchElementException("객실을 추가할 숙소를 찾을 수 없습니다. ID: " + accommodationId);
        }
        if (!accommodation.getHostId().equals(hostId)) {
            throw new IllegalAccessException("해당 숙소에 객실을 추가할 권한이 없습니다.");
        }

        Room room = new Room();
        room.setAccommodationId(accommodationId);
        room.setName(requestDto.getName());
        room.setDescription(requestDto.getDescription());
        room.setPrice(requestDto.getPrice());
        room.setCapacity(requestDto.getCapacity());
        room.setMaxCapacity(requestDto.getMaxCapacity());
        room.setRoomCount(requestDto.getRoomCount());
        room.setRoomType(requestDto.getRoomType());
        room.setAmenities(requestDto.getAmenities()); // JSON string or comma-separated
        room.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : "AVAILABLE"); // 기본 상태
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());

        roomDao.insert(room); // Mybatis의 경우 insert 후 room 객체에 roomId가 채워지도록 설정 필요
        Long roomId = room.getRoomId();
        if (roomId == null) {
            throw new SQLException("객실 정보 저장 실패: ID를 반환받지 못했습니다.");
        }

        List<Image> savedImages = new ArrayList<>();
        MultipartFile mainImageFile = requestDto.getMainImageFile();
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String mainImageUrl = uploadImageToS3(mainImageFile);
            Image mainImage = Image.builder()
                    .referenceId(roomId)
                    .referenceType("ROOM")
                    .accommodationId(accommodationId) // Image 테이블에 accommodation_id도 있다면 설정
                    .roomId(roomId) // Image 테이블에 room_id도 있다면 설정
                    .imageUrl(mainImageUrl)
                    .isMain(true)
                    .caption(mainImageFile.getOriginalFilename())
                    .createdAt(LocalDateTime.now()).build();
            imageDao.insert(mainImage);
            savedImages.add(mainImage);
        }

        List<MultipartFile> imageFiles = requestDto.getImageFiles();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = uploadImageToS3(file);
                    Image additionalImage = Image.builder()
                            .referenceId(roomId)
                            .referenceType("ROOM")
                            .accommodationId(accommodationId)
                            .roomId(roomId)
                            .imageUrl(imageUrl)
                            .isMain(false)
                            .caption(file.getOriginalFilename())
                            .createdAt(LocalDateTime.now()).build();
                    imageDao.insert(additionalImage);
                    savedImages.add(additionalImage);
                }
            }
        }

        updateAccommodationRoomPriceStats(accommodationId); // 숙소 가격 통계 업데이트

        // 저장된 객실 정보와 이미지 정보를 다시 로드하여 DTO 생성 (ID가 채워진 엔티티 사용)
        Room savedRoom = roomDao.getRoomById(roomId);
        // savedImages 리스트는 이미 DB에 저장된 이미지 정보를 반영하고 있으므로 그대로 사용 가능
        // 만약 imageDao.insert 후 ID가 채워진 Image 객체를 반환받는다면 그 객체들로 리스트를 다시 구성하는 것이 더 정확
        // 여기서는 savedImages가 DB 저장 후 ID를 포함한다고 가정하거나, 다시 조회
        List<Image> currentRoomImages = imageDao.getImagesByReference(roomId, "ROOM");

        return RoomResponseDto.fromEntity(savedRoom, currentRoomImages, objectMapper);
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoomAndImages(Long roomId, RoomRequestDto requestDto, Long hostId) throws Exception {
        Room existingRoom = roomDao.getRoomById(roomId);
        if (existingRoom == null) {
            throw new NoSuchElementException("수정할 객실을 찾을 수 없습니다. ID: " + roomId);
        }
        Accommodation accommodation = accommodationDao.getAccommodationById(existingRoom.getAccommodationId());
        if (accommodation == null || !accommodation.getHostId().equals(hostId)) {
            throw new IllegalAccessException("해당 객실을 수정할 권한이 없습니다.");
        }

        // 객실 기본 정보 업데이트
        if (requestDto.getName() != null) existingRoom.setName(requestDto.getName());
        if (requestDto.getDescription() != null) existingRoom.setDescription(requestDto.getDescription());
        if (requestDto.getPrice() != null) existingRoom.setPrice(requestDto.getPrice());
        if (requestDto.getCapacity() != null) existingRoom.setCapacity(requestDto.getCapacity());
        if (requestDto.getMaxCapacity() != null) existingRoom.setMaxCapacity(requestDto.getMaxCapacity());
        if (requestDto.getRoomCount() != null) existingRoom.setRoomCount(requestDto.getRoomCount());
        if (requestDto.getRoomType() != null) existingRoom.setRoomType(requestDto.getRoomType());
        if (requestDto.getAmenities() != null) existingRoom.setAmenities(requestDto.getAmenities());
        if (requestDto.getStatus() != null) existingRoom.setStatus(requestDto.getStatus());
        existingRoom.setUpdatedAt(LocalDateTime.now());

        // 이미지 처리
        // 1. 삭제 요청된 기존 이미지 처리
        if (requestDto.getDeletedImageIds() != null && !requestDto.getDeletedImageIds().isEmpty()) {
            try {
                List<Long> deletedImageIdsList = objectMapper.readValue(requestDto.getDeletedImageIds(), new TypeReference<List<Long>>() {});
                for (Long imageIdToDelete : deletedImageIdsList) {
                    Image imageToDelete = imageDao.getImageById(imageIdToDelete);
                    // 해당 객실의 이미지가 맞는지, 그리고 삭제 권한이 있는지 이중 확인 (이미 위에서 room/accommodation 권한은 확인)
                    if (imageToDelete != null && imageToDelete.getRoomId() != null && imageToDelete.getRoomId().equals(roomId)) {
                        deleteFileFromS3ByUrl(imageToDelete.getImageUrl());
                        imageDao.deleteImage(imageIdToDelete);
                        log.info("객실 ID {}의 이미지 ID {}가 삭제되었습니다.", roomId, imageIdToDelete);
                    }
                }
            } catch (Exception e) {
                log.error("삭제할 객실 이미지 ID 파싱 오류 또는 처리 중 오류: {}", requestDto.getDeletedImageIds(), e);
                // 필요시 예외를 다시 던지거나, 오류 응답을 위한 처리
            }
        }

        // 2. 대표 이미지 처리
        MultipartFile mainImageFile = requestDto.getMainImageFile();
        Long newMainImageIdFromRequest = requestDto.getMainImageId();
        List<Image> currentRoomImages = imageDao.getImagesByReference(roomId, "ROOM");
        Image oldMainImage = currentRoomImages.stream().filter(img -> img.getIsMain() != null && img.getIsMain()).findFirst().orElse(null);

        if (mainImageFile != null && !mainImageFile.isEmpty()) { // 새 대표 이미지 파일이 업로드된 경우
            if (oldMainImage != null) { // 기존 대표 이미지가 있었다면 삭제
                deleteFileFromS3ByUrl(oldMainImage.getImageUrl());
                imageDao.deleteImage(oldMainImage.getImageId());
            }
            String mainImageUrl = uploadImageToS3(mainImageFile);
            Image newMainPic = Image.builder().referenceId(roomId).referenceType("ROOM").roomId(roomId).accommodationId(existingRoom.getAccommodationId())
                    .imageUrl(mainImageUrl).isMain(true).caption(mainImageFile.getOriginalFilename()).createdAt(LocalDateTime.now()).build();
            imageDao.insert(newMainPic);
        } else if (newMainImageIdFromRequest != null) { // 기존 이미지를 새 대표 이미지로 지정한 경우 (파일 업로드 없음)
            if (oldMainImage != null && !oldMainImage.getImageId().equals(newMainImageIdFromRequest)) {
                // 기존 대표 이미지가 있었고, 요청된 새 대표 이미지 ID와 다르면 기존 대표는 isMain=false로
                oldMainImage.setIsMain(false);
                imageDao.updateImage(oldMainImage); // updateImage 메서드가 ImageDao에 필요
            }
            // 새 대표 이미지로 지정된 이미지를 isMain=true로 설정
            Image imageToSetAsMain = imageDao.getImageById(newMainImageIdFromRequest);
            if (imageToSetAsMain != null && imageToSetAsMain.getRoomId().equals(roomId)) {
                imageToSetAsMain.setIsMain(true);
                imageDao.updateImage(imageToSetAsMain); // updateImage 메서드가 ImageDao에 필요
            }
                    } else {
            // 새 대표 이미지 파일도 없고, 유지/변경할 기존 대표 이미지 ID도 없는 경우
            // 만약 oldMainImage가 있었는데 deletedImageIds에 포함되어 삭제되었다면, 대표 이미지가 없는 상태가 됨.
            // 이 경우, 추가 이미지 중 첫 번째를 대표로 삼거나, 대표 이미지 없이 둘 수 있음 (정책에 따라).
            // 여기서는 별도 처리 안 함 (대표 이미지가 없을 수 있음).
            if (oldMainImage != null && requestDto.getDeletedImageIds() != null && 
                objectMapper.readValue(requestDto.getDeletedImageIds(), new TypeReference<List<Long>>() {}).contains(oldMainImage.getImageId())) {
                // 대표 이미지가 삭제된 경우임.
                log.info("객실 ID {}의 대표 이미지가 삭제되었습니다.", roomId);
                    }
                }

        // 3. 추가 이미지 파일들 처리
        List<MultipartFile> additionalImageFiles = requestDto.getImageFiles();
        if (additionalImageFiles != null && !additionalImageFiles.isEmpty()) {
            for (MultipartFile file : additionalImageFiles) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = uploadImageToS3(file);
                    Image additionalImage = Image.builder().referenceId(roomId).referenceType("ROOM").roomId(roomId).accommodationId(existingRoom.getAccommodationId())
                            .imageUrl(imageUrl).isMain(false).caption(file.getOriginalFilename()).createdAt(LocalDateTime.now()).build();
                    imageDao.insert(additionalImage);
                }
            }
        }

        roomDao.updateRoom(existingRoom);
        updateAccommodationRoomPriceStats(accommodation.getAccommodationId());

        Room updatedRoom = roomDao.getRoomById(roomId); // 변경된 최종 정보 다시 로드
        List<Image> finalImages = imageDao.getImagesByReference(roomId, "ROOM");
        return RoomResponseDto.fromEntity(updatedRoom, finalImages, objectMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDto getRoomDetailsForHost(Long roomId, Long hostId) throws Exception {
        // TODO: 구현 필요 (권한 확인, 객실 및 이미지 조회)
        Room room = roomDao.getRoomById(roomId);
        if (room == null) {
            throw new NoSuchElementException("객실을 찾을 수 없습니다. ID: " + roomId);
        }
        Accommodation accommodation = accommodationDao.getAccommodationById(room.getAccommodationId());
        if (accommodation == null || !accommodation.getHostId().equals(hostId)) {
            throw new IllegalAccessException("해당 객실을 조회할 권한이 없습니다.");
        }
        List<Image> images = imageDao.getImagesByReference(roomId, "ROOM");
        return RoomResponseDto.fromEntity(room, images, objectMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDto> getRoomsForHost(Long accommodationId, Long hostId) throws Exception {
        // TODO: 구현 필요 (권한 확인, 숙소의 모든 객실 및 각 객실 이미지 조회)
        Accommodation accommodation = accommodationDao.getAccommodationById(accommodationId);
        if (accommodation == null || !accommodation.getHostId().equals(hostId)) {
            throw new IllegalAccessException("해당 숙소의 객실 목록을 조회할 권한이 없습니다.");
        }
        List<Room> rooms = roomDao.getRoomsByAccommodationId(accommodationId); // 이 메서드는 페이징/필터링 없는 전체 목록 가정
        List<RoomResponseDto> roomResponseDtos = new ArrayList<>();
        for (Room room : rooms) {
            List<Image> images = imageDao.getImagesByReference(room.getRoomId(), "ROOM");
            roomResponseDtos.add(RoomResponseDto.fromEntity(room, images, objectMapper));
        }
        return roomResponseDtos;
    }

    @Override
    @Transactional
    public void deleteRoomAndImages(Long roomId, Long hostId) throws Exception {
        // TODO: 구현 필요 (권한 확인, 이미지 S3 삭제, DB 이미지 레코드 삭제, 객실 레코드 삭제, 가격 통계 업데이트)
        Room room = roomDao.getRoomById(roomId);
        if (room == null) {
            throw new NoSuchElementException("삭제할 객실을 찾을 수 없습니다. ID: " + roomId);
        }
        Accommodation accommodation = accommodationDao.getAccommodationById(room.getAccommodationId());
        if (accommodation == null || !accommodation.getHostId().equals(hostId)) {
            throw new IllegalAccessException("해당 객실을 삭제할 권한이 없습니다.");
        }

        List<Image> imagesToDelete = imageDao.getImagesByReference(roomId, "ROOM");
        for (Image image : imagesToDelete) {
            deleteFileFromS3ByUrl(image.getImageUrl());
            imageDao.deleteImage(image.getImageId()); // 개별 이미지 ID로 삭제
        }
        // imageDao.deleteImagesByReference(roomId, "ROOM"); // 또는 reference로 한번에 삭제 (이 경우 S3 삭제는 위에서 처리)

        roomDao.deleteRoom(roomId);
        updateAccommodationRoomPriceStats(room.getAccommodationId());
        log.info("객실 ID {} 가 삭제되었습니다.", roomId);
    }
}

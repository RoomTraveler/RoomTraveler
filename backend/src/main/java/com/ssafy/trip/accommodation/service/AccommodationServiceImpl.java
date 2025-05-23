package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.AccommodationDao;
import com.ssafy.trip.accommodation.dao.RoomDao;
import com.ssafy.trip.accommodation.dao.ImageDao;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.accommodation.service.ReservationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        int result = accommodationDao.updateAccommodation(accommodation);
        if (images != null) {
            imageDao.deleteImagesByReference(accommodation.getAccommodationId(), "ACCOMMODATION");
            for (Image image : images) {
                image.setReferenceId(accommodation.getAccommodationId());
                image.setReferenceType("ACCOMMODATION");
                image.setAccommodationId(accommodation.getAccommodationId());
                imageDao.insert(image);
            }
        }
        return result;
    }

    /**
     * 객실 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateRoom(Room room, List<Image> images) throws SQLException {
        // 기존 객실 정보 조회 (숙소 ID를 얻기 위함)
        Room existingRoom = roomDao.getRoomById(room.getRoomId());
        if (existingRoom == null) {
            throw new SQLException("수정할 객실을 찾을 수 없습니다: " + room.getRoomId());
        }
        Long accommodationId = existingRoom.getAccommodationId();

        // 객실 정보 업데이트
        int updatedRows = roomDao.updateRoom(room);

        // 이미지 업데이트 (기존 이미지 삭제 및 새 이미지 추가 로직)
        // (이미지 관련 로직은 기존과 동일하게 유지하거나 필요에 따라 수정)
        if (images != null) { // images 파라미터가 null이 아닐 때만 처리 (기존 이미지 유지 또는 변경)
            // 1. 이 객실의 기존 이미지 전체 삭제 (간단한 방식)
            imageDao.deleteImagesByReference(room.getRoomId(), "ROOM");
            // 2. 새 이미지 목록 등록
            if (!images.isEmpty()) {
                for (Image image : images) {
                    image.setReferenceId(room.getRoomId());
                    image.setReferenceType("ROOM");
                    image.setRoomId(room.getRoomId());
                    image.setAccommodationId(accommodationId);
                    imageDao.insert(image);
                }
            }
        }
        
        // 숙소의 객실 가격 통계 업데이트
        if (updatedRows > 0) {
            updateAccommodationRoomPriceStats(accommodationId);
        }
        
        return updatedRows;
    }

    /**
     * 숙소 상태를 업데이트합니다.
     */
    @Override
    public int updateAccommodationStatus(Long accommodationId, String status) throws SQLException {
        return accommodationDao.updateAccommodationStatus(accommodationId, status);
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
        return accommodationDao.deleteAllAccommodations();
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
}

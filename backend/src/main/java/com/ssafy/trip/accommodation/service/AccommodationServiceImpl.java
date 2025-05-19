package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.AccommodationDao;
import com.ssafy.trip.accommodation.dao.RoomDao;
import com.ssafy.trip.accommodation.dao.ImageDao;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.Image;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
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

        // 이미지 등록
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                image.setReferenceId(roomId);
                image.setReferenceType("ROOM");
                image.setRoomId(roomId);
                image.setAccommodationId(room.getAccommodationId());
                imageDao.insert(image);
            }
        }
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
     * 숙소 ID로 객실 목록을 조회합니다.
     */
    @Override
    public List<Room> getRoomsByAccommodationId(Long accommodationId) throws SQLException {
        List<Room> rooms = roomDao.getRoomsByAccommodationId(accommodationId);
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
        int result = roomDao.updateRoom(room);
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
        return result;
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
        imageDao.deleteImagesByReference(roomId, "ROOM");
        return roomDao.deleteRoom(roomId);
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
        log.debug("Service: getFilteredAccommodations - START with filters: {}", filters);

        int page = (int) filters.getOrDefault("page", 1); // 기본값 설정
        int size = (int) filters.getOrDefault("size", 10); // 기본값 설정

        log.debug("Service: Calling DAO countFilteredAccommodations with filters: {}", filters);
        long totalItems = accommodationDao.countFilteredAccommodations(filters);
        log.debug("Service: DAO countFilteredAccommodations returned: {}", totalItems);

        List<Accommodation> accommodations;
        if (totalItems > 0) {
            log.debug("Service: Calling DAO getFilteredAccommodations with filters: {}", filters);
            accommodations = accommodationDao.getFilteredAccommodations(filters);
            log.debug("Service: DAO getFilteredAccommodations returned {} accommodations.", accommodations != null ? accommodations.size() : "null list");
            
            if (accommodations == null) { // 방어 코드
                log.warn("Service: accommodationDao.getFilteredAccommodations returned null. Initializing to empty list.");
                accommodations = new ArrayList<>();
            }
        } else {
            log.debug("Service: No items found by count, returning empty list for accommodations.");
            accommodations = new ArrayList<>();
        }

        try {
            log.debug("Service: Attempting to set main image URLs for {} accommodations.", accommodations.size());
            setMainImageUrlForAccommodations(accommodations); // 이 메소드 내부도 SQLException을 던질 수 있으니 확인 필요
            log.debug("Service: Successfully set main image URLs.");
        } catch (SQLException se) {
            log.error("Service: SQL Exception during setMainImageUrlForAccommodations: {}", se.getMessage(), se);
            throw se; // SQLException은 다시 던져서 컨트롤러에서 처리하도록 함
        } catch (Exception e) {
            log.error("Service: Unexpected Exception during setMainImageUrlForAccommodations: {}", e.getMessage(), e);
            // 일반 Exception은 SQLException으로 감싸서 던지거나, 혹은 별도의 처리 필요
            // 여기서는 RuntimeException으로 변환하여 상황을 알림
            throw new RuntimeException("Unexpected error while setting image URLs: " + e.getMessage(), e);
        }

        int totalPages = (totalItems == 0) ? 0 : (int) Math.ceil((double) totalItems / size);
        if (totalPages == 0 && page > 0 && totalItems == 0) { // 아이템이 없고 페이지가 0보다 크면 현재 페이지를 0 또는 1로 조정
             page = 0; // 혹은 1로 유지할지 정책에 따라 결정. 현재는 0으로.
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", accommodations);
        result.put("currentPage", page);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        log.debug("Service: getFilteredAccommodations - END returning result: {}", result);
        return result;
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
}

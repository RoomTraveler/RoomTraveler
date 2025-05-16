package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.AccommodationDao;
import com.ssafy.trip.accommodation.dao.RoomDao;
import com.ssafy.trip.accommodation.dao.ImageDao;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.Image;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

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
        if (accommodation == null) return;
        List<Image> images = imageDao.getImagesByReference(accommodation.getAccommodationId(), "ACCOMMODATION");
        if (!images.isEmpty()) {
            Image mainImage = images.stream().filter(img -> img.getIsMain() != null && img.getIsMain()).findFirst().orElse(images.get(0));
            String imageUrl = mainImage.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
            } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                imageUrl = "http://" + imageUrl;
            }
            accommodation.setMainImageUrl(imageUrl);
        }
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
        for (Accommodation accommodation : accommodations) {
            setMainImageUrlForSingleAccommodation(accommodation);
        }
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
     * 필터링된 숙소 목록을 조회합니다. (페이징 적용)
     */
    @Override
    public Map<String, Object> getFilteredAccommodations(Map<String, Object> filters, Pageable pageable) throws SQLException {
        Map<String, Object> params = new HashMap<>(filters);
        params.put("offset", pageable.getOffset());
        params.put("limit", pageable.getPageSize());

        log.debug("Service - getFilteredAccommodations - params for DAO: {}", params);

        List<Accommodation> accommodations = accommodationDao.getFilteredAccommodations(params);
        setMainImageUrlForAccommodations(accommodations);

        long totalItems = accommodationDao.countFilteredAccommodations(filters);
        log.debug("Service - getFilteredAccommodations - totalItems: {}", totalItems);

        Map<String, Object> response = new HashMap<>();
        response.put("content", accommodations);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("totalItems", totalItems);
        response.put("totalPages", (int) Math.ceil((double) totalItems / pageable.getPageSize()));
        
        log.debug("Service - getFilteredAccommodations - response: {}", response);
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
}

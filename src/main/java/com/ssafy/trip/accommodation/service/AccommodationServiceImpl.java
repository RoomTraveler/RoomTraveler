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

/**
 * 숙소 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
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
        Long accommodationId = accommodationDao.insert(accommodation);

        // 이미지 등록
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                image.setReferenceId(accommodationId);
                image.setReferenceType("ACCOMMODATION");
                image.setAccommodationId(accommodationId); // Explicitly set accommodation_id
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
        Long roomId = roomDao.insert(room);

        // 이미지 등록
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                image.setReferenceId(roomId);
                image.setReferenceType("ROOM");
                image.setRoomId(roomId); // Explicitly set room_id
                image.setAccommodationId(room.getAccommodationId()); // Explicitly set accommodation_id
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
        return accommodationDao.getAccommodationById(accommodationId);
    }

    /**
     * 객실 ID로 객실을 조회합니다.
     */
    @Override
    public Room getRoomById(Long roomId) throws SQLException {
        Room room = roomDao.getRoomById(roomId);
        if (room != null) {
            // 객실 이미지 URL 목록 조회
            List<Image> images = imageDao.getImagesByReference(roomId, "ROOM");
            List<String> imageUrls = new ArrayList<>();
            for (Image image : images) {
                String imageUrl = image.getImageUrl();
                // 이미지 URL이 비어있거나 유효하지 않은 경우 플레이스홀더 이미지 사용
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
                } else if (!imageUrl.startsWith("http")) {
                    // URL이 http로 시작하지 않으면 http://를 추가
                    imageUrl = "http://" + imageUrl;
                }
                imageUrls.add(imageUrl);
            }
            room.setImageUrls(imageUrls);
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

        // 각 객실의 이미지 URL 목록 조회
        for (Room room : rooms) {
            List<Image> images = imageDao.getImagesByReference(room.getRoomId(), "ROOM");
            List<String> imageUrls = new ArrayList<>();
            for (Image image : images) {
                String imageUrl = image.getImageUrl();
                // 이미지 URL이 비어있거나 유효하지 않은 경우 플레이스홀더 이미지 사용
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
                } else if (!imageUrl.startsWith("http")) {
                    // URL이 http로 시작하지 않으면 http://를 추가
                    imageUrl = "http://" + imageUrl;
                }
                imageUrls.add(imageUrl);
            }
            room.setImageUrls(imageUrls);
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

    /**
     * 숙소 목록에 대표 이미지 URL을 설정합니다.
     */
    private void setMainImageUrlForAccommodations(List<Accommodation> accommodations) throws SQLException {
        for (Accommodation accommodation : accommodations) {
            List<Image> images = imageDao.getImagesByReference(accommodation.getAccommodationId(), "ACCOMMODATION");
            if (!images.isEmpty()) {
                // 대표 이미지 찾기
                Image mainImage = null;
                for (Image image : images) {
                    if (image.getIsMain() != null && image.getIsMain()) {
                        mainImage = image;
                        break;
                    }
                }

                // 대표 이미지가 없으면 첫 번째 이미지 사용
                if (mainImage == null && !images.isEmpty()) {
                    mainImage = images.get(0);
                }

                if (mainImage != null) {
                    String imageUrl = mainImage.getImageUrl();
                    // 이미지 URL이 비어있거나 유효하지 않은 경우 플레이스홀더 이미지 사용
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
                    } else if (!imageUrl.startsWith("http")) {
                        // URL이 http로 시작하지 않으면 http://를 추가
                        imageUrl = "http://" + imageUrl;
                    }
                    accommodation.setMainImageUrl(imageUrl);
                }
            }
        }
    }

    /**
     * 숙소 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateAccommodation(Accommodation accommodation, List<Image> images) throws SQLException {
        int result = accommodationDao.updateAccommodation(accommodation);

        // 이미지 업데이트
        if (images != null) {
            // 기존 이미지 삭제
            imageDao.deleteImagesByReference(accommodation.getAccommodationId(), "ACCOMMODATION");

            // 새 이미지 등록
            for (Image image : images) {
                image.setReferenceId(accommodation.getAccommodationId());
                image.setReferenceType("ACCOMMODATION");
                image.setAccommodationId(accommodation.getAccommodationId()); // Explicitly set accommodation_id
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

        // 이미지 업데이트
        if (images != null) {
            // 기존 이미지 삭제
            imageDao.deleteImagesByReference(room.getRoomId(), "ROOM");

            // 새 이미지 등록
            for (Image image : images) {
                image.setReferenceId(room.getRoomId());
                image.setReferenceType("ROOM");
                image.setRoomId(room.getRoomId()); // Explicitly set room_id
                image.setAccommodationId(room.getAccommodationId()); // Explicitly set accommodation_id
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
        // 숙소에 속한 객실 목록 조회
        List<Room> rooms = roomDao.getRoomsByAccommodationId(accommodationId);

        // 각 객실 및 객실 이미지 삭제
        for (Room room : rooms) {
            imageDao.deleteImagesByReference(room.getRoomId(), "ROOM");
            roomDao.deleteRoom(room.getRoomId());
        }

        // 숙소 이미지 삭제
        imageDao.deleteImagesByReference(accommodationId, "ACCOMMODATION");

        // 숙소 삭제
        return accommodationDao.deleteAccommodation(accommodationId);
    }


    /**
     * 객실을 삭제합니다.
     */
    @Override
    @Transactional
    public int deleteRoom(Long roomId) throws SQLException {
        // 객실 이미지 삭제
        imageDao.deleteImagesByReference(roomId, "ROOM");

        // 객실 삭제
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
    public List<Accommodation> getFilteredAccommodations(Map<String, Object> filters) throws SQLException {
        List<Accommodation> accommodations = accommodationDao.getFilteredAccommodations(filters);
        setMainImageUrlForAccommodations(accommodations);
        return accommodations;
    }

    /**
     * 필터링된 객실 목록을 조회합니다.
     */
    @Override
    public List<Room> getFilteredRooms(Map<String, Object> filters) throws SQLException {
        List<Room> rooms = roomDao.getFilteredRooms(filters);

        // 각 객실의 이미지 URL 목록 조회
        for (Room room : rooms) {
            List<Image> images = imageDao.getImagesByReference(room.getRoomId(), "ROOM");
            List<String> imageUrls = new ArrayList<>();
            for (Image image : images) {
                String imageUrl = image.getImageUrl();
                // 이미지 URL이 비어있거나 유효하지 않은 경우 플레이스홀더 이미지 사용
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
                } else if (!imageUrl.startsWith("http")) {
                    // URL이 http로 시작하지 않으면 http://를 추가
                    imageUrl = "http://" + imageUrl;
                }
                imageUrls.add(imageUrl);
            }
            room.setImageUrls(imageUrls);
        }

        return rooms;
    }

    /**
     * 외부 API에서 가져온 숙소 정보를 저장합니다.
     */
    @Override
    @Transactional
    public Long importFromApi(Accommodation accommodation, List<Room> rooms, List<Image> images) throws SQLException {
        // 1) 숙소 insert: 반환값 없이 accommodation에 ID가 채워집니다.
        accommodationDao.insertFromApi(accommodation);
        // 2) 진짜 생성된 ID를 여기서 꺼내서 사용
        Long accommodationId = accommodation.getAccommodationId();
        System.out.println("[DEBUG_LOG] Generated accommodation ID: " + accommodationId);
        System.out.println("[DEBUG_LOG] Accommodation object after insert: " + accommodation);

        // 숙소 이미지 등록
        List<Image> accommodationImages = new ArrayList<>();
        for (Image image : images) {
            if ("ACCOMMODATION".equals(image.getReferenceType())) {
                image.setReferenceId(accommodationId);
                image.setAccommodationId(accommodationId); // Set accommodation_id for proper foreign key relationship
                accommodationImages.add(image);
                System.out.println("[DEBUG_LOG] Inserting accommodation image with accommodation_id: " + image.getAccommodationId());
                imageDao.insertFromApi(image);
            }
        }

        // 객실 및 객실 이미지 등록
        Map<Long, Long> oldToNewRoomIds = new HashMap<>();
        for (Room room : rooms) {
            // 원래 ID 저장
            Long oldRoomId = room.getRoomId();

            // 숙소 ID 설정
            room.setAccommodationId(accommodationId);
            System.out.println("[DEBUG_LOG] Setting accommodation_id on room: " + room.getAccommodationId());

            // 객실 등록 및 새 ID 받기
            roomDao.insertFromApi(room);
            Long newRoomId = room.getRoomId();
            System.out.println("[DEBUG_LOG] Generated room ID: " + newRoomId);
            System.out.println("[DEBUG_LOG] Room object after insert: " + room);

            // ID 매핑 저장
            oldToNewRoomIds.put(oldRoomId, newRoomId);
        }

        // 객실 이미지 등록 (별도 루프로 분리)
        for (Image image : images) {
            if ("ROOM".equals(image.getReferenceType())) {
                Long oldRoomId = image.getReferenceId();
                Long newRoomId = oldToNewRoomIds.get(oldRoomId);
                System.out.println("[DEBUG_LOG] Room image - oldRoomId: " + oldRoomId + ", newRoomId: " + newRoomId);

                if (newRoomId != null) {
                    image.setReferenceId(newRoomId);
                    image.setRoomId(newRoomId); // Set room_id for proper foreign key relationship
                    image.setAccommodationId(accommodationId); // Set accommodation_id for proper foreign key relationship
                    System.out.println("[DEBUG_LOG] Inserting room image with accommodation_id: " + image.getAccommodationId() + ", room_id: " + image.getRoomId());
                    imageDao.insertFromApi(image);
                } else {
                    System.out.println("[DEBUG_LOG] WARNING: newRoomId is null for oldRoomId: " + oldRoomId);
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

        // 각 숙소의 대표 이미지 URL 설정
        for (Accommodation accommodation : similarAccommodations) {
            List<Image> images = imageDao.getImagesByReference(accommodation.getAccommodationId(), "ACCOMMODATION");
            if (!images.isEmpty()) {
                // 대표 이미지 찾기
                Image mainImage = null;
                for (Image image : images) {
                    if (image.getIsMain() != null && image.getIsMain()) {
                        mainImage = image;
                        break;
                    }
                }

                // 대표 이미지가 없으면 첫 번째 이미지 사용
                if (mainImage == null && !images.isEmpty()) {
                    mainImage = images.get(0);
                }

                if (mainImage != null) {
                    String imageUrl = mainImage.getImageUrl();
                    // 이미지 URL이 비어있거나 유효하지 않은 경우 플레이스홀더 이미지 사용
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = "https://via.placeholder.com/800x600?text=No+Image+Available";
                    } else if (!imageUrl.startsWith("http")) {
                        // URL이 http로 시작하지 않으면 http://를 추가
                        imageUrl = "http://" + imageUrl;
                    }
                    accommodation.setMainImageUrl(imageUrl);
                }
            }
        }

        return similarAccommodations;
    }

    /**
     * 모든 숙소를 삭제합니다.
     * 관련된 모든 객실과 이미지도 함께 삭제됩니다.
     */
    @Override
    @Transactional
    public int deleteAllAccommodations() throws SQLException {
        // 외래 키 제약 조건으로 인해 accommodations 테이블의 레코드를 삭제하면
        // 관련된 모든 rooms와 images도 자동으로 삭제됩니다 (ON DELETE CASCADE).
        return accommodationDao.deleteAllAccommodations();
    }
}

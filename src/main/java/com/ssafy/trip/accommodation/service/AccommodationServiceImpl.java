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
                imageUrls.add(image.getImageUrl());
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
        return accommodationDao.getAccommodationsByHostId(hostId);
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
                imageUrls.add(image.getImageUrl());
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
        return accommodationDao.getAccommodationsByRegion(sidoCode, gugunCode);
    }
    
    /**
     * 키워드로 숙소를 검색합니다.
     */
    @Override
    public List<Accommodation> searchAccommodations(String keyword) throws SQLException {
        return accommodationDao.searchAccommodations(keyword);
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
        return accommodationDao.getAllAccommodations();
    }
    
    /**
     * 필터링된 숙소 목록을 조회합니다.
     */
    @Override
    public List<Accommodation> getFilteredAccommodations(Map<String, Object> filters) throws SQLException {
        return accommodationDao.getFilteredAccommodations(filters);
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
                imageUrls.add(image.getImageUrl());
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
        // 숙소 등록
        Long accommodationId = accommodationDao.insertFromApi(accommodation);
        
        // 숙소 이미지 등록
        List<Image> accommodationImages = new ArrayList<>();
        for (Image image : images) {
            if ("ACCOMMODATION".equals(image.getReferenceType())) {
                image.setReferenceId(accommodationId);
                accommodationImages.add(image);
                imageDao.insertFromApi(image);
            }
        }
        
        // 객실 및 객실 이미지 등록
        for (Room room : rooms) {
            room.setAccommodationId(accommodationId);
            Long roomId = roomDao.insertFromApi(room);
            
            // 객실 이미지 등록
            for (Image image : images) {
                if ("ROOM".equals(image.getReferenceType()) && image.getReferenceId().equals(room.getRoomId())) {
                    image.setReferenceId(roomId);
                    imageDao.insertFromApi(image);
                }
            }
        }
        
        return accommodationId;
    }
}
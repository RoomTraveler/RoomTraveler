// src/main/java/com/ssafy/trip/model/service/trip/MapServiceImpl.java
package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.PlanDTO;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
@Slf4j
@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {
    private final MapDAO mapDAO;
    private final RedisTemplate<String, String> redisTemplate;
    //private final S3Uploader s3Uploader;

    @Override
    public List<ContentType> getContentTypes() {
        return mapDAO.getContentTypes();
    }

    @Override
    @Transactional
    public void savePlan(MapDTO.PlanStoreDTO planStoreDTO) {
        mapDAO.insertPlan(planStoreDTO);
        mapDAO.insertPlanAttractions(planStoreDTO);
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        mapDAO.deletePlanAttractions(planId);
        mapDAO.deletePlan(planId);
    }

    @Override
    public List<MapDTO.PlanDTO> getPlansByUserId(Long userId, int page, int size) {
        int offset = page * size;
        return mapDAO.getPlansByUserId(userId, offset, size);
    }

    @Override
    public List<PlanDTO> getUserPlans(Long userId, int page, int size) {
        int offset = page * size;
        return mapDAO.getUserPlans(userId, offset, size);
    }

    @Override
    public MapDTO.PlanDTO getPlanByPlanId(Long planId) {
        return mapDAO.getPlanByPlanId(planId);
    }

    @Override
    public List<MapDTO.RegionTripRes> getRegionTripWithinMapRange(MapDTO.MapBound mapBound, int contentType, String keyword, Pageable pageable, Long userId) {
        return mapDAO.getRegionTripWithinMapRange(mapBound, contentType, keyword, pageable, userId);
    }

    @Override
    public RegionTripResDto getAttractions(Long id) {
        return mapDAO.getAttractions(id);
    }

    @Override
    public List<RegionTripResDto> getPopularAttractions(int page, int size) {
        int offset = page * size;
        return mapDAO.getPopularAttractions(offset, size);
    }

    @Override
    public List<RegionTripResDto> getAttractionsByUserId(Long userId, int page, int size) {
        int offset = page * size;
        return mapDAO.getAttractionsByUserId(userId, offset, size);
    }

    @Override
    public List<RegionTripResDto> getAttractionsByKeyword(String keyword, int page, int size) {
        int offset = page * size;
        return mapDAO.getAttractionsByKeyword(keyword, offset, size);
    }

    @Override
    public List<MapDTO.PlanDTO> getSharedPlans(int page, int size) {
        int offset = page * size;
        return mapDAO.getSharedPlans(offset, size);
    }

    @Override
    @Transactional
    public void toggleAttractionLike(Long attractionId, Long userId) {
        int count = mapDAO.getCountOfAttractionLike(attractionId, userId);
        log.info("count: {}", count);
        if (count == 0) {
            mapDAO.insertAttractionLike(attractionId, userId);
            mapDAO.incrementAttractionLikes(attractionId);
        } else if (count == 1) {
            mapDAO.deleteAttractionLike(attractionId, userId);
            mapDAO.decrementAttractionLikes(attractionId);
        }
    }

    @Override
    @Transactional
    public void togglePlanLike(Long planId, Long userId) {
        int count = mapDAO.getCountOfPlanLike(planId, userId);
        if (count == 0) {
            mapDAO.insertPlanLike(planId, userId);
            mapDAO.incrementPlanLikes(planId);
        } else if (count == 1) {
            mapDAO.deletePlanLike(planId, userId);
            mapDAO.decrementPlanLikes(planId);
        }
    }

    @Override
    public MapDTO.PlanDTO getPublicPlan(String token) {
        String planIdStr = redisTemplate.opsForValue().get(token);
        if (planIdStr != null) {
            Long planId = Long.parseLong(planIdStr);
            return mapDAO.getPlanByPlanId(planId);
        }
        return null;
    }

    @Override
    @Transactional
    public MapDTO.RecordResponse getRecord(Long planId) {
        MapDTO.RecordResponse record = mapDAO.getRecordByPlanId(planId);
        record.setImages(mapDAO.getRecordImages(record.getRecordId()));
        return record;
    }

    @Override
    public List<MapDTO.RegionTripRes> getLikedAttractionsByUser(Long userId) {
        return mapDAO.findLikedAttractionsByUserId(userId);
    }

//    @Override
//    @Transactional
//    public Long saveRecord(Long planId, MapDTO.Record record) {
//        Long recordId = mapDAO.saveRecordByPlanId(planId);
//
//        List<String> imageUrls = new ArrayList<>();
//        for (MultipartFile image : record.getImages()) {
//            imageUrls.add(s3Uploader.upload(image, image.getOriginalFilename()));
//        }
//
//        mapDAO.saveRecordImages(recordId, imageUrls);
//
//        return recordId;
//    }
}

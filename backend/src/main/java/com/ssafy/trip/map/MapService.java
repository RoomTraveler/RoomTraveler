// src/main/java/com/ssafy/trip/model/service/trip/MapService.java
package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface MapService {
//    List<RegionTripResDto> getRegionTrip(int sidoCode, int gugunCode);
//    List<Sido>              getSidoList();
//    List<Gugun>             getGugunList(int sido);
    List<ContentType>       getContentTypes();
//    List<RegionTripResDto>  getInfoByLocalContent(int sidoCode, int gugunCode, int contentTypeId);
    void                    savePlan(MapDTO.PlanStoreDTO planStoreDTO);
    void deletePlan(Long planId);
    List<MapDTO.PlanDTO> getPlansByUserId(Long userId, int page, int size);

    MapDTO.PlanDTO getPlanByPlanId(Long planId);
//    List<RegionTripResDto>  findShortestPlan(RegionTripResDto startLocation, List<RegionTripResDto> locations);
    List<MapDTO.RegionTripRes>  getRegionTripWithinMapRange(MapDTO.MapBound mapBound, int contentType, String keyword, Pageable pageable, Long userId);
    //MapDTO.TotalPage getRegionTripTotalPage(MapDTO.MapBound mapBound, int contentType, String keyword);
    RegionTripResDto getAttractions(Long id);

    List<MapDTO.PlanDTO> getSharedPlans(int page, int size);

    // 관광지 좋아요 토글
    void toggleAttractionLike(Long attractionId, Long userId);

    // 여행 계획 좋아요 토글
    void togglePlanLike(Long planId, Long userId);

    MapDTO.PlanDTO getPublicPlan(String token);

    MapDTO.RecordResponse getRecord(Long planId);
    //Long saveRecord(Long planId, MapDTO.Record record);

    List<MapDTO.RegionTripRes> getLikedAttractionsByUser(Long userId);
}

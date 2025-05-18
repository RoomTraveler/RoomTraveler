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
    List<MapDTO.PlanDTO> getPlansByUserId(Long userId);

    MapDTO.PlanDTO getPlanByPlanId(Long planId);
//    List<RegionTripResDto>  findShortestPlan(RegionTripResDto startLocation, List<RegionTripResDto> locations);
    List<RegionTripResDto>  getRegionTripWithinMapRange(MapDTO.MapBound mapBound, int contentType, String keyword, Pageable pageable);
    //MapDTO.TotalPage getRegionTripTotalPage(MapDTO.MapBound mapBound, int contentType, String keyword);
    RegionTripResDto getAttractions(Long id);

    List<MapDTO.PlanDTO> getSharedPlans();

    // 관광지 좋아요 토글
    void toggleAttractionLike(Long attractionId, Long userId);

    // 여행 계획 좋아요 토글
    void togglePlanLike(Long planId, Long userId);

    MapDTO.PlanDTO getPublicPlan(String token);
}

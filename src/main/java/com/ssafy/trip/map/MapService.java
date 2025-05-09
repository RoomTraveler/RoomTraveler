// src/main/java/com/ssafy/trip/model/service/trip/MapService.java
package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MapService {
//    List<RegionTripResDto> getRegionTrip(int sidoCode, int gugunCode);
//    List<Sido>              getSidoList();
//    List<Gugun>             getGugunList(int sido);
    List<ContentType>       getContentTypes();
//    List<RegionTripResDto>  getInfoByLocalContent(int sidoCode, int gugunCode, int contentTypeId);
    void                    savePlan(MapDTO.PlanStoreDTO planStoreDTO);
    List<MapDTO.PlanDTO> getPlans(Long userId);
//    List<RegionTripResDto>  findShortestPlan(RegionTripResDto startLocation, List<RegionTripResDto> locations);
    List<RegionTripResDto>  getRegionTripWithinMapRange(MapDTO.MapBound mapBound, int contentType, String keyword, Pageable pageable);
    MapDTO.TotalPage getRegionTripTotalPage(MapDTO.MapBound mapBound, int contentType, String keyword);
}

// src/main/java/com/ssafy/trip/model/service/trip/MapService.java
package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.Gugun;
import com.ssafy.trip.map.MapDTO.Plan;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;
import com.ssafy.trip.map.MapDTO.Sido;

import java.util.List;

public interface MapService {
    List<RegionTripResDto> getRegionTrip(int sidoCode, int gugunCode);
    List<Sido>              getSidoList();
    List<Gugun>             getGugunList(int sido);
    List<ContentType>       getContentTypes();
    List<RegionTripResDto>  getInfoByLocalContent(int sidoCode, int gugunCode, int contentTypeId);
    void                    savePlan(String email, List<Integer> attractionIds);
    List<Plan>              getPlan(Long userId);
    List<RegionTripResDto>  findShortestPlan(RegionTripResDto startLocation, List<RegionTripResDto> locations);
}

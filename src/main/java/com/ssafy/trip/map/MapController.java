// src/main/java/com/ssafy/trip/controller/MapController.java
package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.Gugun;
import com.ssafy.trip.map.MapDTO.Plan;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;
import com.ssafy.trip.map.MapDTO.ShortestPlanRequest;
import com.ssafy.trip.map.MapDTO.Sido;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

//    @Value("${trip.key.vworld}")
//    private String keyVworld;
//    @Value("${trip.key.sgis.service-id}")
//    private String keySgisServiceId;
//    @Value("${trip.key.sgis.security}")
//    private String keySgisSecurity;
//    @Value("${trip.key.data}")
//    private String keyData;

    @GetMapping("/sidos")
    @ResponseBody
    public List<Sido> getSidoList() {
        return mapService.getSidoList();
    }

    @GetMapping("/guguns")
    @ResponseBody
    public List<Gugun> getGugunList(@RequestParam int sido) {
        return mapService.getGugunList(sido);
    }

    @GetMapping("/regions")
    @ResponseBody
    public List<RegionTripResDto> getRegionTrip(@RequestParam("sido") int sidoCode,
                                                @RequestParam("gugun") int gugunCode) {
        return mapService.getRegionTrip(sidoCode, gugunCode);
    }

    @GetMapping("/content-types")
    @ResponseBody
    public List<ContentType> getContentTypes() {
        return mapService.getContentTypes();
    }

    @GetMapping("/regions-content")
    @ResponseBody
    public List<RegionTripResDto> getRegionTripWithContent(@RequestParam("sido") int sidoCode,
                                                           @RequestParam("gugun") int gugunCode,
                                                           @RequestParam("content") int contentTypeId) {
        return mapService.getInfoByLocalContent(sidoCode, gugunCode, contentTypeId);
    }

    @PostMapping("/plan")
    @ResponseBody
    public void savePlan(@RequestParam String email,
                         @RequestBody List<Integer> attractionIds) {
        mapService.savePlan(email, attractionIds);
    }

    @GetMapping("/plan")
    @ResponseBody
    public List<Plan> getPlan(@RequestParam int mno) {
        return mapService.getPlan((long) mno);
    }

    @PostMapping("/shortest-plan")
    @ResponseBody
    public List<RegionTripResDto> findShortestPlan(@RequestBody ShortestPlanRequest req) {
        return mapService.findShortestPlan(req.getStartLocation(), req.getLocations());
    }
}

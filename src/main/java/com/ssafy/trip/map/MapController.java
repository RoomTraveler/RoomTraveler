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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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

//    @GetMapping("/sidos")
//    public List<Sido> getSidoList() {
//        return mapService.getSidoList();
//    }
//
//    @GetMapping("/guguns")
//    public List<Gugun> getGugunList(@RequestParam int sido) {
//        return mapService.getGugunList(sido);
//    }

//    @GetMapping("/regions")
//    public List<RegionTripResDto> getRegionTrip(@RequestParam("sido") int sidoCode,
//                                                @RequestParam("gugun") int gugunCode) {
//        return mapService.getRegionTrip(sidoCode, gugunCode);
//    }

    @PostMapping("/regions-content")
    @ResponseBody
    public List<RegionTripResDto> getRegionTripWithContent(@RequestBody MapDTO.MapBound mapBound) {
        return mapService.getRegionTripWithinMapRange(mapBound);
    }

    @GetMapping("/content-types")
    public List<ContentType> getContentTypes() {
        return mapService.getContentTypes();
    }

//    @GetMapping("/regions-content")
//    public List<RegionTripResDto> getRegionTripWithContent(@RequestParam("sido") int sidoCode,
//                                                           @RequestParam("gugun") int gugunCode,
//                                                           @RequestParam("content") int contentTypeId) {
//        return mapService.getInfoByLocalContent(sidoCode, gugunCode, contentTypeId);
//    }

    @PostMapping("/plan")
    public ResponseEntity<String> savePlan(@RequestBody MapDTO.PlanStoreDTO planStoreDTO) {
        mapService.savePlan(planStoreDTO);
        return ResponseEntity.ok("Plan Saved");
    }

    @GetMapping("/plan")
    public List<Plan> getPlan(@RequestParam Long mno) {
        return mapService.getPlan(mno);
    }
}

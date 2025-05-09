// src/main/java/com/ssafy/trip/controller/MapController.java
package com.ssafy.trip.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;
    private final ObjectMapper objectMapper;

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

    @GetMapping("/region-contents")
    @ResponseBody
    public List<RegionTripResDto> getRegionTripWithContent(@RequestParam String mapBound,
                                                           @RequestParam(required = false) int contentType,
                                                           @RequestParam(required = false) String keyword,
                                                           @PageableDefault(size = 10) Pageable pageable) throws JsonProcessingException {
        MapDTO.MapBound mapBoundObj = objectMapper.readValue(mapBound, MapDTO.MapBound.class);
        return mapService.getRegionTripWithinMapRange(mapBoundObj, contentType, keyword, pageable);
    }

    @GetMapping("/region-content-total-page")
    @ResponseBody
    public MapDTO.TotalPage getRegionTripTotalPage(@RequestParam String mapBound,
                                                   @RequestParam(required = false) int contentType,
                                                   @RequestParam(required = false) String keyword) throws JsonProcessingException {
        MapDTO.MapBound mapBoundObj = objectMapper.readValue(mapBound, MapDTO.MapBound.class);
        return mapService.getRegionTripTotalPage(mapBoundObj, contentType, keyword);
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


    @PostMapping("/plans")
    public ResponseEntity<String> savePlan(@RequestBody MapDTO.PlanStoreDTO planStoreDTO) {
        mapService.savePlan(planStoreDTO);
        return ResponseEntity.ok("Plan Saved");
    }

    @GetMapping("/plans/{userId}")
    public List<MapDTO.PlanDTO> getPlans(@PathVariable Long userId) {
        return mapService.getPlans(userId);
    }
}

package com.ssafy.trip.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.trip.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Tag(name = "PlanRestController", description = "여행 계획 기능 제공")
public class MapController {

    private final MapService mapService;
    private final ObjectMapper objectMapper;

    @GetMapping("/content-types")
    @Operation(summary = "관광지 콘텐츠 타입 조회")
    @ApiResponse(responseCode = "200", description = "관광지 콘텐츠 타입 조회 성공")
    public ResponseEntity<?> getContentTypes() {
        return ResponseEntity.ok(mapService.getContentTypes());
    }

    @GetMapping("/region-contents")
    @Operation(summary = "관광지 조회", description = "지도 범위 내 관광지 조회(콘텐츠 타입, 키워드 필터링)")
    @ApiResponse(responseCode = "200", description = "관광지 조회 성공")
    public ResponseEntity<?>  getRegionTripWithContent(@RequestParam String mapBound,
                                                       @RequestParam(required = false) int contentType,
                                                       @RequestParam(required = false) String keyword,
                                                       @PageableDefault(size = 10) Pageable pageable) throws JsonProcessingException {
        MapDTO.MapBound mapBoundObj = objectMapper.readValue(mapBound, MapDTO.MapBound.class);
        return ResponseEntity.ok(mapService.getRegionTripWithinMapRange(mapBoundObj, contentType, keyword, pageable));
    }

    @GetMapping("/attractions/{id}")
    public ResponseEntity<?> getAttractions(@PathVariable Long id) {
        log.info(id.toString());
        return ResponseEntity.ok(mapService.getAttractions(id));
    }

    @GetMapping("/users/{userId}/plans")
    @Operation(summary = "여행 계획 조회", description = "사용자의 여행 계획 조회")
    @ApiResponse(responseCode = "200", description = "여행 계획 조회 성공")
    public ResponseEntity<?>  getUsersPlans(@PathVariable Long userId, //@CurrentUserId Long userId 이거 사용
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(mapService.getPlansByUserId(userId, page, size));
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<?>  getPlans(@PathVariable Long planId) {
        return ResponseEntity.ok(mapService.getPlanByPlanId(planId));
    }

    @PostMapping("/plans")
    @Operation(summary = "여행 계획 저장", description = "사용자의 여행 계획 저장")
    @ApiResponse(responseCode = "200", description = "여행 계획 저장 성공")
    public ResponseEntity<?> savePlan(@RequestBody MapDTO.PlanStoreDTO planStoreDTO) {
        mapService.savePlan(planStoreDTO);
        return ResponseEntity.ok("Plan Saved");
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        mapService.deletePlan(planId);
        return ResponseEntity.ok("Plan Deleted");
    }

    @GetMapping("/plans")
    public ResponseEntity<?> getPlans() {
        return ResponseEntity.ok(mapService.getSharedPlans());
    }

    @PostMapping("/likes/attractions/{attractionId}")
    @Operation(summary = "관광지 좋아요", description = "관광지 좋아요 토글")
    @ApiResponse(responseCode = "200", description = "관광지 좋아요 처리 성공")
    public ResponseEntity<?> toggleAttractionLike(
            @PathVariable Long attractionId) { // get userId on jwt
        mapService.toggleAttractionLike(attractionId, 1L);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/likes/plans/{planId}")
    @Operation(summary = "여행 계획 좋아요", description = "여행 계획 좋아요 토글")
    @ApiResponse(responseCode = "200", description = "여행 계획 좋아요 처리 성공")
    public ResponseEntity<?> togglePlanLike(
            @PathVariable Long planId) { //get userId on jwt
        mapService.togglePlanLike(planId, 1L);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/plans/public/{token}")
    public ResponseEntity<?> getPublicPlan(@PathVariable String token) {
        MapDTO.PlanDTO planDTO = mapService.getPublicPlan(token);
        if (planDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(planDTO);
    }

    @GetMapping("/plans/{planId}/record")
    public ResponseEntity<?> getRecord(@PathVariable Long planId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 플랜이 이 사람의 것인지 확인
        //userDetails.getUserId()

        MapDTO.RecordResponse record = mapService.getRecord(planId);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

//    @PostMapping("/plans/{planId}/record")
//    public ResponseEntity<?> saveRecord(@PathVariable Long planId, @RequestBody MapDTO.Record record) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//
//        // 플랜이 이 사람의 것인지 확인
//        //userDetails.getUserId()
//
//        Long success = mapService.saveRecord(planId, record);
//        if (success == 0) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok("저장 성공");
//    }
}

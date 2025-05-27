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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
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
        return ResponseEntity.ok(mapService.getRegionTripWithinMapRange(mapBoundObj, contentType, keyword, pageable, 1L));
    }

    @GetMapping("/attractions/{id}")
    public ResponseEntity<?> getAttractions(@PathVariable Long id) {
        log.info(id.toString());
        return ResponseEntity.ok(mapService.getAttractions(id));
    }

    @GetMapping("/attractions")
    public ResponseEntity<?> getAttractions(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(mapService.getPopularAttractions(page, size));
    }

    @GetMapping("/attractions/search/{keyword}")
    public ResponseEntity<?> getAttractionsByKeyword(@PathVariable String keyword,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(mapService.getAttractionsByKeyword(keyword, page, size));
    }

    @GetMapping("/users/attractions")
    public ResponseEntity<?> getAttractions(@CurrentUserId Long userId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        log.info(String.valueOf(userId));
        return ResponseEntity.ok(mapService.getAttractionsByUserId(userId, page, size));
    }

    @GetMapping("/users/plans")
    @Operation(summary = "여행 계획 조회", description = "사용자의 여행 계획 조회")
    @ApiResponse(responseCode = "200", description = "여행 계획 조회 성공")
    public ResponseEntity<?>  getUsersPlans(@CurrentUserId Long userId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(mapService.getPlansByUserId(userId, page, size));
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<?>  getPlans(@PathVariable Long planId) {
        return ResponseEntity.ok(mapService.getPlanByPlanId(planId));
    }

    @GetMapping("/plans")
    public ResponseEntity<?> getPlans(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(mapService.getSharedPlans(page, size));
    }

    @GetMapping("/plans/me")
    public ResponseEntity<?> getMyPlans(@CurrentUserId Long userId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(mapService.getUserPlans(userId, page, size));
    }

    @PostMapping("/plans")
    @Operation(summary = "여행 계획 저장", description = "사용자의 여행 계획 저장")
    @ApiResponse(responseCode = "200", description = "여행 계획 저장 성공")
    public ResponseEntity<?> savePlan(@CurrentUserId Long userId, @RequestBody MapDTO.PlanStoreDTO planStoreDTO) {
        planStoreDTO.setUserId(userId);
        mapService.savePlan(planStoreDTO);
        return ResponseEntity.ok("Plan Saved");
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        mapService.deletePlan(planId);
        return ResponseEntity.ok("Plan Deleted");
    }

    @GetMapping("/likes/attractions")
    @Operation(summary = "사용자 좋아요한 관광지 목록", description = "현재 로그인한 사용자가 좋아요한 관광지들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "관광지 좋아요 목록 조회 성공")
    public ResponseEntity<?> getLikedAttractions(@CurrentUserId Long userId) {
        List<MapDTO. RegionTripRes> likedAttractions = mapService.getLikedAttractionsByUser(userId);
        return ResponseEntity.ok(likedAttractions);
    }

    @PostMapping("/likes/attractions/{attractionId}")
    @Operation(summary = "관광지 좋아요", description = "관광지 좋아요 토글")
    @ApiResponse(responseCode = "200", description = "관광지 좋아요 처리 성공")
    public ResponseEntity<?> toggleAttractionLike(@CurrentUserId Long userId,
            @PathVariable Long attractionId) { // get userId on jwt
        log.info("come? " + attractionId);
        mapService.toggleAttractionLike(attractionId, userId);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/likes/plans/{planId}")
    @Operation(summary = "여행 계획 좋아요", description = "여행 계획 좋아요 토글")
    @ApiResponse(responseCode = "200", description = "여행 계획 좋아요 처리 성공")
    public ResponseEntity<?> togglePlanLike(@CurrentUserId Long userId,
            @PathVariable Long planId) { //get userId on jwt
        mapService.togglePlanLike(planId, userId);
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

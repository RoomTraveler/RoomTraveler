package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.EmailPlanDTO;
import com.ssafy.trip.map.MapDTO.MapBound;
import com.ssafy.trip.map.MapDTO.PlanDTO;
import com.ssafy.trip.map.MapDTO.PlanStoreDTO;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper
public interface MapDAO{

    // 1) 시·군구별 여행 정보 조회
    //List<RegionTripResDto> getRegionTrip(int sidoCode, int gugunCode);

    // 2) 시도 목록 조회
    //List<Sido> getSidos();

    // 3) 구군 목록 조회
    //List<Gugun> getGuguns(int sido);

    // 4) 콘텐츠 타입 목록 조회
    List<ContentType> getContentTypes();

    // 5) 지역+콘텐츠별 상세 정보 조회
    //List<RegionTripResDto> getInfoByLocalContent(int sidoCode, int gugunCode, int contentTypeId);

    // 7) 계획(plan) 생성 & 생성된 PK(plan_id) 획득
    long insertPlan(PlanStoreDTO planStoreDTO);

    void deletePlan(Long planId);
    void deletePlanAttractions(Long planId);

    // 8) plan↔attraction 매핑 테이블에 복수 삽입
    int insertPlanAttractions(PlanStoreDTO planStoreDTO);
    // 9) 사용자별 계획 조회
    List<PlanDTO> getPlansByUserId(Long userId, int offset, int size);

    PlanDTO getPlanByPlanId(Long planId);

    List<RegionTripResDto> getRegionTripWithinMapRange(MapBound mapBound, int contentType, String keyword, Pageable pageable);

    RegionTripResDto getAttractions(Long id);
    //Integer countRegionTrips(MapBound mapBound, int contentType, String keyword);

    List<PlanDTO> getSharedPlans();

    int incrementAttractionLikes(Long attractionId);
    int decrementAttractionLikes(Long attractionId);
    int incrementPlanLikes(Long planId);
    int decrementPlanLikes(Long planId);

    int getCountOfAttractionLike(Long attractionId, Long userId);
    int deleteAttractionLike(Long attractionId, Long userId);
    int insertAttractionLike(Long attractionId, Long userId);

    int getCountOfPlanLike(Long planId, Long userId);
    int insertPlanLike(Long planId, Long userId);
    int deletePlanLike(Long planId, Long userId);

    List<EmailPlanDTO> selectPlansForToday();

    MapDTO.RecordResponse getRecordByPlanId(Long planId);
    List<String> getRecordImages(Long RecordId);

    Long saveRecordByPlanId(Long recordId);
    void saveRecordImages(Long recordId, List<String> imageUrls);
}

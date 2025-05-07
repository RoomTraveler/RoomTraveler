package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.*;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
    long insertPlan(MapDTO.PlanStoreDTO planStoreDTO);

    // 8) plan↔attraction 매핑 테이블에 복수 삽입
    int insertPlanAttractions(MapDTO.PlanStoreDTO planStoreDTO);

    List<Long> getPlanIds(Long userId);
    // 9) 사용자별 계획 조회
    List<Long> getPlans(Long planIds);

    List<RegionTripResDto> getRegionTripWithinMapRange(MapBound mapBound, Pageable pageable);

    Integer countRegionTrips(MapBound mapBound);
}

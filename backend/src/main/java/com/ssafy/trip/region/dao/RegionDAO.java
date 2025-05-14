package com.ssafy.trip.region.dao;

import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.model.Gugun;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 지역 정보 데이터 접근 인터페이스
 * 
 * 시도 및 구군 정보를 데이터베이스에서 조회하는 메서드를 정의합니다.
 * 
 * @author AI Assistant
 */
@Mapper
public interface RegionDAO {

    /**
     * 모든 시도 목록을 조회합니다.
     * 
     * @return 시도 목록
     * @throws Exception 데이터베이스 오류 발생 시
     */
    List<Sido> getSidos() throws Exception;

    /**
     * 특정 시도에 속한 구군 목록을 조회합니다.
     * 
     * @param sidoCode 시도 코드
     * @return 구군 목록
     * @throws Exception 데이터베이스 오류 발생 시
     */
    List<Gugun> getGuguns(int sidoCode) throws Exception;
}

package com.ssafy.trip.region.service;

import com.ssafy.trip.region.dao.RegionDAO;
import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.model.Gugun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * 지역 정보 서비스 구현 클래스
 * 
 * 시도 및 구군 정보를 제공하는 서비스 메서드를 구현합니다.
 * 
 * @author AI Assistant
 */
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {
    
    // SLF4J 로거를 사용하여 디버깅 및 에러 로깅
    private static final Logger logger = LoggerFactory.getLogger(RegionServiceImpl.class);
    
    // 생성자 주입을 통한 의존성 주입
    private final RegionDAO regionDAO;
    
    /**
     * 모든 시도 목록을 조회합니다.
     * 
     * @return 시도 목록
     * @throws Exception 데이터베이스 오류 발생 시
     */
    @Override
    public List<Sido> getSidos() throws Exception {
        logger.debug("시도 목록 조회 서비스 호출");
        return regionDAO.getSidos();
    }
    
    /**
     * 특정 시도에 속한 구군 목록을 조회합니다.
     * 
     * @param sidoCode 시도 코드
     * @return 구군 목록
     * @throws Exception 데이터베이스 오류 발생 시
     */
    @Override
    public List<Gugun> getGuguns(int sidoCode) throws Exception {
        logger.debug("구군 목록 조회 서비스 호출: sidoCode={}", sidoCode);
        return regionDAO.getGuguns(sidoCode);
    }
}
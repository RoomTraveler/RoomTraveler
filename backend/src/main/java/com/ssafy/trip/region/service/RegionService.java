package com.ssafy.trip.region.service;

import com.ssafy.trip.region.model.Gugun;
import com.ssafy.trip.region.model.Sido;

import java.util.List;

/**
 * 지역 정보 서비스 인터페이스
 * 
 * 시도 및 구군 정보를 제공하는 서비스 메서드를 정의합니다.
 * 
 * @author AI Assistant
 */
public interface RegionService {
    
    /**
     * 모든 시도 목록을 조회합니다.
     * 
     * @return 시도 목록
     * @throws Exception 데이터베이스 오류 발생 시
     */
    List<Sido> getAllSidos() throws Exception;
    
    /**
     * 특정 시도에 속한 구군 목록을 조회합니다.
     * 
     * @param sidoCode 시도 코드
     * @return 구군 목록
     * @throws Exception 데이터베이스 오류 발생 시
     */
    List<Gugun> getGugunsBySidoCode(int sidoCode) throws Exception;
    
    /**
     * 특정 시도 정보를 조회합니다.
     * 
     * @param sidoCode 시도 코드
     * @return 시도 정보
     * @throws Exception 데이터베이스 오류 발생 시
     */
    Sido getSidoByCode(int sidoCode) throws Exception;
    
    /**
     * 시도 정보를 추가합니다.
     * 
     * @param sido 시도 정보
     * @throws Exception 데이터베이스 오류 발생 시
     */
    void addSido(Sido sido) throws Exception;
    
    /**
     * 시도 정보를 수정합니다.
     * 
     * @param sido 수정할 시도 정보
     * @throws Exception 데이터베이스 오류 발생 시
     */
    void modifySido(Sido sido) throws Exception;
    
    /**
     * 시도 정보를 삭제합니다.
     * 
     * @param sidoCode 삭제할 시도 코드
     * @throws Exception 데이터베이스 오류 발생 시
     */
    void removeSido(int sidoCode) throws Exception;
    
    /**
     * 모든 시도의 이미지 URL을 미리 정의된 매핑에 따라 일괄 업데이트합니다.
     * @throws Exception 데이터베이스 오류 발생 시
     */
    void updateSidoImageUrls() throws Exception;
}
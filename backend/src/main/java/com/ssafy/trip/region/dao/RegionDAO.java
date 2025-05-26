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

    /**
     * 새로운 시도 정보를 등록합니다.
     * no 컬럼은 AUTO_INCREMENT 입니다.
     * @param sido 등록할 시도 정보 (sido_code, sido_name, sido_img_url 필요)
     * @return 영향을 받은 행 수
     * @throws Exception 데이터베이스 오류 발생 시
     */
    int insertSido(Sido sido) throws Exception;

    /**
     * 기존 시도 정보를 수정합니다. (sido_code를 기준으로)
     * @param sido 수정할 시도 정보 (sido_name, sido_img_url 수정 가능)
     * @return 영향을 받은 행 수
     * @throws Exception 데이터베이스 오류 발생 시
     */
    int updateSido(Sido sido) throws Exception;

    /**
     * 특정 시도 정보를 삭제합니다. (sido_code를 기준으로)
     * @param sidoCode 삭제할 시도 코드
     * @return 영향을 받은 행 수
     * @throws Exception 데이터베이스 오류 발생 시
     */
    int deleteSido(int sidoCode) throws Exception;

    /**
     * 특정 시도 코드로 시도 정보를 조회합니다.
     * @param sidoCode 조회할 시도 코드
     * @return Sido 객체 또는 null
     * @throws Exception 데이터베이스 오류 발생 시
     */
    Sido getSidoByCode(int sidoCode) throws Exception;
}

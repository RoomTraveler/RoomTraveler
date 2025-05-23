package com.ssafy.trip.region.service;

import com.ssafy.trip.region.dao.RegionDAO;
import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.model.Gugun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    
    private static final Map<Integer, String> SIDO_IMAGE_URL_MAP = new HashMap<>();
    static {
        SIDO_IMAGE_URL_MAP.put(1, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/seoul.jpg");
        SIDO_IMAGE_URL_MAP.put(2, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/incheon.jpg");
        SIDO_IMAGE_URL_MAP.put(3, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/daejeon.jpg");
        SIDO_IMAGE_URL_MAP.put(4, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/daegu.png");
        SIDO_IMAGE_URL_MAP.put(5, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/gwangju.jpg");
        SIDO_IMAGE_URL_MAP.put(6, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/busan.jpg");
        SIDO_IMAGE_URL_MAP.put(7, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/ulsan.jpg");
        SIDO_IMAGE_URL_MAP.put(8, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/saejong.gif");
        SIDO_IMAGE_URL_MAP.put(31, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/gyeonggido.jpg");
        SIDO_IMAGE_URL_MAP.put(32, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/Gangwondo.jpg");
        SIDO_IMAGE_URL_MAP.put(33, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/chungcheongbukdo.jpg");
        SIDO_IMAGE_URL_MAP.put(34, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/Chungcheongnamdo.jpg"); // 34aus -> 34
        SIDO_IMAGE_URL_MAP.put(35, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/gyeongsangbukdo.jpg");
        SIDO_IMAGE_URL_MAP.put(36, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/gyeongsangnamdo.jpg");
        SIDO_IMAGE_URL_MAP.put(37, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/jeonbuk.jpg");
        SIDO_IMAGE_URL_MAP.put(38, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/jeonnam.jpg");
        SIDO_IMAGE_URL_MAP.put(39, "https://beomsu-8918.s3.ap-northeast-2.amazonaws.com/trip/jeju.jpg");
    }
    
    /**
     * 모든 시도 목록을 조회합니다.
     * 
     * @return 시도 목록
     * @throws Exception 데이터베이스 오류 발생 시
     */
    @Override
    public List<Sido> getAllSidos() throws Exception {
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
    public List<Gugun> getGugunsBySidoCode(int sidoCode) throws Exception {
        logger.debug("구군 목록 조회 서비스 호출: sidoCode={}", sidoCode);
        return regionDAO.getGuguns(sidoCode);
    }

    @Override
    public Sido getSidoByCode(int sidoCode) throws Exception {
        return regionDAO.getSidoByCode(sidoCode);
    }

    @Override
    @Transactional
    public void addSido(Sido sido) throws Exception {
        // TODO: sido.getCode() (sido_code) 중복 체크 등의 비즈니스 로직 추가 가능
        // 예: if (regionDAO.getSidoByCode(sido.getCode()) != null) { throw new Exception("이미 존재하는 시도 코드입니다."); }
        regionDAO.insertSido(sido);
    }

    @Override
    @Transactional
    public void modifySido(Sido sido) throws Exception {
        // TODO: 수정 전 데이터 존재 확인 등의 비즈니스 로직 추가 가능
        // 예: if (regionDAO.getSidoByCode(sido.getCode()) == null) { throw new Exception("존재하지 않는 시도 코드입니다."); }
        regionDAO.updateSido(sido);
    }

    @Override
    @Transactional
    public void removeSido(int sidoCode) throws Exception {
        // TODO: 삭제 전 데이터 존재 확인 및 관련 구군 정보 처리 등의 로직 추가 가능
        // 예: if (regionDAO.getSidoByCode(sidoCode) == null) { throw new Exception("존재하지 않는 시도 코드입니다."); }
        // List<Gugun> guguns = regionDAO.getGuguns(sidoCode); if (!guguns.isEmpty()) { throw new Exception("해당 시도에 속한 구군이 있어 삭제할 수 없습니다."); }
        regionDAO.deleteSido(sidoCode);
    }

    @Override
    @Transactional
    public void updateSidoImageUrls() throws Exception {
        logger.info("모든 시도 이미지 URL 일괄 업데이트 서비스 호출");
        List<Sido> sidos = regionDAO.getSidos(); // XML의 getSidos는 DISTINCT sido_name, sido_code, sido_img_url FROM sidos 임
        if (sidos == null || sidos.isEmpty()) {
            logger.warn("업데이트할 시도 정보가 없습니다.");
            return;
        }

        int updatedCount = 0;
        for (Sido sido : sidos) {
            String newImageUrl = SIDO_IMAGE_URL_MAP.get(sido.getCode());
            if (newImageUrl != null && !newImageUrl.equals(sido.getSidoImgUrl())) {
                sido.setSidoImgUrl(newImageUrl);
                regionDAO.updateSido(sido); // 개별 업데이트 (N번의 DB 호출)
                updatedCount++;
                logger.debug("시도 코드 [{}] 이미지 URL 업데이트: {}", sido.getCode(), newImageUrl);
            } else if (newImageUrl == null) {
                logger.warn("시도 코드 [{}]에 대한 이미지 URL 매핑 정보가 없습니다.", sido.getCode());
            }
        }
        logger.info("총 {}개의 시도 이미지 URL 업데이트 완료.", updatedCount);
    }
}
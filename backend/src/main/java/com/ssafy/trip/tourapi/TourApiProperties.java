package com.ssafy.trip.tourapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Tour API 설정 정보를 담는 클래스
 */
@Component
@ConfigurationProperties(prefix = "tourapi")
@Data
public class TourApiProperties {
    
    /**
     * API 기본 URL
     */
    private String baseUrl = "https://apis.data.go.kr/B551011/KorService1";
    
    /**
     * 서비스 키
     */
    private String serviceKey = "YOUR_SERVICE_KEY";
    
    /**
     * 모바일 OS
     */
    private String mobileOs = "ETC";
    
    /**
     * 모바일 앱 이름
     */
    private String mobileApp = "AppTest";
    
    /**
     * 기본 페이지 번호
     */
    private int defaultPageNo = 1;
    
    /**
     * 기본 결과 수
     */
    private int defaultNumOfRows = 100;
}
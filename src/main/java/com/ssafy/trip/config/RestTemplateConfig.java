package com.ssafy.trip.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * API 통신을 위한 설정 클래스
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate 빈을 생성합니다.
     * 
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper 빈을 생성합니다.
     * 
     * @return ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

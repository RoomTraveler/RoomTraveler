package com.ssafy.trip.config;

import com.ssafy.trip.map.CurrentUserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 웹 설정 클래스
 * 
 * CORS(Cross-Origin Resource Sharing) 설정을 담당합니다.
 * 프론트엔드(Vue.js)와 백엔드 간의 통신을 가능하게 합니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    /**
     * CORS 설정
     * 
     * 프론트엔드 애플리케이션이 백엔드 API에 접근할 수 있도록 CORS를 구성합니다.
     * 개발 환경에서는 Vue.js 개발 서버의 기본 포트인 5173을 허용합니다.
     * 
     * @param registry CORS 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 API 엔드포인트에 대해 CORS 설정
        registry.addMapping("/api/**")
                // 허용할 오리진(출처) 설정 - Vue.js 개발 서버의 기본 포트는 5173, 현재 프론트엔드는 3000 포트 사용
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                // 허용할 HTTP 메서드 설정
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 인증 정보(쿠키, 인증 헤더 등) 허용
                .allowCredentials(true)
                // 허용할 헤더 설정
                .allowedHeaders("*")
                // 브라우저가 CORS 설정을 캐시하는 시간(초)
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
    }
}

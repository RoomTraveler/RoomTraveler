package com.ssafy.trip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Trip API 애플리케이션의 메인 클래스
 * Spring Boot 애플리케이션의 진입점 역할을 합니다.
 */
@SpringBootApplication
public class TripApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripApiApplication.class, args);
    }

}
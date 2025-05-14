package com.ssafy.trip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 방구석 여행자 백엔드 애플리케이션의 메인 클래스
 * 
 * 이 클래스는 Spring Boot 애플리케이션의 시작점입니다.
 * 모든 Spring 컴포넌트를 초기화하고 애플리케이션을 실행합니다.
 */
@SpringBootApplication
public class TripApplication {

	/**
	 * 애플리케이션의 메인 메소드
	 * 
	 * @param args 명령줄 인자
	 */
	public static void main(String[] args) {
		SpringApplication.run(TripApplication.class, args);
	}

}

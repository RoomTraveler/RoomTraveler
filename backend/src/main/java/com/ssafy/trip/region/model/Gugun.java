package com.ssafy.trip.region.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * 구군 정보를 담는 모델 클래스
 * 
 * 구군 코드와 이름을 포함합니다.
 * 
 * @author AI Assistant
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Gugun {

    /**
     * 구군 코드
     */
    private int code;

    /**
     * 구군 이름
     */
    private String name;

    /**
     * 시도 코드 (외래키)
     */
    private int sidoCode;

    /**
     * 구군 코드와 이름만 받는 생성자
     * 
     * @param code 구군 코드
     * @param name 구군 이름
     */
    public Gugun(int code, String name) {
        this.code = code;
        this.name = name;
        this.sidoCode = 0; // 기본값 설정
    }
}

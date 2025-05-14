package com.ssafy.trip.region.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * 시도 정보를 담는 모델 클래스
 * 
 * 시도 코드와 이름을 포함합니다.
 * 
 * @author AI Assistant
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Sido {
    
    /**
     * 시도 코드
     */
    private int code;
    
    /**
     * 시도 이름
     */
    private String name;
}
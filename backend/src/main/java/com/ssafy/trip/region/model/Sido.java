package com.ssafy.trip.region.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * 시도 정보를 담는 모델 클래스
 * 
 * 시도 코드(code), 시도 이름(name), 시도 이미지 URL(sidoImgUrl)을 포함합니다.
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
     * 시도 코드 (DB 컬럼: sido_code)
     */
    @NotNull(message = "시도 코드는 필수입니다.")
    @PositiveOrZero(message = "시도 코드는 0 이상이어야 합니다.")
    private int code;
    
    /**
     * 시도 이름 (DB 컬럼: sido_name)
     */
    @NotEmpty(message = "시도 이름은 필수입니다.")
    @Size(max = 20, message = "시도 이름은 최대 20자까지 가능합니다.")
    private String name;
    
    /**
     * 시도 이미지 URL (DB 컬럼: sido_img_url)
     */
    @URL(message = "유효한 URL 형식이어야 합니다.")
    @Size(max = 500, message = "이미지 URL은 최대 500자까지 가능합니다.")
    private String sidoImgUrl; // SQL 변경으로 추가된 컬럼 sido_img_url과 매핑
}
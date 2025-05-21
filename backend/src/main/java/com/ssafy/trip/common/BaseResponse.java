package com.ssafy.trip.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모든 API 응답의 기본 형태를 제공하는 제네릭 클래스입니다.
 * 성공 여부(success), 실제 결과(result), 메시지(message)를 포함합니다.
 *
 * @param <T> 응답 결과 데이터 타입
 */
@Data // Lombok: Getter, Setter, toString, equals, hashCode 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 파라미터로 받는 생성자 자동 생성
@NoArgsConstructor  // Lombok: 파라미터 없는 기본 생성자 자동 생성
public class BaseResponse<T> {
    /**
     * 요청 성공 여부
     */
    private boolean success;
    /**
     * 실제 응답 데이터
     */
    private T result;
    /**
     * 응답 메시지 (에러, 설명 등)
     */
    private String message;

    /**
     * 성공(결과 데이터가 있을 때) 응답 생성 메서드
     * @param result 응답 데이터
     * @return 성공 BaseResponse 객체
     */
    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>(true, result, null);
    }

    /**
     * 성공(결과 데이터 없이) 응답 생성 메서드
     * @return 성공 BaseResponse 객체
     */
    public static BaseResponse<Void> onSuccess() {
        return new BaseResponse<>(true, null, null);
    }

    /**
     * 실패 응답 생성 메서드
     * @param message 실패 메시지
     * @return 실패 BaseResponse 객체
     */
    public static <T> BaseResponse<T> onFail(String message) {
        return new BaseResponse<>(false, null, message);
    }
}

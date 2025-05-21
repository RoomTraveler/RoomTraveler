package com.ssafy.trip.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private T result;
    private String message;

    // 성공
    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>(true, result, null);
    }

    // 실패
    public static <T> BaseResponse<T> onFail(String message) {
        return new BaseResponse<>(false, null, message);
    }
}

package com.ssafy.trip.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    AWS_S3_ERROR("AWS_S3_ERROR", "S3 작업 중 오류가 발생했습니다."),
    FILE_NOT_FOUND("FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    // 필요에 따라 추가

    ;

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

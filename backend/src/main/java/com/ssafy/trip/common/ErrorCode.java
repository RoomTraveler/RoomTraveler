package com.ssafy.trip.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    AWS_S3_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWS_S3_ERROR", "S3 작업 중 오류가 발생했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    EMPTY_FILE_ERROR(HttpStatus.BAD_REQUEST, "EMPTY_FILE_ERROR", "업로드할 파일이 비어있습니다."),
    // 필요에 따라 추가

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

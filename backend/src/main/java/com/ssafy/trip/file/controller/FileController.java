package com.ssafy.trip.file.controller;

import com.ssafy.trip.common.BaseResponse;
import com.ssafy.trip.s3.AWSS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final AWSS3Service awsS3Service;

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadEditorImage(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            // 클라이언트에게 직접 오류 메시지를 포함한 응답을 보내는 것이 더 적절할 수 있음
            // return ResponseEntity.badRequest().body(Map.of("error", "업로드할 이미지가 없습니다."));
            throw new IllegalArgumentException("업로드할 이미지가 없습니다."); // 또는 이대로 유지하고 @ControllerAdvice로 처리
        }
        try {
            String imageUrl = awsS3Service.uploadFile(image); 
            Map<String, String> responseData = new HashMap<>();
            responseData.put("imageUrl", imageUrl); 
            // Toast UI Editor는 보통 { "data": { "link": "URL" } } 형태나, 단순히 URL 문자열을 기대할 수 있음.
            // 여기서는 우선 { "imageUrl": "URL" }로 반환하고 프론트에서 필요시 조정.
            return ResponseEntity.ok(responseData);
        } catch (IOException e) {
            // Log error (e.g., using SLF4J logger)
            // 실제 프로덕션에서는 e.printStackTrace() 대신 로깅 프레임워크 사용
            e.printStackTrace(); 
            // 클라이언트에게 오류 상태와 메시지 반환
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 
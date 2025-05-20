package com.ssafy.trip.s3;

import com.ssafy.trip.common.BaseException;
import com.ssafy.trip.common.BaseResponse;
import com.ssafy.trip.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AWSS3Controller {
    private final AWSS3Service awsS3Service;

    // S3 서버에 이미지 업로드
    @PostMapping("/s3")
    public BaseResponse<String> uploadFile(@RequestPart MultipartFile multipartFile) throws IOException {
        String fileName = null;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            fileName = awsS3Service.uploadFile(multipartFile);
        }
        return BaseResponse.onSuccess(fileName);
    }

    // S3 서버에 저장된 이미지 교체
    @PatchMapping("/s3")
    public BaseResponse<String> modifyFile(
            @RequestParam("fileUrl") String fileUrl,
            @RequestPart MultipartFile multipartFile
    ) throws IOException {
        if (fileUrl != null && !fileUrl.isBlank()) {
            // 안전하게 파일명 추출
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            awsS3Service.deleteImage(fileName);

            if (multipartFile != null && !multipartFile.isEmpty()) {
                String newFileUrl = awsS3Service.uploadFile(multipartFile);
                return BaseResponse.onSuccess(newFileUrl);
            }
        }
        throw new BaseException(ErrorCode.AWS_S3_ERROR);
    }
}

package com.ssafy.trip.s3;

import com.ssafy.trip.common.BaseException;
import com.ssafy.trip.common.BaseResponse;
import com.ssafy.trip.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AWSS3Controller {
    private final AWSS3Service awsS3Service;

    // 단일 파일 업로드
    @PostMapping("/s3")
    public BaseResponse<String> uploadFile(@RequestPart MultipartFile multipartFile) throws IOException {
        String fileName = null;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            fileName = awsS3Service.uploadFile(multipartFile);
        }
        return BaseResponse.onSuccess(fileName);
    }

    // 다중 파일 업로드
    @PostMapping("/s3/multi")
    public BaseResponse<List<String>> uploadFiles(@RequestPart List<MultipartFile> files) throws IOException {
        List<String> urls = awsS3Service.uploadFiles(files);
        return BaseResponse.onSuccess(urls);
    }

    // 단일 파일 교체
    @PatchMapping("/s3")
    public BaseResponse<String> modifyFile(
            @RequestParam("fileUrl") String fileUrl,
            @RequestPart MultipartFile multipartFile
    ) throws IOException {
        if (fileUrl != null && !fileUrl.isBlank()) {
            awsS3Service.deleteImage(fileUrl);

            if (multipartFile != null && !multipartFile.isEmpty()) {
                String newFileUrl = awsS3Service.uploadFile(multipartFile);
                return BaseResponse.onSuccess(newFileUrl);
            }
        }
        throw new BaseException(ErrorCode.AWS_S3_ERROR);
    }

    // 단일 파일 삭제
    @DeleteMapping("/s3")
    public BaseResponse<Boolean> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        boolean result = awsS3Service.deleteImage(fileUrl);
        return BaseResponse.onSuccess(result);
    }

    // 다중 파일 삭제
    @DeleteMapping("/s3/multi")
    public BaseResponse<Boolean> deleteFiles(@RequestBody List<String> fileUrls) {
        awsS3Service.deleteFiles(fileUrls);
        return BaseResponse.onSuccess(true);
    }
}

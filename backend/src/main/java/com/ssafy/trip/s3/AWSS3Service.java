package com.ssafy.trip.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AWSS3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    @Value("${app.file.upload-max-size-mb:5}")
    private long maxFileSizeMb;

    // 단일 파일 업로드 (원래 로직으로 복원)
    public String uploadFile(MultipartFile file) throws IOException {
        checkFileSize(file);
        String fileName = createFileName(file.getOriginalFilename()); // 내부에서 S3 저장용 파일명 생성

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
            log.info("S3 파일 업로드 성공: bucket={}, key={}", bucket, fileName);
        } catch (AmazonServiceException e) {
            log.error("S3 업로드 AmazonServiceException: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 서비스 오류로 이미지 업로드에 실패했습니다.");
        } catch (IOException e) {
            log.error("S3 업로드 IOException: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 파일 처리 오류가 발생했습니다.");
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // 다중 파일 업로드
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file)); // 복원된 단일 파일 업로드 메소드 호출
        }
        return urls;
    }

    // 단일 파일 삭제
    public boolean deleteImage(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("삭제할 파일 URL이 제공되지 않음");
            return false;
        }
        try {
            String objectKey = extractObjectKeyFromUrl(fileUrl);
            if (objectKey == null || objectKey.isEmpty()) {
                log.error("S3 URL에서 객체 키 추출 실패: {}", fileUrl);
                return false;
            }
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, objectKey));
            log.info("S3 파일 삭제 성공: {}", objectKey);
            return true;
        } catch (AmazonServiceException e) {
            log.error("S3 파일 삭제 AmazonServiceException ({}): {}", fileUrl, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("S3 파일 삭제 예외 ({}): {}", fileUrl, e.getMessage());
            return false;
        }
    }

    // 다중 파일 삭제
    public void deleteFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls) {
            deleteImage(fileUrl);
        }
    }

    // 파일 크기 체크
    private void checkFileSize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }
        long maxFileSizeBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxFileSizeBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "파일 크기가 너무 큽니다. 최대 " + maxFileSizeMb + "MB까지 업로드 가능합니다.");
        }
    }

    // 파일명 생성 (S3 저장 키 생성)
    private String createFileName(String originalFilename) {
        // "trip/general/" 디렉토리 하위에 UUID와 확장자로 파일명 생성
        return "trip/general/" + UUID.randomUUID().toString().concat(getFileExtension(originalFilename));
    }

    // 확장자 추출
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }

    // S3 URL에서 객체 키 추출 (비공개 헬퍼)
    private String extractObjectKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            if (path.startsWith("/")) path = path.substring(1);
            return path;
        } catch (Exception e) {
            log.error("extractObjectKeyFromUrl 에러: {}", fileUrl, e);
            // Fallback 로직 (기존과 동일하게 유지)
            int lastSlashIndex = fileUrl.lastIndexOf('/');
            if (lastSlashIndex != -1 && fileUrl.startsWith("https://" + bucket + ".s3.")) {
                String domainPart = bucket + ".s3.";
                int domainEndIndex = fileUrl.indexOf(domainPart);
                if (domainEndIndex != -1) {
                    int keyStartIndex = fileUrl.indexOf('/', domainEndIndex + domainPart.length());
                    if (keyStartIndex != -1 && keyStartIndex < fileUrl.length() -1) {
                         return fileUrl.substring(keyStartIndex + 1);
                    }
                }
            }
            if (lastSlashIndex != -1 && lastSlashIndex < fileUrl.length() - 1) {
                 return fileUrl.substring(lastSlashIndex + 1); 
            }
            log.warn("URL에서 객체 키를 정확히 추출하지 못했습니다: {}", fileUrl);
            return null;
        }
    }
}

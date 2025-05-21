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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AWSS3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    @Value("${app.file.upload-max-size-mb:5}") // 기본값 5MB
    private long maxFileSizeMb;

    // MultipartFile을 전달받아 File로 전환 후 S3 서버에 파일 업로드
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        // 파일 크기 검증 (MB 단위)
        long maxFileSizeBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxFileSizeBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "파일 크기가 너무 큽니다. 최대 " + maxFileSizeMb + "MB까지 업로드 가능합니다.");
        }

        // 파일 형식 검증 (선택 사항 - 예: 허용된 이미지 타입만)
        // String contentType = file.getContentType();
        // if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
        //     throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원되지 않는 파일 형식입니다. (jpeg, png, gif만 가능)");
        // }

        String fileName = createFileName(file.getOriginalFilename());

        try{
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            InputStream inputStream = file.getInputStream();
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
        } catch(AmazonServiceException e){
            log.error("S3 업로드 중 AmazonServiceException 발생: {}", e.getMessage());
            e.printStackTrace(); // 개발 시 스택 트레이스 확인
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 서비스 오류로 이미지 업로드에 실패했습니다.");
        } catch (IOException e){
            log.error("S3 업로드 중 IOException 발생: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 파일 처리 오류가 발생했습니다.");
        }

        log.info("서버에 등록한 파일명: {}", fileName);

        // S3 이미지 서버에 등록한 URL을 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // S3 서버에서 파일 삭제
    public boolean deleteImage(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("삭제할 파일 URL이 제공되지 않았습니다.");
            return false;
        }
        try {
            // URL에서 객체 키(파일 이름) 추출
            // 예: https://bucket.s3.region.amazonaws.com/path/to/image.jpg -> path/to/image.jpg
            // 또는 버킷 URL 이후의 경로만 사용한다면 그에 맞게 조정
            String objectKey = extractObjectKeyFromUrl(fileUrl);
            if (objectKey == null || objectKey.isEmpty()) {
                log.error("S3 URL에서 객체 키를 추출할 수 없습니다: {}", fileUrl);
                return false;
            }
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, objectKey));
            log.info("S3에서 파일 삭제 성공: {}", objectKey);
            return true;
        } catch (AmazonServiceException e) {
            log.error("S3 파일 삭제 중 AmazonServiceException 발생 (URL: {}): {}", fileUrl, e.getMessage());
            // 실제 서비스에서는 실패 시 false를 반환하거나, 상황에 따라 예외를 다시 던질 수 있습니다.
            return false; 
        } catch (Exception e) {
            log.error("S3 파일 삭제 중 예기치 않은 오류 발생 (URL: {}): {}", fileUrl, e.getMessage());
            return false;
        }
    }

    // URL에서 S3 객체 키를 추출하는 헬퍼 메서드
    private String extractObjectKeyFromUrl(String fileUrl) {
        // S3 URL 형식은 다양할 수 있으므로, 가장 일반적인 경우를 처리합니다.
        // 예: https://s3.region.amazonaws.com/bucket-name/object-key
        // 또는 https://bucket-name.s3.region.amazonaws.com/object-key
        // 여기서는 URL에서 마지막 '/' 이후의 모든 것을 객체 키로 간주합니다.
        // 실제로는 S3 클라이언트 라이브러리가 생성하는 URL 형식을 정확히 파악하고 파싱하는 것이 좋습니다.
        // 또는 amazonS3.getUrl()로 생성된 URL이라면, 해당 URL에서 버킷 이름과 기본 엔드포인트를 제외한 나머지 부분을 추출해야 합니다.
        // 간단하게는 마지막 '/' 이후를 사용하거나, 더 정확하게는 new URL(fileUrl).getPath()를 사용 후 앞의 '/' 제거.
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            String path = url.getPath();
            // 경로가 "/"로 시작하면 제거
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // 경로가 버킷 이름으로 시작하는 경우 (예: bucket/key), 버킷 이름을 제거해야 할 수도 있음.
            // 여기서는 getUrl()로 생성된 URL이 버킷 이름 없이 키만 반환하거나, path 자체가 키라고 가정.
            // 만약 path가 "bucketName/objectKey" 형태라면, bucket 이름을 알아내서 제거해야 함.
            // 간단하게, bucket 이름을 알고 있다면:
            // if (path.startsWith(bucket + "/")) {
            //    return path.substring(bucket.length() + 1);
            // }
            // 여기서는 URL의 path 부분이 (선행 '/' 제외하고) 바로 object key라고 가정합니다.
            // 이는 amazonS3.getUrl(bucket, fileName)으로 생성된 URL이 "https://<bucket>.s3.../<fileName>" 일 때 유효합니다.
            // 만약 커스텀 도메인이나 다른 경로 구조를 사용한다면 이 로직은 수정되어야 합니다.
            return path;
        } catch (java.net.MalformedURLException e) {
            log.error("잘못된 형식의 S3 URL입니다: {}", fileUrl, e);
            // URL에서 마지막 '/' 이후를 단순 추출하는 fallback (덜 안전함)
            int lastSlashIndex = fileUrl.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < fileUrl.length() - 1) {
                return fileUrl.substring(lastSlashIndex + 1);
            }
            return null;
        }
    }

    // 기존 확장자명을 유지하면서, 식별되는 파일명을 생성
    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    // 파일 확장자 알아내기
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }
}
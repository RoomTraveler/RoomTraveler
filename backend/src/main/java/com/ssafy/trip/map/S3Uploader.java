//package com.ssafy.trip.map;
//
//import lombok.RequiredArgsConstructor;
//import lombok.Value;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
//import java.io.IOException;
//import java.util.Optional;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class S3Uploader {
//
//    private final S3Client s3Client;
//
////    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    public String upload(MultipartFile file, String dirName) {
//        String fileName = createFileName(dirName, file.getOriginalFilename());
//        try {
//            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                    .bucket(bucket)
//                    .key(fileName)
//                    .contentType(file.getContentType())
//                    .acl(ObjectCannedACL.PUBLIC_READ)
//                    .build();
//
//            s3Client.putObject(putObjectRequest,
//                    RequestBody.fromBytes(file.getBytes()));
//
//            return getFileUrl(fileName);
//        } catch (IOException e) {
//            throw new RuntimeException("S3 업로드 실패", e);
//        }
//    }
//
//    private String createFileName(String dirName, String originalFilename) {
//        String ext = Optional.ofNullable(originalFilename)
//                .filter(f -> f.contains("."))
//                .map(f -> f.substring(originalFilename.lastIndexOf(".") + 1))
//                .orElse("");
//        return dirName + "/" + UUID.randomUUID() + "." + ext;
//    }
//
//    private String getFileUrl(String fileName) {
//        return String.format("https://%s.s3.amazonaws.com/%s", bucket, fileName);
//    }
//}

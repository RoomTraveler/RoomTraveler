package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이미지 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
    private Long imageId;             // 이미지 ID
    private Long referenceId;         // 참조 ID (숙소 ID 또는 객실 ID)
    private String referenceType;     // 참조 타입 (ACCOMMODATION, ROOM)
    private Long accommodationId;     // 숙소 ID (외래 키)
    private Long roomId;              // 객실 ID (외래 키)
    private String imageUrl;          // 이미지 URL
    private String imageType;         // 이미지 유형 (MAIN, SUB, ROOM_MAIN 등)
    private String originalFileName;  // 원본 파일명
    private String storedFileName;    // 저장된 파일명 (UUID 등)
    private Long fileSize;            // 파일 크기 (bytes)
    private LocalDateTime uploadedAt;  // 업로드 시간
    private Integer sortOrder;         // 정렬 순서 (대표 이미지가 0 또는 1)
    private String caption;           // 이미지 설명
    private Boolean isMain;           // 대표 이미지 여부
    private LocalDateTime createdAt;  // 생성 시간

    // 추가 필드 - 조인 시 사용
    private String accommodationTitle; // 숙소 이름 (referenceType이 ACCOMMODATION인 경우)
    private String roomName;          // 객실 이름 (referenceType이 ROOM인 경우)

    // Getter and Setter
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}

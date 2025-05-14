package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Image;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.util.List;

/**
 * 이미지 데이터 액세스 인터페이스
 */
@Mapper
public interface ImageDao {

    /**
     * 새 이미지를 등록합니다.
     * @param image 등록할 이미지 정보
     * @return 생성된 이미지의 ID
     */
    Long insert(Image image) throws SQLException;

    /**
     * 이미지 ID로 이미지를 조회합니다.
     * @param imageId 이미지 ID
     * @return 이미지 정보
     */
    Image getImageById(Long imageId) throws SQLException;

    /**
     * 참조 ID와 참조 타입으로 이미지 목록을 조회합니다.
     * @param referenceId 참조 ID (숙소 ID 또는 객실 ID)
     * @param referenceType 참조 타입 (ACCOMMODATION 또는 ROOM)
     * @return 이미지 목록
     */
    List<Image> getImagesByReference(Long referenceId, String referenceType) throws SQLException;

    /**
     * 참조 ID와 참조 타입으로 대표 이미지를 조회합니다.
     * @param referenceId 참조 ID (숙소 ID 또는 객실 ID)
     * @param referenceType 참조 타입 (ACCOMMODATION 또는 ROOM)
     * @return 대표 이미지 정보
     */
    Image getMainImageByReference(Long referenceId, String referenceType) throws SQLException;

    /**
     * 이미지 정보를 업데이트합니다.
     * @param image 업데이트할 이미지 정보
     * @return 업데이트된 행 수
     */
    int updateImage(Image image) throws SQLException;

    /**
     * 이미지를 삭제합니다.
     * @param imageId 삭제할 이미지 ID
     * @return 삭제된 행 수
     */
    int deleteImage(Long imageId) throws SQLException;

    /**
     * 참조 ID와 참조 타입으로 이미지를 삭제합니다.
     * @param referenceId 참조 ID (숙소 ID 또는 객실 ID)
     * @param referenceType 참조 타입 (ACCOMMODATION 또는 ROOM)
     * @return 삭제된 행 수
     */
    int deleteImagesByReference(Long referenceId, String referenceType) throws SQLException;

    /**
     * 이미지를 대표 이미지로 설정합니다.
     * @param imageId 이미지 ID
     * @param referenceId 참조 ID (숙소 ID 또는 객실 ID)
     * @param referenceType 참조 타입 (ACCOMMODATION 또는 ROOM)
     * @return 업데이트된 행 수
     */
    int setMainImage(Long imageId, Long referenceId, String referenceType) throws SQLException;

    /**
     * 외부 API에서 가져온 이미지 정보를 저장합니다.
     * @param image 저장할 이미지 정보
     * @return 생성된 이미지의 ID
     */
    Long insertFromApi(Image image) throws SQLException;
}

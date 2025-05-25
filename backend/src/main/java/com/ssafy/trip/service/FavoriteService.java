package com.ssafy.trip.service;

import com.ssafy.trip.dto.FavoriteDto;
import com.ssafy.trip.dto.response.FavoriteResponseDto; // 추가
// import com.ssafy.trip.dto.response.FavoriteResponseDto; // 응답용 DTO (숙소 정보 포함 가능)

import java.util.List;

public interface FavoriteService {
    /**
     * 찜 추가 또는 복구
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @return 생성되거나 복구된 찜 정보 (FavoriteDto)
     * @throws com.ssafy.trip.exception.AlreadyExistsException 이미 활성화된 찜일 경우
     * @throws com.ssafy.trip.exception.ResourceNotFoundException 복구 후 조회 실패 시
     * @throws RuntimeException 그 외 처리 중 오류 발생 시
     */
    FavoriteDto addOrRecoverFavorite(Long userId, Long accommodationId);

    /**
     * 찜 삭제 (Soft Delete)
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @throws com.ssafy.trip.exception.ResourceNotFoundException 찜이 존재하지 않을 경우
     */
    void removeFavorite(Long userId, Long accommodationId);
    
    /**
     * 찜 삭제 (Soft Delete) - favoriteId 기준
     * @param favoriteId 찜 ID
     * @param userId 현재 요청을 보낸 사용자 ID (본인 확인용)
     * @throws com.ssafy.trip.exception.ResourceNotFoundException 찜이 존재하지 않을 경우
     * @throws com.ssafy.trip.exception.ForbiddenException 본인의 찜이 아닐 경우
     */
    void removeFavoriteById(Long favoriteId, Long userId);

    /**
     * 사용자의 찜 목록 조회 (숙소 정보 포함)
     * @param userId 사용자 ID
     * @return 찜 목록 (List<FavoriteResponseDto>)
     */
    List<FavoriteResponseDto> getFavoritesByUserId(Long userId); // 반환 타입 변경
    
    /**
     * 특정 숙소에 대한 사용자의 찜 여부 확인
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @return 찜 상태이면 true, 아니면 false
     */
    boolean isFavorite(Long userId, Long accommodationId);
} 
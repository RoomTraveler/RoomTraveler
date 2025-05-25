package com.ssafy.trip.dao;

import com.ssafy.trip.dto.FavoriteDto;
import com.ssafy.trip.dto.response.FavoriteResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FavoriteDao {
    /**
     * 찜 추가
     * @param favoriteDto 찜 정보 (userId, accommodationId 필수)
     * @return 삽입된 행의 수
     */
    int addFavorite(FavoriteDto favoriteDto);

    /**
     * 찜 삭제 (soft delete)
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @return 업데이트된 행의 수
     */
    int removeFavorite(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId);
    
    /**
     * 찜 삭제 (soft delete) - favoriteId 기준
     * @param favoriteId 찜 ID
     * @return 업데이트된 행의 수
     */
    int removeFavoriteById(@Param("favoriteId") Long favoriteId);


    /**
     * 사용자의 찜 목록 조회 (삭제되지 않은 것만)
     * @param userId 사용자 ID
     * @return 찜 목록
     */
    List<FavoriteDto> findFavoritesByUserId(@Param("userId") Long userId);

    /**
     * 특정 찜 정보 조회 (삭제되지 않은 것)
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @return 찜 정보 (Optional)
     */
    Optional<FavoriteDto> findFavorite(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId);
    
    /**
     * 특정 찜 정보 조회 (삭제되지 않은 것) - favoriteId 기준
     * @param favoriteId 찜 ID
     * @return 찜 정보 (Optional)
     */
    Optional<FavoriteDto> findFavoriteById(@Param("favoriteId") Long favoriteId);

    /**
     * 찜이 이미 존재하는지 확인 (삭제된 것도 포함하여 unique 제약 조건 위반 방지용)
     * userId와 accommodationId로 deleted_at이 null이거나 null이 아닌 레코드를 찾음
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @return 존재하면 해당 FavoriteDto, 없으면 null
     */
    FavoriteDto findFavoriteEvenIfDeleted(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId);

    /**
     * 찜 복구 (deleted_at을 null로 업데이트)
     * @param favoriteId 찜 ID
     * @return 업데이트된 행의 수
     */
    int recoverFavorite(@Param("favoriteId") Long favoriteId);

    /**
     * 사용자의 찜 목록 상세 조회 (숙소 정보 포함, 삭제되지 않은 것만)
     * @param userId 사용자 ID
     * @return 찜 상세 목록 (숙소 정보 포함)
     */
    List<FavoriteResponseDto> findFavoriteDetailsByUserId(@Param("userId") Long userId);
} 
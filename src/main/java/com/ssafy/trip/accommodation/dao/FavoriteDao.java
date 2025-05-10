package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Favorite;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

/**
 * 즐겨찾기(찜) 데이터 접근 객체 인터페이스
 */
@Mapper
public interface FavoriteDao {
    /**
     * 새 즐겨찾기를 추가합니다.
     */
    int insert(Favorite favorite) throws SQLException;

    /**
     * 즐겨찾기를 삭제합니다.
     */
    int delete(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId) throws SQLException;

    /**
     * 사용자의 모든 즐겨찾기를 조회합니다.
     */
    List<Favorite> selectByUserId(Long userId) throws SQLException;

    /**
     * 사용자가 특정 숙소를 즐겨찾기에 추가했는지 확인합니다.
     */
    Favorite selectByUserIdAndAccommodationId(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId) throws SQLException;

    /**
     * 즐겨찾기 ID로 즐겨찾기를 조회합니다.
     */
    Favorite selectById(Long favoriteId) throws SQLException;
}
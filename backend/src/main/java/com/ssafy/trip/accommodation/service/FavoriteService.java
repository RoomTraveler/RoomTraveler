package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.model.Favorite;

import java.sql.SQLException;
import java.util.List;

/**
 * 즐겨찾기(찜) 서비스 인터페이스
 */
public interface FavoriteService {

    /**
     * 새 즐겨찾기를 추가합니다.
     */
    Long addFavorite(Long userId, Long accommodationId) throws SQLException;

    /**
     * 즐겨찾기를 삭제합니다.
     */
    boolean removeFavorite(Long favoriteId, Long userId) throws SQLException;

    /**
     * 사용자의 모든 즐겨찾기를 조회합니다.
     */
    List<Favorite> getFavoritesByUserId(Long userId) throws SQLException;

    /**
     * 사용자가 특정 숙소를 즐겨찾기에 추가했는지 확인합니다.
     */
    boolean isFavorite(Long userId, Long accommodationId) throws SQLException;

    /**
     * 즐겨찾기 ID로 즐겨찾기를 조회합니다.
     */
    Favorite getFavoriteById(Long favoriteId) throws SQLException;
}
package com.ssafy.trip.favorite.service;

import com.ssafy.trip.favorite.model.Favorite;
import com.ssafy.trip.favorite.model.FavoriteResponseDto;

import java.sql.SQLException;
import java.util.List;

public interface FavoriteService {
    Long addFavorite(Long userId, Long accommodationId) throws SQLException;
    boolean removeFavorite(Long favoriteId, Long userId) throws SQLException;
    List<FavoriteResponseDto> getFavoritesByUserId(Long userId) throws SQLException;
    boolean isFavorite(Long userId, Long accommodationId) throws SQLException;
    Favorite getFavoriteById(Long favoriteId) throws SQLException;
    Long addOrRecoverFavorite(Long userId, Long accommodationId) throws SQLException;
    boolean removeFavoriteById(Long favoriteId, Long userId) throws SQLException;
    boolean removeFavoriteByUserIdAndAccommodationId(Long userId, Long accommodationId) throws SQLException;
}

package com.ssafy.trip.favorite.dao;

import com.ssafy.trip.favorite.model.Favorite;
import com.ssafy.trip.favorite.model.FavoriteResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface FavoriteDao {
    int insert(Favorite favorite) throws SQLException;
    int delete(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId) throws SQLException;
    List<FavoriteResponseDto> selectByUserId(Long userId) throws SQLException;
    Favorite selectByUserIdAndAccommodationId(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId) throws SQLException;
    Favorite selectById(Long favoriteId) throws SQLException;
    Favorite findFavoriteEvenIfDeleted(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId) throws SQLException;
    int recoverFavorite(@Param("favoriteId") Long favoriteId) throws SQLException;
    int removeFavoriteById(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId) throws SQLException;
    int removeFavoriteByUserIdAndAccommodationId(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId) throws SQLException;
}

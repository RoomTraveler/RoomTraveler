package com.ssafy.trip.favorite.service;

import com.ssafy.trip.favorite.dao.FavoriteDao;
import com.ssafy.trip.favorite.model.Favorite;
import com.ssafy.trip.favorite.model.FavoriteResponseDto;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteDao favoriteDao;

    @Override
    public Long addFavorite(Long userId, Long accommodationId) throws SQLException {
        Favorite existingFavorite = favoriteDao.selectByUserIdAndAccommodationId(userId, accommodationId);
        if (existingFavorite != null) return existingFavorite.getFavoriteId();
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setAccommodationId(accommodationId);
        favoriteDao.insert(favorite);
        return favorite.getFavoriteId();
    }

    @Override
    public boolean removeFavorite(Long favoriteId, Long userId) throws SQLException {
        return favoriteDao.delete(favoriteId, userId) > 0;
    }

    @Override
    public List<FavoriteResponseDto> getFavoritesByUserId(Long userId) throws SQLException {
        return favoriteDao.selectByUserId(userId);
    }

    @Override
    public boolean isFavorite(Long userId, Long accommodationId) throws SQLException {
        return favoriteDao.selectByUserIdAndAccommodationId(userId, accommodationId) != null;
    }

    @Override
    public Favorite getFavoriteById(Long favoriteId) throws SQLException {
        return favoriteDao.selectById(favoriteId);
    }

    @Override
    public Long addOrRecoverFavorite(Long userId, Long accommodationId) throws SQLException {
        Favorite favorite = favoriteDao.findFavoriteEvenIfDeleted(userId, accommodationId);
        if (favorite != null) {
            if (favorite.getDeletedAt() != null) {
                favoriteDao.recoverFavorite(favorite.getFavoriteId());
                return favorite.getFavoriteId();
            } else {
                return favorite.getFavoriteId();
            }
        } else {
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(userId);
            newFavorite.setAccommodationId(accommodationId);
            favoriteDao.insert(newFavorite);
            return newFavorite.getFavoriteId();
        }
    }

    @Override
    public boolean removeFavoriteById(Long favoriteId, Long userId) throws SQLException {
        return favoriteDao.removeFavoriteById(favoriteId, userId) > 0;
    }

    @Override
    public boolean removeFavoriteByUserIdAndAccommodationId(Long userId, Long accommodationId) throws SQLException {
        return favoriteDao.removeFavoriteByUserIdAndAccommodationId(userId, accommodationId) > 0;
    }
}

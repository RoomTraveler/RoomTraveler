package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.FavoriteDao;
import com.ssafy.trip.accommodation.model.Favorite;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * 즐겨찾기(찜) 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteDao favoriteDao;

    /**
     * 새 즐겨찾기를 추가합니다.
     */
    @Override
    public Long addFavorite(Long userId, Long accommodationId) throws SQLException {
        // 이미 즐겨찾기에 추가되어 있는지 확인
        Favorite existingFavorite = favoriteDao.selectByUserIdAndAccommodationId(userId, accommodationId);
        if (existingFavorite != null) {
            return existingFavorite.getFavoriteId();
        }

        // 새 즐겨찾기 생성
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setAccommodationId(accommodationId);

        // 즐겨찾기 추가
        favoriteDao.insert(favorite);
        return favorite.getFavoriteId();
    }

    /**
     * 즐겨찾기를 삭제합니다.
     */
    @Override
    public boolean removeFavorite(Long favoriteId, Long userId) throws SQLException {
        return favoriteDao.delete(favoriteId, userId) > 0;
    }

    /**
     * 사용자의 모든 즐겨찾기를 조회합니다.
     */
    @Override
    public List<Favorite> getFavoritesByUserId(Long userId) throws SQLException {
        return favoriteDao.selectByUserId(userId);
    }

    /**
     * 사용자가 특정 숙소를 즐겨찾기에 추가했는지 확인합니다.
     */
    @Override
    public boolean isFavorite(Long userId, Long accommodationId) throws SQLException {
        return favoriteDao.selectByUserIdAndAccommodationId(userId, accommodationId) != null;
    }

    /**
     * 즐겨찾기 ID로 즐겨찾기를 조회합니다.
     */
    @Override
    public Favorite getFavoriteById(Long favoriteId) throws SQLException {
        return favoriteDao.selectById(favoriteId);
    }
}
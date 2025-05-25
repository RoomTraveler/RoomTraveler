package com.ssafy.trip.service;

import com.ssafy.trip.dao.FavoriteDao;
import com.ssafy.trip.dto.FavoriteDto;
import com.ssafy.trip.dto.response.FavoriteResponseDto;
import com.ssafy.trip.exception.AlreadyExistsException;
import com.ssafy.trip.exception.ForbiddenException;
import com.ssafy.trip.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteServiceImpl.class);
    private final FavoriteDao favoriteDao;

    public FavoriteServiceImpl(FavoriteDao favoriteDao) {
        this.favoriteDao = favoriteDao;
    }

    @Override
    @Transactional
    public FavoriteDto addOrRecoverFavorite(Long userId, Long accommodationId) {
        if (userId == null || accommodationId == null) {
            throw new IllegalArgumentException("사용자 ID와 숙소 ID는 필수입니다.");
        }

        FavoriteDto existingFavorite = favoriteDao.findFavoriteEvenIfDeleted(userId, accommodationId);

        if (existingFavorite != null) {
            if (existingFavorite.getDeletedAt() == null) {
                throw new AlreadyExistsException("이미 찜 목록에 있는 숙소입니다."); 
            } else {
                int recoveredRows = favoriteDao.recoverFavorite(existingFavorite.getFavoriteId());
                if (recoveredRows > 0) {
                    logger.info("찜 복구 성공. favoriteId: {}", existingFavorite.getFavoriteId());
                    return favoriteDao.findFavoriteById(existingFavorite.getFavoriteId())
                                      .orElseThrow(() -> new ResourceNotFoundException("찜 복구 후 조회에 실패했습니다. ID: " + existingFavorite.getFavoriteId()));
                } else {
                    logger.error("찜 복구 실패. favoriteId: {}", existingFavorite.getFavoriteId());
                    throw new RuntimeException("찜 복구에 실패했습니다. ID: " + existingFavorite.getFavoriteId());
                }
            }
        } else {
            FavoriteDto newFavorite = new FavoriteDto();
            newFavorite.setUserId(userId);
            newFavorite.setAccommodationId(accommodationId);

            int addedRows = favoriteDao.addFavorite(newFavorite);
            if (addedRows > 0 && newFavorite.getFavoriteId() != null) {
                logger.info("새로운 찜 추가 성공. favoriteId: {}", newFavorite.getFavoriteId());
                return newFavorite;
            } else {
                logger.error("새로운 찜 추가 실패. userId: {}, accommodationId: {}", userId, accommodationId);
                throw new RuntimeException("찜 추가에 실패했습니다.");
            }
        }
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long accommodationId) {
         if (userId == null || accommodationId == null) {
            throw new IllegalArgumentException("사용자 ID와 숙소 ID는 필수입니다.");
        }
        // 존재 여부 확인을 위해 findFavorite 사용
        favoriteDao.findFavorite(userId, accommodationId)
            .orElseThrow(() -> new ResourceNotFoundException("삭제할 찜 정보가 존재하지 않습니다. 사용자 ID: " + userId + ", 숙소 ID: " + accommodationId));
        
        int affectedRows = favoriteDao.removeFavorite(userId, accommodationId);
        if (affectedRows == 0) {
             logger.warn("찜 삭제 시도했으나 변경된 행 없음 (이미 삭제되었거나 동시성 문제). userId: {}, accommodationId: {}", userId, accommodationId);
             // throw new RuntimeException("찜 삭제에 실패했거나 이미 삭제된 항목입니다."); // 필요시 예외 발생
        }
         logger.info("찜 삭제(soft delete) 성공. userId: {}, accommodationId: {}", userId, accommodationId);
    }
    
    @Override
    @Transactional
    public void removeFavoriteById(Long favoriteId, Long currentUserId) {
        if (favoriteId == null || currentUserId == null) {
            throw new IllegalArgumentException("찜 ID와 사용자 ID는 필수입니다.");
        }
        FavoriteDto favorite = favoriteDao.findFavoriteById(favoriteId)
            .orElseThrow(() -> new ResourceNotFoundException("삭제할 찜 정보가 존재하지 않습니다. 찜 ID: " + favoriteId));
        
        if (!favorite.getUserId().equals(currentUserId)) {
             logger.warn("본인의 찜이 아니므로 삭제할 수 없습니다. favoriteId: {}, currentUserId: {}", favoriteId, currentUserId);
            throw new ForbiddenException("본인의 찜만 삭제할 수 있습니다.");
        }

        int affectedRows = favoriteDao.removeFavoriteById(favoriteId);
         if (affectedRows == 0) {
            logger.warn("찜 삭제 시도했으나 변경된 행 없음 (이미 삭제되었거나 동시성 문제). favoriteId: {}", favoriteId);
            // throw new RuntimeException("찜 삭제에 실패했거나 이미 삭제된 항목입니다."); // 필요시 예외 발생
        }
        logger.info("찜 삭제(soft delete) 성공. favoriteId: {}", favoriteId);
    }


    @Override
    public List<FavoriteResponseDto> getFavoritesByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        return favoriteDao.findFavoriteDetailsByUserId(userId); // 호출 메소드 변경
    }

    @Override
    public boolean isFavorite(Long userId, Long accommodationId) {
        if (userId == null || accommodationId == null) {
            return false;
        }
        return favoriteDao.findFavorite(userId, accommodationId).isPresent();
    }
} 
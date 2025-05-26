package com.ssafy.trip.review.dao;

import com.ssafy.trip.review.model.ReviewImage;

import java.util.List;

public interface ReviewImgDao {
    List<ReviewImage> findUnmappedImages();
    void deleteImageById(Long imgId);
}

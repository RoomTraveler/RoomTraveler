package com.ssafy.trip.event.dao;

import com.ssafy.trip.event.model.EventBoardImg;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EventBoardImgDao {
    List<EventBoardImg> findUnmappedImages();
    void deleteImageById(Long imgId);
}

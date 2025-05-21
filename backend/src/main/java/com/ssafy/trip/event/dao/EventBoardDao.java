package com.ssafy.trip.event.dao;

import com.ssafy.trip.event.model.EventBoard;
import com.ssafy.trip.event.model.EventBoardImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface EventBoardDao {
    // 게시글
    void insertEvent(EventBoard eventBoard);
    EventBoard selectEventBoardByEventId(Long eventId);
    List<EventBoard> selectAll();
    void updateEvent(EventBoard eventBoard);
    void deleteEvent(Long eventId);
    void incrementViewCount(Long eventId);

    // 이미지
    void insertEventImages(@Param("eventId") Long eventId, @Param("imgUrls") List<String> imgUrls);
    List<EventBoardImg> selectEventImages(Long eventId);
    void deleteEventImages(Long eventId); // 게시글 전체 이미지
    void deleteEventImageByUrl(String imgUrl); // 단일 이미지
}
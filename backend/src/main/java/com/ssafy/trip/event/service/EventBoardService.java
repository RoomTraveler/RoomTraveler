package com.ssafy.trip.event.service;


import com.ssafy.trip.event.model.EventBoard;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface EventBoardService {
    void createEventBoard(EventBoard eventBoard, MultipartFile thumbnailImage) throws IOException;
    List<EventBoard> getAllEventBoard();
    EventBoard getEventBoard(Long eventId);
    void updateEventBoard(EventBoard eventBoard, MultipartFile newThumbnailImage, List<String> deleteImgUrls) throws IOException;
    void deleteEventBoard(Long eventId);
}

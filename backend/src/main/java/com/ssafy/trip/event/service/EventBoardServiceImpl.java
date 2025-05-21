package com.ssafy.trip.event.service;

import com.ssafy.trip.event.dao.EventBoardDao;
import com.ssafy.trip.event.model.EventBoard;
import com.ssafy.trip.event.model.EventBoardImg;
import com.ssafy.trip.s3.AWSS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventBoardServiceImpl implements EventBoardService {
    private final EventBoardDao eventBoardDao;
    private final AWSS3Service awsS3Service;

    @Override
    @Transactional
    public void createEventBoard(EventBoard eventBoard, List<MultipartFile> images) throws IOException {
        if(eventBoard.getStatus() == null) eventBoard.setStatus("ONGOING");
        if(eventBoard.getViewCount() == null) eventBoard.setViewCount(0);
        eventBoardDao.insertEvent(eventBoard);
        if (images != null && !images.isEmpty()) {
            List<String> imgUrls = awsS3Service.uploadFiles(images);
            eventBoardDao.insertEventImages(eventBoard.getEventId(), imgUrls);
        }
    }

    @Override
    public List<EventBoard> getAllEventBoard() {
        List<EventBoard> list = eventBoardDao.selectAll();
        // 각 게시글에 이미지 채워주기
        for (EventBoard board : list) {
            board.setImages(eventBoardDao.selectEventImages(board.getEventId()));
        }
        return list;
    }

    @Override
    public EventBoard getEventBoard(Long eventId) {
        eventBoardDao.incrementViewCount(eventId);
        EventBoard board = eventBoardDao.selectEventBoardByEventId(eventId);
        board.setImages(eventBoardDao.selectEventImages(eventId));
        return board;
    }

    @Override
    @Transactional
    public void updateEventBoard(EventBoard eventBoard, List<MultipartFile> newImages, List<String> deleteImgUrls) throws IOException {
        eventBoardDao.updateEvent(eventBoard);
        if (deleteImgUrls != null) {
            for (String url : deleteImgUrls) {
                awsS3Service.deleteImage(url);
                eventBoardDao.deleteEventImageByUrl(url);
            }
        }
        if (newImages != null && !newImages.isEmpty()) {
            List<String> imgUrls = awsS3Service.uploadFiles(newImages);
            eventBoardDao.insertEventImages(eventBoard.getEventId(), imgUrls);
        }
    }

    @Override
    @Transactional
    public void deleteEventBoard(Long eventId) {
        List<EventBoardImg> imgs = eventBoardDao.selectEventImages(eventId);
        for (EventBoardImg img : imgs) {
            awsS3Service.deleteImage(img.getImgUrl());
        }
        eventBoardDao.deleteEventImages(eventId);
        eventBoardDao.deleteEvent(eventId);
    }
}


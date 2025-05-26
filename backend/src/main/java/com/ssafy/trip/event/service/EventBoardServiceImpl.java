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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventBoardServiceImpl implements EventBoardService {
    private final EventBoardDao eventBoardDao;
    private final AWSS3Service awsS3Service;

    @Override
    @Transactional
    public void createEventBoard(EventBoard eventBoard, MultipartFile thumbnailImage) throws IOException {
        if(eventBoard.getStatus() == null) eventBoard.setStatus("ONGOING");
        if(eventBoard.getViewCount() == null) eventBoard.setViewCount(0);
        eventBoardDao.insertEvent(eventBoard);

        Long eventId = eventBoard.getEventId();
        List<EventBoardImg> eventImagesToSave = new ArrayList<>();

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            String thumbnailUrl = awsS3Service.uploadFile(thumbnailImage);
            eventImagesToSave.add(EventBoardImg.builder()
                    .eventId(eventId)
                    .imgUrl(thumbnailUrl)
                    .isThumbnail(true)
                    .build());
        }

        if (!eventImagesToSave.isEmpty()) {
            eventBoardDao.insertEventImages(eventImagesToSave);
        }
    }

    @Override
    public List<EventBoard> getAllEventBoard() {
        List<EventBoard> list = eventBoardDao.selectAll();
        for (EventBoard board : list) {
            board.setImages(eventBoardDao.selectEventImages(board.getEventId()));
        }
        return list;
    }

    @Override
    public EventBoard getEventBoard(Long eventId) {
        eventBoardDao.incrementViewCount(eventId);
        EventBoard board = eventBoardDao.selectEventBoardByEventId(eventId);
        if (board != null) {
            board.setImages(eventBoardDao.selectEventImages(eventId));
        }
        return board;
    }

    @Override
    @Transactional
    public void updateEventBoard(EventBoard eventBoard, MultipartFile newThumbnailImage, List<String> deleteImgUrls) throws IOException {
        eventBoardDao.updateEvent(eventBoard);
        Long eventId = eventBoard.getEventId();

        if (deleteImgUrls != null && !deleteImgUrls.isEmpty()) {
            for (String url : deleteImgUrls) {
                awsS3Service.deleteImage(url);
                eventBoardDao.deleteEventImageByUrl(url);
            }
        }

        if (newThumbnailImage != null && !newThumbnailImage.isEmpty()) {
            List<EventBoardImg> oldThumbnails = eventBoardDao.selectEventImages(eventId).stream().filter(img -> Boolean.TRUE.equals(img.getIsThumbnail())).toList();
            for(EventBoardImg oldThumb : oldThumbnails){
                if(!deleteImgUrls.contains(oldThumb.getImgUrl())) {
                     awsS3Service.deleteImage(oldThumb.getImgUrl());
                     eventBoardDao.deleteEventImageByUrl(oldThumb.getImgUrl());
                }
            }

            String thumbnailUrl = awsS3Service.uploadFile(newThumbnailImage);
            List<EventBoardImg> thumbnailToSave = new ArrayList<>();
            thumbnailToSave.add(EventBoardImg.builder()
                    .eventId(eventId)
                    .imgUrl(thumbnailUrl)
                    .isThumbnail(true)
                    .build());
            eventBoardDao.insertEventImages(thumbnailToSave);
        }
    }

    @Override
    @Transactional
    public void deleteEventBoard(Long eventId) {
        List<EventBoardImg> imgs = eventBoardDao.selectEventImages(eventId);
        if (imgs != null) {
            for (EventBoardImg img : imgs) {
                if (img.getImgUrl() != null && Boolean.TRUE.equals(img.getIsThumbnail())) {
                    awsS3Service.deleteImage(img.getImgUrl());
                }
            }
        }
        eventBoardDao.deleteEventImages(eventId);
        eventBoardDao.deleteEvent(eventId);
    }
}


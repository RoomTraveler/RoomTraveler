package com.ssafy.trip.review.scheduler;

import com.ssafy.trip.event.dao.EventBoardImgDao;
import com.ssafy.trip.event.model.EventBoardImg;
import com.ssafy.trip.s3.AWSS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


//수정 시 지우셈.
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewUnmappedImageCleanupScheduler {

    private final EventBoardImgDao eventBoardImgDao;
    private final AWSS3Service awsS3Service;

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시에 실행
    @Transactional
    public void cleanupUnmappedImages() {
        List<EventBoardImg> unmappedImages = eventBoardImgDao.findUnmappedImages();

        for (EventBoardImg image : unmappedImages) {
            // S3에서 이미지 삭제
            awsS3Service.deleteImage(image.getImgUrl());
            // DB에서 이미지 레코드 삭제
            eventBoardImgDao.deleteImageById(image.getImgId());
        }

        log.info("[cleanupUnmappedImages 실행] 삭제완료 - {}건", unmappedImages.size());
    }
}

package com.ssafy.trip.event.controller;

import com.ssafy.trip.common.BaseException;
import com.ssafy.trip.common.BaseResponse;
import com.ssafy.trip.common.ErrorCode;
import com.ssafy.trip.event.model.EventBoard;
import com.ssafy.trip.event.service.EventBoardService;
import com.ssafy.trip.s3.AWSS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ApiEventBoardController {
    private final EventBoardService eventBoardService;
    private final AWSS3Service awsS3Service;

    // 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> createEventBoard(
            @RequestPart EventBoard eventBoard,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) throws IOException {
        eventBoardService.createEventBoard(eventBoard, thumbnailImage);
        return BaseResponse.onSuccess();
    }

    // 전체 목록
    @GetMapping
    public BaseResponse<List<EventBoard>> selectAll() {
        return BaseResponse.onSuccess(eventBoardService.getAllEventBoard());
    }

    // 상세
    @GetMapping("/{eventId}")
    public BaseResponse<EventBoard> getOne(@PathVariable Long eventId) {
        EventBoard eventBoard = eventBoardService.getEventBoard(eventId);
        // eventBoard가 null일 경우, BaseResponse.onSuccess(null)로 클라이언트에 전달됩니다.
        // Jackson은 EventBoard 객체를 직렬화할 때 getMainImageUrl() 메소드를 호출하여
        // mainImageUrl 필드를 JSON에 포함시킵니다 (EventBoard.java에 해당 메소드가 올바르게 구현되어 있다면).
        return BaseResponse.onSuccess(eventBoard);
    }

    // 수정
    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> updateEventBoard(
            @PathVariable Long eventId,
            @RequestPart EventBoard eventBoard,
            @RequestPart(value = "newThumbnailImage", required = false) MultipartFile newThumbnailImage,
            @RequestParam(required = false) List<String> deleteImgUrls
    ) throws IOException {
        eventBoard.setEventId(eventId);
        eventBoardService.updateEventBoard(eventBoard, newThumbnailImage, deleteImgUrls);
        return BaseResponse.onSuccess();
    }

    // 삭제
    @DeleteMapping("/{eventId}")
    public BaseResponse<Void> deleteEventBoard(@PathVariable Long eventId) {
        eventBoardService.deleteEventBoard(eventId);
        return BaseResponse.onSuccess();
    }

    // Toast UI Editor 이미지 업로드용 엔드포인트
    @PostMapping("/upload-editor-image")
    public BaseResponse<Map<String, String>> uploadEditorImage(@RequestPart("image") MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new BaseException(ErrorCode.EMPTY_FILE_ERROR);
        }
        String imageUrl = awsS3Service.uploadFile(image);
        Map<String, String> responseData = new HashMap<>();
        responseData.put("imageUrl", imageUrl);
        return BaseResponse.onSuccess(responseData);
    }
}

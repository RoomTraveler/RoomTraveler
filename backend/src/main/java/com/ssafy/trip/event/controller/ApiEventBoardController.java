package com.ssafy.trip.event.controller;

import com.ssafy.trip.common.BaseResponse;
import com.ssafy.trip.event.model.EventBoard;
import com.ssafy.trip.event.service.EventBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ApiEventBoardController {
    private final EventBoardService eventBoardService;

    // 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> createEventBoard(
            @RequestPart EventBoard eventBoard,
            @RequestPart(required = false) List<MultipartFile> images) throws IOException {
        eventBoardService.createEventBoard(eventBoard, images);
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
        return BaseResponse.onSuccess(eventBoardService.getEventBoard(eventId));
    }

    // 수정
    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> updateEventBoard(
            @PathVariable Long eventId,
            @RequestPart EventBoard eventBoard,
            @RequestPart(required = false) List<MultipartFile> newImages,
            @RequestParam(required = false) List<String> deleteImgUrls
    ) throws IOException {
        eventBoard.setEventId(eventId);
        eventBoardService.updateEventBoard(eventBoard, newImages, deleteImgUrls);
        return BaseResponse.onSuccess();
    }

    // 삭제
    @DeleteMapping("/{eventId}")
    public BaseResponse<Void> deleteEventBoard(@PathVariable Long eventId) {
        eventBoardService.deleteEventBoard(eventId);
        return BaseResponse.onSuccess();
    }
}

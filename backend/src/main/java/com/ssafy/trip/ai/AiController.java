package com.ssafy.trip.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
public class AiController {

    private final AiChatService aiChatService;

    @PostMapping("/evaluation")
    public ResponseEntity<?> aiEvaluation(@RequestBody TravelRoute travelRoute) {
        return ResponseEntity.ok(aiChatService.evaluateAndRecommend(travelRoute));
    }
}

package com.ssafy.trip.map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessageController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 클라이언트가 /app/squad/join으로 보낸 메시지를 받음
    @MessageMapping("/squad/join")
    public void join(JoinMessage message) {
        log.info("Squad joined: " + message.getSquadId());
        messagingTemplate.convertAndSend("/topic/squad." + message.getSquadId(), message);
    }

    @MessageMapping("/squad/selectedPlaces")
    public void selectedPlaces(SelectedPlacesMessage message) {
        log.info("Squad selectedPlaces: " + message.getContent());
        messagingTemplate.convertAndSend("/topic/squad." + message.getSquadId(), message);
    }

    @MessageMapping("/squad/mouseMove")
    public void mouseMove(MouseMoveMessage message) {
        log.info("Squad MouseMove: " + message.getContent());
        messagingTemplate.convertAndSend("/topic/squad." + message.getSquadId(), message);
    }

    @MessageMapping("/squad/savePlan")
    public void savePlan(SaveMessage message) {
        messagingTemplate.convertAndSend("/topic/squad." + message.getSquadId(), message);
    }
}
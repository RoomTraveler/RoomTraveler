package com.ssafy.trip.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    @Qualifier("simpleChatClient")
    private final ChatClient simpleChatClient;

    public Object simpleGeneration(String userInput) {
        var spec = simpleChatClient.prompt()
                .system(t -> t.param("language", "korean")
                        .param("character", "kind and intelligence"))
                .user(userInput)
                .call();
        return spec.content();
    }
}

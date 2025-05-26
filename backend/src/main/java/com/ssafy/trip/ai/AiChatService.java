package com.ssafy.trip.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient chatClient;

    public Object evaluateAndRecommend(TravelRoute travelRoute) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("아래 여행 일정에 대해 전반적인 평가와 개선 추천을 해줘.\n");
        prompt.append("선택된 장소들:\n");
        for (int i = 0; i < travelRoute.getSelectedPlaces().size(); i++) {
            Place p = travelRoute.getSelectedPlaces().get(i);
            prompt.append(String.format("%d. %s (위도: %.5f, 경도: %.5f, 콘텐츠 타입: %s, 주소: %s)\n",
                    i+1, p.getName(), p.getLatitude(), p.getLongitude(), p.getContentType(), p.getAddress()));
        }
        prompt.append("\n1) 이 계획의 장단점을 평가하는데 이건 관광지 자체에 대한거만 해줘\n");
        prompt.append("2) 최적화하거나 추가하면 좋을 장소나 이동 순서를 추천해줘.");
        prompt.append("7초 내로 대답해줘.");

        log.info(prompt.toString());

        log.info(chatClient.prompt().toString());
        var spec = chatClient.prompt()
                .user(prompt.toString())
                .call();

        log.info(spec.toString());
        log.info(spec.content());

        return spec.content();
    }
}

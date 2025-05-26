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
        prompt.append("\n답변은 장점, 단점, 추천으로 나누어 답변해주고 장단점은 관광지 자체에 대한거만 평가해줘\n");
        prompt.append("추천은 동선을 최적화하거나 추가하면 좋을 장소나 이동 순서를 추천해줘.");
        prompt.append("가능한 7초 내로 빠르게 대답해줘.");

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

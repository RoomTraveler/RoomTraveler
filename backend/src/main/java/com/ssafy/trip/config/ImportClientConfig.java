package com.ssafy.trip.config;

import com.siot.IamportRestClient.IamportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class ImportClientConfig {

    // ★ 실제 발급받은 아임포트 REST API 키/시크릿 입력
    @Value("${ImPort.REST.API.Key}")
    private String apiKey;

    @Value("${ImPort.REST.API.Secret}")
    private String apiSecret;

    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(apiKey, apiSecret);
    }
}

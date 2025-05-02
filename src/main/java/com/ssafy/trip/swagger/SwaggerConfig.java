package com.ssafy.trip.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;


@Configuration
public class SwaggerConfig {

    // OpenAPI 메타정보 설정
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API 문서")
                        .description("API 문서 설명")
                        .version("1.0.0")
                );
    }

    // com.ssafy.trip 패키지의 컨트롤러 그룹핑 설정
    @Bean
    public GroupedOpenApi tripApiGroup() {
        return GroupedOpenApi.builder()
                .group("trip-api")
                .packagesToScan("com.ssafy.trip")
                .pathsToMatch("/**")
                .build();
    }
}
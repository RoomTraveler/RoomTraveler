package com.ssafy.trip.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    // OpenAPI 메타정보 및 JWT 인증 설정 추가
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth"; // 보안 스키마 이름
        return new OpenAPI()
                .info(new Info()
                        .title("방구석 여행자")
                        .description("이 문서에서는 RoomTraveler 애플리케이션에서 사용할 수 있는 모든 REST API에 대한 세부 정보를 제공합니다. 이 인터페이스를 사용하여 API를 테스트하고 상호 작용할 수 있습니다.")
                        .version("1.0.0")
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName)) // 모든 API에 보안 요구사항 추가
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
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

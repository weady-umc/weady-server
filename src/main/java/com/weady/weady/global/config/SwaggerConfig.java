package com.weady.weady.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "bearerAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Weady API Documentation")
                .description("Weady 프로젝트 API 명세서입니다.")
                .version("v0.0.1");
    }

    /*
    * 각 API 도메인 별로 그룹화하여 Swagger UI에 표시합니다.
    * */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1. 인증 API") // Swagger UI에 표시될 그룹 이름
                .pathsToMatch("/api/v1/auth/**") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    // 추후 다른 도메인 API가 추가되면 여기에 GroupedOpenApi Bean을 추가해주세요
}
package com.weady.weady.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
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
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components)
                .addServersItem(new Server().url("https://weadyapi.pro"));  // ✅ 이거 추가!

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
                .group("auth")
                .displayName("1. 인증 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch("/api/v1/auth/**") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi boardApi() {
        return GroupedOpenApi.builder()
                .group("board")
                .displayName("2. 웨디 보드 API")
                .pathsToMatch("/api/v1/board/**")
                .build();
    }

    @Bean
    public GroupedOpenApi weatherApi() {
        return GroupedOpenApi.builder()
                .group("weather")
                .displayName("3. 날씨 API")
                .pathsToMatch("/api/v1/weather/**")
                .build();
    }

    @Bean
    public GroupedOpenApi curationApi() {
        return GroupedOpenApi.builder()
                .group("curation")
                .displayName("4. 큐레이션 API")
                .pathsToMatch("/api/v1/curation/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fashionApi() {
        return GroupedOpenApi.builder()
                .group("fashion")
                .displayName("5. 패션 API")
                .pathsToMatch("/api/v1/fashion/**")
                .build();
    }

    @Bean
    public GroupedOpenApi tagsApi() {
        return GroupedOpenApi.builder()
                .group("tag,category")
                .displayName("5. category, tag API")
                .pathsToMatch("/api/v1/categories/**")
                .pathsToMatch("/api/v1/tags/**")
                .build();
    }

    @Bean
    public GroupedOpenApi locationApi() {
        return GroupedOpenApi.builder()
                .group("location")
                .displayName("6. 지역 API")
                .pathsToMatch("/api/v1/location/**")
                .build();
    }

    @Bean
    public GroupedOpenApi weadychiveApi() {
        return GroupedOpenApi.builder()
                .group("weadychive")
                .displayName("7. 웨디카이브 API")
                .pathsToMatch("/api/v1/weadychive/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .displayName("8. 유저 API")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }

    // 추후 다른 도메인 API가 추가되면 여기에 GroupedOpenApi Bean을 추가해주세요
}
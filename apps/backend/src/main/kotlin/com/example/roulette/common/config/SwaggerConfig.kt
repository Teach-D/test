package com.example.roulette.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val securityScheme =
            SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")

        return OpenAPI()
            .info(
                Info()
                    .title("포인트 룰렛 서비스 API")
                    .description("매일 룰렛을 돌려 포인트를 획득하고, 상품을 구매할 수 있는 서비스")
                    .version("1.0.0"),
            ).components(Components().addSecuritySchemes("bearerAuth", securityScheme))
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
    }
}

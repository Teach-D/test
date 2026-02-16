package com.example.roulette.common

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Flyway 마이그레이션 설정
 *
 * 체크섬 불일치 시 자동으로 repair 후 migrate를 실행한다.
 * Windows(CRLF)와 Linux(LF) 환경 간 줄바꿈 차이로 인한
 * 체크섬 불일치를 방지한다.
 */
@Configuration
class FlywayConfig {
    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy =
        FlywayMigrationStrategy { flyway ->
            flyway.repair()
            flyway.migrate()
        }
}

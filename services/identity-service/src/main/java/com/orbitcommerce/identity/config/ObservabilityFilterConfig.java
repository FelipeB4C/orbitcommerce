package com.orbitcommerce.identity.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityFilterConfig {

    @Bean
    public ObservationPredicate customTracingFilter() {
        return (name, context) -> {
            // 1. Remove os spans de iteração de linhas (result-set)
            if (name.contains("result-set")) {
                return false;
            }

            // 2. Remove o span longo de empréstimo de conexão do HikariCP (connection)
            if (name.contains("connection")) {
                return false;
            }

            return true;
        };
    }

}

package br.edu.safeplace.backend.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Strategy de fonte de tempo do sistema (#247).
 * Fornece a estratégia de relógio para injeção nos casos de uso e validadores,
 * permitindo que a produção utilize o horário do sistema e os testes usem Clock.fixed(...).
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

package br.edu.safeplace.backend.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Strategy de fonte de tempo do sistema (#247).
 * Fornece a estratégia de relógio para injeção nos casos de uso e validadores,
 * permitindo que a produção utilize o horário do sistema e os testes usem Clock.fixed(...).
 *
 * <p>O fuso é explícito ({@code safeplace.fuso-horario}, padrão {@code America/Sao_Paulo}): o
 * contêiner do backend roda em UTC, e com {@code systemDefaultZone()} o "mês corrente" e a hora de
 * geração dos relatórios (#299) saíam três horas à frente.</p>
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${safeplace.fuso-horario:America/Sao_Paulo}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}

/*
 * Liga as rotinas agendadas, como a verificação dos alertas de prazo das ações do plano (UC07, regra 2;
 * issue #196). A propriedade safeplace.agendamento.ativo permite desligá-las, o que a suíte de testes
 * faz para que nenhuma rotina altere o banco no meio de um teste.
 */
package br.edu.safeplace.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "safeplace.agendamento.ativo", havingValue = "true", matchIfMissing = true)
public class AgendamentoConfig {
}

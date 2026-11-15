/*
 * Testes do Null Object NotificadorOcorrenciaNulo (issue #227): ele cumpre o contrato da porta
 * NotificadorOcorrencia sem efeito colateral, aceitando qualquer relato e listando nada.
 */
package br.edu.safeplace.backend.adapters.out.notificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;

@DisplayName("NotificadorOcorrenciaNulo (Null Object)")
class NotificadorOcorrenciaNuloTest {

    private final NotificadorOcorrencia notificador = new NotificadorOcorrenciaNulo();

    @Test
    @DisplayName("Notificar um relato não faz nada e não lança exceção")
    void notificarNaoTemEfeito() {
        Incidente incidente = Incidente.builder()
                .idOcorrencia(7)
                .descricao("Piso molhado")
                .dataOcorrencia(LocalDateTime.of(2026, 10, 1, 9, 0))
                .dataRegistro(LocalDateTime.of(2026, 10, 1, 9, 5))
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .situacaoRisco("Piso molhado")
                .potencialDano("Queda")
                .build();

        assertThatCode(() -> notificador.notificar(incidente)).doesNotThrowAnyException();
        assertThat(notificador.listar()).isEmpty();
    }

    @Test
    @DisplayName("Listar devolve uma lista vazia e imutável, nunca null")
    void listarDevolveListaVaziaImutavel() {
        assertThat(notificador.listar()).isNotNull().isEmpty();
        assertThatThrownBy(() -> notificador.listar().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}

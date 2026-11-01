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
        Incidente incidente = new Incidente(7, "Piso molhado", LocalDateTime.of(2026, 10, 1, 9, 0),
                LocalDateTime.of(2026, 10, 1, 9, 5), StatusOcorrencia.ABERTA, null, null, null, null, null,
                "Piso molhado", "Queda");

        assertThatCode(() -> notificador.notificarGestor(incidente)).doesNotThrowAnyException();
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

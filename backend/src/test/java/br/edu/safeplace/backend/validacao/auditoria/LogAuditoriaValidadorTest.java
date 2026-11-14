package br.edu.safeplace.backend.validacao.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;

/** Os casos de recusa vieram de {@code LogAuditoriaTest} com as mesmas asserções (issue #297). */
@DisplayName("LogAuditoriaValidador - cadeia de validação da trilha de auditoria (Chain of Responsibility)")
class LogAuditoriaValidadorTest {

    private final LogAuditoriaValidador validador = new LogAuditoriaValidador();

    @Test
    @DisplayName("Aceita log com tipo de operação e responsável")
    void aceitaLogValido() {
        assertThatCode(() -> validador.validar(LogAuditoria.registrarCadastro("gestor@safeplace.com", "Epi", 1, "Cadastro")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar ao criar log sem tipo de operação")
    void deveFalharSemTipoOperacao() {
        assertThatThrownBy(() -> validador.validar(
                new LogAuditoria(null, null, LocalDateTime.now(), "operador", null, "Entidade", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de operação é obrigatório.");

        assertThatThrownBy(() -> validador.validar(
                new LogAuditoria(null, "   ", LocalDateTime.now(), "operador", null, "Entidade", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de operação é obrigatório.");
    }

    @Test
    @DisplayName("Deve falhar ao criar log sem responsável")
    void deveFalharSemResponsavel() {
        assertThatThrownBy(() -> validador.validar(
                new LogAuditoria(null, "ALTERACAO", LocalDateTime.now(), null, null, "Entidade", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsável pela operação é obrigatório.");

        assertThatThrownBy(() -> validador.validar(
                new LogAuditoria(null, "ALTERACAO", LocalDateTime.now(), "   ", null, "Entidade", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsável pela operação é obrigatório.");
    }

    @Test
    @DisplayName("Sem tipo e sem responsável, recusa pelo tipo primeiro, na ordem do antigo construtor")
    void recusaNaOrdemDoConstrutor() {
        LogAuditoria vazio = new LogAuditoria(null, null, LocalDateTime.now(), null, null, "Entidade", 1);

        assertThat(validador.verificar(vazio).mensagens())
                .containsExactly("Tipo de operação é obrigatório.", "Responsável pela operação é obrigatório.");
        assertThatThrownBy(() -> validador.validar(vazio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de operação é obrigatório.");
    }
}

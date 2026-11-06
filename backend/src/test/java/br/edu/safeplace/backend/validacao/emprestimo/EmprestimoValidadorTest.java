package br.edu.safeplace.backend.validacao.emprestimo;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.epi;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;

/**
 * Os casos vieram de {@code EmprestimoTest} com as mesmas asserções (issue #265): antes, quem lançava era
 * o construtor de {@code Emprestimo}; agora é a cadeia, antes do repository.
 */
@DisplayName("EmprestimoValidador - cadeia de validação do empréstimo (Chain of Responsibility)")
class EmprestimoValidadorTest {

    private static final LocalDateTime RETIRADA = LocalDateTime.of(2026, 9, 21, 9, 30);
    private static final LocalDate PREVISTA = LocalDate.of(2026, 9, 28);

    private final EmprestimoValidador emprestimo = new EmprestimoValidador();

    @Test
    @DisplayName("Construção exige EPI, colaborador, responsável, datas coerentes e termo de cautela")
    void construcaoValidaObrigatorios() {
        assertThatThrownBy(() -> emprestimo.validar(Emprestimo.registrarEntrega(epi(null), pessoa(3), pessoa(2), RETIRADA, PREVISTA, "termo", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("EPI");
        assertThatThrownBy(() -> emprestimo.validar(Emprestimo.registrarEntrega(epi(10), pessoa(null), pessoa(2), RETIRADA, PREVISTA, "termo", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Colaborador");
        assertThatThrownBy(() -> emprestimo.validar(Emprestimo.registrarEntrega(epi(10), pessoa(3), pessoa(null), RETIRADA, PREVISTA, "termo", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Responsável");
        assertThatThrownBy(() -> emprestimo.validar(Emprestimo.registrarEntrega(epi(10), pessoa(3), pessoa(2), RETIRADA, RETIRADA.toLocalDate().minusDays(1), "termo", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("anterior");
        assertThatThrownBy(() -> emprestimo.validar(Emprestimo.registrarEntrega(epi(10), pessoa(3), pessoa(2), RETIRADA, PREVISTA, " ", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Termo");
    }

    @Test
    @DisplayName("Reconstrução exige coerência entre status e devolução")
    void reconstrucaoExigeCoerencia() {
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo(1, epi(10), pessoa(3), pessoa(2), RETIRADA, PREVISTA, "termo", null,
                RETIRADA.plusDays(1), pessoa(5), EstadoDevolucao.APTO, ClassificacaoEmprestimo.INDISPONIVEL)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo(1, epi(10), pessoa(3), pessoa(2), RETIRADA, PREVISTA, "termo", null,
                null, pessoa(null), null, ClassificacaoEmprestimo.DISPONIVEL)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo(1, epi(10), pessoa(3), pessoa(2), RETIRADA, PREVISTA, "termo", null,
                RETIRADA.plusDays(1), pessoa(5), null, ClassificacaoEmprestimo.DISPONIVEL)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package br.edu.safeplace.backend.validacao.emprestimo;

import br.edu.safeplace.backend.suporte.EmprestimoDeTeste;
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
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo.Builder()
                .beneficiario(pessoa(3))
                .responsavelEntrega(pessoa(2))
                .retiradaEm(RETIRADA)
                .devolucaoPrevistaPara(PREVISTA)
                .termoCautela("termo")
                .build()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("EPI");
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo.Builder()
                .epi(epi(10))
                .responsavelEntrega(pessoa(2))
                .retiradaEm(RETIRADA)
                .devolucaoPrevistaPara(PREVISTA)
                .termoCautela("termo")
                .build()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Colaborador");
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo.Builder()
                .epi(epi(10))
                .beneficiario(pessoa(3))
                .retiradaEm(RETIRADA)
                .devolucaoPrevistaPara(PREVISTA)
                .termoCautela("termo")
                .build()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Responsável");
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo.Builder()
                .epi(epi(10))
                .beneficiario(pessoa(3))
                .responsavelEntrega(pessoa(2))
                .retiradaEm(RETIRADA)
                .devolucaoPrevistaPara(RETIRADA.toLocalDate().minusDays(1))
                .termoCautela("termo")
                .build()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("anterior");
        assertThatThrownBy(() -> emprestimo.validar(new Emprestimo.Builder()
                .epi(epi(10))
                .beneficiario(pessoa(3))
                .responsavelEntrega(pessoa(2))
                .retiradaEm(RETIRADA)
                .devolucaoPrevistaPara(PREVISTA)
                .termoCautela(" ")
                .build()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Termo");
    }

    @Test
    @DisplayName("Reconstrução exige coerência entre status e devolução")
    void reconstrucaoExigeCoerencia() {
        assertThatThrownBy(() -> emprestimo.validar(EmprestimoDeTeste.umEmprestimo()
                .comId(1)
                .comEpi(epi(10))
                .comBeneficiario(pessoa(3))
                .comResponsavelEntrega(pessoa(2))
                .retiradoEm(RETIRADA)
                .comDevolucaoPrevistaPara(PREVISTA)
                .comTermoCautela("termo")
                .devolvidoEm(RETIRADA.plusDays(1), pessoa(5), EstadoDevolucao.APTO)
                .comStatus(ClassificacaoEmprestimo.INDISPONIVEL)
                .build()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> emprestimo.validar(EmprestimoDeTeste.umEmprestimo()
                .comId(1)
                .comEpi(epi(10))
                .comBeneficiario(pessoa(3))
                .comResponsavelEntrega(pessoa(2))
                .retiradoEm(RETIRADA)
                .comDevolucaoPrevistaPara(PREVISTA)
                .comTermoCautela("termo")
                .comStatus(ClassificacaoEmprestimo.DISPONIVEL)
                .build()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> emprestimo.validar(EmprestimoDeTeste.umEmprestimo()
                .comId(1)
                .comEpi(epi(10))
                .comBeneficiario(pessoa(3))
                .comResponsavelEntrega(pessoa(2))
                .retiradoEm(RETIRADA)
                .comDevolucaoPrevistaPara(PREVISTA)
                .comTermoCautela("termo")
                .devolvidoEm(RETIRADA.plusDays(1), pessoa(5), null)
                .comStatus(ClassificacaoEmprestimo.DISPONIVEL)
                .build()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

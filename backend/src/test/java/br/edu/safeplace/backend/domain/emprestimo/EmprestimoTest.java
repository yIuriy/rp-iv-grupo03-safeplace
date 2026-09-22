/*
 * Testes unitários do domínio Emprestimo (issue #106): invariantes de construção, entrega,
 * devolução com estado, classificação conforme o enumerador do diagrama e detecção de atraso.
 */
package br.edu.safeplace.backend.domain.emprestimo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;

class EmprestimoTest {

    private static final LocalDateTime RETIRADA = LocalDateTime.of(2026, 9, 21, 9, 30);
    private static final LocalDate PREVISTA = LocalDate.of(2026, 9, 28);

    private Emprestimo entregaPadrao() {
        return Emprestimo.registrarEntrega(10, 3, 2, RETIRADA, PREVISTA, "Termo de cautela de teste", null);
    }

    @Test
    @DisplayName("Entrega nasce ativa, INDISPONIVEL, com data/hora, responsável e termo de cautela")
    void entregaNasceAtivaEIndisponivel() {
        Emprestimo emprestimo = entregaPadrao();

        assertThat(emprestimo.getStatus()).isEqualTo(ClassificacaoEmprestimo.INDISPONIVEL);
        assertThat(emprestimo.estaAtivo()).isTrue();
        assertThat(emprestimo.getDataRetirada()).isEqualTo(RETIRADA);
        assertThat(emprestimo.getResponsavelEntregaId()).isEqualTo(2);
        assertThat(emprestimo.getColaboradorId()).isEqualTo(3);
        assertThat(emprestimo.getEpiId()).isEqualTo(10);
        assertThat(emprestimo.getTermoCautela()).isEqualTo("Termo de cautela de teste");
        assertThat(emprestimo.getDataDevolucaoReal()).isNull();
        assertThat(emprestimo.getEstadoDevolucao()).isNull();
    }

    @Test
    @DisplayName("Dentro do prazo a classificação é INDISPONIVEL; após a data prevista, DEVOLUCAO_ATRASADA")
    void classificacaoMudaParaAtrasadaAposPrazo() {
        Emprestimo emprestimo = entregaPadrao();

        assertThat(emprestimo.classificarEm(PREVISTA)).isEqualTo(ClassificacaoEmprestimo.INDISPONIVEL);
        assertThat(emprestimo.estaAtrasadoEm(PREVISTA)).isFalse();
        assertThat(emprestimo.classificarEm(PREVISTA.plusDays(1))).isEqualTo(ClassificacaoEmprestimo.DEVOLUCAO_ATRASADA);
        assertThat(emprestimo.estaAtrasadoEm(PREVISTA.plusDays(1))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(EstadoDevolucao.class)
    @DisplayName("Devolução encerra o empréstimo (DISPONIVEL), guarda estado, responsável e data/hora")
    void devolucaoEncerraEGuardaEstado(EstadoDevolucao estado) {
        Emprestimo emprestimo = entregaPadrao();
        LocalDateTime devolucao = RETIRADA.plusDays(3);

        emprestimo.devolver(estado, 5, devolucao);

        assertThat(emprestimo.getStatus()).isEqualTo(ClassificacaoEmprestimo.DISPONIVEL);
        assertThat(emprestimo.estaAtivo()).isFalse();
        assertThat(emprestimo.getEstadoDevolucao()).isEqualTo(estado);
        assertThat(emprestimo.getResponsavelDevolucaoId()).isEqualTo(5);
        assertThat(emprestimo.getDataDevolucaoReal()).isEqualTo(devolucao);
        assertThat(emprestimo.estaAtrasadoEm(PREVISTA.plusYears(1))).isFalse();
        assertThat(emprestimo.classificarEm(PREVISTA.plusYears(1))).isEqualTo(ClassificacaoEmprestimo.DISPONIVEL);
    }

    @Test
    @DisplayName("Só o estado APTO devolve o item ao estoque disponível")
    void apenasAptoRetornaAoEstoque() {
        assertThat(EstadoDevolucao.APTO.retornaAoEstoque()).isTrue();
        assertThat(EstadoDevolucao.MANUTENCAO.retornaAoEstoque()).isFalse();
        assertThat(EstadoDevolucao.DESCARTE.retornaAoEstoque()).isFalse();
    }

    @Test
    @DisplayName("Segunda devolução do mesmo empréstimo é rejeitada")
    void naoDevolveDuasVezes() {
        Emprestimo emprestimo = entregaPadrao();
        emprestimo.devolver(EstadoDevolucao.APTO, 5, RETIRADA.plusDays(1));

        assertThatThrownBy(() -> emprestimo.devolver(EstadoDevolucao.APTO, 5, RETIRADA.plusDays(2)))
                .isInstanceOf(EmprestimoJaEncerradoException.class);
    }

    @Test
    @DisplayName("Devolução anterior à retirada e sem estado são rejeitadas sem alterar o empréstimo")
    void devolucaoInvalidaNaoAltera() {
        Emprestimo emprestimo = entregaPadrao();

        assertThatThrownBy(() -> emprestimo.devolver(EstadoDevolucao.APTO, 5, RETIRADA.minusMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> emprestimo.devolver(null, 5, RETIRADA.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(emprestimo.estaAtivo()).isTrue();
        assertThat(emprestimo.getDataDevolucaoReal()).isNull();
    }

    @Test
    @DisplayName("Construção exige EPI, colaborador, responsável, datas coerentes e termo de cautela")
    void construcaoValidaObrigatorios() {
        assertThatThrownBy(() -> Emprestimo.registrarEntrega(null, 3, 2, RETIRADA, PREVISTA, "termo", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("EPI");
        assertThatThrownBy(() -> Emprestimo.registrarEntrega(10, null, 2, RETIRADA, PREVISTA, "termo", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Colaborador");
        assertThatThrownBy(() -> Emprestimo.registrarEntrega(10, 3, null, RETIRADA, PREVISTA, "termo", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Responsável");
        assertThatThrownBy(() -> Emprestimo.registrarEntrega(10, 3, 2, RETIRADA, RETIRADA.toLocalDate().minusDays(1), "termo", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("anterior");
        assertThatThrownBy(() -> Emprestimo.registrarEntrega(10, 3, 2, RETIRADA, PREVISTA, " ", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Termo");
    }

    @Test
    @DisplayName("Reconstrução exige coerência entre status e devolução")
    void reconstrucaoExigeCoerencia() {
        assertThatThrownBy(() -> new Emprestimo(1, 10, 3, 2, RETIRADA, PREVISTA, "termo", null,
                RETIRADA.plusDays(1), 5, EstadoDevolucao.APTO, ClassificacaoEmprestimo.INDISPONIVEL))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Emprestimo(1, 10, 3, 2, RETIRADA, PREVISTA, "termo", null,
                null, null, null, ClassificacaoEmprestimo.DISPONIVEL))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Emprestimo(1, 10, 3, 2, RETIRADA, PREVISTA, "termo", null,
                RETIRADA.plusDays(1), 5, null, ClassificacaoEmprestimo.DISPONIVEL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package br.edu.safeplace.backend.validacao.projecao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.exception.DadosInsuficientesParaProjecaoException;

/**
 * Pré-condições da projeção (issue #267). Os 5 primeiros casos vieram de
 * {@code SubstituicaoInteligenteServiceTest} com as mesmas asserções: antes, quem lançava era o
 * cálculo; agora é a cadeia, executada antes dele.
 */
@DisplayName("ProjecaoValidador - pré-condições da projeção de substituição (Chain of Responsibility)")
class ProjecaoValidadorTest {

    private ProjecaoValidador validador;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        validador = new ProjecaoValidador();
        hoje = LocalDate.of(2026, 9, 24);
    }

    @Test
    @DisplayName("Deve lançar exceção quando EPI não possui durabilidade nominal cadastrada")
    void deveFalharQuandoEpiNaoPossuiDurabilidadeNominal() {
        Epi epiSemVidaUtil = criarEpi(3, "Luva de Raspa", null, hoje.plusYears(1), 5, 1);
        List<Emprestimo> emprestimos = List.of(criarEmprestimo(3, 3, hoje.minusDays(5).atStartOfDay(), null));

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> validador.validar(new ContextoProjecao(epiSemVidaUtil, NivelPerigo.MEDIO, emprestimos, hoje, 90))
        );

        assertEquals(3, ex.getEpiId());
        assertTrue(ex.getMessage().contains("durabilidade nominal"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando EPI não possui CA cadastrado")
    void deveFalharQuandoEpiNaoPossuiCaValido() {
        Epi epiMock = org.mockito.Mockito.mock(Epi.class);
        org.mockito.Mockito.when(epiMock.getId()).thenReturn(4);
        org.mockito.Mockito.when(epiMock.getNome()).thenReturn("Máscara PFF2");
        org.mockito.Mockito.when(epiMock.getVidaUtilDias()).thenReturn(120);
        org.mockito.Mockito.when(epiMock.getDataValidadeCa()).thenReturn(null);

        List<Emprestimo> emprestimos = List.of(criarEmprestimo(4, 4, hoje.minusDays(5).atStartOfDay(), null));

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> validador.validar(new ContextoProjecao(epiMock, NivelPerigo.BAIXO, emprestimos, hoje, 90))
        );

        assertEquals(4, ex.getEpiId());
        assertTrue(ex.getMessage().contains("Certificado de Aprovação"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando histórico de empréstimos for vazio")
    void deveFalharQuandoHistoricoEmprestimosForVazio() {
        Epi epi = criarEpi(5, "Protetor Auditivo", 180, hoje.plusYears(1), 10, 2);

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> validador.validar(new ContextoProjecao(epi, NivelPerigo.BAIXO, List.of(), hoje, 90))
        );

        assertEquals(5, ex.getEpiId());
        assertTrue(ex.getMessage().contains("empréstimo"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nível de perigo do setor for nulo")
    void deveFalharQuandoSetorNulo() {
        Epi epi = criarEpi(6, "Calçado de Segurança", 180, hoje.plusYears(1), 10, 2);
        List<Emprestimo> emprestimos = List.of(criarEmprestimo(6, 6, hoje.minusDays(5).atStartOfDay(), null));

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> validador.validar(new ContextoProjecao(epi, null, emprestimos, hoje, 90))
        );

        assertEquals(6, ex.getEpiId());
        assertTrue(ex.getMessage().contains("nível de periculosidade"));
    }

    @Test
    @DisplayName("#199 / DS10 2.3: CA vencido não gera estimativa, gera pendência")
    void caVencidoGeraPendencia() {
        Epi epi = criarEpi(13, "Respirador", 180, hoje.minusDays(1), 10, 2);
        List<Emprestimo> emprestimos = List.of(criarEmprestimo(13, 13, hoje.minusDays(5).atStartOfDay(), null));

        DadosInsuficientesParaProjecaoException ex = assertThrows(DadosInsuficientesParaProjecaoException.class,
                () -> validador.validar(new ContextoProjecao(epi, NivelPerigo.BAIXO, emprestimos, hoje, 90)));

        assertTrue(ex.getMessage().contains("venceu em " + hoje.minusDays(1)));
    }

    @Test
    @DisplayName("Aceita EPI com vida útil, setor, CA válido e histórico")
    void aceitaContextoCompleto() {
        Epi epi = criarEpi(7, "Capacete", 180, hoje.plusYears(1), 10, 2);
        List<Emprestimo> emprestimos = List.of(criarEmprestimo(7, 7, hoje.minusDays(5).atStartOfDay(), null));

        assertThatCode(() -> validador.validar(new ContextoProjecao(epi, NivelPerigo.MEDIO, emprestimos, hoje, 90)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Lista todas as pendências na ordem antiga; validar lança a primeira, como o cálculo fazia")
    void listaPendenciasNaOrdemDosElos() {
        Epi epi = criarEpi(8, "Luva", null, null, 10, 2);
        ContextoProjecao contexto = new ContextoProjecao(epi, null, List.of(), hoje, 90);

        assertThat(validador.verificar(contexto).mensagens()).containsExactly(
                "EPI 'Luva' (ID 8) não possui durabilidade nominal (vida útil em dias) cadastrada.",
                "EPI 'Luva' (ID 8) não possui setor com nível de periculosidade definido.",
                "EPI 'Luva' (ID 8) não possui Certificado de Aprovação (CA) com validade cadastrada.",
                "EPI 'Luva' (ID 8) não possui registros de empréstimo para apuração da frequência de uso.");
        assertThatThrownBy(() -> validador.validar(contexto))
                .isInstanceOf(DadosInsuficientesParaProjecaoException.class)
                .hasMessageContaining("durabilidade nominal");
    }

    @Test
    @DisplayName("Empréstimo com retirada depois da data de referência não conta no histórico")
    void emprestimoFuturoNaoContaNoHistorico() {
        Epi epi = criarEpi(9, "Óculos", 180, hoje.plusYears(1), 10, 2);
        List<Emprestimo> emprestimos = List.of(criarEmprestimo(9, 9, hoje.plusDays(3).atStartOfDay(), null));

        assertThatThrownBy(() -> validador.validar(new ContextoProjecao(epi, NivelPerigo.BAIXO, emprestimos, hoje, 90)))
                .isInstanceOf(DadosInsuficientesParaProjecaoException.class)
                .hasMessageContaining("registros de empréstimo");
    }

    @Test
    @DisplayName("Sem EPI, interrompe a cadeia com IllegalArgumentException")
    void semEpiInterrompe() {
        assertThatThrownBy(() -> validador.validar(new ContextoProjecao(null, NivelPerigo.BAIXO, List.of(), hoje, 90)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("EPI é obrigatório para a projeção.");
    }

    private Epi criarEpi(Integer id, String nome, Integer vidaUtilDias, LocalDate validadeCa,
                         int quantidade, int estoqueMinimo) {
        return new Epi(id, nome, "12345", quantidade, estoqueMinimo, StatusEpi.DISPONIVEL,
                validadeCa, vidaUtilDias, "Descrição", ClassificacaoEPI.PROTECAO_DE_CABECA, "Setor A");
    }

    private Emprestimo criarEmprestimo(Integer id, Integer epiId, LocalDateTime retirada, LocalDateTime devolucao) {
        ClassificacaoEmprestimo status = devolucao != null ? ClassificacaoEmprestimo.DISPONIVEL : ClassificacaoEmprestimo.INDISPONIVEL;
        EstadoDevolucao estadoDev = devolucao != null ? EstadoDevolucao.APTO : null;
        Integer respDev = devolucao != null ? 1 : null;
        return new Emprestimo(id, epiId, 10, 1, retirada,
                retirada.toLocalDate().plusDays(15), "Termo #1", null, devolucao, respDev, estadoDev, status);
    }
}

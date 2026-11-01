package br.edu.safeplace.backend.validacao.epi;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.epi.AlertaSubstituicaoEpi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;

/**
 * Movimentação, manutenção, projeção e alerta (issue #264). Os casos de recusa vieram de
 * {@code MovimentacaoEstoqueTest}, {@code ManutencaoEpiTest} e {@code ProjecaoSubstituicaoTest} com as
 * mesmas asserções: antes, quem lançava eram os construtores.
 */
@DisplayName("Validadores de movimentação, manutenção, projeção e alerta (Chain of Responsibility)")
class RegistrosEpiValidadoresTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 9, 11, 10, 0);
    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    private final MovimentacaoEstoqueValidador movimentacao = new MovimentacaoEstoqueValidador();
    private final ManutencaoEpiValidador manutencao = new ManutencaoEpiValidador();
    private final ProjecaoSubstituicaoValidador projecao = new ProjecaoSubstituicaoValidador();
    private final AlertaSubstituicaoValidador alerta = new AlertaSubstituicaoValidador();

    @Test
    void deveRejeitarTipoAusente() {
        assertThatThrownBy(() -> movimentacao.validar(new MovimentacaoEstoque(null, 1, null, 5, DATA_HORA, "Reposição")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de movimentação é obrigatório.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void deveRejeitarQuantidadeNaoPositiva(int quantidade) {
        assertThatThrownBy(() -> movimentacao.validar(new MovimentacaoEstoque(
                null, 1, TipoMovimentacao.ENTRADA, quantidade, DATA_HORA, "Reposição")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade movimentada deve ser maior que zero.");
    }

    @Test
    void deveRejeitarDataHoraAusente() {
        assertThatThrownBy(() -> movimentacao.validar(new MovimentacaoEstoque(
                null, 1, TipoMovimentacao.ENTRADA, 5, null, "Reposição")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data e hora da movimentação são obrigatórias.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void deveRejeitarMotivoAusenteOuEmBranco(String motivo) {
        assertThatThrownBy(() -> movimentacao.validar(new MovimentacaoEstoque(
                null, 1, TipoMovimentacao.ENTRADA, 5, DATA_HORA, motivo)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motivo da movimentação é obrigatório.");
    }

    @Test
    void manutencaoValidaCamposObrigatorios() {
        LocalDateTime agora = LocalDateTime.now();

        assertThatCode(() -> manutencao.validar(new ManutencaoEpi(1, 10, agora, TipoManutencao.PREVENTIVA,
                "Desc", ResultadoManutencao.APROVADO, "Resp"))).doesNotThrowAnyException();
        assertThrows(IllegalArgumentException.class, () -> manutencao.validar(
                new ManutencaoEpi(1, null, agora, TipoManutencao.PREVENTIVA, "Desc", ResultadoManutencao.APROVADO, "Resp")));
        assertThrows(IllegalArgumentException.class, () -> manutencao.validar(
                new ManutencaoEpi(1, 10, null, TipoManutencao.PREVENTIVA, "Desc", ResultadoManutencao.APROVADO, "Resp")));
        assertThrows(IllegalArgumentException.class, () -> manutencao.validar(
                new ManutencaoEpi(1, 10, agora, TipoManutencao.PREVENTIVA, "   ", ResultadoManutencao.APROVADO, "Resp")));
        assertThrows(IllegalArgumentException.class, () -> manutencao.validar(
                new ManutencaoEpi(1, 10, agora, TipoManutencao.PREVENTIVA, "Desc", null, "Resp")));
        assertThrows(IllegalArgumentException.class, () -> manutencao.validar(
                new ManutencaoEpi(1, 10, agora, TipoManutencao.PREVENTIVA, "Desc", ResultadoManutencao.APROVADO, "  ")));
    }

    @Test
    void projecaoValidaCamposObrigatorios() {
        assertThatCode(() -> projecao.validar(new ProjecaoSubstituicao(null, 1, hoje, hoje.plusDays(10), "Ok")))
                .doesNotThrowAnyException();
        assertThrows(IllegalArgumentException.class, () ->
                projecao.validar(new ProjecaoSubstituicao(null, null, hoje, hoje.plusDays(10), "Ok")));
        assertThrows(IllegalArgumentException.class, () ->
                projecao.validar(new ProjecaoSubstituicao(null, 1, null, hoje.plusDays(10), "Ok")));
        assertThrows(IllegalArgumentException.class, () ->
                projecao.validar(new ProjecaoSubstituicao(null, 1, hoje, null, "Ok")));
        assertThrows(IllegalArgumentException.class, () ->
                projecao.validar(new ProjecaoSubstituicao(null, 1, hoje, hoje.plusDays(10), "   ")));
    }

    @Test
    void alertaValidaCamposObrigatorios() {
        assertThatCode(() -> alerta.validar(AlertaSubstituicaoEpi.estoqueCritico(1, "Luva", 1, 5)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> alerta.validar(AlertaSubstituicaoEpi.estoqueCritico(null, "Luva", 1, 5)))
                .hasMessage("Identificador do EPI é obrigatório no alerta.");
        assertThatThrownBy(() -> alerta.validar(AlertaSubstituicaoEpi.estoqueCritico(1, " ", 1, 5)))
                .hasMessage("Nome do EPI é obrigatório no alerta.");
    }
}

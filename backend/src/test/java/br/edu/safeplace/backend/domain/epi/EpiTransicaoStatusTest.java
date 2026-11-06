package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import br.edu.safeplace.backend.domain.epi.exception.TransicaoStatusEpiInvalidaException;

/** Papel Context do State (#224): o {@link Epi} só muda de situação por {@code transicionarPara}. */
class EpiTransicaoStatusTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 6);

    private static Epi epi(StatusEpi status, int quantidade) {
        return EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Luva")
                .comCa("12345")
                .comQuantidade(quantidade)
                .comEstoqueMinimo(1)
                .comStatus(status)
                .comValidadeCa(HOJE.plusYears(1))
                .comVidaUtilDias(365)
                .build();
    }

    private static ManutencaoEpi manutencao(ResultadoManutencao resultado) {
        return ManutencaoEpi.novo(1, LocalDateTime.of(2026, 11, 6, 10, 0), TipoManutencao.CORRETIVA,
                "Revisão", resultado, "Técnico");
    }

    @Test
    void transicaoPermitidaMudaASituacao() {
        Epi epi = epi(StatusEpi.ESGOTADO, 0);

        epi.transicionarPara(StatusEpi.EM_MANUTENCAO);

        assertThat(epi.getStatus()).isEqualTo(StatusEpi.EM_MANUTENCAO);
    }

    @Test
    void transicaoProibidaNaoMudaASituacao() {
        Epi epi = epi(StatusEpi.EM_MANUTENCAO, 0);

        assertThatThrownBy(() -> epi.transicionarPara(StatusEpi.ESGOTADO))
                .isInstanceOf(TransicaoStatusEpiInvalidaException.class)
                .hasMessage("O EPI com ID 1 não pode passar de EM_MANUTENCAO para ESGOTADO.");
        assertThat(epi.getStatus()).isEqualTo(StatusEpi.EM_MANUTENCAO);
    }

    @ParameterizedTest
    @EnumSource(StatusEpi.class)
    void pedirAMesmaSituacaoNaoMudaNada(StatusEpi status) {
        Epi epi = epi(status, 0);

        epi.transicionarPara(status);

        assertThat(epi.getStatus()).isEqualTo(status);
    }

    @Test
    void devolucaoParaManutencaoDeEpiDescartadoEhRecusada() {
        Epi epi = epi(StatusEpi.DESCARTADO, 0);

        assertThatThrownBy(() -> epi.registrarDevolucaoManutencao(manutencao(ResultadoManutencao.REPROVADO)))
                .isInstanceOf(TransicaoStatusEpiInvalidaException.class);
        assertThat(epi.getStatus()).isEqualTo(StatusEpi.DESCARTADO);
    }

    @Test
    void devolucaoParaManutencaoComSaldoZeradoLevaAManutencao() {
        Epi epi = epi(StatusEpi.ESGOTADO, 0);

        epi.registrarDevolucaoManutencao(manutencao(ResultadoManutencao.REPROVADO));

        assertThat(epi.getStatus()).isEqualTo(StatusEpi.EM_MANUTENCAO);
    }

    @Test
    void devolucaoParaDescarteComSaldoZeradoDescartaOEpiEmManutencao() {
        Epi epi = epi(StatusEpi.EM_MANUTENCAO, 0);

        epi.registrarDevolucaoDescarte();

        assertThat(epi.getStatus()).isEqualTo(StatusEpi.DESCARTADO);
    }

    @Test
    void entradaDeEstoqueNaoTiraOEpiDaManutencao() {
        Epi epi = epi(StatusEpi.EM_MANUTENCAO, 0);

        epi.adicionarEstoque(2, "Reposição");

        assertThat(epi.getStatus()).isEqualTo(StatusEpi.EM_MANUTENCAO);
    }

    @Test
    void registrarManutencaoDeEpiDisponivelPassaPorManutencaoEVoltaSeAprovada() {
        Epi epi = epi(StatusEpi.DISPONIVEL, 3);

        epi.registrarManutencao(manutencao(ResultadoManutencao.APROVADO), HOJE);

        assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        assertThat(epi.obterHistoricoManutencao()).hasSize(1);
    }

    @Test
    void registrarManutencaoReprovadaDeixaOEpiEmManutencao() {
        Epi epi = epi(StatusEpi.DISPONIVEL, 3);

        epi.registrarManutencao(manutencao(ResultadoManutencao.REPROVADO), HOJE);

        assertThat(epi.getStatus()).isEqualTo(StatusEpi.EM_MANUTENCAO);
    }
}

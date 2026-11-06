package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.usuario.Colaborador;

import static org.assertj.core.api.Assertions.assertThat;

class MovimentacaoEstoqueTest {

    private static final Epi EPI = EpiDeTeste.umEpi()
            .comId(1)
            .comNome("Luva")
            .comCa("12345")
            .comQuantidade(10)
            .comEstoqueMinimo(1)
            .comStatus(StatusEpi.DISPONIVEL)
            .comValidadeCa(LocalDate.of(2030, 1, 1))
            .comVidaUtilDias(365)
            .build();

    private static final LocalDateTime DATA_HORA =
            LocalDateTime.of(2026, 9, 11, 10, 0);

    @ParameterizedTest
    @EnumSource(
            value = TipoMovimentacao.class,
            names = {"ENTRADA", "SAIDA"}
    )
    void deveCriarMovimentacaoValida(TipoMovimentacao tipo) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null, EPI, null, null, tipo, 5, DATA_HORA, "Reposição de equipamentos"
        );

        assertThat(movimentacao.getId()).isNull();
        assertThat(movimentacao.getEpiId()).isEqualTo(1);
        assertThat(movimentacao.getTipo()).isEqualTo(tipo);
        assertThat(movimentacao.getQuantidade()).isEqualTo(5);
        assertThat(movimentacao.getDataHora()).isEqualTo(DATA_HORA);
        assertThat(movimentacao.getMotivo())
                .isEqualTo("Reposição de equipamentos");
    }

    @Test
    void epiLoteEResponsavelSaoAssociacoesEOsIdsVemDelas() {
        LoteEPI lote = new LoteEPI(4, "L-1", null, null, null, 10, new ModeloEPI(2, 12345, null, DATA_HORA.toLocalDate()));
        Colaborador responsavel = new Colaborador(
                8, "52998224725", "Ana", LocalDate.of(1990, 5, 10), null, true, DATA_HORA, DATA_HORA);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null, EPI, lote, responsavel, TipoMovimentacao.SAIDA, 1, DATA_HORA, "Empréstimo");

        assertThat(movimentacao.getEpi()).isSameAs(EPI);
        assertThat(movimentacao.getEpiId()).isEqualTo(1);
        assertThat(movimentacao.getLoteId()).isEqualTo(4);
        assertThat(movimentacao.getResponsavelId()).isEqualTo(8);
    }

    @Test
    void semLoteNemResponsavelOsIdsSaoNulos() {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null, EPI, null, null, TipoMovimentacao.ENTRADA, 1, DATA_HORA, "Reposição");

        assertThat(movimentacao.getLoteId()).isNull();
        assertThat(movimentacao.getResponsavelId()).isNull();
    }
}

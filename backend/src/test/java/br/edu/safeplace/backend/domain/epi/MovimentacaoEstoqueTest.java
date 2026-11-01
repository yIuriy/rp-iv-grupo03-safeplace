package br.edu.safeplace.backend.domain.epi;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MovimentacaoEstoqueTest {

    private static final LocalDateTime DATA_HORA =
            LocalDateTime.of(2026, 9, 11, 10, 0);

    @ParameterizedTest
    @EnumSource(
            value = TipoMovimentacao.class,
            names = {"ENTRADA", "SAIDA"}
    )
    void deveCriarMovimentacaoValida(TipoMovimentacao tipo) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null, 1, tipo, 5, DATA_HORA, "Reposição de equipamentos"
        );

        assertThat(movimentacao.getId()).isNull();
        assertThat(movimentacao.getEpiId()).isEqualTo(1);
        assertThat(movimentacao.getTipo()).isEqualTo(tipo);
        assertThat(movimentacao.getQuantidade()).isEqualTo(5);
        assertThat(movimentacao.getDataHora()).isEqualTo(DATA_HORA);
        assertThat(movimentacao.getMotivo())
                .isEqualTo("Reposição de equipamentos");
    }
}
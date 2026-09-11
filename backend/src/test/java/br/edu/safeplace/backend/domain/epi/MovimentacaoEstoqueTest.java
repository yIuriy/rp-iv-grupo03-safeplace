package br.edu.safeplace.backend.domain.epi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void deveRejeitarTipoAusente() {
        assertThatThrownBy(() -> new MovimentacaoEstoque(
                null, 1, null, 5, DATA_HORA, "Reposição"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de movimentação é obrigatório.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void deveRejeitarQuantidadeNaoPositiva(int quantidade) {
        assertThatThrownBy(() -> new MovimentacaoEstoque(
                null, 1, TipoMovimentacao.ENTRADA,
                quantidade, DATA_HORA, "Reposição"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade movimentada deve ser maior que zero.");
    }

    @Test
    void deveRejeitarDataHoraAusente() {
        assertThatThrownBy(() -> new MovimentacaoEstoque(
                null, 1, TipoMovimentacao.ENTRADA, 5, null, "Reposição"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data e hora da movimentação são obrigatórias.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void deveRejeitarMotivoAusenteOuEmBranco(String motivo) {
        assertThatThrownBy(() -> new MovimentacaoEstoque(
                null, 1, TipoMovimentacao.ENTRADA,
                5, DATA_HORA, motivo
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motivo da movimentação é obrigatório.");
    }
}
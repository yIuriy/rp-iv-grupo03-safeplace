package br.edu.safeplace.backend.domain.epi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.epi.exception.EspecificacaoEPI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EspecificacaoEPITest {

    @Test
    void devePreservarDadosDaEspecificacao() {
        EspecificacaoEPI especificacao = new EspecificacaoEPI(
                "Óculos de proteção",
                5,
                ClassificacaoEPI.PROTECAO_DE_OLHOS
        );

        assertThat(especificacao.getDescricao())
                .isEqualTo("Óculos de proteção");

        assertThat(especificacao.getQuantidadeMinima())
                .isEqualTo(5);

        assertThat(especificacao.getClassificacao())
                .isEqualTo(ClassificacaoEPI.PROTECAO_DE_OLHOS);
    }

    @Test
    void devePermitirQuantidadeMinimaZero() {
        EspecificacaoEPI especificacao = new EspecificacaoEPI(
                "Protetor auditivo",
                0,
                ClassificacaoEPI.PROTECAO_AUDITIVA
        );

        assertThat(especificacao.getQuantidadeMinima()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    void deveRejeitarQuantidadeMinimaNegativa(int quantidadeMinima) {
        assertThatThrownBy(() -> new EspecificacaoEPI(
                "Capacete",
                quantidadeMinima,
                ClassificacaoEPI.PROTECAO_DE_CABECA
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade mínima não pode ser negativa.");
    }
}
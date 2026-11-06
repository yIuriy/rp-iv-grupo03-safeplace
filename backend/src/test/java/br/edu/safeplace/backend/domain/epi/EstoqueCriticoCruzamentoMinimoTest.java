/*
 * Testes da regra de estoque crítico no domínio (issue #123, RF03, UC06 alternativo I, US03
 * regra 3): saldo igual ou inferior à quantidade mínima da EspecificacaoEPI é crítico. Partem do
 * saldo 10 dos critérios da US03 e cruzam o mínimo por saída e por entrada.
 */
package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;

class EstoqueCriticoCruzamentoMinimoTest {

    private static final int SALDO_INICIAL = 10;
    private static final int MINIMO = 5;

    private Epi epiComSaldoDez() {
        return EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Luva nitrílica")
                .comCa("12345")
                .comQuantidade(SALDO_INICIAL)
                .comEstoqueMinimo(MINIMO)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.now().plusYears(1))
                .comVidaUtilDias(180)
                .comDescricao("Luva de proteção química")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES)
                .build();
    }

    @Test
    @DisplayName("Saldo 10 com mínimo 5 começa fora da situação crítica")
    void saldoAcimaDoMinimoNaoEhCritico() {
        Epi epi = epiComSaldoDez();

        assertThat(epi.isEstoqueCritico()).isFalse();
        assertThat(epi.compararComQuantMinima()).isFalse();
        assertThat(epi.buscarEPIsAbaixoDaQuantidadeMinima()).isEmpty();
    }

    @Test
    @DisplayName("Saída que deixa o saldo igual ao mínimo torna o estoque crítico (igualdade conta)")
    void saidaQueIgualaOMinimoTornaCritico() {
        Epi epi = epiComSaldoDez();

        epi.removerEstoque(5, "Entrega para a equipe");

        assertThat(epi.getQuantidade()).isEqualTo(MINIMO);
        assertThat(epi.isEstoqueCritico()).isTrue();
        assertThat(epi.buscarEPIsAbaixoDaQuantidadeMinima()).containsExactly(epi);
    }

    @Test
    @DisplayName("Saída que deixa o saldo abaixo do mínimo mantém o estoque crítico")
    void saidaAbaixoDoMinimoMantemCritico() {
        Epi epi = epiComSaldoDez();

        epi.removerEstoque(4, "Primeira entrega");
        assertThat(epi.isEstoqueCritico()).isFalse();

        epi.removerEstoque(3, "Segunda entrega");

        assertThat(epi.getQuantidade()).isEqualTo(3);
        assertThat(epi.isEstoqueCritico()).isTrue();
    }

    @Test
    @DisplayName("Entrada que leva o saldo acima do mínimo encerra a situação crítica")
    void entradaAcimaDoMinimoDeixaDeSerCritico() {
        Epi epi = epiComSaldoDez();
        epi.removerEstoque(7, "Entregas da semana");
        assertThat(epi.isEstoqueCritico()).isTrue();

        epi.adicionarEstoque(3, "Reposição parcial");

        assertThat(epi.getQuantidade()).isEqualTo(6);
        assertThat(epi.isEstoqueCritico()).isFalse();
    }

    @Test
    @DisplayName("Entrada que leva o saldo exatamente ao mínimo continua crítica")
    void entradaQueApenasIgualaOMinimoContinuaCritica() {
        Epi epi = epiComSaldoDez();
        epi.removerEstoque(8, "Entregas da semana");

        epi.adicionarEstoque(3, "Reposição parcial");

        assertThat(epi.getQuantidade()).isEqualTo(MINIMO);
        assertThat(epi.isEstoqueCritico()).isTrue();
    }

    @Test
    @DisplayName("Saída acima do saldo é bloqueada e não altera saldo nem situação crítica")
    void saidaAcimaDoSaldoNaoAlteraSituacao() {
        Epi epi = epiComSaldoDez();

        assertThatThrownBy(() -> epi.removerEstoque(11, "Tentativa indevida"))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(epi.getQuantidade()).isEqualTo(SALDO_INICIAL);
        assertThat(epi.isEstoqueCritico()).isFalse();
    }

    @Test
    @DisplayName("Mínimo zero: só o saldo zerado é crítico")
    void minimoZeroSoEhCriticoComSaldoZero() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(2)
                .comNome("Protetor auricular")
                .comCa("777")
                .comQuantidade(1)
                .comEstoqueMinimo(0)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.now().plusYears(1))
                .comVidaUtilDias(null)
                .comClassificacao(ClassificacaoEPI.PROTECAO_AUDITIVA)
                .build();

        assertThat(epi.isEstoqueCritico()).isFalse();

        epi.removerEstoque(1, "Última unidade entregue");

        assertThat(epi.getQuantidade()).isZero();
        assertThat(epi.isEstoqueCritico()).isTrue();
        assertThat(epi.getStatus()).isEqualTo(StatusEpi.ESGOTADO);
    }
}

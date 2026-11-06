package br.edu.safeplace.backend.domain.epi;

import static br.edu.safeplace.backend.domain.epi.StatusEpi.DESCARTADO;
import static br.edu.safeplace.backend.domain.epi.StatusEpi.DISPONIVEL;
import static br.edu.safeplace.backend.domain.epi.StatusEpi.EM_MANUTENCAO;
import static br.edu.safeplace.backend.domain.epi.StatusEpi.EM_USO;
import static br.edu.safeplace.backend.domain.epi.StatusEpi.ESGOTADO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/** Tabela de transições do State de {@link StatusEpi} (#224), conferida com UC01, UC06, UC12 e a decisão #124. */
class StatusEpiTest {

    private static final Map<StatusEpi, Set<StatusEpi>> PERMITIDAS = Map.of(
            DISPONIVEL, EnumSet.of(ESGOTADO, EM_MANUTENCAO, DESCARTADO),
            EM_USO, EnumSet.of(DISPONIVEL, ESGOTADO, EM_MANUTENCAO, DESCARTADO),
            EM_MANUTENCAO, EnumSet.of(DISPONIVEL, DESCARTADO),
            ESGOTADO, EnumSet.of(DISPONIVEL, EM_MANUTENCAO, DESCARTADO),
            DESCARTADO, EnumSet.noneOf(StatusEpi.class));

    static Stream<Arguments> todosOsPares() {
        return Stream.of(StatusEpi.values()).flatMap(origem -> Stream.of(StatusEpi.values())
                .map(destino -> Arguments.of(origem, destino, PERMITIDAS.get(origem).contains(destino))));
    }

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @MethodSource("todosOsPares")
    void cadaSituacaoDeclaraOsDestinosPermitidos(StatusEpi origem, StatusEpi destino, boolean permitida) {
        assertThat(origem.podeTransicaoPara(destino)).isEqualTo(permitida);
    }

    @ParameterizedTest
    @EnumSource(StatusEpi.class)
    void nenhumaSituacaoVaiParaNuloNemParaEmUso(StatusEpi origem) {
        assertThat(origem.podeTransicaoPara(null)).isFalse();
        assertThat(origem.podeTransicaoPara(EM_USO)).isFalse();
    }

    @Test
    void descartadoEhTerminal() {
        assertThat(Stream.of(StatusEpi.values())).noneMatch(DESCARTADO::podeTransicaoPara);
    }

    @ParameterizedTest(name = "{0} com saldo {1} -> {2}")
    @CsvSource({
            "DISPONIVEL, 0, ESGOTADO",
            "DISPONIVEL, 3, DISPONIVEL",
            "ESGOTADO, 2, DISPONIVEL",
            "ESGOTADO, 0, ESGOTADO",
            "EM_MANUTENCAO, 0, EM_MANUTENCAO",
            "EM_MANUTENCAO, 5, EM_MANUTENCAO",
            "EM_USO, 0, EM_USO",
            "DESCARTADO, 4, DESCARTADO"
    })
    void soDisponivelEEsgotadoAcompanhamOSaldo(StatusEpi atual, int saldo, StatusEpi esperado) {
        assertThat(atual.aposMudancaDeSaldo(saldo)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @EnumSource(StatusEpi.class)
    void aSituacaoAposOSaldoEhSempreUmaTransicaoPermitida(StatusEpi atual) {
        for (int saldo : new int[] {0, 1}) {
            StatusEpi depois = atual.aposMudancaDeSaldo(saldo);
            assertThat(depois == atual || atual.podeTransicaoPara(depois)).isTrue();
        }
    }
}

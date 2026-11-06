package br.edu.safeplace.backend.domain.epi;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/** Papéis Creator e ConcreteCreator do Factory Method (#225). */
class FabricaEpiPadraoTest {

    private static final LocalDate VALIDADE_CA = LocalDate.of(2030, 1, 1);

    private final FabricaEpi fabrica = new FabricaEpiPadrao();

    private static LoteEPI lote(int quantidade, LocalDate validadeCa) {
        return new LoteEPI(null, "L-1", "NF-1", null, null, quantidade, new ModeloEPI(1234, "Protege", validadeCa));
    }

    @Test
    void criaUmEpiNovoComAEspecificacaoInteira() {
        EspecificacaoEPI especificacao = new EspecificacaoEPI("Lentes incolores", 2, ClassificacaoEPI.PROTECAO_DE_OLHOS);
        LoteEPI lote = lote(10, VALIDADE_CA);

        Epi epi = fabrica.criar("Óculos", especificacao, lote, 10, 365, "Armário 1");

        assertThat(epi.getId()).isNull();
        assertThat(epi.getNome()).isEqualTo("Óculos");
        assertThat(epi.getEspecificacao()).isSameAs(especificacao);
        assertThat(epi.getLote()).isSameAs(lote);
        assertThat(epi.getQuantidade()).isEqualTo(10);
        assertThat(epi.getVidaUtilDias()).isEqualTo(365);
        assertThat(epi.getLocalizacao()).isEqualTo("Armário 1");
    }

    @Test
    void aSituacaoInicialAcompanhaOSaldoRecebido() {
        EspecificacaoEPI especificacao = new EspecificacaoEPI(null, 1, null);

        assertThat(fabrica.criar("Luva", especificacao, lote(5, VALIDADE_CA), 5, null, null).getStatus())
                .isEqualTo(StatusEpi.DISPONIVEL);
        assertThat(fabrica.criar("Luva", especificacao, lote(0, VALIDADE_CA), 0, null, null).getStatus())
                .isEqualTo(StatusEpi.ESGOTADO);
    }

    @Test
    void naoValidaAsRegrasDeCadastro() {
        // CA vencido e mínimo negativo são recusados pelo EpiValidador, antes do repository.
        Epi epi = fabrica.criar(null, new EspecificacaoEPI(null, -1, null), lote(1, LocalDate.of(2000, 1, 1)), 1,
                null, null);

        assertThat(epi.getEstoqueMinimo()).isEqualTo(-1);
        assertThat(epi.getModelo().verificarCA(VALIDADE_CA)).isFalse();
    }

    @Test
    void oEpiNaoTemConstrutorPublicoNemMetodoEstaticoDeCriacao() {
        assertThat(Arrays.stream(Epi.class.getDeclaredConstructors()).map(Constructor::getModifiers))
                .noneMatch(Modifier::isPublic);
        assertThat(Arrays.stream(Epi.class.getDeclaredMethods())
                .filter(metodo -> Modifier.isStatic(metodo.getModifiers()))
                .filter(metodo -> metodo.getReturnType() == Epi.class)
                .map(Method::getName))
                .isEmpty();
    }
}

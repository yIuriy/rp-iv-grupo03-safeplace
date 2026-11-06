package br.edu.safeplace.backend.domain.emprestimo;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.epi;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/** Papéis Builder e Product do padrão Builder (#226). */
class EmprestimoBuilderTest {

    private static final LocalDateTime RETIRADA = LocalDateTime.of(2026, 11, 6, 9, 0);
    private static final LocalDate PREVISTA = LocalDate.of(2026, 11, 20);

    @Test
    void montaAEntregaComCadaDadoNomeado() {
        Emprestimo emprestimo = new Emprestimo.Builder()
                .epi(epi(10))
                .beneficiario(pessoa(3))
                .responsavelEntrega(pessoa(2))
                .retiradaEm(RETIRADA)
                .devolucaoPrevistaPara(PREVISTA)
                .termoCautela("  Termo de cautela  ")
                .justificativaSubstituicao("Reposição")
                .build();

        assertThat(emprestimo.getId()).isNull();
        assertThat(emprestimo.getEpiId()).isEqualTo(10);
        assertThat(emprestimo.getColaboradorId()).isEqualTo(3);
        assertThat(emprestimo.getResponsavelEntregaId()).isEqualTo(2);
        assertThat(emprestimo.getDataRetirada()).isEqualTo(RETIRADA);
        assertThat(emprestimo.getDataDevolucaoPrevista()).isEqualTo(PREVISTA);
        assertThat(emprestimo.getTermoCautela()).isEqualTo("Termo de cautela");
        assertThat(emprestimo.getJustificativaSubstituicao()).isEqualTo("Reposição");
    }

    @Test
    void oEmprestimoNasceAtivoESemDevolucao() {
        Emprestimo emprestimo = new Emprestimo.Builder().epi(epi(10)).retiradaEm(RETIRADA).build();

        assertThat(emprestimo.getStatus()).isEqualTo(ClassificacaoEmprestimo.INDISPONIVEL);
        assertThat(emprestimo.estaAtivo()).isTrue();
        assertThat(emprestimo.getDataDevolucaoReal()).isNull();
        assertThat(emprestimo.getResponsavelDevolucao()).isNull();
        assertThat(emprestimo.getEstadoDevolucao()).isNull();
    }

    @Test
    void oBuilderNaoValida() {
        // A falta de EPI, beneficiário, datas e termo é recusada pelo EmprestimoValidador, antes do repository.
        Emprestimo incompleto = new Emprestimo.Builder().build();

        assertThat(incompleto.getEpi()).isNull();
        assertThat(incompleto.getColaborador()).isNull();
        assertThat(incompleto.getTermoCautela()).isNull();
    }

    @Test
    void cadaBuildCriaUmEmprestimoNovo() {
        Emprestimo.Builder builder = new Emprestimo.Builder().epi(epi(10));

        assertThat(builder.build()).isNotSameAs(builder.build());
    }

    @Test
    void oEmprestimoNaoTemConstrutorPublicoNemCriacaoEstatica() {
        assertThat(Arrays.stream(Emprestimo.class.getDeclaredConstructors()).map(Constructor::getModifiers))
                .noneMatch(Modifier::isPublic);
        assertThat(Arrays.stream(Emprestimo.class.getDeclaredMethods())
                .filter(metodo -> Modifier.isStatic(metodo.getModifiers()))
                .filter(metodo -> metodo.getReturnType() == Emprestimo.class))
                .isEmpty();
    }
}

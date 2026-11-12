package br.edu.safeplace.backend.validacao.capacitacao;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoas;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

/**
 * Os casos vieram de {@code CertificacaoTest} e {@code TreinamentoTest} com as mesmas asserções (issue #265):
 * antes, quem lançava era o construtor ou a fábrica {@code cadastrar}; agora é a cadeia, antes do repository.
 */
@DisplayName("CertificacaoValidador e TreinamentoValidador - cadeias da capacitação (Chain of Responsibility)")
class CapacitacaoValidadoresTest {

    private final CertificacaoValidador certificacao = new CertificacaoValidador();
    private final TreinamentoValidador treinamento = new TreinamentoValidador();

    @Test
    @DisplayName("Deve rejeitar cadastro com data de vencimento no passado ou presente")
    void deveRejeitarCadastroComVencimentoInvalido() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> certificacao.validar(Certificacao.cadastrar("NR-10", hoje.minusDays(1), hoje.minusMonths(1), pessoa(10))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser no passado ou presente no momento do cadastro.");
    }

    @Test
    @DisplayName("Deve rejeitar emissão no futuro")
    void deveRejeitarEmissaoNoFuturo() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> certificacao.validar(new Certificacao(null, "NR-10", hoje.plusYears(1), hoje.plusDays(1), pessoa(10))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de emissão não pode ser no futuro.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar nome nulo ou em branco")
    void deveRejeitarNomeInvalido(String nomeInvalido) {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> certificacao.validar(new Certificacao(null, nomeInvalido, hoje.plusYears(1), hoje.minusMonths(1), pessoa(10))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da certificação é obrigatório.");
    }

    @Test
    @DisplayName("Deve rejeitar certificação sem colaborador vinculado")
    void deveRejeitarSemColaborador() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> certificacao.validar(new Certificacao(null, "NR-10", hoje.plusYears(1), hoje.minusMonths(1), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Colaborador é obrigatório.");
    }

    @Test
    @DisplayName("Emissão posterior ao vencimento é recusada (assert que estava em deveValidarInconsistenciaCronologica)")
    void deveRecusarEmissaoPosteriorAoVencimento() {
        LocalDate hoje = LocalDate.now();
        LocalDate emissao = hoje.minusMonths(1);
        LocalDate vencimento = hoje.plusYears(1);
        assertThatThrownBy(() -> certificacao.validar(new Certificacao(null, "NR-10", vencimento.minusYears(2).plusDays(100), emissao, pessoa(10))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar data de realização no futuro")
    void deveRejeitarDataNoFuturo() {
        LocalDate futuro = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> treinamento.validar(Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", futuro, 4, pessoas(List.of(1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de realização não pode ser no futuro.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar nome nulo ou em branco")
    void treinamentoDeveRejeitarNomeInvalido(String nomeInvalido) {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> treinamento.validar(Treinamento.cadastrar(nomeInvalido, "Conteúdo", "Instrutor", hoje, 4, pessoas(List.of(1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome do treinamento é obrigatório.");
    }

    @Test
    @DisplayName("Deve rejeitar treinamento sem nenhum colaborador")
    void treinamentoDeveRejeitarSemColaborador() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> treinamento.validar(Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", hoje, 4, Collections.emptyList())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Treinamento deve possuir ao menos um colaborador vinculado.");

        assertThatThrownBy(() -> treinamento.validar(Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", hoje, 4, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Treinamento deve possuir ao menos um colaborador vinculado.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    @DisplayName("Validade deve ser de ao menos 1 mês")
    void deveRejeitarValidadeInvalida(int validade) {
        assertThatThrownBy(() -> treinamento.validar(Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", LocalDate.now(), 4, validade, pessoas(List.of(1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Validade do treinamento deve ser de ao menos 1 mês.");
    }

    @Test
    @DisplayName("Exigência precisa de nome e de todos os campos de vínculo")
    void exigenciaValidaNomeEVinculo() {
        ExigenciaCapacitacaoValidador exigencia = new ExigenciaCapacitacaoValidador();
        assertThatThrownBy(() -> exigencia.validar(new ExigenciaCapacitacao(null, " ",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 1, ExigenciaCapacitacao.TipoAlvo.TAREFA)))
                .hasMessage("Nome da capacitação exigida é obrigatório.");
        assertThatThrownBy(() -> exigencia.validar(new ExigenciaCapacitacao(null, "NR-10",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, null, ExigenciaCapacitacao.TipoAlvo.TAREFA)))
                .hasMessage("Todos os campos de vínculo são obrigatórios.");
    }

    @Test
    @DisplayName("No cadastro, o vencimento no futuro vem antes dos demais campos; certificação gravada não é cobrada")
    void vencimentoNoCadastroSoParaCertificacaoNova() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> certificacao.validar(Certificacao.cadastrar(" ", hoje, hoje.minusMonths(1), null)))
                .hasMessage("Data de vencimento não pode ser no passado ou presente no momento do cadastro.");
        org.assertj.core.api.Assertions.assertThatCode(() -> certificacao.validar(
                new Certificacao(5, "NR-10", hoje.minusDays(1), hoje.minusYears(1), pessoa(10)))).doesNotThrowAnyException();
    }
}

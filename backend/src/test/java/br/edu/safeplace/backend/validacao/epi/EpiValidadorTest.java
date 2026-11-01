package br.edu.safeplace.backend.validacao.epi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.LoteEPI;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

/**
 * Os casos de recusa vieram de {@code EpiTest}, {@code EspecificacaoEPITest} e {@code LoteEPITest}
 * com as mesmas asserções (issue #264): antes, quem lançava eram os construtores e as fábricas.
 */
@DisplayName("EpiValidador - cadeia de validação do EPI (Chain of Responsibility)")
class EpiValidadorTest {

    private static final LocalDate DATA_CADASTRO = LocalDate.of(2026, 9, 11);
    private static final LocalDate VALIDADE_CA = DATA_CADASTRO.plusYears(1);

    private final EpiValidador validador = new EpiValidador(() -> DATA_CADASTRO);

    @Test
    @DisplayName("Aceita EPI novo com CA vigente e EPI gravado com CA já vencido")
    void aceitaEpiValido() {
        assertThatCode(() -> validador.validar(Epi.novo("Capacete", "CA-1234", 10, 2,
                VALIDADE_CA, 365, DATA_CADASTRO, null, null))).doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(new Epi(1, "Capacete", "CA-1234", 10, 2,
                StatusEpi.DISPONIVEL, DATA_CADASTRO.minusYears(1), 365, null, null))).doesNotThrowAnyException();
    }

    @Test
    void deveImpedirCadastroComCaVencidoPeloConstrutorPublico() {
        EpiValidador comHoje = new EpiValidador();
        assertThatThrownBy(() -> comHoje.validar(new Epi(
                null, "Capacete", "CA-1234", 10, 2,
                StatusEpi.DISPONIVEL, LocalDate.MIN, 365, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é permitido cadastrar EPI com CA vencido.");
    }

    @Test
    void deveRejeitarCadastroComCaVencido() {
        assertThatThrownBy(() -> validador.validar(Epi.novo(
                "Capacete", "CA-1234", 10, 2,
                DATA_CADASTRO.minusDays(1), 365, DATA_CADASTRO, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é permitido cadastrar EPI com CA vencido.");
    }

    @Test
    void deveRejeitarCadastroSemValidadeDoCa() {
        assertThatThrownBy(() -> validador.validar(Epi.novo(
                "Capacete", "CA-1234", 10, 2,
                null, 365, DATA_CADASTRO, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de validade do CA é obrigatória.");
    }

    @Test
    void deveRejeitarMinimoNegativoNaCriacaoDoEpi() {
        assertThatThrownBy(() -> validador.validar(Epi.novo(
                "Óculos", "1234", 10, -1, VALIDADE_CA, 365, DATA_CADASTRO, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade mínima não pode ser negativa.");
    }

    @Test
    void deveRejeitarNomeNulo() {
        assertThatThrownBy(() -> validador.validar(new Epi(1, null, "1234", 10, 2, StatusEpi.DISPONIVEL,
                VALIDADE_CA, 365, "Capacete de segurança", ClassificacaoEPI.PROTECAO_DE_CABECA)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome do EPI é obrigatório.");
    }

    @Test
    void deveRejeitarStatusNulo() {
        assertThatThrownBy(() -> validador.validar(new Epi(1, "Capacete", "1234", 10, 2, null,
                VALIDADE_CA, 365, "Capacete de segurança", ClassificacaoEPI.PROTECAO_DE_CABECA)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status do EPI é obrigatório.");
    }

    @Test
    void deveValidarCamposObrigatoriosNaConstrucao() {
        assertThrows(IllegalArgumentException.class, () -> validador.validar(
                new Epi(1, "", "CA-111", 5, 1, StatusEpi.DISPONIVEL, VALIDADE_CA, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> validador.validar(
                new Epi(1, "Luva", null, 5, 1, StatusEpi.DISPONIVEL, VALIDADE_CA, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> validador.validar(
                new Epi(1, "Luva", "CA-111", -1, 1, StatusEpi.DISPONIVEL, VALIDADE_CA, null, null, null)));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    @DisplayName("Quantidade mínima negativa (regra que estava em EspecificacaoEPI)")
    void deveRejeitarQuantidadeMinimaNegativa(int quantidadeMinima) {
        assertThatThrownBy(() -> validador.validar(Epi.reconstituir(1, "Capacete", lote(), 10, quantidadeMinima,
                StatusEpi.DISPONIVEL, 365, "Capacete", ClassificacaoEPI.PROTECAO_DE_CABECA, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade mínima não pode ser negativa.");
    }

    @Test
    @DisplayName("Lote sem modelo (regra que estava em LoteEPI)")
    void deveRejeitarLoteSemModelo() {
        LoteEPI semModelo = new LoteEPI("LOTE-001", "NF-123", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), 20, null);

        assertThatThrownBy(() -> validador.validar(Epi.reconstituir(1, "Capacete", semModelo, 10, 2,
                StatusEpi.DISPONIVEL, 365, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Modelo do EPI é obrigatório para o lote.");
    }

    @Test
    @DisplayName("Lista os problemas na ordem antiga: modelo, especificação, campos do EPI e CA vigente")
    void listaProblemasNaOrdemDosElos() {
        Epi epi = Epi.reconstituir(null, " ", lote(), -1, -2, null, null, null, null, null);

        assertThat(validador.verificar(epi).mensagens()).containsExactly(
                "Quantidade mínima não pode ser negativa.",
                "Nome do EPI é obrigatório.",
                "Quantidade em estoque não pode ser negativa.",
                "Status do EPI é obrigatório.");
    }

    private static LoteEPI lote() {
        return new LoteEPI("LOTE-001", "NF-123", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), 20,
                new ModeloEPI(1234, "Marca", VALIDADE_CA));
    }
}

package br.edu.safeplace.backend.validacao.epi;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
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
        assertThatCode(() -> validador.validar(EpiDeTeste.umEpi()
                .comNome("Capacete")
                .comCa("CA-1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comValidadeCa(VALIDADE_CA)
                .comVidaUtilDias(365)
                .build())).doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comCa("CA-1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(DATA_CADASTRO.minusYears(1))
                .comVidaUtilDias(365)
                .build())).doesNotThrowAnyException();
    }

    @Test
    void deveImpedirCadastroComCaVencidoPeloConstrutorPublico() {
        EpiValidador comHoje = new EpiValidador();
        assertThatThrownBy(() -> comHoje.validar(EpiDeTeste.umEpi()
                .comNome("Capacete")
                .comCa("CA-1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.MIN)
                .comVidaUtilDias(365)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é permitido cadastrar EPI com CA vencido.");
    }

    @Test
    void deveRejeitarCadastroComCaVencido() {
        assertThatThrownBy(() -> validador.validar(EpiDeTeste.umEpi()
                .comNome("Capacete")
                .comCa("CA-1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comValidadeCa(DATA_CADASTRO.minusDays(1))
                .comVidaUtilDias(365)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é permitido cadastrar EPI com CA vencido.");
    }

    @Test
    void deveRejeitarCadastroSemValidadeDoCa() {
        assertThatThrownBy(() -> validador.validar(EpiDeTeste.umEpi()
                .comNome("Capacete")
                .comCa("CA-1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comValidadeCa(null)
                .comVidaUtilDias(365)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de validade do CA é obrigatória.");
    }

    @Test
    void deveRejeitarMinimoNegativoNaCriacaoDoEpi() {
        assertThatThrownBy(() -> validador.validar(EpiDeTeste.umEpi()
                .comNome("Óculos")
                .comCa("1234")
                .comQuantidade(10)
                .comEstoqueMinimo(-1)
                .comValidadeCa(VALIDADE_CA)
                .comVidaUtilDias(365)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade mínima não pode ser negativa.");
    }

    @Test
    void deveRejeitarNomeNulo() {
        assertThatThrownBy(() -> validador.validar(EpiDeTeste.umEpi()
                .comId(1)
                .comNome(null)
                .comCa("1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(VALIDADE_CA)
                .comVidaUtilDias(365)
                .comDescricao("Capacete de segurança")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome do EPI é obrigatório.");
    }

    @Test
    void deveRejeitarStatusNulo() {
        assertThatThrownBy(() -> validador.validar(EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comCa("1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comValidadeCa(VALIDADE_CA)
                .comVidaUtilDias(365)
                .comDescricao("Capacete de segurança")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                .semStatus()
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status do EPI é obrigatório.");
    }

    @Test
    void deveValidarCamposObrigatoriosNaConstrucao() {
        assertThrows(IllegalArgumentException.class, () -> validador.validar(
                EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("")
                        .comCa("CA-111")
                        .comQuantidade(5)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(null)
                        .build()));
        assertThrows(IllegalArgumentException.class, () -> validador.validar(
                EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva")
                        .comCa(null)
                        .comQuantidade(5)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(null)
                        .build()));
        assertThrows(IllegalArgumentException.class, () -> validador.validar(
                EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva")
                        .comCa("CA-111")
                        .comQuantidade(-1)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(null)
                        .build()));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    @DisplayName("Quantidade mínima negativa (regra que estava em EspecificacaoEPI)")
    void deveRejeitarQuantidadeMinimaNegativa(int quantidadeMinima) {
        assertThatThrownBy(() -> validador.validar(EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comLote(lote())
                .comQuantidade(10)
                .comEstoqueMinimo(quantidadeMinima)
                .comStatus(StatusEpi.DISPONIVEL)
                .comVidaUtilDias(365)
                .comDescricao("Capacete")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade mínima não pode ser negativa.");
    }

    @Test
    @DisplayName("Lote sem modelo (regra que estava em LoteEPI)")
    void deveRejeitarLoteSemModelo() {
        LoteEPI semModelo = new LoteEPI("LOTE-001", "NF-123", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), 20, null);

        assertThatThrownBy(() -> validador.validar(EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comLote(semModelo)
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comVidaUtilDias(365)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Modelo do EPI é obrigatório para o lote.");
    }

    @Test
    @DisplayName("Lista os problemas na ordem antiga: modelo, especificação, campos do EPI e CA vigente")
    void listaProblemasNaOrdemDosElos() {
        Epi epi = EpiDeTeste.umEpi()
                .comNome(" ")
                .comLote(lote())
                .comQuantidade(-1)
                .comEstoqueMinimo(-2)
                .comVidaUtilDias(null)
                .semStatus()
                .build();

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

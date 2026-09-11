package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;

class EpiTest {

        private static final LocalDate DATA_CADASTRO = LocalDate.of(2026, 9, 11);

        private static final LocalDate VALIDADE_CA = DATA_CADASTRO.plusYears(1);

        @Test
        void deveCriarEpiComDadosValidos() {
                Epi epi = new Epi(1, "Óculos de Proteção", "CA-9988", 10, 2,
                                StatusEpi.DISPONIVEL, LocalDate.of(2028, 1, 1), 365);

                assertEquals(Integer.valueOf(1), epi.getId());
                assertEquals("Óculos de Proteção", epi.getNome());
                assertEquals("9988", epi.getNumeroCa());
                assertEquals(10, epi.getQuantidade());
                assertEquals(2, epi.getEstoqueMinimo());
                assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
                assertFalse(epi.isEstoqueCritico());
        }

        @Test
        void deveCriarNovoEpiComStatusDisponivelQuandoQuantidadeMaiorQueZero() {
                Epi epi = Epi.novo(
                                "Óculos de Proteção", "CA-9988", 10, 2,
                                VALIDADE_CA, 365, DATA_CADASTRO);

                assertNull(epi.getId());
                assertEquals("Óculos de Proteção", epi.getNome());
                assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
        }

        @Test
        void deveCriarNovoEpiComStatusEsgotadoQuandoQuantidadeForZero() {
                Epi epi = Epi.novo(
                                "Luva", "CA-1234", 0, 5,
                                VALIDADE_CA, null, DATA_CADASTRO);

                assertNull(epi.getId());
                assertEquals(StatusEpi.ESGOTADO, epi.getStatus());
        }

        @Test
        void deveAdicionarEstoqueETransitarDeEsgotadoParaDisponivel() {
                Epi epi = new Epi(1, "Luva de Vaqueta", "CA-1234", 0, 5,
                                StatusEpi.ESGOTADO, VALIDADE_CA, null);

                MovimentacaoEstoque mov = epi.adicionarEstoque(15, "Chegada de lote");

                assertEquals(15, epi.getQuantidade());
                assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
                assertEquals(TipoMovimentacao.ENTRADA, mov.getTipo());
                assertEquals(15, mov.getQuantidade());
                assertEquals("Chegada de lote", mov.getMotivo());
        }

        @Test
        void deveRemoverEstoqueETransitarParaEsgotadoAoZerar() {
                Epi epi = new Epi(1, "Protetor Auricular", "CA-5678", 5, 2,
                                StatusEpi.DISPONIVEL, VALIDADE_CA, null);

                MovimentacaoEstoque mov = epi.removerEstoque(5, "Distribuição para equipe");

                assertEquals(0, epi.getQuantidade());
                assertEquals(StatusEpi.ESGOTADO, epi.getStatus());
                assertEquals(TipoMovimentacao.SAIDA, mov.getTipo());
                assertEquals(5, mov.getQuantidade());
                assertTrue(epi.isEstoqueCritico());
        }

        @Test
        void deveLancarExcecaoAoTentarRemoverMaisQueOSaldoDisponivel() {
                Epi epi = new Epi(1, "Capacete", "CA-1111", 10, 3,
                                StatusEpi.DISPONIVEL, VALIDADE_CA, null);

                assertThrows(SaldoInsuficienteException.class, () -> epi.removerEstoque(11, "Retirada excessiva"));
                assertEquals(10, epi.getQuantidade());
        }

        @Test
        void deveDetectarEstoqueCritico() {
                Epi epi = new Epi(1, "Máscara PFF2", "CA-2222", 3, 3,
                                StatusEpi.DISPONIVEL, VALIDADE_CA, null);

                assertTrue(epi.isEstoqueCritico());
        }

        @Test
        void deveValidarCamposObrigatoriosNaConstrucao() {
                assertThrows(IllegalArgumentException.class,
                                () -> new Epi(1, "", "CA-111", 5, 1, StatusEpi.DISPONIVEL, VALIDADE_CA, null));

                assertThrows(IllegalArgumentException.class,
                                () -> new Epi(1, "Luva", null, 5, 1, StatusEpi.DISPONIVEL, VALIDADE_CA, null));

                assertThrows(IllegalArgumentException.class,
                                () -> new Epi(1, "Luva", "CA-111", -1, 1, StatusEpi.DISPONIVEL, VALIDADE_CA, null));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " ", "\t", "\n" })
        void devePreservarSaldoEStatusQuandoEntradaTiverMotivoInvalido(
                        String motivo) {
                Epi epi = new Epi(
                                1, "Luva", "CA-1234", 0, 2,
                                StatusEpi.ESGOTADO, LocalDate.of(2028, 1, 1), 365);

                assertThatThrownBy(() -> epi.adicionarEstoque(5, motivo))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Motivo da movimentação é obrigatório.");

                assertThat(epi.getQuantidade()).isZero();
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.ESGOTADO);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " ", "\t", "\n" })
        void devePreservarSaldoEStatusQuandoSaidaTiverMotivoInvalido(
                        String motivo) {
                Epi epi = new Epi(
                                1, "Luva", "CA-1234", 5, 2,
                                StatusEpi.DISPONIVEL, LocalDate.of(2028, 1, 1), 365);

                assertThatThrownBy(() -> epi.removerEstoque(5, motivo))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Motivo da movimentação é obrigatório.");

                assertThat(epi.getQuantidade()).isEqualTo(5);
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        }

        @Test
        void deveRejeitarEntradaQueUltrapasseLimiteDeInteiro() {
                Epi epi = new Epi(
                                1, "Luva", "CA-1234", Integer.MAX_VALUE, 2,
                                StatusEpi.DISPONIVEL, LocalDate.of(2028, 1, 1), 365);

                assertThatThrownBy(() -> epi.adicionarEstoque(1, "Reposição"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Entrada excede o limite de quantidade do estoque.");

                assertThat(epi.getQuantidade()).isEqualTo(Integer.MAX_VALUE);
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        }

        @Test
        void devePermitirEntradaQueAtinjaLimiteDeInteiro() {
                Epi epi = new Epi(
                                1, "Luva", "CA-1234", Integer.MAX_VALUE - 1, 2,
                                StatusEpi.DISPONIVEL, LocalDate.of(2028, 1, 1), 365);

                MovimentacaoEstoque movimentacao = epi.adicionarEstoque(1, "Reposição");

                assertThat(epi.getQuantidade()).isEqualTo(Integer.MAX_VALUE);
                assertThat(movimentacao.getQuantidade()).isEqualTo(1);
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, Integer.MIN_VALUE })
        void deveRejeitarQuantidadesNaoPositivasSemAlterarEstoque(int quantidade) {
                Epi epi = new Epi(
                                1, "Luva", "CA-1234", 5, 2,
                                StatusEpi.DISPONIVEL, LocalDate.of(2028, 1, 1), 365);

                assertThatThrownBy(() -> epi.adicionarEstoque(quantidade, "Reposição"))
                                .isInstanceOf(IllegalArgumentException.class);

                assertThatThrownBy(() -> epi.removerEstoque(quantidade, "Distribuição"))
                                .isInstanceOf(IllegalArgumentException.class);

                assertThat(epi.getQuantidade()).isEqualTo(5);
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        }

        @Test
        void deveCadastrarEpiComCertificadoValido() {
                Epi epi = Epi.novo(
                                "Capacete", "CA-1234", 10, 2,
                                VALIDADE_CA, 365, DATA_CADASTRO);

                assertThat(epi.getId()).isNull();
                assertThat(epi.getCertificadoAprovacao()).isEqualTo(
                                new CertificadoAprovacao("1234", VALIDADE_CA));
                assertThat(epi.getNumeroCa()).isEqualTo("1234");
                assertThat(epi.getDataValidadeCa()).isEqualTo(VALIDADE_CA);
        }

        @Test
        void deveRejeitarCadastroComCaVencido() {
                assertThatThrownBy(() -> Epi.novo(
                                "Capacete", "CA-1234", 10, 2,
                                DATA_CADASTRO.minusDays(1), 365, DATA_CADASTRO))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Não é permitido cadastrar EPI com CA vencido.");
        }

        @Test
        void devePermitirCadastroNoDiaDoVencimento() {
                Epi epi = Epi.novo(
                                "Capacete", "CA-1234", 10, 2,
                                DATA_CADASTRO, 365, DATA_CADASTRO);

                assertThat(epi.getDataValidadeCa()).isEqualTo(DATA_CADASTRO);
        }

        @Test
        void deveRejeitarCadastroSemValidadeDoCa() {
                assertThatThrownBy(() -> Epi.novo(
                                "Capacete", "CA-1234", 10, 2,
                                null, 365, DATA_CADASTRO))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Data de validade do CA é obrigatória.");
        }

        @Test
        void deveRejeitarCadastroComFormatoDeCaInvalido() {
                assertThatThrownBy(() -> Epi.novo(
                                "Capacete", "ABC", 10, 2,
                                VALIDADE_CA, 365, DATA_CADASTRO))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage(
                                                "Número do CA deve conter apenas dígitos, com prefixo CA- opcional.");
        }

        @Test
        void deveReconstruirEpiPersistidoComCaVencido() {
                LocalDate validadeAntiga = LocalDate.of(2000, 1, 1);

                Epi epi = new Epi(
                                1, "Capacete", "CA-1234", 10, 2,
                                StatusEpi.DISPONIVEL, validadeAntiga, 365);

                assertThat(epi.getId()).isEqualTo(1);
                assertThat(epi.getDataValidadeCa()).isEqualTo(validadeAntiga);
                assertThat(epi.getCertificadoAprovacao()
                                .estaVencidoEm(DATA_CADASTRO)).isTrue();
        }

        @Test
        void deveImpedirCadastroComCaVencidoPeloConstrutorPublico() {
                assertThatThrownBy(() -> new Epi(
                                null, "Capacete", "CA-1234", 10, 2,
                                StatusEpi.DISPONIVEL, LocalDate.MIN, 365))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Não é permitido cadastrar EPI com CA vencido.");
        }

        @Test
        void deveArmazenarQuantidadeMinimaNaEspecificacao() {
                Epi epi = Epi.novo(
                                "Óculos",
                                "1234",
                                10,
                                5,
                                VALIDADE_CA,
                                365,
                                DATA_CADASTRO);

                assertThat(epi.getEspecificacao()).isNotNull();
                assertThat(epi.getEspecificacao().getQuantidadeMinima())
                                .isEqualTo(5);
                assertThat(epi.getEstoqueMinimo()).isEqualTo(5);
        }

        @ParameterizedTest
        @CsvSource({
                        "4, 5, true",
                        "5, 5, true",
                        "6, 5, false",
                        "0, 0, true"
        })
        void deveCalcularEstoqueCriticoPeloMinimoDaEspecificacao(
                        int quantidade,
                        int minimo,
                        boolean esperado) {
                Epi epi = Epi.novo(
                                "Óculos",
                                "1234",
                                quantidade,
                                minimo,
                                VALIDADE_CA,
                                365,
                                DATA_CADASTRO);

                assertThat(epi.isEstoqueCritico()).isEqualTo(esperado);
        }

        @Test
        void deveRejeitarMinimoNegativoNaCriacaoDoEpi() {
                assertThatThrownBy(() -> Epi.novo(
                                "Óculos",
                                "1234",
                                10,
                                -1,
                                VALIDADE_CA,
                                365,
                                DATA_CADASTRO))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Quantidade mínima não pode ser negativa.");
        }
}

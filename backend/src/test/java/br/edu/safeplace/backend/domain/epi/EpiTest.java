package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Óculos de Proteção")
                        .comCa("CA-9988")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.of(2028, 1, 1))
                        .comVidaUtilDias(365)
                        .build();

                assertEquals(Integer.valueOf(1), epi.getId());
                assertEquals("Óculos de Proteção", epi.getNome());
                assertEquals("9988", epi.getNumeroCa());
                assertEquals(10, epi.getQuantidade());
                assertEquals(2, epi.getEstoqueMinimo());
                assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
                assertFalse(epi.isEstoqueCritico());
        }

        @Test
        void deveAdicionarEstoqueETransitarDeEsgotadoParaDisponivel() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva de Vaqueta")
                        .comCa("CA-1234")
                        .comQuantidade(0)
                        .comEstoqueMinimo(5)
                        .comStatus(StatusEpi.ESGOTADO)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(null)
                        .build();

                MovimentacaoEstoque mov = epi.adicionarEstoque(15, "Chegada de lote");

                assertEquals(15, epi.getQuantidade());
                assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
                assertEquals(TipoMovimentacao.ENTRADA, mov.getTipo());
                assertEquals(15, mov.getQuantidade());
                assertEquals("Chegada de lote", mov.getMotivo());
        }

        @Test
        void deveRemoverEstoqueETransitarParaEsgotadoAoZerar() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Protetor Auricular")
                        .comCa("CA-5678")
                        .comQuantidade(5)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(null)
                        .build();

                MovimentacaoEstoque mov = epi.removerEstoque(5, "Distribuição para equipe");

                assertEquals(0, epi.getQuantidade());
                assertEquals(StatusEpi.ESGOTADO, epi.getStatus());
                assertEquals(TipoMovimentacao.SAIDA, mov.getTipo());
                assertEquals(5, mov.getQuantidade());
                assertTrue(epi.isEstoqueCritico());
        }

        @Test
        void deveLancarExcecaoAoTentarRemoverMaisQueOSaldoDisponivel() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Capacete")
                        .comCa("CA-1111")
                        .comQuantidade(10)
                        .comEstoqueMinimo(3)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(null)
                        .build();

                assertThrows(SaldoInsuficienteException.class, () -> epi.removerEstoque(11, "Retirada excessiva"));
                assertEquals(10, epi.getQuantidade());
        }

        @Test
        void deveDetectarEstoqueCritico() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Máscara PFF2")
                        .comCa("CA-2222")
                        .comQuantidade(3)
                        .comEstoqueMinimo(3)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(null)
                        .build();

                assertTrue(epi.isEstoqueCritico());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " ", "\t", "\n" })
        void devePreservarSaldoEStatusQuandoEntradaTiverMotivoInvalido(
                        String motivo) {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(0)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.ESGOTADO)
                        .comValidadeCa(LocalDate.of(2028, 1, 1))
                        .comVidaUtilDias(365)
                        .build();

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
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(5)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.of(2028, 1, 1))
                        .comVidaUtilDias(365)
                        .build();

                assertThatThrownBy(() -> epi.removerEstoque(5, motivo))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Motivo da movimentação é obrigatório.");

                assertThat(epi.getQuantidade()).isEqualTo(5);
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        }

        @Test
        void deveRejeitarEntradaQueUltrapasseLimiteDeInteiro() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(Integer.MAX_VALUE)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.of(2028, 1, 1))
                        .comVidaUtilDias(365)
                        .build();

                assertThatThrownBy(() -> epi.adicionarEstoque(1, "Reposição"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Entrada excede o limite de quantidade do estoque.");

                assertThat(epi.getQuantidade()).isEqualTo(Integer.MAX_VALUE);
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        }

        @Test
        void devePermitirEntradaQueAtinjaLimiteDeInteiro() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(Integer.MAX_VALUE - 1)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.of(2028, 1, 1))
                        .comVidaUtilDias(365)
                        .build();

                MovimentacaoEstoque movimentacao = epi.adicionarEstoque(1, "Reposição");

                assertThat(epi.getQuantidade()).isEqualTo(Integer.MAX_VALUE);
                assertThat(movimentacao.getQuantidade()).isEqualTo(1);
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, Integer.MIN_VALUE })
        void deveRejeitarQuantidadesNaoPositivasSemAlterarEstoque(int quantidade) {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(5)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.of(2028, 1, 1))
                        .comVidaUtilDias(365)
                        .build();

                assertThatThrownBy(() -> epi.adicionarEstoque(quantidade, "Reposição"))
                                .isInstanceOf(IllegalArgumentException.class);

                assertThatThrownBy(() -> epi.removerEstoque(quantidade, "Distribuição"))
                                .isInstanceOf(IllegalArgumentException.class);

                assertThat(epi.getQuantidade()).isEqualTo(5);
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        }

        @Test
        void deveCadastrarEpiComCertificadoValido() {
                Epi epi = EpiDeTeste.umEpi()
                        .comNome("Capacete")
                        .comCa("CA-1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .build();

                assertThat(epi.getId()).isNull();
                assertThat(epi.getModelo().getCa()).isEqualTo(1234);
                assertThat(epi.getModelo().getValidadeCA()).isEqualTo(VALIDADE_CA);
                assertThat(epi.getNumeroCa()).isEqualTo("1234");
                assertThat(epi.getDataValidadeCa()).isEqualTo(VALIDADE_CA);
        }

        @Test
        void devePermitirCadastroNoDiaDoVencimento() {
                Epi epi = EpiDeTeste.umEpi()
                        .comNome("Capacete")
                        .comCa("CA-1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comValidadeCa(DATA_CADASTRO)
                        .comVidaUtilDias(365)
                        .build();

                assertThat(epi.getDataValidadeCa()).isEqualTo(DATA_CADASTRO);
        }

        @Test
        void deveReconstruirEpiPersistidoComCaVencido() {
                LocalDate validadeAntiga = LocalDate.of(2000, 1, 1);

                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Capacete")
                        .comCa("CA-1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(validadeAntiga)
                        .comVidaUtilDias(365)
                        .build();

                assertThat(epi.getId()).isEqualTo(1);
                assertThat(epi.getDataValidadeCa()).isEqualTo(validadeAntiga);
                assertThat(epi.getModelo().verificarCA(DATA_CADASTRO)).isFalse();
        }

        @Test
        void deveArmazenarQuantidadeMinimaNaEspecificacao() {
                Epi epi = EpiDeTeste.umEpi()
                        .comNome("Óculos")
                        .comCa("1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(5)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .build();

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
                Epi epi = EpiDeTeste.umEpi()
                        .comNome("Óculos")
                        .comCa("1234")
                        .comQuantidade(quantidade)
                        .comEstoqueMinimo(minimo)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .build();

                assertThat(epi.isEstoqueCritico()).isEqualTo(esperado);
        }

        @Test
        void deveManterStatusDisponivelQuandoSaidaNaoZerarEstoque() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Capacete")
                        .comCa("1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .comDescricao("Capacete de segurança")
                        .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                        .build();

                epi.removerEstoque(4, "Distribuição operacional");

                assertThat(epi.getQuantidade()).isEqualTo(6);
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        }

        @Test
        void deveManterStatusQuandoSaidaZeraEstoqueDeEpiEmUso() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Capacete")
                        .comCa("1234")
                        .comQuantidade(5)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.EM_USO)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .comDescricao("Capacete de segurança")
                        .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                        .build();

                epi.removerEstoque(5, "Baixa operacional");

                assertThat(epi.getQuantidade()).isZero();
                assertThat(epi.getStatus()).isEqualTo(StatusEpi.EM_USO);
        }

        // @Test
        // void devePermitirAlterarLocalizacao() {
        //         Epi epi = EpiDeTeste.umEpi().comId(102).comLocalizacao("Setor A").comQuantidade(20).comEstoqueMinimo(0).comStatus(StatusEpi.DISPONIVEL).comVidaUtilDias(null).build();
        //         epi.setLocalizacao("Setor B");

        //         assertEquals("Setor B", epi.getLocalizacao());
        // }

        @Test
        void deveBuscarEPIsRetornandoLista() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(10)
                        .comLocalizacao("Estoque 1")
                        .comQuantidade(15)
                        .comEstoqueMinimo(0)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comVidaUtilDias(null)
                        .build();

                List<Epi> lista = epi.buscarEPIs();

                assertThat(lista).containsExactly(epi);
        }

        @Test
        void deveBuscarEpiPorCodigo() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(20)
                        .comLocalizacao("Estoque 2")
                        .comQuantidade(10)
                        .comEstoqueMinimo(0)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comVidaUtilDias(null)
                        .build();

                assertThat(epi.buscarEPI(20)).isSameAs(epi);
                assertThat(epi.buscarEPI(999)).isNull();
        }

        @Test
        void deveObterHistoricoManutencaoERegistrarManutencoes() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Capacete")
                        .comCa("CA-1234")
                        .comQuantidade(5)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .comDescricao("Capacete")
                        .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                        .comLocalizacao("Prateleira 1")
                        .build();

                assertThat(epi.obterHistoricoManutencao()).isEmpty();

                ManutencaoEpi m1 = new ManutencaoEpi.Builder()
                                .epi(epi)
                                .realizadaEm(LocalDateTime.now())
                                .tipo(TipoManutencao.PREVENTIVA)
                                .descricao("Revisão geral")
                                .resultado(ResultadoManutencao.APROVADO)
                                .responsavelManutencao("Técnico Silva")
                                .build();
                epi.adicionarManutencao(m1);

                assertThat(epi.obterHistoricoManutencao()).containsExactly(m1);

                epi.enviarParaManutencao(DATA_CADASTRO);
                ManutencaoEpi m2 = new ManutencaoEpi.Builder()
                                .epi(epi)
                                .realizadaEm(LocalDateTime.now())
                                .tipo(TipoManutencao.CORRETIVA)
                                .descricao("Troca de carneira")
                                .resultado(ResultadoManutencao.APROVADO)
                                .responsavelManutencao("Técnico Silva")
                                .build();
                epi.concluirManutencao(m2);

                assertThat(epi.obterHistoricoManutencao()).containsExactly(m1, m2);
        }

        @Test
        void deveValidarVinculoEpisObrigatorios() {
                Epi epiValido = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Óculos")
                        .comCa("CA-1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .comDescricao("Óculos")
                        .comClassificacao(ClassificacaoEPI.PROTECAO_DE_OLHOS)
                        .comLocalizacao("Armário 1")
                        .build();
                assertTrue(epiValido.validarVinculoEPIsObrigatorios(DATA_CADASTRO));

                Epi epiDescartado = EpiDeTeste.umEpi()
                        .comId(2)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(0)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DESCARTADO)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .comDescricao("Luva")
                        .comClassificacao(ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES)
                        .comLocalizacao("Descarte")
                        .build();
                assertFalse(epiDescartado.validarVinculoEPIsObrigatorios(DATA_CADASTRO));

                Epi epiVencido = EpiDeTeste.umEpi()
                        .comId(3)
                        .comNome("Bota")
                        .comCa("CA-1234")
                        .comQuantidade(5)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.of(2020, 1, 1))
                        .comVidaUtilDias(365)
                        .comDescricao("Bota")
                        .comClassificacao(ClassificacaoEPI.PROTECAO_DE_MEMBROS_INFERIORES)
                        .comLocalizacao("Armário 2")
                        .build();
                assertFalse(epiVencido.validarVinculoEPIsObrigatorios(DATA_CADASTRO));
        }

        @Test
        void deveCompararComQuantidadeMinimaEBuscarAbaixoDaQuantidadeMinima() {
                EspecificacaoEPI esp = new EspecificacaoEPI("Protetor auricular", 10, ClassificacaoEPI.PROTECAO_AUDITIVA);
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Protetor")
                        .comCa("CA-5555")
                        .comQuantidade(8)
                        .comEstoqueMinimo(10)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .comDescricao("Protetor auricular")
                        .comClassificacao(ClassificacaoEPI.PROTECAO_AUDITIVA)
                        .comLocalizacao("Setor Ruído")
                        .build();

                assertTrue(epi.compararComQuantidadeMinima());
                assertTrue(epi.compararComQuantMinima());
                assertThat(epi.buscarEPIsAbaixoDaQuantidadeMinima()).containsExactly(epi);

                epi.atualizarQuantidade(esp, 15);
                assertFalse(epi.compararComQuantidadeMinima());
                assertFalse(epi.compararComQuantMinima());
                assertThat(epi.buscarEPIsAbaixoDaQuantidadeMinima()).isEmpty();
        }

        @Test
        void deveAtualizarQuantidadeETransitarStatus() {
                EspecificacaoEPI novaEsp = new EspecificacaoEPI("Nova spec", 5, ClassificacaoEPI.PROTECAO_DE_CABECA);
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Capacete")
                        .comCa("CA-1111")
                        .comQuantidade(10)
                        .comEstoqueMinimo(5)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .build();

                epi.atualizarQuantidade(novaEsp, 0);
                assertEquals(0, epi.getQuantidade());
                assertEquals(StatusEpi.ESGOTADO, epi.getStatus());
                assertEquals(novaEsp, epi.getEspecificacao());

                epi.atualizarQuantidade(null, 12);
                assertEquals(12, epi.getQuantidade());
                assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());

                assertThatThrownBy(() -> epi.atualizarQuantidade(null, -1))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Quantidade em estoque não pode ser negativa.");
        }

        @Test
        void deveGerenciarEBuscarLotes() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(1)
                        .comNome("Óculos")
                        .comCa("CA-1234")
                        .comQuantidade(20)
                        .comEstoqueMinimo(5)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .build();

                ModeloEPI modelo = new ModeloEPI(9876, "3M", VALIDADE_CA);
                LoteEPI lote1 = new LoteEPI("1001", "NF-1", LocalDate.now(), LocalDate.now().plusYears(2), 10, modelo);
                LoteEPI lote2 = new LoteEPI("LOTE-X", "NF-2", LocalDate.now(), LocalDate.now().plusYears(2), 10, modelo);

                epi.adicionarLote(lote1);
                epi.adicionarLote(lote2);

                assertThat(epi.getLotes()).containsExactly(lote1, lote2);
                assertThat(epi.buscarLote(9876)).isSameAs(lote1); // busca pelo CA do modelo
                assertThat(epi.buscarLote(1001)).isSameAs(lote1); // busca pelo número do lote
                assertThat(epi.buscarLote(5555)).isNull();
        }

        @Test
        void deveValidarBaixaComSucessoERejeitarCasosInvalidos() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(5)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .build();

                int saldoRestante = epi.validarBaixa(5, 3);
                assertEquals(7, saldoRestante);

                assertThatThrownBy(() -> epi.validarBaixa(999, 3))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Código do EPI informado não corresponde ao EPI atual.");

                assertThatThrownBy(() -> epi.validarBaixa(5, 0))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Quantidade para baixa deve ser maior que zero.");

                assertThatThrownBy(() -> epi.validarBaixa(5, 15))
                                .isInstanceOf(SaldoInsuficienteException.class);
        }

        @Test
        void deveDarBaixaComSucessoERecusarQuantidadesInvalidas() {
                Epi epi = EpiDeTeste.umEpi()
                        .comId(5)
                        .comNome("Luva")
                        .comCa("CA-1234")
                        .comQuantidade(10)
                        .comEstoqueMinimo(2)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(VALIDADE_CA)
                        .comVidaUtilDias(365)
                        .build();

                assertFalse(epi.darBaixa(0));
                assertFalse(epi.darBaixa(-2));
                assertFalse(epi.darBaixa(11));
                assertEquals(10, epi.getQuantidade());

                assertTrue(epi.darBaixa(4));
                assertEquals(6, epi.getQuantidade());

                assertTrue(epi.darBaixa(6));
                assertEquals(0, epi.getQuantidade());
                assertEquals(StatusEpi.ESGOTADO, epi.getStatus());
        }
}

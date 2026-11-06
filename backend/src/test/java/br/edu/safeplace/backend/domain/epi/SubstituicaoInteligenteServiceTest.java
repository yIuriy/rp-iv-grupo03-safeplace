package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.epi.exception.DadosInsuficientesParaProjecaoException;

class SubstituicaoInteligenteServiceTest {

    private SubstituicaoInteligenteService service;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        service = new SubstituicaoInteligenteService();
        hoje = LocalDate.of(2026, 9, 24);
    }

    @Test
    @DisplayName("Deve retornar os multiplicadores ambientais corretos para cada NivelPerigo")
    void deveRetornarMultiplicadoresAmbienteCorretos() {
        assertEquals(1.0, service.obterMultiplicadorAmbiente(NivelPerigo.BAIXO));
        assertEquals(1.25, service.obterMultiplicadorAmbiente(NivelPerigo.MEDIO));
        assertEquals(1.5, service.obterMultiplicadorAmbiente(NivelPerigo.ALTO));
        assertEquals(2.0, service.obterMultiplicadorAmbiente(NivelPerigo.CRITICO));
    }

    @Test
    @DisplayName("Deve calcular projeção com desgaste acelerado quando data física é anterior ao CA")
    void deveCalcularProjecaoPorDesgasteAcelerado() {
        // EPI com vida útil de 180 dias e CA válido por 2 anos
        LocalDate validadeCa = hoje.plusYears(2);
        Epi epi = criarEpi(1, "Capacete de Segurança", 180, validadeCa, 10, 2);

        // 30 dias de uso nos últimos 90 dias (taxa U = 30/90 = 0.3333)
        // Ambiente ALTO (fator = 1.5)
        // TD = 0.3333 * 1.5 = 0.50 dias nominais/dia
        // Desgaste consumido = 30 * 1.5 = 45 dias
        // Dias restantes = (180 - 45) / 0.50 = 270 dias
        // Data estimada = hoje + 270 dias (anterior a hoje + 2 anos)
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(1, 1, hoje.minusDays(40).atStartOfDay(), hoje.minusDays(10).atStartOfDay())
        );

        ProjecaoSubstituicao projecao = service.calcularProjecao(new ContextoProjecao(epi, NivelPerigo.ALTO, emprestimos, hoje, 90));

        assertNotNull(projecao);
        assertEquals(1, projecao.getEpiId());
        assertEquals(hoje, projecao.getDataProjecao());
        assertTrue(projecao.getDataEstimadaDescarte().isBefore(validadeCa));
        assertTrue(projecao.getJustificativa().contains("ALTO"));
        assertTrue(projecao.getJustificativa().contains("1,50") || projecao.getJustificativa().contains("1.50"));
    }

    @Test
    @DisplayName("Deve truncar a data estimada de descarte na validade do CA se o desgaste físico for posterior")
    void deveTruncarNaValidadeDoCaQuandoDesgasteForPosterior() {
        // EPI com vida útil de 360 dias, mas CA vencendo em 60 dias
        LocalDate validadeCa = hoje.plusDays(60);
        Epi epi = criarEpi(2, "Óculos de Proteção", 360, validadeCa, 10, 2);

        // 10 dias de uso, ambiente BAIXO (fator 1.0)
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(2, 2, hoje.minusDays(15).atStartOfDay(), hoje.minusDays(5).atStartOfDay())
        );

        ProjecaoSubstituicao projecao = service.calcularProjecao(new ContextoProjecao(epi, NivelPerigo.BAIXO, emprestimos, hoje, 90));

        assertNotNull(projecao);
        assertEquals(validadeCa, projecao.getDataEstimadaDescarte());
        assertTrue(projecao.getJustificativa().contains("Substituição antecipada pelo vencimento legal do CA"));
    }

    @Test
    @DisplayName("Deve emitir alertas de descarte próximo, CA próximo e estoque crítico")
    void deveEmitirAlertasPreventivosCorretamente() {
        // EPI com descarte em 20 dias (<= 30), CA em 15 dias (<= 30) e saldo (2) <= estoqueMinimo (5)
        LocalDate descarteProximo = hoje.plusDays(20);
        LocalDate caProximo = hoje.plusDays(15);
        Epi epi = criarEpi(7, "Cinto Paraquedista", 120, caProximo, 2, 5);

        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(7, hoje, descarteProximo, "Teste");

        List<AlertaSubstituicaoEpi> alertas = service.emitirAlertas(epi, projecao, hoje);

        assertEquals(3, alertas.size());
        assertTrue(alertas.stream().anyMatch(a -> a.getTipoAlerta() == TipoAlertaSubstituicao.ESTOQUE_CRITICO));
        assertTrue(alertas.stream().anyMatch(a -> a.getTipoAlerta() == TipoAlertaSubstituicao.DESCARTE_PROXIMO));
        assertTrue(alertas.stream().anyMatch(a -> a.getTipoAlerta() == TipoAlertaSubstituicao.CA_PROXIMO_VENCIMENTO));
    }

    @Test
    @DisplayName("Não deve emitir alertas quando prazos forem superiores a 30 dias e estoque suficiente")
    void naoDeveEmitirAlertasQuandoCondicoesForemSeguras() {
        LocalDate descarteLonge = hoje.plusDays(100);
        LocalDate caLonge = hoje.plusYears(2);
        Epi epi = criarEpi(8, "Avental de Raspa", 365, caLonge, 20, 5);

        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(8, hoje, descarteLonge, "Teste seguro");

        List<AlertaSubstituicaoEpi> alertas = service.emitirAlertas(epi, projecao, hoje);

        assertTrue(alertas.isEmpty());
    }

    @Test
    @DisplayName("#199: devolução em DESCARTE não antecipa a projeção; a regra não consta do DS10")
    void devolucaoEmDescarteSegueAFormulaDoDs10() {
        // CA distante: 8 dias de uso em 90 projetam ~1530 dias, abaixo do teto do CA
        Epi epi = criarEpi(9, "Luva de Alta Tensão", 180, hoje.plusYears(10), 10, 2);
        LocalDateTime retirada = hoje.minusDays(10).atStartOfDay();
        Emprestimo emprestimoAvariado = new Emprestimo(
                100, 9, 10, 1, retirada,
                retirada.toLocalDate().plusDays(15), "Termo", null,
                hoje.minusDays(2).atStartOfDay(), 1, EstadoDevolucao.DESCARTE, ClassificacaoEmprestimo.DISPONIVEL);

        ProjecaoSubstituicao projecao = service.calcularProjecao(new ContextoProjecao(epi, NivelPerigo.MEDIO, List.of(emprestimoAvariado), hoje, 90));

        assertFalse(projecao.getJustificativa().contains("avaria"));
        assertTrue(projecao.getJustificativa().contains("MEDIO"));
    }

    @Test
    @DisplayName("#199: sem período informado, a amostragem vai da primeira retirada até a referência")
    void periodoDeAmostragemDerivadoDoHistorico() {
        Epi epi = criarEpi(10, "Capacete", 180, hoje.plusYears(2), 10, 2);
        // 30 dias de uso em 40 dias de histórico: U = 0,75; ALTO (1,5): TD = 1,125; desgaste = 45
        // dias restantes = (180 - 45) / 1,125 = 120
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(10, 10, hoje.minusDays(40).atStartOfDay(), hoje.minusDays(10).atStartOfDay()));

        ProjecaoSubstituicao projecao = service.calcularProjecao(new ContextoProjecao(epi, NivelPerigo.ALTO, emprestimos, hoje, null));

        assertEquals(hoje.plusDays(120), projecao.getDataEstimadaDescarte());
        assertTrue(projecao.getJustificativa().contains("75,0%") || projecao.getJustificativa().contains("75.0%"));
    }

    @Test
    @DisplayName("#199: uso baixo não é elevado a um piso fictício de 5%")
    void taxaDeUsoSemPisoFicticio() {
        Epi epi = criarEpi(11, "Avental", 360, hoje.plusYears(500), 10, 2);
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(11, 11, hoje.minusDays(200).atStartOfDay(), hoje.minusDays(199).atStartOfDay()));

        ProjecaoSubstituicao projecao = service.calcularProjecao(new ContextoProjecao(epi, NivelPerigo.BAIXO, emprestimos, hoje, 365));

        // U = 1/365 = 0,3%; com o piso antigo a justificativa mostraria 5,0%
        assertTrue(projecao.getJustificativa().contains("0,3%") || projecao.getJustificativa().contains("0.3%"));
    }

    @Test
    @DisplayName("#199: sem uso no período de amostragem informado, gera pendência em vez de estimativa")
    void semUsoNoPeriodoGeraPendencia() {
        Epi epi = criarEpi(12, "Botina", 180, hoje.plusYears(1), 10, 2);
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(12, 12, hoje.minusDays(200).atStartOfDay(), hoje.minusDays(190).atStartOfDay()));

        DadosInsuficientesParaProjecaoException ex = assertThrows(DadosInsuficientesParaProjecaoException.class,
                () -> service.calcularProjecao(new ContextoProjecao(epi, NivelPerigo.BAIXO, emprestimos, hoje, 30)));

        assertTrue(ex.getMessage().contains("não possui uso registrado no período de amostragem"));
    }

    @Test
    @DisplayName("#199 / DS10 2.4: CA já vencido gera alerta de CA mesmo sem projeção")
    void caVencidoGeraAlertaSemProjecao() {
        Epi epi = criarEpi(14, "Respirador", 180, hoje.minusDays(3), 10, 2);

        List<AlertaSubstituicaoEpi> alertas = service.emitirAlertas(epi, null, hoje);

        assertEquals(1, alertas.size());
        assertEquals(TipoAlertaSubstituicao.CA_PROXIMO_VENCIMENTO, alertas.get(0).getTipoAlerta());
        assertTrue(alertas.get(0).getMensagem().contains("venceu em " + hoje.minusDays(3)));
        assertEquals(-3L, alertas.get(0).getDiasRestantes());
    }

    // Métodos auxiliares de criação de fixtures
    private Epi criarEpi(Integer id, String nome, Integer vidaUtilDias, LocalDate validadeCa,
                         int quantidade, int estoqueMinimo) {
        return EpiDeTeste.umEpi()
                .comId(id)
                .comNome(nome)
                .comCa("12345")
                .comQuantidade(quantidade)
                .comEstoqueMinimo(estoqueMinimo)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(validadeCa)
                .comVidaUtilDias(vidaUtilDias)
                .comDescricao("Descrição")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                .comLocalizacao("Setor A")
                .build();
    }

    private Emprestimo criarEmprestimo(Integer id, Integer epiId, LocalDateTime retirada, LocalDateTime devolucao) {
        ClassificacaoEmprestimo status = devolucao != null ? ClassificacaoEmprestimo.DISPONIVEL : ClassificacaoEmprestimo.INDISPONIVEL;
        EstadoDevolucao estadoDev = devolucao != null ? EstadoDevolucao.APTO : null;
        Integer respDev = devolucao != null ? 1 : null;
        return new Emprestimo(id, epiId, 10, 1, retirada,
                retirada.toLocalDate().plusDays(15), "Termo #1", null, devolucao, respDev, estadoDev, status);
    }
}

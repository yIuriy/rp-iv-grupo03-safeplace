package br.edu.safeplace.backend.domain.epi;

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

        ProjecaoSubstituicao projecao = service.calcularProjecao(epi, NivelPerigo.ALTO, emprestimos, hoje, 90);

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

        ProjecaoSubstituicao projecao = service.calcularProjecao(epi, NivelPerigo.BAIXO, emprestimos, hoje, 90);

        assertNotNull(projecao);
        assertEquals(validadeCa, projecao.getDataEstimadaDescarte());
        assertTrue(projecao.getJustificativa().contains("Substituição antecipada pelo vencimento legal do CA"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando EPI não possui durabilidade nominal cadastrada")
    void deveFalharQuandoEpiNaoPossuiDurabilidadeNominal() {
        Epi epiSemVidaUtil = criarEpi(3, "Luva de Raspa", null, hoje.plusYears(1), 5, 1);
        List<Emprestimo> emprestimos = List.of(criarEmprestimo(3, 3, hoje.minusDays(5).atStartOfDay(), null));

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> service.calcularProjecao(epiSemVidaUtil, NivelPerigo.MEDIO, emprestimos, hoje, 90)
        );

        assertEquals(3, ex.getEpiId());
        assertTrue(ex.getMessage().contains("durabilidade nominal"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando EPI não possui CA cadastrado")
    void deveFalharQuandoEpiNaoPossuiCaValido() {
        Epi epiMock = org.mockito.Mockito.mock(Epi.class);
        org.mockito.Mockito.when(epiMock.getId()).thenReturn(4);
        org.mockito.Mockito.when(epiMock.getNome()).thenReturn("Máscara PFF2");
        org.mockito.Mockito.when(epiMock.getVidaUtilDias()).thenReturn(120);
        org.mockito.Mockito.when(epiMock.getDataValidadeCa()).thenReturn(null);

        List<Emprestimo> emprestimos = List.of(criarEmprestimo(4, 4, hoje.minusDays(5).atStartOfDay(), null));

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> service.calcularProjecao(epiMock, NivelPerigo.BAIXO, emprestimos, hoje, 90)
        );

        assertEquals(4, ex.getEpiId());
        assertTrue(ex.getMessage().contains("Certificado de Aprovação"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando histórico de empréstimos for vazio")
    void deveFalharQuandoHistoricoEmprestimosForVazio() {
        Epi epi = criarEpi(5, "Protetor Auditivo", 180, hoje.plusYears(1), 10, 2);

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> service.calcularProjecao(epi, NivelPerigo.BAIXO, List.of(), hoje, 90)
        );

        assertEquals(5, ex.getEpiId());
        assertTrue(ex.getMessage().contains("empréstimo"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nível de perigo do setor for nulo")
    void deveFalharQuandoSetorNulo() {
        Epi epi = criarEpi(6, "Calçado de Segurança", 180, hoje.plusYears(1), 10, 2);
        List<Emprestimo> emprestimos = List.of(criarEmprestimo(6, 6, hoje.minusDays(5).atStartOfDay(), null));

        DadosInsuficientesParaProjecaoException ex = assertThrows(
                DadosInsuficientesParaProjecaoException.class,
                () -> service.calcularProjecao(epi, null, emprestimos, hoje, 90)
        );

        assertEquals(6, ex.getEpiId());
        assertTrue(ex.getMessage().contains("nível de periculosidade"));
    }

    @Test
    @DisplayName("Deve emitir alertas de descarte próximo, CA próximo e estoque crítico")
    void deveEmitirAlertasPreventivosCorretamente() {
        // EPI com descarte em 20 dias (<= 30), CA em 15 dias (<= 30) e saldo (2) <= estoqueMinimo (5)
        LocalDate descarteProximo = hoje.plusDays(20);
        LocalDate caProximo = hoje.plusDays(15);
        Epi epi = criarEpi(7, "Cinto Paraquedista", 120, caProximo, 2, 5);

        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(7, hoje, descarteProximo, "Teste");

        List<AlertaSubstituicaoEpi> alertas = service.emitirAlertas(epi, projecao, hoje, 30L);

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

        List<AlertaSubstituicaoEpi> alertas = service.emitirAlertas(epi, projecao, hoje, 30L);

        assertTrue(alertas.isEmpty());
    }

    @Test
    @DisplayName("Deve antecipar data de descarte e emitir justificativa emergencial quando empréstimo foi devolvido para DESCARTE")
    void deveAnteciparDescarteQuandoEmprestimoFoiDevolvidoComEstadoDescarte() {
        LocalDate validadeCa = hoje.plusYears(2);
        Epi epi = criarEpi(9, "Luva de Alta Tensão", 180, validadeCa, 10, 2);

        LocalDateTime retirada = hoje.minusDays(10).atStartOfDay();
        LocalDateTime devolucaoComAvaria = hoje.minusDays(2).atStartOfDay();

        Emprestimo emprestimoAvariado = new Emprestimo(
                100, 9, 10, 1, retirada,
                retirada.toLocalDate().plusDays(15), "Termo", null,
                devolucaoComAvaria, 1, EstadoDevolucao.DESCARTE, ClassificacaoEmprestimo.DISPONIVEL);

        ProjecaoSubstituicao projecao = service.calcularProjecao(
                epi, NivelPerigo.MEDIO, List.of(emprestimoAvariado), hoje, 90);

        assertEquals(devolucaoComAvaria.toLocalDate(), projecao.getDataEstimadaDescarte());
        assertTrue(projecao.getJustificativa().contains("Substituição imediata por avaria"));

        // Alertas gerados devem incluir DESCARTE_PROXIMO com prioridade ALTA
        List<AlertaSubstituicaoEpi> alertas = service.emitirAlertas(epi, projecao, hoje, 30L);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipoAlerta() == TipoAlertaSubstituicao.DESCARTE_PROXIMO && "ALTA".equals(a.getPrioridade())));
    }

    // Métodos auxiliares de criação de fixtures
    private Epi criarEpi(Integer id, String nome, Integer vidaUtilDias, LocalDate validadeCa,
                         int quantidade, int estoqueMinimo) {
        return new Epi(id, nome, "12345", quantidade, estoqueMinimo, StatusEpi.DISPONIVEL,
                validadeCa, vidaUtilDias, "Descrição", ClassificacaoEPI.PROTECAO_DE_CABECA, "Setor A");
    }

    private Emprestimo criarEmprestimo(Integer id, Integer epiId, LocalDateTime retirada, LocalDateTime devolucao) {
        ClassificacaoEmprestimo status = devolucao != null ? ClassificacaoEmprestimo.DISPONIVEL : ClassificacaoEmprestimo.INDISPONIVEL;
        EstadoDevolucao estadoDev = devolucao != null ? EstadoDevolucao.APTO : null;
        Integer respDev = devolucao != null ? 1 : null;
        return new Emprestimo(id, epiId, 10, 1, retirada,
                retirada.toLocalDate().plusDays(15), "Termo #1", null, devolucao, respDev, estadoDev, status);
    }
}

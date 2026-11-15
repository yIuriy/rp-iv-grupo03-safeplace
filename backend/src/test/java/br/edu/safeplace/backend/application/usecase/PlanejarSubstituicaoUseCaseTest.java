package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.suporte.EmprestimoDeTeste;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.epi;
import br.edu.safeplace.backend.suporte.EpiDeTeste;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.safeplace.backend.application.dto.input.GerarAnaliseProjecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlertaSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProjecaoSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ResultadoAnaliseSubstituicaoDTO;
import br.edu.safeplace.backend.application.port.out.EmprestimoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

@ExtendWith(MockitoExtension.class)
class PlanejarSubstituicaoUseCaseTest {

    @Mock
    private EpiRepository epiRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private SetorRepository setorRepository;

    @Mock
    private ProjecaoSubstituicaoRepository projecaoRepository;

    private PlanejarSubstituicaoService useCase;
    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    @BeforeEach
    void setUp() {
        useCase = new PlanejarSubstituicaoService(
                epiRepository, emprestimoRepository, setorRepository, projecaoRepository);
    }

    @Test
    @DisplayName("Deve gerar análise com sucesso para EPI completo e persistir as projeções")
    void deveGerarAnaliseComSucessoParaEpiValido() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete H-700")
                .comCa("54321")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(hoje.plusYears(1))
                .comVidaUtilDias(180)
                .comDescricao("Proteção crânio")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_CABECA)
                .comLocalizacao("Setor Usinagem")
                .build();

        Setor setor = new Setor(10, "Setor Usinagem", "Usinagem pesada", "Galpão 1", NivelPerigo.ALTO);

        Emprestimo emp = EmprestimoDeTeste.umEmprestimo()
                .comId(1)
                .comEpi(epi(1))
                .comBeneficiario(pessoa(100))
                .comResponsavelEntrega(pessoa(2))
                .retiradoEm(hoje.minusDays(30).atStartOfDay())
                .comDevolucaoPrevistaPara(hoje.plusDays(10))
                .comTermoCautela("Termo #10")
                .devolvidoEm(hoje.minusDays(5).atStartOfDay(), pessoa(2), EstadoDevolucao.APTO)
                .comStatus(ClassificacaoEmprestimo.DISPONIVEL)
                .build();

        when(epiRepository.listar()).thenReturn(List.of(epi));
        when(setorRepository.buscarPorNome("Setor Usinagem")).thenReturn(Optional.of(setor));
        when(emprestimoRepository.listarPorEpi(1)).thenReturn(List.of(emp));

        GerarAnaliseProjecaoInputDTO input = new GerarAnaliseProjecaoInputDTO(hoje, null, 90);
        ResultadoAnaliseSubstituicaoDTO resultado = useCase.gerarAnalise(input);

        assertNotNull(resultado);
        assertEquals(1, resultado.totalProcessados());
        assertEquals(1, resultado.totalSucesso());
        assertEquals(0, resultado.totalPendencias());
        assertEquals(1, resultado.projecoes().size());
        assertEquals("Capacete H-700", resultado.projecoes().get(0).nomeEpi());

        verify(projecaoRepository).salvar(anyList());
    }

    @Test
    @DisplayName("Deve segregar falhas/pendências por ausência de dados permitindo nova tentativa")
    void deveReportarFalhasSemAbortarProcessamento() {
        // EPI 1: sem histórico de empréstimos
        Epi epiSemHistorico = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Luva Nitrílica")
                .comCa("11111")
                .comQuantidade(5)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(hoje.plusYears(1))
                .comVidaUtilDias(90)
                .comDescricao("Proteção mãos")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES)
                .comLocalizacao("Pintura")
                .build();

        // EPI 2: válido
        Epi epiValido = EpiDeTeste.umEpi()
                .comId(2)
                .comNome("Óculos Ampla Visão")
                .comCa("22222")
                .comQuantidade(15)
                .comEstoqueMinimo(3)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(hoje.plusYears(1))
                .comVidaUtilDias(120)
                .comDescricao("Proteção olhos")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_OLHOS)
                .comLocalizacao("Pintura")
                .build();

        Setor setor = new Setor(20, "Pintura", "Cabine de pintura", "Galpão 2", NivelPerigo.MEDIO);

        Emprestimo empValido = EmprestimoDeTeste.umEmprestimo()
                .comId(2)
                .comEpi(epi(2))
                .comBeneficiario(pessoa(100))
                .comResponsavelEntrega(pessoa(2))
                .retiradoEm(hoje.minusDays(20).atStartOfDay())
                .comDevolucaoPrevistaPara(hoje.plusDays(10))
                .comTermoCautela("Termo #11")
                .devolvidoEm(hoje.minusDays(2).atStartOfDay(), pessoa(2), EstadoDevolucao.APTO)
                .comStatus(ClassificacaoEmprestimo.DISPONIVEL)
                .build();

        when(epiRepository.listar()).thenReturn(List.of(epiSemHistorico, epiValido));
        when(setorRepository.buscarPorNome("Pintura")).thenReturn(Optional.of(setor));
        when(emprestimoRepository.listarPorEpi(1)).thenReturn(List.of()); // Histórico vazio
        when(emprestimoRepository.listarPorEpi(2)).thenReturn(List.of(empValido));

        GerarAnaliseProjecaoInputDTO input = new GerarAnaliseProjecaoInputDTO(hoje, null, 90);
        ResultadoAnaliseSubstituicaoDTO resultado = useCase.gerarAnalise(input);

        assertNotNull(resultado);
        assertEquals(2, resultado.totalProcessados());
        assertEquals(1, resultado.totalSucesso());
        assertEquals(1, resultado.totalPendencias());

        assertFalse(resultado.pendenciasFalhas().isEmpty());
        assertEquals(1, resultado.pendenciasFalhas().get(0).epiId());
        assertTrue(resultado.pendenciasFalhas().get(0).motivoFalha().contains("empréstimo"));

        verify(projecaoRepository).salvar(anyList());
    }

    @Test
    @DisplayName("Deve emitir alertas preventivos para EPIs com descarte ou CA próximo")
    void deveEmitirAlertasPreventivos() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Máscara Facial")
                .comCa("33333")
                .comQuantidade(1)
                .comEstoqueMinimo(5)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(hoje.plusDays(25))
                .comVidaUtilDias(180)
                .comDescricao("Proteção resp.")
                .comClassificacao(ClassificacaoEPI.PROTECAO_RESPIRATORIA)
                .comLocalizacao("Setor A")
                .build();

        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(1, hoje, hoje.plusDays(20), "Substituição");

        when(epiRepository.listar()).thenReturn(List.of(epi));
        when(projecaoRepository.buscarUltimaPorEpi(1)).thenReturn(Optional.of(projecao));

        List<AlertaSubstituicaoOutputDTO> alertas = useCase.emitirAlertas(hoje);

        assertNotNull(alertas);
        // Espera alerta de estoque crítico (1 <= 5), descarte próximo (20 dias) e CA próximo (25 dias)
        assertEquals(3, alertas.size());
    }

    @Test
    @DisplayName("#199: setor informado e inexistente é recusado, sem processar nem persistir")
    void setorInformadoInexistenteERecusado() {
        when(setorRepository.buscarPorId(99)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> useCase.gerarAnalise(new GerarAnaliseProjecaoInputDTO(hoje, 99, null)));

        org.mockito.Mockito.verifyNoInteractions(projecaoRepository);
    }

    @Test
    @DisplayName("#199: localização numérica não é tratada como id de setor; gera pendência e mantém alertas")
    void localizacaoNumericaGeraPendenciaEMantemAlertas() {
        // Saldo 1 <= mínimo 5: alerta de estoque crítico independe da projeção (DS10, 2.4)
        Epi epi = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Máscara Facial")
                .comCa("33333")
                .comQuantidade(1)
                .comEstoqueMinimo(5)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(hoje.plusYears(1))
                .comVidaUtilDias(180)
                .comDescricao("Proteção resp.")
                .comClassificacao(ClassificacaoEPI.PROTECAO_RESPIRATORIA)
                .comLocalizacao("10")
                .build();
        when(epiRepository.listar()).thenReturn(List.of(epi));

        ResultadoAnaliseSubstituicaoDTO resultado = useCase.gerarAnalise(new GerarAnaliseProjecaoInputDTO(hoje, null, null));

        assertEquals(1, resultado.totalPendencias());
        assertTrue(resultado.pendenciasFalhas().get(0).motivoFalha().contains("setor"));
        assertTrue(resultado.alertas().stream().anyMatch(a -> "ESTOQUE_CRITICO".equals(a.tipoAlerta())));
        org.mockito.Mockito.verify(setorRepository, org.mockito.Mockito.never()).buscarPorId(any());
    }

    @Test
    @DisplayName("Deve listar projeções com sucesso mapeando dados do EPI")
    void deveListarProjecoesCadastradas() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Bota de PVC")
                .comCa("44444")
                .comQuantidade(8)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(hoje.plusYears(1))
                .comVidaUtilDias(180)
                .comDescricao("Proteção pés")
                .comClassificacao(ClassificacaoEPI.PROTECAO_DE_MEMBROS_INFERIORES)
                .comLocalizacao("Setor B")
                .build();

        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.reconstituir(10, 1, hoje, hoje.plusDays(50), "Desgaste");

        when(epiRepository.listar()).thenReturn(List.of(epi));
        when(projecaoRepository.listar()).thenReturn(List.of(projecao));

        List<ProjecaoSubstituicaoOutputDTO> lista = useCase.listar();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("Bota de PVC", lista.get(0).nomeEpi());
        assertEquals(10, lista.get(0).id());
    }
}

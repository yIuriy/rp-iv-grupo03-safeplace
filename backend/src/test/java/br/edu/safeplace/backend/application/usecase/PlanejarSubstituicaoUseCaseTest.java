package br.edu.safeplace.backend.application.usecase;

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
import br.edu.safeplace.backend.application.port.out.EmprestimoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
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
    private EpiRepositoryPort epiRepository;

    @Mock
    private EmprestimoRepositoryPort emprestimoRepository;

    @Mock
    private SetorRepositoryPort setorRepository;

    @Mock
    private ProjecaoSubstituicaoRepositoryPort projecaoRepository;

    private PlanejarSubstituicaoUseCaseImpl useCase;
    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    @BeforeEach
    void setUp() {
        useCase = new PlanejarSubstituicaoUseCaseImpl(
                epiRepository, emprestimoRepository, setorRepository, projecaoRepository);
    }

    @Test
    @DisplayName("Deve gerar análise com sucesso para EPI completo e persistir as projeções")
    void deveGerarAnaliseComSucessoParaEpiValido() {
        Epi epi = new Epi(1, "Capacete H-700", "54321", 10, 2, StatusEpi.DISPONIVEL,
                hoje.plusYears(1), 180, "Proteção crânio", ClassificacaoEPI.PROTECAO_DE_CABECA, "Setor Usinagem");

        Setor setor = new Setor(10, "Setor Usinagem", "Usinagem pesada", "Galpão 1", NivelPerigo.ALTO);

        Emprestimo emp = new Emprestimo(1, 1, 100, 2, hoje.minusDays(30).atStartOfDay(),
                hoje.plusDays(10), "Termo #10", null, hoje.minusDays(5).atStartOfDay(),
                2, EstadoDevolucao.APTO, ClassificacaoEmprestimo.DISPONIVEL);

        when(epiRepository.listarTodos()).thenReturn(List.of(epi));
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

        verify(projecaoRepository).salvarTodas(anyList());
    }

    @Test
    @DisplayName("Deve segregar falhas/pendências por ausência de dados permitindo nova tentativa")
    void deveReportarFalhasSemAbortarProcessamento() {
        // EPI 1: sem histórico de empréstimos
        Epi epiSemHistorico = new Epi(1, "Luva Nitrílica", "11111", 5, 2, StatusEpi.DISPONIVEL,
                hoje.plusYears(1), 90, "Proteção mãos", ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES, "Pintura");

        // EPI 2: válido
        Epi epiValido = new Epi(2, "Óculos Ampla Visão", "22222", 15, 3, StatusEpi.DISPONIVEL,
                hoje.plusYears(1), 120, "Proteção olhos", ClassificacaoEPI.PROTECAO_DE_OLHOS, "Pintura");

        Setor setor = new Setor(20, "Pintura", "Cabine de pintura", "Galpão 2", NivelPerigo.MEDIO);

        Emprestimo empValido = new Emprestimo(2, 2, 100, 2, hoje.minusDays(20).atStartOfDay(),
                hoje.plusDays(10), "Termo #11", null, hoje.minusDays(2).atStartOfDay(),
                2, EstadoDevolucao.APTO, ClassificacaoEmprestimo.DISPONIVEL);

        when(epiRepository.listarTodos()).thenReturn(List.of(epiSemHistorico, epiValido));
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

        verify(projecaoRepository).salvarTodas(anyList());
    }

    @Test
    @DisplayName("Deve emitir alertas preventivos para EPIs com descarte ou CA próximo")
    void deveEmitirAlertasPreventivos() {
        Epi epi = new Epi(1, "Máscara Facial", "33333", 1, 5, StatusEpi.DISPONIVEL,
                hoje.plusDays(25), 180, "Proteção resp.", ClassificacaoEPI.PROTECAO_RESPIRATORIA, "Setor A");

        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(1, hoje, hoje.plusDays(20), "Substituição");

        when(epiRepository.listarTodos()).thenReturn(List.of(epi));
        when(projecaoRepository.buscarUltimaPorEpi(1)).thenReturn(Optional.of(projecao));

        List<AlertaSubstituicaoOutputDTO> alertas = useCase.emitirAlertasPreventivos(hoje, 30L);

        assertNotNull(alertas);
        // Espera alerta de estoque crítico (1 <= 5), descarte próximo (20 dias) e CA próximo (25 dias)
        assertEquals(3, alertas.size());
    }

    @Test
    @DisplayName("Deve listar projeções com sucesso mapeando dados do EPI")
    void deveListarProjecoesCadastradas() {
        Epi epi = new Epi(1, "Bota de PVC", "44444", 8, 2, StatusEpi.DISPONIVEL,
                hoje.plusYears(1), 180, "Proteção pés", ClassificacaoEPI.PROTECAO_DE_MEMBROS_INFERIORES, "Setor B");

        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.reconstituir(10, 1, hoje, hoje.plusDays(50), "Desgaste");

        when(epiRepository.listarTodos()).thenReturn(List.of(epi));
        when(projecaoRepository.listarTodas()).thenReturn(List.of(projecao));

        List<ProjecaoSubstituicaoOutputDTO> lista = useCase.listarProjecoes();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("Bota de PVC", lista.get(0).nomeEpi());
        assertEquals(10, lista.get(0).id());
    }
}

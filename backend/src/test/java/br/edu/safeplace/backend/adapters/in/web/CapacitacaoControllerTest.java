package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.safeplace.backend.application.dto.capacitacao.AlertaCertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.AlertaTreinamentoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.AlertasCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CapacitacoesColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ColaboradorCapacitacaoResumoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ExigenciaCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.PendenciaColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.TreinamentoOutputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;
import br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.capacitacao.AcompanharCapacitacoesUseCase;
import br.edu.safeplace.backend.application.port.in.capacitacao.CadastrarCapacitacaoUseCase;
import br.edu.safeplace.backend.config.RestSegurancaExceptionHandler;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusTreinamento;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CapacitacaoController.class, CapacitacaoCadastroController.class, ApiExceptionHandler.class})
@Import({SecurityConfig.class, RestSegurancaExceptionHandler.class})
class CapacitacaoControllerTest {

    private static final LocalDate VENCIMENTO = LocalDate.of(2026, 10, 20);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcompanharCapacitacoesUseCase acompanharUseCase;

    @MockitoBean
    private AlocarColaboradorTarefaUseCase alocarUseCase;

    @MockitoBean
    private CadastrarCapacitacaoUseCase cadastrarUseCase;

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC02 cenário IV: Supervisor lista colaboradores filtrando pela situação")
    void deveListarColaboradoresFiltrados() throws Exception {
        when(acompanharUseCase.listarColaboradores(StatusCertificacao.VENCIDA)).thenReturn(List.of(
                new ColaboradorCapacitacaoResumoOutputDTO(3, "Ana", StatusCertificacao.VENCIDA, false, 2, 1, false)));

        mockMvc.perform(get("/api/capacitacoes/colaboradores").param("situacao", "VENCIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].colaboradorId").value(3))
                .andExpect(jsonPath("$[0].situacaoCertificacoes").value("VENCIDA"))
                .andExpect(jsonPath("$[0].semCertificacoes").value(false))
                .andExpect(jsonPath("$[0].cpf").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Situação fora do vocabulário é recusada com 400")
    void deveRecusarSituacaoInvalida() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/colaboradores").param("situacao", "QUALQUER"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(acompanharUseCase);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("UC02 passos 4 e 5: Gestor consulta os detalhes de capacitação do colaborador")
    void deveDetalharColaborador() throws Exception {
        CertificacaoOutputDTO certificacao = new CertificacaoOutputDTO(
                1, "NR-35", StatusCertificacao.PROXIMA_DO_VENCIMENTO, VENCIMENTO, LocalDate.of(2025, 10, 20));
        TreinamentoOutputDTO treinamento = new TreinamentoOutputDTO(
                2, "NR-10", "Riscos elétricos", "Eng. Paula", LocalDate.of(2026, 3, 10), 40, null, null, StatusTreinamento.REALIZADO);
        when(acompanharUseCase.consultarColaborador(3)).thenReturn(new CapacitacoesColaboradorOutputDTO(
                3, "Ana", StatusCertificacao.PROXIMA_DO_VENCIMENTO, List.of(certificacao), List.of(treinamento),
                List.of(), List.of(certificacao), false,
                List.of("Colaborador possui 1 certificação(ões) próxima(s) do vencimento (30 dias)")));

        mockMvc.perform(get("/api/capacitacoes/colaboradores/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colaboradorNome").value("Ana"))
                .andExpect(jsonPath("$.certificacoes[0].nome").value("NR-35"))
                .andExpect(jsonPath("$.certificacoes[0].status").value("PROXIMA_DO_VENCIMENTO"))
                .andExpect(jsonPath("$.certificacoes[0].dataVencimento").value("2026-10-20"))
                .andExpect(jsonPath("$.certificacoes[0].dataEmissao").value("2025-10-20"))
                .andExpect(jsonPath("$.treinamentos[0].conteudo").value("Riscos elétricos"))
                .andExpect(jsonPath("$.treinamentos[0].instrutor").value("Eng. Paula"))
                .andExpect(jsonPath("$.treinamentos[0].dataRealizacao").value("2026-03-10"))
                .andExpect(jsonPath("$.treinamentos[0].cargaHoraria").value(40))
                .andExpect(jsonPath("$.certificacoesProximasDoVencimento[0].id").value(1))
                .andExpect(jsonPath("$.pendencias[0]").value(
                        "Colaborador possui 1 certificação(ões) próxima(s) do vencimento (30 dias)"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Colaborador inexistente devolve 404")
    void deveDevolver404ParaColaboradorInexistente() throws Exception {
        when(acompanharUseCase.consultarColaborador(99))
                .thenThrow(new UsuarioNaoEncontradoException("Colaborador não encontrado: 99"));

        mockMvc.perform(get("/api/capacitacoes/colaboradores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Colaborador não encontrado: 99"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC02 regra 1: alertas trazem próximos vencimentos, vencidas e pendências")
    void deveConsultarAlertas() throws Exception {
        when(acompanharUseCase.consultarAlertas()).thenReturn(new AlertasCapacitacaoOutputDTO(
                LocalDate.of(2026, 10, 4), 30,
                List.of(new AlertaCertificacaoOutputDTO(1, "NR-35", StatusCertificacao.PROXIMA_DO_VENCIMENTO,
                        VENCIMENTO, LocalDate.of(2025, 10, 20), 3, "Ana")),
                List.of(new AlertaCertificacaoOutputDTO(2, "NR-10", StatusCertificacao.VENCIDA,
                        LocalDate.of(2026, 9, 1), LocalDate.of(2024, 9, 1), 4, "Bruno")),
                List.of(new PendenciaColaboradorOutputDTO(5, "Carla",
                        List.of("Pendência da trilha inicial de integração"))),
                List.of(new AlertaTreinamentoOutputDTO(7, "NR-33", StatusTreinamento.PROXIMO_DO_VENCIMENTO,
                        LocalDate.of(2025, 10, 25), VENCIMENTO.plusDays(5), 3, "Ana")),
                List.of(new AlertaTreinamentoOutputDTO(8, "NR-10", StatusTreinamento.VENCIDO,
                        LocalDate.of(2025, 1, 10), LocalDate.of(2026, 1, 10), 4, "Bruno"))));

        mockMvc.perform(get("/api/capacitacoes/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasAntecedenciaAlerta").value(30))
                .andExpect(jsonPath("$.proximasDoVencimento[0].colaboradorNome").value("Ana"))
                .andExpect(jsonPath("$.vencidas[0].status").value("VENCIDA"))
                .andExpect(jsonPath("$.pendencias[0].mensagens[0]").value("Pendência da trilha inicial de integração"))
                .andExpect(jsonPath("$.treinamentosProximosDoVencimento[0].nome").value("NR-33"))
                .andExpect(jsonPath("$.treinamentosVencidos[0].status").value("VENCIDO"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("#117: consulta de aptidão delega à mesma regra da alocação e informa impedimentos")
    void deveConsultarAptidaoPelaRegraDaAlocacao() throws Exception {
        when(alocarUseCase.consultarAptidao(7, 3)).thenReturn(ConsultaAptidaoOutputDTO.inapto(
                7, "Trabalho em altura", 3, "Ana", List.of("certificação obrigatória 'NR-35' ausente ou vencida")));

        mockMvc.perform(get("/api/capacitacoes/colaboradores/3/aptidao").param("tarefaId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apto").value(false))
                .andExpect(jsonPath("$.impedimentos[0]").value("certificação obrigatória 'NR-35' ausente ou vencida"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Consulta de aptidão exige a tarefa pretendida")
    void deveExigirTarefaNaConsultaDeAptidao() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/colaboradores/3/aptidao"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(alocarUseCase);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Consulta de aptidão para tarefa inexistente devolve 404")
    void deveDevolver404ParaTarefaInexistente() throws Exception {
        when(alocarUseCase.consultarAptidao(anyInt(), any())).thenThrow(new TarefaNaoEncontradaException(70));

        mockMvc.perform(get("/api/capacitacoes/colaboradores/3/aptidao").param("tarefaId", "70"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RNF03: consulta sem autenticação devolve 401")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/alertas"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(acompanharUseCase);
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    @DisplayName("RNF03: perfil sem acesso não consulta capacitações")
    void deveNegarPerfilSemAcesso() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/colaboradores"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(acompanharUseCase);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("#192: Gestor cadastra certificação, treinamento e exigência")
    void gestorDeveCadastrar() throws Exception {
        when(cadastrarUseCase.cadastrar(any(CadastrarCertificacaoInputDTO.class))).thenReturn(
                new CertificacaoOutputDTO(1, "NR-35", StatusCertificacao.EM_DIA, VENCIMENTO.plusYears(1), VENCIMENTO.minusYears(1)));
        when(cadastrarUseCase.cadastrar(any(CadastrarTreinamentoInputDTO.class))).thenReturn(new TreinamentoOutputDTO(
                2, "NR-10", null, null, VENCIMENTO.minusDays(5), 8, 12, VENCIMENTO.plusMonths(12), StatusTreinamento.REALIZADO));
        when(cadastrarUseCase.cadastrar(any(DefinirExigenciaInputDTO.class))).thenReturn(new ExigenciaCapacitacaoOutputDTO(
                3, "NR-35", ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, ExigenciaCapacitacao.TipoAlvo.TAREFA, 5));

        mockMvc.perform(post("/api/capacitacoes/certificacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"NR-35\",\"dataVencimento\":\"2027-10-20\",\"dataEmissao\":\"2025-10-20\",\"colaboradorId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(post("/api/capacitacoes/treinamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"NR-10\",\"dataRealizacao\":\"2026-10-15\",\"cargaHoraria\":8,\"validadeMeses\":12,\"colaboradoresIds\":[3]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.validadeMeses").value(12))
                .andExpect(jsonPath("$.status").value("REALIZADO"));
        mockMvc.perform(post("/api/capacitacoes/exigencias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeCapacitacao\":\"NR-35\",\"tipoCapacitacao\":\"CERTIFICACAO\",\"alvo\":\"TAREFA\",\"alvoId\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alvo").value("TAREFA"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("#192: corpo inválido é recusado com 400 sem chamar o caso de uso")
    void deveRecusarCorpoInvalido() throws Exception {
        mockMvc.perform(post("/api/capacitacoes/certificacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" \",\"colaboradorId\":3}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/capacitacoes/treinamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"NR-10\",\"dataRealizacao\":\"2026-10-15\",\"validadeMeses\":0,\"colaboradoresIds\":[]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(cadastrarUseCase);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("#192: Supervisor não cadastra capacitações")
    void supervisorNaoDeveCadastrar() throws Exception {
        String corpo = "{\"nome\": \"NR-35\", \"colaboradorId\": 3}";

        mockMvc.perform(post("/api/capacitacoes/certificacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/capacitacoes/treinamentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/capacitacoes/exigencias").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        verifyNoInteractions(cadastrarUseCase);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Métodos de escrita além do cadastro continuam negados")
    void deveNegarOutrosMetodosDeEscrita() throws Exception {
        String corpo = "{\"nome\": \"NR-35\", \"colaboradorId\": 3}";

        mockMvc.perform(put("/api/capacitacoes/colaboradores/3").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/capacitacoes/colaboradores/3"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(acompanharUseCase, alocarUseCase, cadastrarUseCase);
    }
}

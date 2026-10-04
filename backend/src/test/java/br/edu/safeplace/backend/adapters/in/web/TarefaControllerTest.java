package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiTarefaUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TarefaController.class, TarefaTratadorExcecoes.class})
@Import(SecurityConfig.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClassificarTarefaUseCase useCase;

    @MockitoBean
    private AlocarColaboradorTarefaUseCase alocarUseCase;

    @MockitoBean
    private GerenciarEpiTarefaUseCase gerenciarEpiUseCase;

    private static TarefaOutputDTO tarefaClassificada() {
        return new TarefaOutputDTO(1, "Solda em altura", NivelPerigo.ALTO, true,
                LocalDateTime.of(2026, 9, 13, 10, 15, 30));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor de Segurança deve cadastrar tarefa classificada")
    void deveCriarTarefaComSucesso() throws Exception {
        when(useCase.cadastrarTarefa(any(CadastrarTarefaInputDTO.class))).thenReturn(tarefaClassificada());

        String json = """
                {
                    "descricao": "Solda em altura",
                    "nivelPerigo": "ALTO"
                }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Solda em altura"))
                .andExpect(jsonPath("$.nivelPerigo").value("ALTO"))
                .andExpect(jsonPath("$.classificada").value(true));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("UC11: deve aceitar cadastro de tarefa sem classificação")
    void deveCriarTarefaSemClassificacao() throws Exception {
        when(useCase.cadastrarTarefa(any(CadastrarTarefaInputDTO.class)))
                .thenReturn(new TarefaOutputDTO(2, "Inspeção visual", null, false, null));

        String json = """
                {
                    "descricao": "Inspeção visual"
                }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classificada").value(false))
                .andExpect(jsonPath("$.nivelPerigo").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC11: Supervisor é ator secundário e não pode cadastrar tarefa")
    void supervisorNaoDeveCadastrarTarefa() throws Exception {
        String json = """
                {
                    "descricao": "Solda em altura",
                    "nivelPerigo": "ALTO"
                }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC11: Supervisor não pode reclassificar tarefa")
    void supervisorNaoDeveClassificarTarefa() throws Exception {
        mockMvc.perform(patch("/api/tarefas/1/classificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelPerigo\": \"CRITICO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC11: Supervisor deve consultar a matriz de periculosidade")
    void supervisorDeveConsultarMatriz() throws Exception {
        when(useCase.listarPorNivelPerigo(null)).thenReturn(List.of(tarefaClassificada()));

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descricao").value("Solda em altura"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Deve filtrar a matriz de periculosidade pelo grau de perigo")
    void deveFiltrarMatrizPorNivelPerigo() throws Exception {
        when(useCase.listarPorNivelPerigo(eq(NivelPerigo.ALTO))).thenReturn(List.of(tarefaClassificada()));

        mockMvc.perform(get("/api/tarefas").param("nivelPerigo", "ALTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("UC11: Gestor deve classificar tarefa existente")
    void gestorDeveClassificarTarefa() throws Exception {
        when(useCase.classificar(1, NivelPerigo.CRITICO))
                .thenReturn(new TarefaOutputDTO(1, "Solda em altura", NivelPerigo.CRITICO, true,
                        LocalDateTime.of(2026, 9, 13, 11, 0), 7, "Mariana"));

        mockMvc.perform(patch("/api/tarefas/1/classificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelPerigo\": \"CRITICO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelPerigo").value("CRITICO"))
                .andExpect(jsonPath("$.classificada").value(true))
                .andExpect(jsonPath("$.responsavelClassificacaoId").value(7))
                .andExpect(jsonPath("$.responsavelClassificacaoNome").value("Mariana"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("#112: classificação sem responsável técnico identificável responde 403")
    void classificacaoSemResponsavelRespondeProibido() throws Exception {
        when(useCase.classificar(1, NivelPerigo.ALTO))
                .thenThrow(new AccessDeniedException("sem Gestor de Segurança responsável"));

        mockMvc.perform(patch("/api/tarefas/1/classificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelPerigo\": \"ALTO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("#112: termos de UC11 fora do vocabulário único NivelPerigo são recusados")
    void deveRecusarTermoForaDoVocabularioUnico() throws Exception {
        mockMvc.perform(patch("/api/tarefas/1/classificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelPerigo\": \"LEVE\"}"))
                .andExpect(status().isBadRequest());

        verify(useCase, never()).classificar(any(), any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve recusar classificação sem nível de perigo")
    void deveRecusarClassificacaoSemNivel() throws Exception {
        mockMvc.perform(patch("/api/tarefas/1/classificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve retornar 404 para tarefa inexistente")
    void deveRetornar404ParaTarefaInexistente() throws Exception {
        when(useCase.buscarPorId(99)).thenThrow(new TarefaNaoEncontradaException(99));

        mockMvc.perform(get("/api/tarefas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve recusar cadastro sem descrição")
    void deveRecusarCadastroSemDescricao() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelPerigo\": \"ALTO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR", username = "supervisor@safeplace.com")
    @DisplayName("Supervisor deve alocar colaborador com sucesso (201 Created)")
    void deveAlocarColaboradorComSucesso() throws Exception {
        br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO dto =
                new br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO(
                        1, 10, "Solda em altura", 2, "Carlos", LocalDateTime.now(), 5, "Supervisor Bruno", "Observação");

        when(alocarUseCase.alocar(eq(10), any(), eq("supervisor@safeplace.com"))).thenReturn(dto);

        mockMvc.perform(post("/api/tarefas/10/alocacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"colaboradorId\": 2, \"observacao\": \"Observação\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.colaboradorNome").value("Carlos"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR", username = "supervisor@safeplace.com")
    @DisplayName("Deve retornar 409 Conflict ao alocar em tarefa sem classificação")
    void deveRetornar409AoAlocarEmTarefaSemClassificacao() throws Exception {
        when(alocarUseCase.alocar(eq(10), any(), any()))
                .thenThrow(new br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException("Solda em altura"));

        mockMvc.perform(post("/api/tarefas/10/alocacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"colaboradorId\": 2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Tarefa Sem Classificação"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA", username = "gestor@safeplace.com")
    @DisplayName("Deve retornar 422 Unprocessable Entity ao alocar com capacitação pendente")
    void deveRetornar422AoAlocarComCapacitacaoPendente() throws Exception {
        when(alocarUseCase.alocar(eq(10), any(), any()))
                .thenThrow(new br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException(
                        List.of("certificação 'NR-35' ausente ou vencida")));

        mockMvc.perform(post("/api/tarefas/10/alocacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"colaboradorId\": 2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Alocação Bloqueada"))
                .andExpect(jsonPath("$.impedimentos[0]").value("certificação 'NR-35' ausente ou vencida"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Deve consultar aptidão prévia com sucesso (200 OK)")
    void deveConsultarAptidaoPrevia() throws Exception {
        br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO dto =
                br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO.inapto(
                        10, "Solda em altura", 2, "Carlos", List.of("NR-35 ausente"));

        when(alocarUseCase.consultarAptidao(10, 2)).thenReturn(dto);

        mockMvc.perform(get("/api/tarefas/10/aptidao/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apto").value(false))
                .andExpect(jsonPath("$.impedimentos[0]").value("NR-35 ausente"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF12 / UC08: Gestor de Segurança deve associar EPIs obrigatórios a uma tarefa (200 OK)")
    void gestorDeveAssociarEpisComSucesso() throws Exception {
        TarefaOutputDTO dto = new TarefaOutputDTO(1, "Solda em altura", NivelPerigo.ALTO, true,
                LocalDateTime.of(2026, 9, 13, 10, 15, 30), null, null, null, List.of(), List.of(10, 20));

        when(gerenciarEpiUseCase.associarEpis(eq(1), eq(List.of(10, 20)))).thenReturn(dto);

        String json = """
                {
                    "epiIds": [10, 20]
                }
                """;

        mockMvc.perform(put("/api/tarefas/1/epis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.episIds[0]").value(10))
                .andExpect(jsonPath("$.episIds[1]").value(20));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("RF12 / UC08: Supervisor não pode associar EPIs à tarefa (403 Forbidden)")
    void supervisorNaoDeveAssociarEpis() throws Exception {
        mockMvc.perform(put("/api/tarefas/1/epis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"epiIds\": [10]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF12 / UC08: Deve retornar 404 ao associar EPIs em tarefa inexistente")
    void deveRetornar404AoAssociarEpisEmTarefaInexistente() throws Exception {
        when(gerenciarEpiUseCase.associarEpis(eq(99), any()))
                .thenThrow(new TarefaNaoEncontradaException(99));

        mockMvc.perform(put("/api/tarefas/99/epis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"epiIds\": [10]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF12 / UC08: Deve retornar 404 ao associar EPI que não existe no catálogo")
    void deveRetornar404AoAssociarEpiNaoExistente() throws Exception {
        when(gerenciarEpiUseCase.associarEpis(eq(1), any()))
                .thenThrow(new EpiNaoEncontradoException(999));

        mockMvc.perform(put("/api/tarefas/1/epis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"epiIds\": [999]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF12 / UC08: Deve retornar 400 ao enviar corpo de requisição sem lista de EPIs")
    void deveRecusarAssociacaoComListaNula() throws Exception {
        mockMvc.perform(put("/api/tarefas/1/epis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("RF12 / UC08: Supervisor deve consultar EPIs obrigatórios da tarefa (200 OK)")
    void supervisorDeveConsultarEpisObrigatorios() throws Exception {
        EpiOutputDTO epiDto = new EpiOutputDTO(
                10, "Capacete", "12345", 5, 2,
                StatusEpi.DISPONIVEL, null, 365, false,
                "Capacete de proteção", ClassificacaoEPI.PROTECAO_DE_CABECA);

        when(gerenciarEpiUseCase.listarEpisObrigatorios(1)).thenReturn(List.of(epiDto));

        mockMvc.perform(get("/api/tarefas/1/epis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nome").value("Capacete"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF12 / UC08: Gestor deve consultar EPIs obrigatórios da tarefa (200 OK)")
    void gestorDeveConsultarEpisObrigatorios() throws Exception {
        when(gerenciarEpiUseCase.listarEpisObrigatorios(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/tarefas/1/epis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

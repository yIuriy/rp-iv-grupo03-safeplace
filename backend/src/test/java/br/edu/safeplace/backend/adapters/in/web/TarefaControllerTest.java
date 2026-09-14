package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
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

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TarefaController.class, TarefaTratadorExcecoes.class})
@Import(SecurityConfig.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClassificarTarefaUseCase useCase;

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
                        LocalDateTime.of(2026, 9, 13, 11, 0)));

        mockMvc.perform(patch("/api/tarefas/1/classificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelPerigo\": \"CRITICO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelPerigo").value("CRITICO"))
                .andExpect(jsonPath("$.classificada").value(true));
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
}

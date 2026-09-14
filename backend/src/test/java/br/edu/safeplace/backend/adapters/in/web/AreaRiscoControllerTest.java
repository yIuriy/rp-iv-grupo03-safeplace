package br.edu.safeplace.backend.adapters.in.web;

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

import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiResumoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AreaRiscoController.class, AreaRiscoTratadorExcecoes.class})
@Import(SecurityConfig.class)
class AreaRiscoControllerTest {

    private static final String JSON_VALIDO = """
            {
                "codigo": "SET-01",
                "nome": "Linha de Produção",
                "descricao": "Setor de solda e montagem",
                "nivelPerigo": "ALTO",
                "episObrigatoriosIds": [1, 2]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarAreaRiscoUseCase useCase;

    private static AreaRiscoOutputDTO areaSalva() {
        return new AreaRiscoOutputDTO(1, "SET-01", "Linha de Produção", "Setor de solda e montagem",
                NivelPerigo.ALTO, List.of(
                        new EpiResumoOutputDTO(1, "Capacete", "CA-1"),
                        new EpiResumoOutputDTO(2, "Luva térmica", "CA-2")));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor de Segurança deve cadastrar área de risco")
    void deveCriarAreaRiscoComSucesso() throws Exception {
        when(useCase.cadastrarAreaRisco(any(CadastrarAreaRiscoInputDTO.class))).thenReturn(areaSalva());

        mockMvc.perform(post("/api/areas-risco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("SET-01"))
                .andExpect(jsonPath("$.nivelPerigo").value("ALTO"))
                .andExpect(jsonPath("$.episObrigatorios.length()").value(2))
                .andExpect(jsonPath("$.episObrigatorios[0].numeroCa").value("CA-1"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC03: Supervisor é ator secundário e não pode cadastrar área de risco")
    void supervisorNaoDeveCadastrarAreaRisco() throws Exception {
        mockMvc.perform(post("/api/areas-risco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC03: Supervisor deve consultar o mapa de riscos")
    void supervisorDeveConsultarMapaDeRiscos() throws Exception {
        when(useCase.listarPorNivelPerigo(null)).thenReturn(List.of(areaSalva()));

        mockMvc.perform(get("/api/areas-risco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value("SET-01"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("UC03 alternativo I: deve filtrar a listagem pelo grau de perigo")
    void deveFiltrarListagemPorNivelPerigo() throws Exception {
        when(useCase.listarPorNivelPerigo(eq(NivelPerigo.CRITICO))).thenReturn(List.of());

        mockMvc.perform(get("/api/areas-risco").param("nivelPerigo", "CRITICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve buscar área de risco por ID")
    void deveBuscarAreaRiscoPorId() throws Exception {
        when(useCase.buscarPorId(1)).thenReturn(areaSalva());

        mockMvc.perform(get("/api/areas-risco/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Linha de Produção"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve retornar 404 para área de risco inexistente")
    void deveRetornar404ParaAreaInexistente() throws Exception {
        when(useCase.buscarPorId(99)).thenThrow(new AreaRiscoNaoEncontradaException(99));

        mockMvc.perform(get("/api/areas-risco/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("UC03 exceção I: código duplicado deve retornar 409")
    void codigoDuplicadoDeveRetornar409() throws Exception {
        when(useCase.cadastrarAreaRisco(any(CadastrarAreaRiscoInputDTO.class)))
                .thenThrow(new CodigoAreaRiscoDuplicadoException("SET-01"));

        mockMvc.perform(post("/api/areas-risco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Código Já Em Uso"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("UC03 exceção II: ausência de EPI obrigatório vinda do domínio deve retornar 400")
    void ausenciaDeEpiNoDominioDeveRetornar400() throws Exception {
        when(useCase.cadastrarAreaRisco(any(CadastrarAreaRiscoInputDTO.class)))
                .thenThrow(new AreaRiscoSemEpiObrigatorioException("SET-01"));

        mockMvc.perform(post("/api/areas-risco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EPI Obrigatório Ausente"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("UC03 exceção II: lista vazia de EPIs é barrada já na validação da requisição")
    void listaVaziaDeEpisDeveRetornar400() throws Exception {
        String jsonSemEpis = """
                {
                    "codigo": "SET-01",
                    "nome": "Linha de Produção",
                    "nivelPerigo": "ALTO",
                    "episObrigatoriosIds": []
                }
                """;

        mockMvc.perform(post("/api/areas-risco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemEpis))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve recusar cadastro sem nível de perigo")
    void deveRecusarCadastroSemNivelPerigo() throws Exception {
        String jsonSemNivel = """
                {
                    "codigo": "SET-01",
                    "nome": "Linha de Produção",
                    "episObrigatoriosIds": [1]
                }
                """;

        mockMvc.perform(post("/api/areas-risco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemNivel))
                .andExpect(status().isBadRequest());
    }
}

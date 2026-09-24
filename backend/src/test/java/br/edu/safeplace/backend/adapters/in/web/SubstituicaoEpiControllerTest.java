package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.safeplace.backend.application.dto.input.GerarAnaliseProjecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlertaSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.FalhaProjecaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProjecaoSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ResultadoAnaliseSubstituicaoDTO;
import br.edu.safeplace.backend.application.port.in.PlanejarSubstituicaoUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;

@WebMvcTest(SubstituicaoEpiController.class)
@Import(SecurityConfig.class)
class SubstituicaoEpiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanejarSubstituicaoUseCase planejarSubstituicaoUseCase;

    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor de Segurança deve solicitar análise de projeções com sucesso (HTTP 200)")
    void deveGerarAnaliseComSucesso() throws Exception {
        ProjecaoSubstituicaoOutputDTO proj = new ProjecaoSubstituicaoOutputDTO(
                1, 10, "Capacete", hoje, hoje.plusDays(60), "Desgaste", hoje.plusYears(1), 8, 2);

        FalhaProjecaoOutputDTO falha = new FalhaProjecaoOutputDTO(20, "Luva", "Sem empréstimos");

        AlertaSubstituicaoOutputDTO alerta = new AlertaSubstituicaoOutputDTO(
                10, "Capacete", "DESCARTE_PROXIMO", "Alerta", hoje.plusDays(20), 20L, "ALTA");

        ResultadoAnaliseSubstituicaoDTO resultado = ResultadoAnaliseSubstituicaoDTO.criar(
                List.of(proj), List.of(falha), List.of(alerta));

        when(planejarSubstituicaoUseCase.gerarAnalise(any(GerarAnaliseProjecaoInputDTO.class)))
                .thenReturn(resultado);

        String jsonRequest = """
                {
                    "dataReferencia": "2026-09-24",
                    "setorId": 1,
                    "diasAmostragem": 90
                }
                """;

        mockMvc.perform(post("/api/projecoes/analise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessados").value(2))
                .andExpect(jsonPath("$.totalSucesso").value(1))
                .andExpect(jsonPath("$.totalPendencias").value(1))
                .andExpect(jsonPath("$.projecoes[0].nomeEpi").value("Capacete"))
                .andExpect(jsonPath("$.pendenciasFalhas[0].nomeEpi").value("Luva"))
                .andExpect(jsonPath("$.alertas[0].tipoAlerta").value("DESCARTE_PROXIMO"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor não tem permissão para acionar geração de análise (HTTP 403)")
    void supervisorNaoPodeGerarAnalise() throws Exception {
        mockMvc.perform(post("/api/projecoes/analise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor e Gestor podem consultar alertas preventivos (HTTP 200)")
    void deveConsultarAlertas() throws Exception {
        AlertaSubstituicaoOutputDTO alerta = new AlertaSubstituicaoOutputDTO(
                1, "Óculos", "ESTOQUE_CRITICO", "Saldo 1 <= Mínimo 2", null, null, "ALTA");

        when(planejarSubstituicaoUseCase.emitirAlertasPreventivos(any(LocalDate.class), eq(30L)))
                .thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/projecoes/alertas")
                        .param("dataReferencia", "2026-09-24")
                        .param("diasAntecedencia", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeEpi").value("Óculos"))
                .andExpect(jsonPath("$[0].tipoAlerta").value("ESTOQUE_CRITICO"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve listar projeções de um EPI específico (HTTP 200)")
    void deveListarProjecoesPorEpi() throws Exception {
        ProjecaoSubstituicaoOutputDTO proj = new ProjecaoSubstituicaoOutputDTO(
                5, 10, "Bota de Segurança", hoje, hoje.plusDays(40), "Desgaste", hoje.plusYears(1), 12, 3);

        when(planejarSubstituicaoUseCase.listarProjecoesPorEpi(10)).thenReturn(List.of(proj));

        mockMvc.perform(get("/api/projecoes/epi/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeEpi").value("Bota de Segurança"))
                .andExpect(jsonPath("$[0].justificativa").value("Desgaste"));
    }
}

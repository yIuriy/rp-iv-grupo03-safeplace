/*
 * Testes do GET /api/ocorrencias com critérios (issue #128): os parâmetros chegam ao caso de uso como
 * FiltroOcorrenciasDTO, a ausência deles vira consulta sem filtro e período inválido responde 400.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;

@WebMvcTest({OcorrenciaController.class, OcorrenciaExceptionHandler.class})
@Import(SecurityConfig.class)
class OcorrenciaConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarOcorrenciaUseCase registrarUseCase;

    @MockitoBean
    private GerenciarOcorrenciaUseCase gerenciarUseCase;

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Os quatro critérios chegam ao caso de uso como um único filtro")
    void repassaCriteriosAoCasoDeUso() throws Exception {
        when(registrarUseCase.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/ocorrencias")
                        .param("dataInicio", "2026-09-01")
                        .param("dataFim", "2026-09-30")
                        .param("setorId", "3")
                        .param("colaboradorId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(registrarUseCase).listar(
                new FiltroOcorrenciasDTO(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 3, 7));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Sem parâmetros, a consulta segue sem filtro")
    void semParametrosConsultaSemFiltro() throws Exception {
        when(registrarUseCase.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/ocorrencias")).andExpect(status().isOk());

        verify(registrarUseCase).listar(FiltroOcorrenciasDTO.SEM_FILTRO);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Data inicial posterior à final responde 400 com a mensagem e não consulta")
    void periodoInvertidoResponde400() throws Exception {
        mockMvc.perform(get("/api/ocorrencias")
                        .param("dataInicio", "2026-09-30")
                        .param("dataFim", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A data inicial do período não pode ser posterior à data final."));

        verifyNoInteractions(registrarUseCase);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Data fora do formato AAAA-MM-DD responde 400 e não consulta")
    void dataMalFormadaResponde400() throws Exception {
        mockMvc.perform(get("/api/ocorrencias").param("dataInicio", "01/09/2026"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrarUseCase);
    }
}

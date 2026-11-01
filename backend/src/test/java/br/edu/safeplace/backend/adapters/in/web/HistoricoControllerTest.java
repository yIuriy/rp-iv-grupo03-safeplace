/*
 * Testes HTTP das rotas de histórico de áreas de risco e tarefas (issue #114): resposta para o Gestor,
 * recusa para o Supervisor, 404 para cadastro inexistente e consulta comum do Supervisor preservada.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AreaRiscoController.class, TarefaController.class,
        AreaRiscoExceptionHandler.class, TarefaExceptionHandler.class})
@Import(SecurityConfig.class)
class HistoricoControllerTest {

    private static final LocalDateTime MOMENTO = LocalDateTime.of(2026, 10, 2, 14, 30);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarAreaRiscoUseCase areaUseCase;

    @MockitoBean
    private ClassificarTarefaUseCase tarefaService;

    @MockitoBean
    private AlocarColaboradorTarefaUseCase alocarUseCase;

    @MockitoBean
    private br.edu.safeplace.backend.application.port.in.GerenciarEpiTarefaUseCase gerenciarEpiUseCase;

    @MockitoBean
    private br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase consultarCapacitacaoUseCase;

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor consulta o histórico de uma área com data, operador e o que mudou")
    void gestorConsultaHistoricoDaArea() throws Exception {
        when(areaUseCase.listarHistorico(1)).thenReturn(List.of(new RegistroHistoricoOutputDTO(
                12, "ALTERACAO", MOMENTO, "gestor@safeplace.com",
                "Alteração da área de risco: limites: de \"A\" para \"B\".")));

        mockMvc.perform(get("/api/areas-risco/1/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].tipoOperacao").value("ALTERACAO"))
                .andExpect(jsonPath("$[0].dataHora").value("2026-10-02T14:30:00"))
                .andExpect(jsonPath("$[0].responsavel").value("gestor@safeplace.com"))
                .andExpect(jsonPath("$[0].descricao").value("Alteração da área de risco: limites: de \"A\" para \"B\"."));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor não consulta o histórico de áreas")
    void supervisorNaoConsultaHistoricoDaArea() throws Exception {
        mockMvc.perform(get("/api/areas-risco/1/historico"))
                .andExpect(status().isForbidden());
        verify(areaUseCase, never()).listarHistorico(any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor continua consultando os detalhes da área")
    void supervisorContinuaConsultandoArea() throws Exception {
        when(areaUseCase.buscarPorId(1)).thenReturn(new AreaRiscoOutputDTO(
                1, "AR-01", "Solda", null, NivelPerigo.ALTO, List.of()));

        mockMvc.perform(get("/api/areas-risco/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("AR-01"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Histórico de área inexistente responde 404")
    void historicoDeAreaInexistente() throws Exception {
        when(areaUseCase.listarHistorico(99)).thenThrow(new AreaRiscoNaoEncontradaException(99));

        mockMvc.perform(get("/api/areas-risco/99/historico"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor consulta o histórico de classificações de uma tarefa")
    void gestorConsultaHistoricoDaTarefa() throws Exception {
        when(tarefaService.listarHistorico(3)).thenReturn(List.of(new RegistroHistoricoOutputDTO(
                5, "ALTERACAO", MOMENTO, "gestor@safeplace.com",
                "Classificação de periculosidade da tarefa: de MEDIO para ALTO")));

        mockMvc.perform(get("/api/tarefas/3/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Classificação de periculosidade da tarefa: de MEDIO para ALTO"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor não consulta o histórico de tarefas")
    void supervisorNaoConsultaHistoricoDaTarefa() throws Exception {
        mockMvc.perform(get("/api/tarefas/3/historico"))
                .andExpect(status().isForbidden());
        verify(tarefaService, never()).listarHistorico(any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Histórico de tarefa inexistente responde 404")
    void historicoDeTarefaInexistente() throws Exception {
        when(tarefaService.listarHistorico(99)).thenThrow(new TarefaNaoEncontradaException(99));

        mockMvc.perform(get("/api/tarefas/99/historico"))
                .andExpect(status().isNotFound());
    }
}

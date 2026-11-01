/*
 * Testes da rota GET /api/usuarios/me/historico (RNF05): Gestor e Supervisor consultam as próprias
 * ações pelo e-mail do token, o limite chega ao caso de uso, a rota não é confundida com
 * /api/usuarios/{id} e quem não está autenticado recebe 401.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.safeplace.backend.application.dto.output.AcaoUsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.in.ConsultarHistoricoProprioUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioUseCase;
import br.edu.safeplace.backend.config.RestSegurancaExceptionHandler;
import br.edu.safeplace.backend.config.SecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({HistoricoProprioController.class, UsuarioController.class, UsuarioExceptionHandler.class})
@Import({SecurityConfig.class, RestSegurancaExceptionHandler.class})
class HistoricoProprioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarHistoricoProprioUseCase casoDeUso;

    @MockitoBean
    private GerenciarUsuarioUseCase usuarios;

    @Test
    @WithMockUser(username = "gestor.demo@safeplace.com", roles = "GESTOR_SEGURANCA")
    void gestorConsultaAsPropriasAcoesComLimitePadrao() throws Exception {
        when(casoDeUso.listarAcoes("gestor.demo@safeplace.com", 20)).thenReturn(List.of(new AcaoUsuarioOutputDTO(
                31, "CADASTRO", LocalDateTime.of(2026, 10, 5, 20, 53), "Inspecao", 5,
                "Inspeção de 03/10/2026 na área AR-03 - Corredor de estoque: 2 itens verificados, 0 não conformidades.")));

        mockMvc.perform(get("/api/usuarios/me/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(31))
                .andExpect(jsonPath("$[0].tipoOperacao").value("CADASTRO"))
                .andExpect(jsonPath("$[0].dataHora").value("2026-10-05T20:53:00"))
                .andExpect(jsonPath("$[0].entidade").value("Inspecao"))
                .andExpect(jsonPath("$[0].entidadeId").value(5))
                .andExpect(jsonPath("$[0].descricao").value(org.hamcrest.Matchers.startsWith("Inspeção de 03/10/2026")))
                .andExpect(jsonPath("$[0].responsavel").doesNotExist());

        verify(usuarios, never()).buscarPorId(any());
    }

    @Test
    @WithMockUser(username = "supervisor.demo@safeplace.com", roles = "SUPERVISOR")
    void supervisorInformaOLimite() throws Exception {
        when(casoDeUso.listarAcoes("supervisor.demo@safeplace.com", 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios/me/historico").param("limite", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(casoDeUso).listarAcoes("supervisor.demo@safeplace.com", 5);
    }

    @Test
    @WithAnonymousUser
    void semAutenticacaoResponde401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me/historico"))
                .andExpect(status().isUnauthorized());

        verify(casoDeUso, never()).listarAcoes(anyString(), anyInt());
    }
}

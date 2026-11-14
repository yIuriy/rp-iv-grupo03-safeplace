/*
 * Testes HTTP das rotas de inspeção (issue #134): 201 e 200 para o Gestor, 400 para registro inválido,
 * 403 para o Supervisor (registro e consulta) e 404 para área inexistente.
 */
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

import br.edu.safeplace.backend.application.dto.input.RegistrarInspecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.InspecaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarInspecaoUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({InspecaoController.class, ApiExceptionHandler.class})
@Import(SecurityConfig.class)
class InspecaoControllerTest {

    private static final String CORPO_VALIDO = """
            {"dataInspecao": "2026-10-01", "itens": [
              {"descricao": "NR-12: proteções fixas", "conformidade": true},
              {"descricao": "NR-26: sinalização", "conformidade": false, "observacoes": "Faixa apagada"}]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarInspecaoUseCase useCase;

    private static InspecaoOutputDTO inspecao() {
        return new InspecaoOutputDTO(10, 4, LocalDate.of(2026, 10, 1), 1, "Gestora Ana", 1, List.of(
                new InspecaoOutputDTO.Item(20, "NR-12: proteções fixas", true, null),
                new InspecaoOutputDTO.Item(21, "NR-26: sinalização", false, "Faixa apagada")));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor registra inspeção e recebe 201 com itens e não conformidades")
    void gestorRegistra() throws Exception {
        when(useCase.registrar(eq(4), any(RegistrarInspecaoInputDTO.class))).thenReturn(inspecao());

        mockMvc.perform(post("/api/areas-risco/4/inspecoes").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.dataInspecao").value("2026-10-01"))
                .andExpect(jsonPath("$.responsavelNome").value("Gestora Ana"))
                .andExpect(jsonPath("$.naoConformidades").value(1))
                .andExpect(jsonPath("$.itens[1].conformidade").value(false))
                .andExpect(jsonPath("$.itens[1].observacoes").value("Faixa apagada"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Sem data, sem itens ou com item sem resultado responde 400 sem chamar o caso de uso")
    void validacaoDaRequisicao() throws Exception {
        for (String corpo : List.of(
                "{\"itens\": [{\"descricao\": \"NR-26\", \"conformidade\": true}]}",
                "{\"dataInspecao\": \"2026-10-01\", \"itens\": []}",
                "{\"dataInspecao\": \"2026-10-01\", \"itens\": [{\"descricao\": \"NR-26\"}]}",
                "{\"dataInspecao\": \"2026-10-01\", \"itens\": [{\"descricao\": \" \", \"conformidade\": true}]}")) {
            mockMvc.perform(post("/api/areas-risco/4/inspecoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
        verify(useCase, never()).registrar(any(), any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Data futura recusada pelo domínio responde 400 com a mensagem")
    void dataFutura() throws Exception {
        when(useCase.registrar(eq(4), any(RegistrarInspecaoInputDTO.class)))
                .thenThrow(new InspecaoInvalidaException("A data da inspeção não pode ser posterior a hoje."));

        mockMvc.perform(post("/api/areas-risco/4/inspecoes").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A data da inspeção não pode ser posterior a hoje."));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Adapter (#236): sem Gestor identificável pela porta do operador, o registro responde 403")
    void semGestorIdentificavelResponde403() throws Exception {
        String mensagem = "O registro de inspeção exige um Gestor de Segurança autenticado como responsável.";
        when(useCase.registrar(eq(4), any(RegistrarInspecaoInputDTO.class))).thenThrow(new AcessoNegadoException(mensagem));

        mockMvc.perform(post("/api/areas-risco/4/inspecoes").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acesso Proibido"))
                .andExpect(jsonPath("$.message").value(mensagem));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Área inexistente responde 404 no registro e na consulta")
    void areaInexistente() throws Exception {
        when(useCase.registrar(eq(99), any(RegistrarInspecaoInputDTO.class))).thenThrow(new AreaRiscoNaoEncontradaException(99));
        when(useCase.listarPorArea(99)).thenThrow(new AreaRiscoNaoEncontradaException(99));

        mockMvc.perform(post("/api/areas-risco/99/inspecoes").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/areas-risco/99/inspecoes"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor consulta o histórico de inspeções da área")
    void gestorConsulta() throws Exception {
        when(useCase.listarPorArea(4)).thenReturn(List.of(inspecao()));

        mockMvc.perform(get("/api/areas-risco/4/inspecoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itens.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor não registra nem consulta inspeções enquanto o grupo não decidir")
    void supervisorRecusado() throws Exception {
        mockMvc.perform(post("/api/areas-risco/4/inspecoes").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/areas-risco/4/inspecoes"))
                .andExpect(status().isForbidden());
        verify(useCase, never()).registrar(any(), any());
        verify(useCase, never()).listarPorArea(any());
    }
}

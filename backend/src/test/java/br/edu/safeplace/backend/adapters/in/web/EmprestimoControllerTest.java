/*
 * Testes MockMvc do controlador de empréstimos (issue #106): contratos HTTP, validação do corpo,
 * mapeamento das exceções (404, 409, 422) e a matriz de perfis do UC12 (Supervisor e Gestor
 * operam; ausência de autenticação recebe 401).
 */
package br.edu.safeplace.backend.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.safeplace.backend.application.dto.input.RegistrarDevolucaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarEntregaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase.Situacao;
import br.edu.safeplace.backend.config.RestSegurancaTratadorExcecoes;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.emprestimo.exception.DuplicidadeDePosseException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoNaoEncontradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;

@WebMvcTest({EmprestimoController.class, EmprestimoExceptionHandler.class})
@Import({SecurityConfig.class, RestSegurancaTratadorExcecoes.class})
class EmprestimoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ControlarEmprestimoUseCase useCase;

    private static EmprestimoOutputDTO emprestimoAtivo() {
        return new EmprestimoOutputDTO(7, 1, "Capacete", 3, "João da Silva", 2,
                LocalDateTime.of(2026, 9, 21, 9, 0), LocalDate.of(2026, 9, 28), null, null, null,
                ClassificacaoEmprestimo.INDISPONIVEL, false, "Termo de cautela", null);
    }

    private static final String CORPO_ENTREGA = """
            {"epiId": 1, "colaboradorId": 3, "dataDevolucaoPrevista": "2026-09-28"}
            """;

    @Test
    @WithMockUser(username = "supervisor@safeplace.test", roles = "SUPERVISOR")
    void supervisorRegistraEntregaComOperadorDoToken() throws Exception {
        when(useCase.registrarEntrega(any(), eq("supervisor@safeplace.test"))).thenReturn(emprestimoAtivo());

        mockMvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(CORPO_ENTREGA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("INDISPONIVEL"))
                .andExpect(jsonPath("$.colaboradorNome").value("João da Silva"))
                .andExpect(jsonPath("$.termoCautela").value("Termo de cautela"))
                .andExpect(jsonPath("$.atrasado").value(false));

        ArgumentCaptor<RegistrarEntregaInputDTO> entrada = ArgumentCaptor.forClass(RegistrarEntregaInputDTO.class);
        verify(useCase).registrarEntrega(entrada.capture(), eq("supervisor@safeplace.test"));
        org.assertj.core.api.Assertions.assertThat(entrada.getValue().confirmarSubstituicao()).isFalse();
        org.assertj.core.api.Assertions.assertThat(entrada.getValue().dataDevolucaoPrevista())
                .isEqualTo(LocalDate.of(2026, 9, 28));
    }

    @Test
    @WithMockUser(username = "gestor@safeplace.test", roles = "GESTOR_SEGURANCA")
    void gestorTambemOperaEmprestimos() throws Exception {
        when(useCase.registrarEntrega(any(), eq("gestor@safeplace.test"))).thenReturn(emprestimoAtivo());

        mockMvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(CORPO_ENTREGA))
                .andExpect(status().isCreated());
    }

    @Test
    void semAutenticacaoRecebe401ENaoChamaOCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(CORPO_ENTREGA))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/emprestimos")).andExpect(status().isUnauthorized());

        verify(useCase, never()).registrarEntrega(any(), any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void corpoSemCamposObrigatoriosRecebe400() throws Exception {
        mockMvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"epiId\": 1}"))
                .andExpect(status().isBadRequest());

        verify(useCase, never()).registrarEntrega(any(), any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void entregaImpedidaRecebe422ComMotivo() throws Exception {
        when(useCase.registrarEntrega(any(), any()))
                .thenThrow(new EntregaImpedidaException("O EPI Capacete está sem saldo disponível em estoque."));

        mockMvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(CORPO_ENTREGA))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Entrega Impedida"))
                .andExpect(jsonPath("$.message").value("O EPI Capacete está sem saldo disponível em estoque."));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void duplicidadeRecebe409ComEmprestimoAtivo() throws Exception {
        when(useCase.registrarEntrega(any(), any()))
                .thenThrow(new DuplicidadeDePosseException(4, "PROTECAO_DE_CABECA"));

        mockMvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(CORPO_ENTREGA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicidade de Posse"))
                .andExpect(jsonPath("$.emprestimoAtivoId").value(4));
    }

    @Test
    @WithMockUser(username = "supervisor@safeplace.test", roles = "SUPERVISOR")
    void devolucaoEnviaEstadoEOperador() throws Exception {
        EmprestimoOutputDTO devolvido = new EmprestimoOutputDTO(7, 1, "Capacete", 3, "João da Silva", 2,
                LocalDateTime.of(2026, 9, 21, 9, 0), LocalDate.of(2026, 9, 28),
                LocalDateTime.of(2026, 9, 25, 17, 0), 2, EstadoDevolucao.MANUTENCAO,
                ClassificacaoEmprestimo.DISPONIVEL, false, "Termo de cautela", null);
        when(useCase.registrarDevolucao(eq(7), any(), eq("supervisor@safeplace.test"))).thenReturn(devolvido);

        mockMvc.perform(post("/api/emprestimos/7/devolucao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"MANUTENCAO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.estadoDevolucao").value("MANUTENCAO"));

        ArgumentCaptor<RegistrarDevolucaoInputDTO> entrada = ArgumentCaptor.forClass(RegistrarDevolucaoInputDTO.class);
        verify(useCase).registrarDevolucao(eq(7), entrada.capture(), eq("supervisor@safeplace.test"));
        org.assertj.core.api.Assertions.assertThat(entrada.getValue().estado()).isEqualTo(EstadoDevolucao.MANUTENCAO);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void devolucaoSemEstadoRecebe400EEmprestimoInexistente404() throws Exception {
        mockMvc.perform(post("/api/emprestimos/7/devolucao").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        when(useCase.registrarDevolucao(eq(99), any(), any())).thenThrow(new EmprestimoNaoEncontradoException(99));
        mockMvc.perform(post("/api/emprestimos/99/devolucao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"APTO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void consultaFiltraPorSituacaoEColaborador() throws Exception {
        when(useCase.listar(Situacao.ATRASADOS, 3)).thenReturn(List.of(emprestimoAtivo()));

        mockMvc.perform(get("/api/emprestimos").param("situacao", "ATRASADOS").param("colaboradorId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].epiNome").value("Capacete"));

        verify(useCase).listar(Situacao.ATRASADOS, 3);
        verify(useCase, never()).listar(eq(Situacao.TODOS), anyInt());
    }
}

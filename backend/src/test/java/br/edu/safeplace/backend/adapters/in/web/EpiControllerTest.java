package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest({EpiController.class, EpiExceptionHandler.class})
@Import(SecurityConfig.class)
@WithMockUser(roles = "SUPERVISOR")
class EpiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarEpiUseCase useCase;

    @Test
    void deveCriarEpiComSucesso() throws Exception {
        EpiOutputDTO epiSalvo = new EpiOutputDTO(1, "Capacete H-700", "CA-12345", 20, 5,
                StatusEpi.DISPONIVEL, LocalDate.of(2028, 6, 30), 365, false, null, null);

        when(useCase.cadastrarEpi(any(CadastrarEpiInputDTO.class))).thenReturn(epiSalvo);

        String jsonRequest = """
                {
                    "nome": "Capacete H-700",
                    "numeroCa": "CA-12345",
                    "quantidade": 20,
                    "estoqueMinimo": 5,
                    "dataValidadeCa": "2028-06-30",
                    "vidaUtilDias": 365
                }
                """;

        mockMvc.perform(post("/api/epis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Capacete H-700"))
                .andExpect(jsonPath("$.numeroCa").value("CA-12345"))
                .andExpect(jsonPath("$.quantidade").value(20))
                .andExpect(jsonPath("$.estoqueMinimo").value(5))
                .andExpect(jsonPath("$.estoqueCritico").value(false))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));
    }

    @Test
    void deveListarEpis() throws Exception {
        EpiOutputDTO epi1 = new EpiOutputDTO(1, "Capacete", "CA-1", 10, 2, StatusEpi.DISPONIVEL, null, null, false, null, null);
        EpiOutputDTO epi2 = new EpiOutputDTO(2, "Luva", "CA-2", 0, 5, StatusEpi.ESGOTADO, null, null, true, null, null);

        when(useCase.listar()).thenReturn(List.of(epi1, epi2));

        mockMvc.perform(get("/api/epis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Capacete"))
                .andExpect(jsonPath("$[1].status").value("ESGOTADO"))
                .andExpect(jsonPath("$[1].estoqueCritico").value(true));
    }

    @Test
    void deveRegistrarMovimentacaoDeEntrada() throws Exception {
        MovimentacaoEstoqueOutputDTO mov = new MovimentacaoEstoqueOutputDTO(1, 1, TipoMovimentacao.ENTRADA, 10,
                LocalDateTime.of(2026, 9, 6, 16, 0), "Reposição", 30);

        when(useCase.registrarMovimentacao(eq(1), eq(TipoMovimentacao.ENTRADA), eq(10), eq("Reposição")))
                .thenReturn(mov);

        String jsonRequest = """
                {
                    "tipo": "ENTRADA",
                    "quantidade": 10,
                    "motivo": "Reposição"
                }
                """;

        mockMvc.perform(post("/api/epis/1/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.epiId").value(1))
                .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.quantidade").value(10))
                .andExpect(jsonPath("$.saldoAtual").value(30))
                .andExpect(jsonPath("$.motivo").value("Reposição"));
    }

    @Test
    void deveRetornarBadRequestQuandoSaldoForInsuficiente() throws Exception {
        when(useCase.registrarMovimentacao(eq(1), eq(TipoMovimentacao.SAIDA), eq(50), any()))
                .thenThrow(new SaldoInsuficienteException("Saldo insuficiente em estoque."));

        String jsonRequest = """
                {
                    "tipo": "SAIDA",
                    "quantidade": 50,
                    "motivo": "Tentativa excessiva"
                }
                """;

        mockMvc.perform(post("/api/epis/1/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Saldo Insuficiente"))
                .andExpect(jsonPath("$.message").value("Saldo insuficiente em estoque."));
    }

    @Test
    void deveRetornarNotFoundQuandoEpiNaoExistir() throws Exception {
        when(useCase.registrarMovimentacao(eq(999), any(), anyInt(), any()))
                .thenThrow(new EpiNaoEncontradoException(999));

        String jsonRequest = """
                {
                    "tipo": "ENTRADA",
                    "quantidade": 5,
                    "motivo": "Teste inexistente"
                }
                """;

        mockMvc.perform(post("/api/epis/999/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"))
                .andExpect(jsonPath("$.message").value("EPI com ID 999 não encontrado."));
    }
}

package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.AtualizarResponsavelInputDTO;
import br.edu.safeplace.backend.application.dto.input.AtualizarStatusPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ConfigurarAlertaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ProrrogarPrazoInputDTO;
import br.edu.safeplace.backend.application.dto.output.PlanoDeAcaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProrrogacaoPrazoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarPlanoDeAcaoUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPlanoNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({PlanoDeAcaoController.class, ApiExceptionHandler.class})
@Import(SecurityConfig.class)
class PlanoDeAcaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarPlanoDeAcaoUseCase useCase;

    private static PlanoDeAcaoOutputDTO acaoDTO(Integer id, Integer idOcorrencia) {
        return new PlanoDeAcaoOutputDTO(
                id,
                idOcorrencia,
                "Isolar painel de energia",
                "Instalar trava LOTO permanente",
                LocalDate.of(2026, 10, 15),
                "ABERTA",
                "ABERTA",
                "COLABORADOR",
                10,
                "Carlos Eletricista",
                true,
                3,
                LocalDate.of(2026, 10, 12),
                true,
                false,
                1,
                "Gestor Seguranca"
        );
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve cadastrar ação do plano via POST /api/ocorrencias/{idOcorrencia}/planos-acao")
    void deveCadastrarAcao() throws Exception {
        when(useCase.cadastrarAcao(eq(1), any(CriarAcaoPlanoInputDTO.class)))
                .thenReturn(acaoDTO(100, 1));

        String payload = """
                {
                    "medidasCorretivas": "Isolar painel de energia",
                    "medidasPreventivas": "Instalar trava LOTO permanente",
                    "prazo": "2026-10-15",
                    "responsavelColaboradorId": 10,
                    "alertaAtivo": true,
                    "antecedenciaAlertaDias": 3
                }
                """;

        mockMvc.perform(post("/api/ocorrencias/1/planos-acao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.idOcorrencia").value(1))
                .andExpect(jsonPath("$.responsavelTipo").value("COLABORADOR"))
                .andExpect(jsonPath("$.alertaAtivo").value(true));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve retornar 400 ao tentar cadastrar plano em ocorrencia grave sem acao preventiva")
    void deveRetornar400AoFaltarAcaoPreventiva() throws Exception {
        when(useCase.cadastrarAcao(eq(1), any(CriarAcaoPlanoInputDTO.class)))
                .thenThrow(new AcaoPreventivaObrigatoriaException(1));

        String payload = """
                {
                    "medidasCorretivas": "Socorro imediato",
                    "prazo": "2026-10-15",
                    "responsavelColaboradorId": 10
                }
                """;

        mockMvc.perform(post("/api/ocorrencias/1/planos-acao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ação Preventiva Obrigatória"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve retornar 400 com prazo retroativo")
    void deveRetornar400ComPrazoRetroativo() throws Exception {
        when(useCase.cadastrarAcao(eq(1), any(CriarAcaoPlanoInputDTO.class)))
                .thenThrow(new PrazoAcaoInvalidoException("O prazo da ação não pode ser anterior à data do fato"));

        String payload = """
                {
                    "medidasCorretivas": "Isolar",
                    "prazo": "2020-01-01",
                    "responsavelColaboradorId": 10
                }
                """;

        mockMvc.perform(post("/api/ocorrencias/1/planos-acao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Prazo Inválido"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve listar ações do plano por ocorrencia via GET /api/ocorrencias/{idOcorrencia}/planos-acao")
    void deveListarAcoesPorOcorrencia() throws Exception {
        when(useCase.listarPorOcorrencia(1)).thenReturn(List.of(acaoDTO(100, 1)));

        mockMvc.perform(get("/api/ocorrencias/1/planos-acao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].idOcorrencia").value(1))
                .andExpect(jsonPath("$[0].alertaDevido").value(true))
                .andExpect(jsonPath("$[0].emAtraso").value(false));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Ação inexistente responde 404 na consulta e nas alterações (issue #196)")
    void acaoInexistenteResponde404() throws Exception {
        when(useCase.buscarPorId(999)).thenThrow(new AcaoPlanoNaoEncontradaException(999));
        when(useCase.atualizarStatus(eq(999), any(AtualizarStatusPlanoInputDTO.class)))
                .thenThrow(new AcaoPlanoNaoEncontradaException(999));
        when(useCase.configurarAlerta(eq(999), any(ConfigurarAlertaInputDTO.class)))
                .thenThrow(new AcaoPlanoNaoEncontradaException(999));

        mockMvc.perform(get("/api/planos-acao/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"))
                .andExpect(jsonPath("$.message").value("Ação do plano não encontrada para o ID: 999"));
        mockMvc.perform(patch("/api/planos-acao/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CONCLUIDA\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/planos-acao/999/alerta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\": true, \"antecedenciaDias\": 2}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve atualizar status da ação via PATCH /api/planos-acao/{id}/status")
    void deveAtualizarStatus() throws Exception {
        when(useCase.atualizarStatus(eq(100), any(AtualizarStatusPlanoInputDTO.class)))
                .thenReturn(acaoDTO(100, 1));

        String payload = """
                {
                    "status": "CONCLUIDA"
                }
                """;

        mockMvc.perform(patch("/api/planos-acao/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve prorrogar prazo com justificativa via POST /api/planos-acao/{id}/prorrogacoes")
    void deveProrrogarPrazo() throws Exception {
        ProrrogacaoPrazoOutputDTO prorrogacaoDTO = new ProrrogacaoPrazoOutputDTO(
                100, LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 30), "Atraso de componentes importados"
        );
        when(useCase.prorrogarPrazo(eq(100), any(ProrrogarPrazoInputDTO.class)))
                .thenReturn(prorrogacaoDTO);

        String payload = """
                {
                    "novoPrazo": "2026-10-30",
                    "justificativa": "Atraso de componentes importados"
                }
                """;

        mockMvc.perform(post("/api/planos-acao/100/prorrogacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAcao").value(100))
                .andExpect(jsonPath("$.novoPrazo").value("2026-10-30"))
                .andExpect(jsonPath("$.justificativa").value("Atraso de componentes importados"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve listar historico de prorrogacoes via GET /api/planos-acao/{id}/prorrogacoes")
    void deveListarProrrogacoes() throws Exception {
        ProrrogacaoPrazoOutputDTO prorrogacaoDTO = new ProrrogacaoPrazoOutputDTO(
                100, LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 30), "Atraso de componentes"
        );
        when(useCase.listarProrrogacoes(100)).thenReturn(List.of(prorrogacaoDTO));

        mockMvc.perform(get("/api/planos-acao/100/prorrogacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAcao").value(100))
                .andExpect(jsonPath("$[0].justificativa").value("Atraso de componentes"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve reatribuir responsavel via PATCH /api/planos-acao/{id}/responsavel")
    void deveAtualizarResponsavel() throws Exception {
        when(useCase.atualizarResponsavel(eq(100), any(AtualizarResponsavelInputDTO.class)))
                .thenReturn(acaoDTO(100, 1));

        String payload = """
                {
                    "setorId": 20
                }
                """;

        mockMvc.perform(patch("/api/planos-acao/100/responsavel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve configurar alerta via PATCH /api/planos-acao/{id}/alerta")
    void deveConfigurarAlerta() throws Exception {
        when(useCase.configurarAlerta(eq(100), any(ConfigurarAlertaInputDTO.class)))
                .thenReturn(acaoDTO(100, 1));

        String payload = """
                {
                    "ativo": true,
                    "antecedenciaDias": 5
                }
                """;

        mockMvc.perform(patch("/api/planos-acao/100/alerta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }
}

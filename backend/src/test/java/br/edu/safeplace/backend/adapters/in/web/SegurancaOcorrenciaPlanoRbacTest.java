/*
 * Testes da matriz de perfis (RNF03) nas rotas de ocorrências, planos de ação e notificações de novos
 * relatos: 401 sem token, 403 para o perfil sem permissão e acesso liberado conforme o SecurityConfig.
 */
package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.input.AtualizarStatusPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.PlanoDeAcaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesAcaoUseCase;
import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarPlanoDeAcaoUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.GerenciadorToken;
import br.edu.safeplace.backend.config.RestSegurancaExceptionHandler;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        OcorrenciaController.class,
        PlanoDeAcaoController.class,
        NotificacaoOcorrenciaController.class,
        NotificacaoAcaoController.class
})
@Import({SecurityConfig.class, RestSegurancaExceptionHandler.class})
@DisplayName("Testes de Integração de Segurança e RBAC para Ocorrências e Planos de Ação (Issue #99)")
class SegurancaOcorrenciaPlanoRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarOcorrenciaUseCase registrarOcorrenciaUseCase;

    @MockitoBean
    private GerenciarOcorrenciaUseCase gerenciarOcorrenciaUseCase;

    @MockitoBean
    private GerenciarPlanoDeAcaoUseCase gerenciarPlanoDeAcaoUseCase;

    @MockitoBean
    private ConsultarNotificacoesOcorrenciaUseCase consultarNotificacoesUseCase;

    @MockitoBean
    private ConsultarNotificacoesAcaoUseCase consultarNotificacoesAcaoUseCase;

    @MockitoBean
    private GerenciadorToken gerenciadorToken;

    private static final String JSON_CRIAR_OCORRENCIA = """
            {
                "tipoOcorrencia": "ACIDENTE",
                "dataOcorrencia": "2026-09-20T10:00:00",
                "descricao": "Queda de material",
                "causaRaiz": "FALHA_EPI",
                "tipo": "Típico",
                "dano": "Leve",
                "destino": "Ambulatório",
                "gravidade": "BAIXO",
                "areaId": 1,
                "colaboradorId": 1
            }
            """;

    private static final String JSON_PLANO = """
            {
                "medidasCorretivas": "Isolar área",
                "prazo": "2026-10-15",
                "responsavelColaboradorId": 1
            }
            """;

    private static final String JSON_STATUS_PLANO = """
            {
                "status": "CONCLUIDA"
            }
            """;

    private static final String JSON_PRORROGACAO = """
            {
                "novoPrazo": "2026-10-30",
                "justificativa": "Aguardando peças"
            }
            """;

    private static final String JSON_RESPONSAVEL = """
            {
                "colaboradorId": 2
            }
            """;

    private static final String JSON_ALERTA = """
            {
                "ativo": true,
                "antecedenciaDias": 3
            }
            """;

    private OcorrenciaOutputDTO mockOcorrencia() {
        return new OcorrenciaOutputDTO(
                1, "ACIDENTE", LocalDateTime.now(), LocalDateTime.now(), "ABERTA",
                "Setor A", "Queda de material", null, "FALHA_EPI", "Típico", "Leve",
                "CAT-2026-09-0001", "CAT-2026-09-0001", "Ambulatório", null, null, "BAIXO",
                1, "Setor A", 1, "João", 3, "Sérgio", "SUPERVISOR"
        );
    }

    private PlanoDeAcaoOutputDTO mockPlano() {
        return new PlanoDeAcaoOutputDTO(
                10, 1, "Isolar área", null, LocalDate.now().plusDays(10), "ABERTA", "ABERTA",
                "COLABORADOR", 1, "João", false, null, null, false, false, 2, "Gestor"
        );
    }

    @Nested
    @DisplayName("1. Acesso Não Autenticado (401 Unauthorized)")
    class AcessoNaoAutenticado {

        @Test
        @DisplayName("GET /api/ocorrencias sem token deve retornar 401")
        void getOcorrenciasSemTokenRetorna401() throws Exception {
            mockMvc.perform(get("/api/ocorrencias"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("POST /api/ocorrencias sem token deve retornar 401")
        void postOcorrenciasSemTokenRetorna401() throws Exception {
            mockMvc.perform(post("/api/ocorrencias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_CRIAR_OCORRENCIA))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("GET /api/notificacoes/acoes sem token deve retornar 401")
        void getAvisosDeAcaoSemTokenRetorna401() throws Exception {
            mockMvc.perform(get("/api/notificacoes/acoes"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/notificacoes sem token deve retornar 401")
        void getNotificacoesSemTokenRetorna401() throws Exception {
            mockMvc.perform(get("/api/notificacoes"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/planos-acao/1 sem token deve retornar 401")
        void getPlanosAcaoSemTokenRetorna401() throws Exception {
            mockMvc.perform(get("/api/planos-acao/1"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("PATCH /api/ocorrencias/1/triagem sem token deve retornar 401")
        void patchTriagemSemTokenRetorna401() throws Exception {
            mockMvc.perform(patch("/api/ocorrencias/1/triagem"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }
    }

    @Nested
    @DisplayName("2. Supervisor sem permissão para gestão (403 Forbidden)")
    class AcessoSupervisorRestrito {

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode realizar triagem de ocorrência (UC05)")
        void supervisorNaoPodeFazerTriagem() throws Exception {
            mockMvc.perform(patch("/api/ocorrencias/1/triagem"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não consulta os avisos das ações do plano (UC07, issue #196)")
        void supervisorNaoPodeConsultarAvisosDeAcao() throws Exception {
            mockMvc.perform(get("/api/notificacoes/acoes"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não consulta notificações de novos relatos (UC09, passo 12)")
        void supervisorNaoPodeConsultarNotificacoes() throws Exception {
            mockMvc.perform(get("/api/notificacoes"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode arquivar ocorrência")
        void supervisorNaoPodeArquivar() throws Exception {
            mockMvc.perform(patch("/api/ocorrencias/1/arquivar"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode consolidar CAT")
        void supervisorNaoPodeConsolidarCat() throws Exception {
            mockMvc.perform(patch("/api/ocorrencias/1/consolidar-cat"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode cadastrar ação no plano (UC07)")
        void supervisorNaoPodeCadastrarAcao() throws Exception {
            mockMvc.perform(post("/api/ocorrencias/1/planos-acao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_PLANO))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode atualizar status de ação")
        void supervisorNaoPodeAtualizarStatusAcao() throws Exception {
            mockMvc.perform(patch("/api/planos-acao/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_STATUS_PLANO))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode prorrogar prazo de ação")
        void supervisorNaoPodeProrrogarAcao() throws Exception {
            mockMvc.perform(post("/api/planos-acao/1/prorrogacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_PRORROGACAO))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode reatribuir responsável de ação")
        void supervisorNaoPodeReatribuirResponsavel() throws Exception {
            mockMvc.perform(patch("/api/planos-acao/1/responsavel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_RESPONSAVEL))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode configurar alerta de ação")
        void supervisorNaoPodeConfigurarAlerta() throws Exception {
            mockMvc.perform(put("/api/planos-acao/1/alerta")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ALERTA))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }
    }

    @Nested
    @DisplayName("3. Acesso Autorizado para Gestor e Supervisor (2xx Sucesso)")
    class AcessoAutorizado {

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode registrar ocorrência (UC09)")
        void supervisorPodeRegistrarOcorrencia() throws Exception {
            when(registrarOcorrenciaUseCase.registrarAcidente(any(RegistrarAcidenteInputDTO.class)))
                    .thenReturn(mockOcorrencia());

            mockMvc.perform(post("/api/ocorrencias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_CRIAR_OCORRENCIA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.idOcorrencia").value(1));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode consultar ocorrências")
        void supervisorPodeConsultarOcorrencias() throws Exception {
            when(registrarOcorrenciaUseCase.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(List.of(mockOcorrencia()));

            mockMvc.perform(get("/api/ocorrencias"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode consultar detalhes de plano de ação")
        void supervisorPodeConsultarPlanoDeAcao() throws Exception {
            when(gerenciarPlanoDeAcaoUseCase.buscarPorId(1)).thenReturn(mockPlano());

            mockMvc.perform(get("/api/planos-acao/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança pode realizar triagem (UC05)")
        void gestorPodeFazerTriagem() throws Exception {
            when(gerenciarOcorrenciaUseCase.enviarParaTriagem(1))
                    .thenReturn(mockOcorrencia());

            mockMvc.perform(patch("/api/ocorrencias/1/triagem"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idOcorrencia").value(1));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança pode cadastrar ação no plano (UC07)")
        void gestorPodeCadastrarAcao() throws Exception {
            when(gerenciarPlanoDeAcaoUseCase.cadastrarAcao(eq(1), any(CriarAcaoPlanoInputDTO.class)))
                    .thenReturn(mockPlano());

            mockMvc.perform(post("/api/ocorrencias/1/planos-acao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_PLANO))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança pode atualizar status de ação no plano (UC07)")
        void gestorPodeAtualizarStatusAcao() throws Exception {
            when(gerenciarPlanoDeAcaoUseCase.atualizarStatus(eq(1), any(AtualizarStatusPlanoInputDTO.class)))
                    .thenReturn(mockPlano());

            mockMvc.perform(patch("/api/planos-acao/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_STATUS_PLANO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança consulta notificações de novos relatos (UC09, passo 12)")
        void gestorPodeConsultarNotificacoes() throws Exception {
            when(consultarNotificacoesUseCase.listar()).thenReturn(List.of(new NotificacaoOcorrencia(
                    5, 1, TipoOcorrencia.ACIDENTE, "CAT-2026-09-0001", "Setor A",
                    "Novo acidente registrado (protocolo CAT-2026-09-0001) na área Setor A. Aguarda triagem.",
                    LocalDateTime.of(2026, 9, 20, 10, 5))));

            mockMvc.perform(get("/api/notificacoes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(5))
                    .andExpect(jsonPath("$[0].ocorrenciaId").value(1))
                    .andExpect(jsonPath("$[0].tipoOcorrencia").value("ACIDENTE"))
                    .andExpect(jsonPath("$[0].numeroProtocoloCAT").value("CAT-2026-09-0001"));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança consulta os avisos de prazo e de reatribuição das ações (UC07, issue #196)")
        void gestorPodeConsultarAvisosDeAcao() throws Exception {
            when(consultarNotificacoesAcaoUseCase.listar()).thenReturn(List.of(new NotificacaoAcao(
                    8, 10, 1, MotivoAvisoAcao.REATRIBUICAO, "SETOR", 4, "Manutenção",
                    LocalDate.of(2026, 10, 25), LocalDate.of(2026, 10, 18),
                    "Ação #10 da ocorrência #1 reatribuída a setor Manutenção. Prazo: 25/10/2026. Comunique o novo responsável.",
                    LocalDateTime.of(2026, 10, 18, 9, 30))));

            mockMvc.perform(get("/api/notificacoes/acoes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(8))
                    .andExpect(jsonPath("$[0].idAcao").value(10))
                    .andExpect(jsonPath("$[0].motivo").value("REATRIBUICAO"))
                    .andExpect(jsonPath("$[0].responsavelTipo").value("SETOR"))
                    .andExpect(jsonPath("$[0].prazo").value("2026-10-25"));
        }
    }
}

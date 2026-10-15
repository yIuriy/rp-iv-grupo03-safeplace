package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.input.LoginEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TokenSaidaDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioCasoDeUso;
import br.edu.safeplace.backend.application.port.in.ControlarManutencaoEpiUseCase;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.application.port.out.TokenPorta;
import br.edu.safeplace.backend.config.RestSegurancaTratadorExcecoes;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.usuario.Perfil;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        UsuarioControlador.class,
        EpiController.class,
        AreaRiscoController.class,
        AutenticacaoControlador.class
})
@Import({SecurityConfig.class, RestSegurancaTratadorExcecoes.class})
@DisplayName("Testes de Integração de Segurança e Controle de Acesso (RBAC)")
class SegurancaControleAcessoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarUsuarioCasoDeUso gerenciarUsuarioCasoDeUso;

    @MockitoBean
    private GerenciarEpiUseCase gerenciarEpiUseCase;

    @MockitoBean
    private ControlarManutencaoEpiUseCase controlarManutencaoEpiUseCase;

    @MockitoBean
    private GerenciarAreaRiscoUseCase gerenciarAreaRiscoUseCase;

    @MockitoBean
    private AutenticarUsuarioCasoDeUso autenticarUsuarioCasoDeUso;

    @MockitoBean
    private TokenPorta tokenPorta;

    private static final String JSON_EPI = """
            {
                "nome": "Capacete H-700",
                "numeroCa": "CA-12345",
                "quantidade": 20,
                "estoqueMinimo": 5,
                "dataValidadeCa": "2028-06-30",
                "vidaUtilDias": 365,
                "classificacao": "PROTECAO_DE_CABECA"
            }
            """;

    private static final String JSON_MOVIMENTACAO = """
            {
                "tipo": "ENTRADA",
                "quantidade": 10,
                "motivo": "Reposição de estoque"
            }
            """;

    private static final String JSON_MANUTENCAO = """
            {
                "dataManutencao": "2026-09-19T14:30:00",
                "descricao": "Inspeção visual",
                "resultado": "APROVADO"
            }
            """;

    private static final String JSON_SUPERVISOR = """
            {
                "nome": "Carlos Supervisor",
                "cpf": "52998224725",
                "dataNascimento": "1985-05-20",
                "email": "carlos.supervisor@safeplace.com"
            }
            """;

    private static final String JSON_COLABORADOR = """
            {
                "nome": "João Operador",
                "cpf": "52998224725",
                "dataNascimento": "1990-10-15",
                "email": "joao.operador@safeplace.com"
            }
            """;

    private static final String JSON_AREA_RISCO = """
            {
                "codigo": "SET-01",
                "nome": "Área de Pintura",
                "descricao": "Ambiente com vapores orgânicos",
                "nivelPerigo": "ALTO",
                "episObrigatoriosIds": [1]
            }
            """;

    private static final String JSON_LOGIN = """
            {
                "email": "gestor@safeplace.com",
                "senha": "SenhaValida@123"
            }
            """;

    @Nested
    @DisplayName("1. Acesso Não Autenticado (401 Unauthorized)")
    class AcessoNaoAutenticado {

        @Test
        @DisplayName("POST /api/epis sem autenticação deve retornar 401")
        void postEpisSemAutenticacaoRetorna401() throws Exception {
            mockMvc.perform(post("/api/epis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_EPI))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("POST /api/epis/1/movimentacoes sem autenticação deve retornar 401")
        void postMovimentacaoSemAutenticacaoRetorna401() throws Exception {
            mockMvc.perform(post("/api/epis/1/movimentacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_MOVIMENTACAO))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("POST /api/usuarios/supervisores sem autenticação deve retornar 401")
        void postSupervisoresSemAutenticacaoRetorna401() throws Exception {
            mockMvc.perform(post("/api/usuarios/supervisores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_SUPERVISOR))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("GET /api/epis sem autenticação deve retornar 401")
        void getEpisSemAutenticacaoRetorna401() throws Exception {
            mockMvc.perform(get("/api/epis"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("POST /api/epis/1/manutencoes sem autenticação deve retornar 401")
        void postManutencaoSemAutenticacaoRetorna401() throws Exception {
            mockMvc.perform(post("/api/epis/1/manutencoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_MANUTENCAO))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }

        @Test
        @DisplayName("GET /api/usuarios/colaboradores sem autenticação deve retornar 401")
        void getColaboradoresSemAutenticacaoRetorna401() throws Exception {
            mockMvc.perform(get("/api/usuarios/colaboradores"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Não Autorizado"));
        }
    }

    @Nested
    @DisplayName("2. Acesso com Papel Sem Permissão (403 Forbidden)")
    class AcessoPapelSemPermissao {

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode cadastrar EPI (UC01 - restrito ao Gestor)")
        void supervisorNaoPodeCadastrarEpi() throws Exception {
            mockMvc.perform(post("/api/epis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_EPI))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode movimentar estoque de EPI (#132 - restrito ao Gestor)")
        void supervisorNaoPodeMovimentarEstoque() throws Exception {
            mockMvc.perform(post("/api/epis/1/movimentacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_MOVIMENTACAO))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode registrar manutenção de EPI (#103 - restrito ao Gestor)")
        void supervisorNaoPodeRegistrarManutencao() throws Exception {
            mockMvc.perform(post("/api/epis/1/manutencoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_MANUTENCAO))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode cadastrar Supervisor (RF23 - restrito ao Gestor)")
        void supervisorNaoPodeCadastrarSupervisor() throws Exception {
            mockMvc.perform(post("/api/usuarios/supervisores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_SUPERVISOR))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode listar todos os usuários (RF23 - restrito ao Gestor)")
        void supervisorNaoPodeListarUsuarios() throws Exception {
            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode atualizar Supervisor (RF23 - restrito ao Gestor)")
        void supervisorNaoPodeAtualizarSupervisor() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/usuarios/supervisores/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_SUPERVISOR))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode consultar dados de outro Supervisor por ID (RF23)")
        void supervisorNaoPodeConsultarSupervisorPorId() throws Exception {
            when(gerenciarUsuarioCasoDeUso.buscarPorId(1))
                    .thenThrow(new br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException("Supervisor não tem permissão para consultar dados de Supervisor."));

            mockMvc.perform(get("/api/usuarios/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor não pode cadastrar área de risco (UC03 - restrito ao Gestor)")
        void supervisorNaoPodeCadastrarAreaRisco() throws Exception {
            mockMvc.perform(post("/api/areas-risco")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_AREA_RISCO))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Acesso Proibido"));
        }
    }

    @Nested
    @DisplayName("3. Acesso com Papel Autorizado (2xx Sucesso)")
    class AcessoPapelAutorizado {

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança pode cadastrar EPI")
        void gestorPodeCadastrarEpi() throws Exception {
            EpiOutputDTO saida = new EpiOutputDTO(1, "Capacete H-700", "CA-12345", 20, 5,
                    StatusEpi.DISPONIVEL, LocalDate.of(2028, 6, 30), 365, false, null, null);
            when(gerenciarEpiUseCase.cadastrarEpi(any(CadastrarEpiInputDTO.class))).thenReturn(saida);

            mockMvc.perform(post("/api/epis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_EPI))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("Capacete H-700"));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA", username = "gestor@safeplace.com")
        @DisplayName("Gestor de Segurança pode movimentar estoque de EPI")
        void gestorPodeMovimentarEstoque() throws Exception {
            MovimentacaoEstoqueOutputDTO mov = new MovimentacaoEstoqueOutputDTO(1, 1, TipoMovimentacao.ENTRADA, 10,
                    LocalDateTime.now(), "Reposição de estoque", 30);
            when(gerenciarEpiUseCase.registrarMovimentacao(eq(1), eq(TipoMovimentacao.ENTRADA), eq(10), anyString(), anyString()))
                    .thenReturn(mov);

            mockMvc.perform(post("/api/epis/1/movimentacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_MOVIMENTACAO))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.quantidade").value(10));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA", username = "gestor@safeplace.com")
        @DisplayName("Gestor de Segurança pode registrar manutenção de EPI (UC01)")
        void gestorPodeRegistrarManutencao() throws Exception {
            ManutencaoEpiOutputDTO saida = new ManutencaoEpiOutputDTO(
                    1, 1, LocalDateTime.now(), TipoManutencao.PREVENTIVA,
                    "Inspeção visual", ResultadoManutencao.APROVADO, "Técnico", 1, StatusEpi.DISPONIVEL);
            when(controlarManutencaoEpiUseCase.concluirManutencao(any(ConcluirManutencaoInputDTO.class), anyString()))
                    .thenReturn(saida);

            mockMvc.perform(post("/api/epis/1/manutencoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_MANUTENCAO))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança pode cadastrar Supervisor")
        void gestorPodeCadastrarSupervisor() throws Exception {
            UsuarioSaidaDTO supervisor = new UsuarioSaidaDTO(1, "Carlos Supervisor", "52998224725",
                    LocalDate.of(1985, 5, 20), "carlos.supervisor@safeplace.com", Perfil.SUPERVISOR,
                    true, LocalDateTime.now(), "SenhaInicial@123");
            when(gerenciarUsuarioCasoDeUso.cadastrarSupervisor(any(CadastrarSupervisorInputDTO.class))).thenReturn(supervisor);

            mockMvc.perform(post("/api/usuarios/supervisores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_SUPERVISOR))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.perfil").value("SUPERVISOR"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode consultar lista de EPIs (UC06)")
        void supervisorPodeConsultarEpis() throws Exception {
            when(gerenciarEpiUseCase.listar()).thenReturn(List.of());

            mockMvc.perform(get("/api/epis"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode cadastrar Colaborador (RF23)")
        void supervisorPodeCadastrarColaborador() throws Exception {
            UsuarioSaidaDTO colaborador = new UsuarioSaidaDTO(2, "João Operador", "52998224725",
                    LocalDate.of(1990, 10, 15), "joao.operador@safeplace.com", null,
                    true, LocalDateTime.now(), null);
            when(gerenciarUsuarioCasoDeUso.cadastrarColaborador(any(CadastrarColaboradorInputDTO.class))).thenReturn(colaborador);

            mockMvc.perform(post("/api/usuarios/colaboradores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_COLABORADOR))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.perfil").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança também pode cadastrar Colaborador (RF23)")
        void gestorTambemPodeCadastrarColaborador() throws Exception {
            UsuarioSaidaDTO colaborador = new UsuarioSaidaDTO(2, "João Operador", "52998224725",
                    LocalDate.of(1990, 10, 15), "joao.operador@safeplace.com", null,
                    true, LocalDateTime.now(), null);
            when(gerenciarUsuarioCasoDeUso.cadastrarColaborador(any(CadastrarColaboradorInputDTO.class))).thenReturn(colaborador);

            mockMvc.perform(post("/api/usuarios/colaboradores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_COLABORADOR))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.perfil").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode consultar áreas de risco (UC03)")
        void supervisorPodeConsultarAreasRisco() throws Exception {
            when(gerenciarAreaRiscoUseCase.listar()).thenReturn(List.of());

            mockMvc.perform(get("/api/areas-risco"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança pode listar todos os usuários (RF23)")
        void gestorPodeListarUsuarios() throws Exception {
            when(gerenciarUsuarioCasoDeUso.listarUsuarios()).thenReturn(List.of());

            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode listar colaboradores (RF23)")
        void supervisorPodeListarColaboradores() throws Exception {
            when(gerenciarUsuarioCasoDeUso.listarColaboradores(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/usuarios/colaboradores"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR", username = "supervisor@safeplace.com")
        @DisplayName("Supervisor pode consultar o próprio perfil (GET /api/usuarios/me)")
        void supervisorPodeConsultarProprioPerfil() throws Exception {
            UsuarioSaidaDTO supervisor = new UsuarioSaidaDTO(1, "Carlos Supervisor", "52998224725",
                    LocalDate.of(1985, 5, 20), "supervisor@safeplace.com", Perfil.SUPERVISOR,
                    true, LocalDateTime.now(), null);
            when(gerenciarUsuarioCasoDeUso.buscarPorEmail("supervisor@safeplace.com")).thenReturn(supervisor);

            mockMvc.perform(get("/api/usuarios/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("supervisor@safeplace.com"));
        }

        @Test
        @WithMockUser(roles = "SUPERVISOR")
        @DisplayName("Supervisor pode consultar dados de Colaborador por ID (RF23)")
        void supervisorPodeConsultarColaboradorPorId() throws Exception {
            UsuarioSaidaDTO colaborador = new UsuarioSaidaDTO(2, "João Operador", "52998224725",
                    LocalDate.of(1990, 10, 15), "joao.operador@safeplace.com", null,
                    true, LocalDateTime.now(), null);
            when(gerenciarUsuarioCasoDeUso.buscarPorId(2)).thenReturn(colaborador);

            mockMvc.perform(get("/api/usuarios/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.perfil").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "GESTOR_SEGURANCA")
        @DisplayName("Gestor de Segurança pode consultar qualquer usuário por ID (RF23)")
        void gestorPodeConsultarUsuarioPorId() throws Exception {
            UsuarioSaidaDTO supervisor = new UsuarioSaidaDTO(1, "Carlos Supervisor", "52998224725",
                    LocalDate.of(1985, 5, 20), "supervisor@safeplace.com", Perfil.SUPERVISOR,
                    true, LocalDateTime.now(), null);
            when(gerenciarUsuarioCasoDeUso.buscarPorId(1)).thenReturn(supervisor);

            mockMvc.perform(get("/api/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.perfil").value("SUPERVISOR"));
        }
    }

    @Nested
    @DisplayName("4. Rotas Públicas (Sem necessidade de Token)")
    class RotasPublicas {

        @Test
        @DisplayName("POST /api/auth/login é público e não exige autenticação prévia")
        void loginEhPublico() throws Exception {
            TokenSaidaDTO tokenSaida = new TokenSaidaDTO("jwt-token-exemplo", "Bearer",
                    "gestor@safeplace.com", "Gestor", "GESTOR_SEGURANCA");
            when(autenticarUsuarioCasoDeUso.autenticar(any(LoginEntradaDTO.class))).thenReturn(tokenSaida);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_LOGIN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token-exemplo"))
                    .andExpect(jsonPath("$.tipo").value("Bearer"));
        }
    }
}

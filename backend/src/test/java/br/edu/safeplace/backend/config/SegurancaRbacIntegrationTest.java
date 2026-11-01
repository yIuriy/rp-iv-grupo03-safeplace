package br.edu.safeplace.backend.config;

import br.edu.safeplace.backend.adapters.in.web.AutenticacaoController;
import br.edu.safeplace.backend.adapters.in.web.HealthController;
import br.edu.safeplace.backend.adapters.in.web.UsuarioController;
import br.edu.safeplace.backend.adapters.in.web.UsuarioExceptionHandler;
import br.edu.safeplace.backend.adapters.out.seguranca.JwtTokenAdapter;
import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioUseCase;
import br.edu.safeplace.backend.application.port.out.GerenciadorToken;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        HealthController.class,
        UsuarioController.class,
        AutenticacaoController.class,
        UsuarioExceptionHandler.class
})
@Import({
        SecurityConfig.class,
        JwtTokenAdapter.class,
        RestSegurancaExceptionHandler.class
})
@DisplayName("Testes de Integração de Segurança e Filtro JWT (RBAC)")
class SegurancaRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GerenciadorToken gerenciadorToken;

    @MockitoBean
    private GerenciarUsuarioUseCase gerenciarUsuarioUseCase;

    @MockitoBean
    private AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    private String tokenGestor;
    private String tokenSupervisor;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        tokenGestor = "Bearer " + gerenciadorToken.gerarToken("gestor@safeplace.com", Perfil.GESTOR_SEGURANCA.name());
        tokenSupervisor = "Bearer " + gerenciadorToken.gerarToken("supervisor@safeplace.com", Perfil.SUPERVISOR.name());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Rota pública /api/health deve ser acessível sem autenticação (200 OK)")
    void rotaPublicaDeveSerAcessivelSemToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rota protegida sem token deve retornar 401 Unauthorized com JSON estruturado")
    void rotaProtegidaSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/usuarios/colaboradores"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Não Autorizado"));
    }

    @Test
    @DisplayName("Supervisor acessando listagem geral GET /api/usuarios deve retornar 403 Forbidden (RF23)")
    void supervisorListandoTodosUsuariosDeveRetornar403() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", tokenSupervisor))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso Proibido"));
    }

    @Test
    @DisplayName("Gestor de Segurança acessando listagem geral GET /api/usuarios deve retornar 200 OK")
    void gestorListandoTodosUsuariosDeveRetornar200() throws Exception {
        when(gerenciarUsuarioUseCase.listarUsuarios()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", tokenGestor))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Supervisor acessando GET /api/usuarios/colaboradores deve retornar 200 OK")
    void supervisorConsultandoColaboradoresDeveRetornar200() throws Exception {
        when(gerenciarUsuarioUseCase.listarColaboradores(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios/colaboradores")
                        .header("Authorization", tokenSupervisor))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Supervisor tentando cadastrar Supervisor deve retornar 403 Forbidden (RF23)")
    void supervisorTentandoCriarSupervisorDeveRetornar403() throws Exception {
        String jsonSupervisor = """
                {
                    "nome": "Novo Supervisor",
                    "cpf": "52998224725",
                    "dataNascimento": "1988-04-12",
                    "email": "supervisor.novo@safeplace.com"
                }
                """;

        mockMvc.perform(post("/api/usuarios/supervisores")
                        .header("Authorization", tokenSupervisor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSupervisor))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso Proibido"));
    }

    @Test
    @DisplayName("Gestor de Segurança cadastrando Supervisor deve retornar 201 Created (RF23)")
    void gestorCriandoSupervisorDeveRetornar201() throws Exception {
        UsuarioOutputDTO saida = new UsuarioOutputDTO(
                10, "Novo Supervisor", "52998224725", LocalDate.of(1988, 4, 12),
                "supervisor.novo@safeplace.com", Perfil.SUPERVISOR, true, LocalDateTime.now(), "senhaInicial123"
        );
        when(gerenciarUsuarioUseCase.cadastrarSupervisor(any())).thenReturn(saida);

        String jsonSupervisor = """
                {
                    "nome": "Novo Supervisor",
                    "cpf": "52998224725",
                    "dataNascimento": "1988-04-12",
                    "email": "supervisor.novo@safeplace.com"
                }
                """;

        mockMvc.perform(post("/api/usuarios/supervisores")
                        .header("Authorization", tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSupervisor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("SUPERVISOR"));
    }

    @Test
    @DisplayName("Supervisor cadastrando Colaborador deve retornar 201 Created (RF23)")
    void supervisorCriandoColaboradorDeveRetornar201() throws Exception {
        UsuarioOutputDTO saida = new UsuarioOutputDTO(
                11, "Novo Colaborador", "52998224725", LocalDate.of(1992, 6, 18),
                "colaborador@safeplace.com", null, true, LocalDateTime.now(), null
        );
        when(gerenciarUsuarioUseCase.cadastrarColaborador(any())).thenReturn(saida);

        String jsonColaborador = """
                {
                    "nome": "Novo Colaborador",
                    "cpf": "52998224725",
                    "dataNascimento": "1992-06-18",
                    "email": "colaborador@safeplace.com"
                }
                """;

        mockMvc.perform(post("/api/usuarios/colaboradores")
                        .header("Authorization", tokenSupervisor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonColaborador))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").doesNotExist());
    }

    @Test
    @DisplayName("Rota descontinuada POST /api/usuarios deve ser negada com 403 Forbidden")
    void rotaDescontinuadaPostUsuariosNegada() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}

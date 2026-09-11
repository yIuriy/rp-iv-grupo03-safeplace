package br.edu.safeplace.backend.config;

import br.edu.safeplace.backend.adapters.in.web.*;
import br.edu.safeplace.backend.adapters.out.seguranca.JwtTokenAdaptador;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioCasoDeUso;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.application.port.out.TokenPorta;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        HealthController.class,
        UsuarioControlador.class,
        AutenticacaoControlador.class,
        UsuarioTratadorExcecoes.class
})
@Import({
        SecurityConfig.class,
        JwtTokenAdaptador.class,
        RestSegurancaTratadorExcecoes.class
})
class SegurancaRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenPorta tokenPorta;

    @MockitoBean
    private GerenciarUsuarioCasoDeUso gerenciarUsuarioCasoDeUso;

    @MockitoBean
    private AutenticarUsuarioCasoDeUso autenticarUsuarioCasoDeUso;

    private String tokenGestor;
    private String tokenSupervisor;

    @BeforeEach
    void setUp() {
        tokenGestor = "Bearer " + tokenPorta.gerarToken("gestor@safeplace.com", Perfil.GESTOR_SEGURANCA.name());
        tokenSupervisor = "Bearer " + tokenPorta.gerarToken("supervisor@safeplace.com", Perfil.SUPERVISOR.name());
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
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Não Autorizado"));
    }

    @Test
    @DisplayName("Rota protegida com token válido deve retornar 200 OK")
    void rotaProtegidaComTokenDeveRetornar200() throws Exception {
        when(gerenciarUsuarioCasoDeUso.listarUsuarios()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", tokenSupervisor))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Supervisor tentando cadastrar conta de Supervisor deve retornar 403 Forbidden")
    void supervisorTentandoCriarSupervisorDeveRetornar403() throws Exception {
        String jsonSupervisor = """
                {
                    "nome": "Novo Supervisor",
                    "cpf": "52998224725",
                    "dataNascimento": "1988-04-12",
                    "email": "supervisor.novo@safeplace.com",
                    "perfil": "SUPERVISOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", tokenSupervisor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSupervisor))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso Proibido"));
    }

    @Test
    @DisplayName("Gestor de Segurança cadastrando Supervisor deve retornar 201 Created")
    void gestorCriandoSupervisorDeveRetornar201() throws Exception {
        UsuarioSaidaDTO saida = new UsuarioSaidaDTO(
                10, "Novo Supervisor", "52998224725", LocalDate.of(1988, 4, 12),
                "supervisor.novo@safeplace.com", Perfil.SUPERVISOR, true, LocalDateTime.now(), "senhaInicial123"
        );
        when(gerenciarUsuarioCasoDeUso.cadastrarUsuario(any())).thenReturn(saida);

        String jsonSupervisor = """
                {
                    "nome": "Novo Supervisor",
                    "cpf": "52998224725",
                    "dataNascimento": "1988-04-12",
                    "email": "supervisor.novo@safeplace.com",
                    "perfil": "SUPERVISOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSupervisor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("SUPERVISOR"));
    }

    @Test
    @DisplayName("Supervisor cadastrando Colaborador deve retornar 201 Created")
    void supervisorCriandoColaboradorDeveRetornar201() throws Exception {
        UsuarioSaidaDTO saida = new UsuarioSaidaDTO(
                11, "Novo Colaborador", "52998224725", LocalDate.of(1992, 6, 18),
                "colaborador@safeplace.com", Perfil.COLABORADOR, true, LocalDateTime.now(), null
        );
        when(gerenciarUsuarioCasoDeUso.cadastrarUsuario(any())).thenReturn(saida);

        String jsonColaborador = """
                {
                    "nome": "Novo Colaborador",
                    "cpf": "52998224725",
                    "dataNascimento": "1992-06-18",
                    "email": "colaborador@safeplace.com",
                    "perfil": "COLABORADOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", tokenSupervisor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonColaborador))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("COLABORADOR"));
    }
}

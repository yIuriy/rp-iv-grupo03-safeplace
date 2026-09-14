package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroColaboradorDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoints por papel de RF23 (issue #40): permissões, validação de entrada e não exposição de
 * credenciais.
 */
@WebMvcTest({UsuarioControlador.class, UsuarioTratadorExcecoes.class})
@Import(SecurityConfig.class)
class UsuarioControladorPorPapelTest {

    private static final String JSON_SUPERVISOR = """
            {
                "nome": "Joana Ribeiro",
                "cpf": "52998224725",
                "dataNascimento": "1988-04-20",
                "email": "joana.ribeiro@safeplace.com"
            }
            """;

    private static final String JSON_COLABORADOR = """
            {
                "nome": "João da Silva",
                "cpf": "52998224725",
                "dataNascimento": "1992-06-18",
                "email": "joao.silva@safeplace.com"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarUsuarioCasoDeUso casoDeUso;

    private static UsuarioSaidaDTO supervisorSalvo() {
        return new UsuarioSaidaDTO(10, "Joana Ribeiro", "52998224725", LocalDate.of(1988, 4, 20),
                "joana.ribeiro@safeplace.com", Perfil.SUPERVISOR, true, LocalDateTime.now(), "Xk7!pQ2#mA9z");
    }

    private static UsuarioSaidaDTO colaboradorSalvo() {
        return new UsuarioSaidaDTO(11, "João da Silva", "52998224725", LocalDate.of(1992, 6, 18),
                "joao.silva@safeplace.com", Perfil.COLABORADOR, true, LocalDateTime.now(), null);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF23: Gestor cadastra Supervisor e recebe a senha inicial uma única vez")
    void gestorCadastraSupervisorERecebeSenhaInicial() throws Exception {
        when(casoDeUso.cadastrarSupervisor(any(CadastrarSupervisorInputDTO.class)))
                .thenReturn(supervisorSalvo());

        mockMvc.perform(post("/api/usuarios/supervisores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SUPERVISOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.perfil").value("SUPERVISOR"))
                .andExpect(jsonPath("$.senhaInicial").value("Xk7!pQ2#mA9z"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("RF23: Supervisor não pode cadastrar outro Supervisor")
    void supervisorNaoCadastraSupervisor() throws Exception {
        mockMvc.perform(post("/api/usuarios/supervisores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SUPERVISOR))
                .andExpect(status().isForbidden());

        verify(casoDeUso, never()).cadastrarSupervisor(any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("RF23: Supervisor cadastra Colaborador, sem senha inicial na resposta")
    void supervisorCadastraColaborador() throws Exception {
        when(casoDeUso.cadastrarColaborador(any(CadastrarColaboradorInputDTO.class)))
                .thenReturn(colaboradorSalvo());

        mockMvc.perform(post("/api/usuarios/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_COLABORADOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("COLABORADOR"))
                .andExpect(jsonPath("$.senhaInicial").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor também pode cadastrar Colaborador")
    void gestorTambemCadastraColaborador() throws Exception {
        when(casoDeUso.cadastrarColaborador(any(CadastrarColaboradorInputDTO.class)))
                .thenReturn(colaboradorSalvo());

        mockMvc.perform(post("/api/usuarios/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_COLABORADOR))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("RF23: o cadastro de Colaborador recusa um corpo que tente enviar senha")
    void cadastroDeColaboradorRecusaSenha() throws Exception {
        String comSenha = """
                {
                    "nome": "João da Silva",
                    "cpf": "52998224725",
                    "dataNascimento": "1992-06-18",
                    "email": "joao.silva@safeplace.com",
                    "senha": "Segredo@123"
                }
                """;

        mockMvc.perform(post("/api/usuarios/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comSenha))
                .andExpect(status().isBadRequest());

        verify(casoDeUso, never()).cadastrarColaborador(any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("O cadastro de Colaborador recusa uma tentativa de elevar o perfil")
    void cadastroDeColaboradorRecusaPerfil() throws Exception {
        String comPerfil = """
                {
                    "nome": "João da Silva",
                    "cpf": "52998224725",
                    "dataNascimento": "1992-06-18",
                    "perfil": "GESTOR_SEGURANCA"
                }
                """;

        mockMvc.perform(post("/api/usuarios/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comPerfil))
                .andExpect(status().isBadRequest());

        verify(casoDeUso, never()).cadastrarColaborador(any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("O cadastro de Supervisor recusa um corpo que tente definir a senha")
    void cadastroDeSupervisorRecusaSenha() throws Exception {
        String comSenha = """
                {
                    "nome": "Joana Ribeiro",
                    "cpf": "52998224725",
                    "dataNascimento": "1988-04-20",
                    "email": "joana.ribeiro@safeplace.com",
                    "senha": "EuEscolhi@123"
                }
                """;

        mockMvc.perform(post("/api/usuarios/supervisores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comSenha))
                .andExpect(status().isBadRequest());

        verify(casoDeUso, never()).cadastrarSupervisor(any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("CPF sintaticamente inválido é barrado pela validação da requisição")
    void cpfInvalidoEhBarradoNaValidacao() throws Exception {
        String cpfInvalido = """
                {
                    "nome": "Joana Ribeiro",
                    "cpf": "12345678900",
                    "dataNascimento": "1988-04-20",
                    "email": "joana.ribeiro@safeplace.com"
                }
                """;

        mockMvc.perform(post("/api/usuarios/supervisores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cpfInvalido))
                .andExpect(status().isBadRequest());

        verify(casoDeUso, never()).cadastrarSupervisor(any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Supervisor sem e-mail é recusado, pois o e-mail é a credencial de login")
    void supervisorSemEmailEhRecusado() throws Exception {
        String semEmail = """
                {
                    "nome": "Joana Ribeiro",
                    "cpf": "52998224725",
                    "dataNascimento": "1988-04-20"
                }
                """;

        mockMvc.perform(post("/api/usuarios/supervisores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(semEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Duplicidade de CPF responde 409")
    void duplicidadeDeCpfResponde409() throws Exception {
        when(casoDeUso.cadastrarColaborador(any(CadastrarColaboradorInputDTO.class)))
                .thenThrow(new CpfJaCadastradoException("CPF já cadastrado no sistema: 52998224725"));

        mockMvc.perform(post("/api/usuarios/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_COLABORADOR))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Listagem de colaboradores repassa os filtros de nome e CPF")
    void listagemRepassaFiltros() throws Exception {
        when(casoDeUso.listarColaboradores(any(FiltroColaboradorDTO.class)))
                .thenReturn(List.of(colaboradorSalvo()));

        mockMvc.perform(get("/api/usuarios/colaboradores")
                        .param("nome", "silva")
                        .param("cpf", "529.982.247-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("João da Silva"))
                .andExpect(jsonPath("$[0].senhaInicial").doesNotExist());

        verify(casoDeUso).listarColaboradores(eq(new FiltroColaboradorDTO("silva", "52998224725")));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Listagem sem filtros não é confundida com a busca por ID")
    void listagemSemFiltrosNaoColideComBuscaPorId() throws Exception {
        when(casoDeUso.listarColaboradores(any(FiltroColaboradorDTO.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios/colaboradores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(casoDeUso, never()).buscarPorId(any());
    }
}

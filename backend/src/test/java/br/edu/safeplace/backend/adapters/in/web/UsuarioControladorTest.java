package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
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

@WebMvcTest({UsuarioControlador.class, UsuarioTratadorExcecoes.class})
@Import(SecurityConfig.class)
class UsuarioControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarUsuarioCasoDeUso casoDeUso;

    private final String cpfValido = "52998224725";

    @Test
    void deveCriarUsuarioComSucessoESemExporSenha() throws Exception {
        UsuarioSaidaDTO saida = new UsuarioSaidaDTO(
                1, "Ana Lima", cpfValido, LocalDate.of(1985, 7, 20),
                "ana@empresa.com", Perfil.SUPERVISOR, true, LocalDateTime.now(), null
        );

        when(casoDeUso.cadastrarUsuario(any(CadastrarUsuarioEntradaDTO.class))).thenReturn(saida);

        String jsonRequest = """
                {
                    "nome": "Ana Lima",
                    "cpf": "52998224725",
                    "dataNascimento": "1985-07-20",
                    "email": "ana@empresa.com",
                    "senha": "password123",
                    "perfil": "SUPERVISOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Lima"))
                .andExpect(jsonPath("$.cpf").value(cpfValido))
                .andExpect(jsonPath("$.perfil").value("SUPERVISOR"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveListarUsuarios() throws Exception {
        UsuarioSaidaDTO u1 = new UsuarioSaidaDTO(
                1, "Carlos Souza", cpfValido, LocalDate.of(1995, 3, 10),
                "carlos@empresa.com", Perfil.COLABORADOR, true, LocalDateTime.now(), null
        );
        when(casoDeUso.listarUsuarios()).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Carlos Souza"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    void deveRetornar409QuandoCpfJaCadastrado() throws Exception {
        when(casoDeUso.cadastrarUsuario(any()))
                .thenThrow(new CpfJaCadastradoException("CPF já cadastrado no sistema."));

        String jsonRequest = """
                {
                    "nome": "Carlos",
                    "cpf": "52998224725",
                    "dataNascimento": "1995-03-10",
                    "perfil": "COLABORADOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflito de Dados"));
    }

    @Test
    void deveRetornar400QuandoCpfInvalido() throws Exception {
        when(casoDeUso.cadastrarUsuario(any()))
                .thenThrow(new CpfInvalidoException("CPF deve conter exatamente 11 dígitos numéricos."));

        String jsonRequest = """
                {
                    "nome": "Carlos",
                    "cpf": "123",
                    "dataNascimento": "1995-03-10",
                    "perfil": "COLABORADOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CPF Inválido"));
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoEncontrado() throws Exception {
        when(casoDeUso.buscarPorId(99))
                .thenThrow(new UsuarioNaoEncontradoException("Usuário não encontrado"));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }
}

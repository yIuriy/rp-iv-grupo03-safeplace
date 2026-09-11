package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.usuario.Perfil;
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

/**
 * Issue #91: a senha inicial do Supervisor aparece apenas na resposta 201 do cadastro.
 */
@WebMvcTest({UsuarioControlador.class, UsuarioTratadorExcecoes.class})
@Import(SecurityConfig.class)
class UsuarioControladorSenhaInicialTest {

    private static final String CPF = "52998224725";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarUsuarioCasoDeUso casoDeUso;

    private UsuarioSaidaDTO supervisor(String senhaInicial) {
        return new UsuarioSaidaDTO(1, "Ana Lima", CPF, LocalDate.of(1985, 7, 20),
                "ana@empresa.com", Perfil.SUPERVISOR, true, LocalDateTime.now(), senhaInicial);
    }

    @Test
    void cadastroDeSupervisorSemSenhaDeveRetornar201ComSenhaInicial() throws Exception {
        when(casoDeUso.cadastrarUsuario(any(CadastrarUsuarioEntradaDTO.class))).thenReturn(supervisor("Xk7!pQ2#mA9z"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Ana Lima",
                                    "cpf": "52998224725",
                                    "dataNascimento": "1985-07-20",
                                    "email": "ana@empresa.com",
                                    "perfil": "SUPERVISOR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senhaInicial").value("Xk7!pQ2#mA9z"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void cadastroDeColaboradorNaoDeveTrazerSenhaInicial() throws Exception {
        UsuarioSaidaDTO colaborador = new UsuarioSaidaDTO(2, "Carlos Souza", CPF, LocalDate.of(1995, 3, 10),
                null, Perfil.COLABORADOR, true, LocalDateTime.now(), null);
        when(casoDeUso.cadastrarUsuario(any(CadastrarUsuarioEntradaDTO.class))).thenReturn(colaborador);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Carlos Souza",
                                    "cpf": "52998224725",
                                    "dataNascimento": "1995-03-10",
                                    "perfil": "COLABORADOR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senhaInicial").doesNotExist());
    }

    @Test
    void listagemEBuscaPorIdNaoDevemTrazerSenhaInicial() throws Exception {
        when(casoDeUso.listarUsuarios()).thenReturn(List.of(supervisor(null)));
        when(casoDeUso.buscarPorId(1)).thenReturn(supervisor(null));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senhaInicial").doesNotExist());

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senhaInicial").doesNotExist());
    }
}

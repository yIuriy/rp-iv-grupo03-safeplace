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

import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #122: rotas de atualização por papel, permissões e recusa de campos fora do contrato.
 */
@WebMvcTest({UsuarioControlador.class, UsuarioTratadorExcecoes.class})
@Import(SecurityConfig.class)
class UsuarioControladorAtualizacaoTest {

    private static final String JSON_SUPERVISOR = """
            {
                "nome": "Joana Ribeiro Souza",
                "dataNascimento": "1988-04-20",
                "email": "joana.souza@safeplace.com"
            }
            """;

    private static final String JSON_COLABORADOR = """
            {
                "nome": "João Pedro da Silva",
                "dataNascimento": "1992-06-18"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarUsuarioCasoDeUso casoDeUso;

    private static UsuarioSaidaDTO supervisoraAtualizada() {
        return new UsuarioSaidaDTO(10, "Joana Ribeiro Souza", "52998224725", LocalDate.of(1988, 4, 20),
                "joana.souza@safeplace.com", Perfil.SUPERVISOR, true, LocalDateTime.now(), null);
    }

    private static UsuarioSaidaDTO colaboradorAtualizado() {
        return new UsuarioSaidaDTO(11, "João Pedro da Silva", "12345678909", LocalDate.of(1992, 6, 18),
                null, Perfil.COLABORADOR, true, LocalDateTime.now(), null);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF23: Gestor atualiza Supervisor e a resposta não traz credenciais")
    void gestorAtualizaSupervisor() throws Exception {
        when(casoDeUso.atualizarSupervisor(eq(10), any(AtualizarPessoaInputDTO.class))).thenReturn(supervisoraAtualizada());

        mockMvc.perform(put("/api/usuarios/supervisores/10").contentType(MediaType.APPLICATION_JSON).content(JSON_SUPERVISOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Joana Ribeiro Souza"))
                .andExpect(jsonPath("$.perfil").value("SUPERVISOR"))
                .andExpect(jsonPath("$.senhaInicial").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist());

        verify(casoDeUso).atualizarSupervisor(eq(10), eq(new AtualizarPessoaInputDTO(
                "Joana Ribeiro Souza", LocalDate.of(1988, 4, 20), "joana.souza@safeplace.com")));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("RF23: Supervisor não atualiza Supervisor")
    void supervisorNaoAtualizaSupervisor() throws Exception {
        mockMvc.perform(put("/api/usuarios/supervisores/10").contentType(MediaType.APPLICATION_JSON).content(JSON_SUPERVISOR))
                .andExpect(status().isForbidden());

        verify(casoDeUso, never()).atualizarSupervisor(anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("RF23: Supervisor atualiza Colaborador sem enviar credenciais")
    void supervisorAtualizaColaborador() throws Exception {
        when(casoDeUso.atualizarColaborador(eq(11), any(AtualizarPessoaInputDTO.class))).thenReturn(colaboradorAtualizado());

        mockMvc.perform(put("/api/usuarios/colaboradores/11").contentType(MediaType.APPLICATION_JSON).content(JSON_COLABORADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("COLABORADOR"))
                .andExpect(jsonPath("$.senhaInicial").doesNotExist());

        verify(casoDeUso).atualizarColaborador(eq(11), eq(new AtualizarPessoaInputDTO(
                "João Pedro da Silva", LocalDate.of(1992, 6, 18), null)));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("RF23: Gestor também atualiza Colaborador")
    void gestorAtualizaColaborador() throws Exception {
        when(casoDeUso.atualizarColaborador(eq(11), any(AtualizarPessoaInputDTO.class))).thenReturn(colaboradorAtualizado());

        mockMvc.perform(put("/api/usuarios/colaboradores/11").contentType(MediaType.APPLICATION_JSON).content(JSON_COLABORADOR))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Campos fora do contrato (cpf, senha, perfil, ativo) são recusados com 400 sem chegar ao caso de uso")
    void camposForaDoContratoSaoRecusados() throws Exception {
        List<String> extras = List.of(
                "\"cpf\": \"11144477735\"",
                "\"senha\": \"NovaSenha@1\"",
                "\"perfil\": \"GESTOR_SEGURANCA\"",
                "\"ativo\": false");

        for (String extra : extras) {
            String corpo = "{ \"nome\": \"Joana\", \"dataNascimento\": \"1988-04-20\", \"email\": \"joana@safeplace.com\", " + extra + " }";
            mockMvc.perform(put("/api/usuarios/supervisores/10").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(put("/api/usuarios/colaboradores/11").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }

        verify(casoDeUso, never()).atualizarSupervisor(anyInt(), any());
        verify(casoDeUso, never()).atualizarColaborador(anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Supervisor sem e-mail é recusado com 400: o e-mail é a identificação de acesso")
    void supervisorSemEmailEhRecusado() throws Exception {
        mockMvc.perform(put("/api/usuarios/supervisores/10").contentType(MediaType.APPLICATION_JSON).content(JSON_COLABORADOR))
                .andExpect(status().isBadRequest());

        verify(casoDeUso, never()).atualizarSupervisor(anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Id de outro perfil ou inexistente responde 404 com mensagem")
    void idNaoEncontradoResponde404() throws Exception {
        when(casoDeUso.atualizarSupervisor(eq(99), any(AtualizarPessoaInputDTO.class)))
                .thenThrow(new UsuarioNaoEncontradoException("Supervisor não encontrado com ID: 99"));

        mockMvc.perform(put("/api/usuarios/supervisores/99").contentType(MediaType.APPLICATION_JSON).content(JSON_SUPERVISOR))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supervisor não encontrado com ID: 99"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("E-mail já usado por outra pessoa responde 409")
    void emailDuplicadoResponde409() throws Exception {
        when(casoDeUso.atualizarColaborador(eq(11), any(AtualizarPessoaInputDTO.class)))
                .thenThrow(new EmailJaCadastradoException("Email já cadastrado no sistema: joana@safeplace.com"));

        String corpo = "{ \"nome\": \"João\", \"dataNascimento\": \"1992-06-18\", \"email\": \"joana@safeplace.com\" }";
        mockMvc.perform(put("/api/usuarios/colaboradores/11").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema: joana@safeplace.com"));
    }
}

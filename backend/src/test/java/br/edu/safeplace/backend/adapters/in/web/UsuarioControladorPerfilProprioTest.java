/*
 * Testes da rota GET /api/usuarios/me, usada pela tela de perfil: Gestor e Supervisor consultam
 * os próprios dados pelo e-mail do token, a rota não é confundida com /{id}, a resposta não traz
 * senha e quem não está autenticado recebe 401.
 */

package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.config.RestSegurancaTratadorExcecoes;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UsuarioControlador.class, UsuarioTratadorExcecoes.class})
@Import({SecurityConfig.class, RestSegurancaTratadorExcecoes.class})
class UsuarioControladorPerfilProprioTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarUsuarioCasoDeUso casoDeUso;

    @Test
    @WithMockUser(username = "sara@safeplace.com", roles = "SUPERVISOR")
    void supervisorConsultaOsPropriosDadosPeloEmailDoToken() throws Exception {
        when(casoDeUso.buscarPorEmail("sara@safeplace.com")).thenReturn(new UsuarioSaidaDTO(
                7, "Sara Lima", "52998224725", LocalDate.of(1990, 4, 12), "sara@safeplace.com",
                Perfil.SUPERVISOR, true, LocalDateTime.of(2026, 9, 20, 10, 30), null));

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Sara Lima"))
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.dataNascimento").value("1990-04-12"))
                .andExpect(jsonPath("$.email").value("sara@safeplace.com"))
                .andExpect(jsonPath("$.perfil").value("SUPERVISOR"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaInicial").doesNotExist());

        verify(casoDeUso, never()).buscarPorId(any());
    }

    @Test
    @WithMockUser(username = "gestor@safeplace.com", roles = "GESTOR_SEGURANCA")
    void gestorConsultaOsPropriosDados() throws Exception {
        when(casoDeUso.buscarPorEmail("gestor@safeplace.com")).thenReturn(new UsuarioSaidaDTO(
                1, "Gestor Demonstração", "11144477735", LocalDate.of(1985, 1, 15), "gestor@safeplace.com",
                Perfil.GESTOR_SEGURANCA, true, LocalDateTime.of(2026, 9, 1, 8, 0), null));

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("GESTOR_SEGURANCA"));
    }

    @Test
    @WithMockUser(username = "removido@safeplace.com", roles = "SUPERVISOR")
    void cadastroInexistenteResponde404() throws Exception {
        when(casoDeUso.buscarPorEmail("removido@safeplace.com"))
                .thenThrow(new UsuarioNaoEncontradoException("Usuário não encontrado com e-mail: removido@safeplace.com"));

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void semAutenticacaoResponde401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }
}

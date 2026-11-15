package br.edu.safeplace.backend.adapters.in.web;

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

import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SetorController.class)
@Import(SecurityConfig.class)
class SetorControllerTest {

    private static final String JSON_VALIDO = """
            {
                "nome": "Galpão Principal",
                "descricao": "Montagem estrutural",
                "localizacao": "Bloco A",
                "status": "ALTO"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerenciarSetorUseCase useCase;

    private static SetorOutputDTO setorSalvo() {
        return new SetorOutputDTO(1, "Galpão Principal", "Montagem estrutural", "Bloco A", NivelPerigo.ALTO);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor de Segurança deve cadastrar setor físico")
    void gestorDeveCadastrarSetor() throws Exception {
        when(useCase.cadastrar(any(CadastrarSetorInputDTO.class), any())).thenReturn(setorSalvo());

        mockMvc.perform(post("/api/setores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Galpão Principal"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor não deve ter permissão para cadastrar setor físico")
    void supervisorNaoDeveCadastrarSetor() throws Exception {
        mockMvc.perform(post("/api/setores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor deve ter permissão para listar setores físicos")
    void supervisorDeveListarSetores() throws Exception {
        when(useCase.listar()).thenReturn(List.of(setorSalvo()));

        mockMvc.perform(get("/api/setores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Galpão Principal"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor deve ter permissão para buscar setor físico por ID")
    void supervisorDeveBuscarSetorPorId() throws Exception {
        when(useCase.buscarPorId(1)).thenReturn(setorSalvo());

        mockMvc.perform(get("/api/setores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.localizacao").value("Bloco A"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor de Segurança deve atualizar setor físico com sucesso")
    void gestorDeveAtualizarSetor() throws Exception {
        when(useCase.atualizar(eq(1), any(br.edu.safeplace.backend.application.dto.input.AtualizarSetorInputDTO.class), any()))
                .thenReturn(new SetorOutputDTO(1, "Galpão Principal Modificado", "Nova descrição", "Bloco B", NivelPerigo.CRITICO));

        mockMvc.perform(put("/api/setores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Galpão Principal Modificado"))
                .andExpect(jsonPath("$.status").value("CRITICO"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor não deve atualizar setor físico (proibido)")
    void supervisorNaoDeveAtualizarSetor() throws Exception {
        mockMvc.perform(put("/api/setores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }
}

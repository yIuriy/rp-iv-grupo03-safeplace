package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("bancoDisponivel")
@DisplayName("Integração RF12/UC08: persistência real e fluxo integrado de EPIs obrigatórios em tarefas")
class TarefaEpiApiIntegracaoTest {

    private static final String EMAIL_GESTOR = "gestor.epi135@safeplace-teste.local";
    private static final String CPF_GESTOR = "13520261088";
    private static final String DESCRICAO_TAREFA = "Manutenção em Linha Viva 135";
    private static final int ID_INEXISTENTE = 999_999;

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EpiRepository epiRepository;

    @Autowired
    private UsuarioRepository usuarioRepositorio;

    private final List<Integer> epis = new ArrayList<>();
    private Integer tarefaId;

    @BeforeEach
    void prepararDados() throws Exception {
        limpar();
        usuarioRepositorio.salvar(GestorDeSeguranca.novo(
                "Gestor EPI 135", CPF_GESTOR, LocalDate.of(1985, 5, 20), EMAIL_GESTOR, "hash-de-teste"));
        epis.add(novoEpi("Luva Isolante 135", "13501"));
        epis.add(novoEpi("Viseira de Proteção 135", "13502"));

        mockMvc.perform(post("/api/tarefas").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"%s\", \"nivelPerigo\": \"CRITICO\"}".formatted(DESCRICAO_TAREFA)))
                .andExpect(status().isCreated());

        tarefaId = jdbc.queryForObject(
                "SELECT id FROM tarefas WHERE descricao = ?", Integer.class, DESCRICAO_TAREFA);
    }

    @AfterEach
    void limparDados() {
        limpar();
    }

    private void limpar() {
        if (tarefaId != null) {
            jdbc.update("DELETE FROM tarefas_epis WHERE tarefa_id = ?", tarefaId);
            jdbc.update("DELETE FROM tarefas WHERE id = ?", tarefaId);
        } else {
            jdbc.update("DELETE FROM tarefas_epis WHERE tarefa_id IN (SELECT id FROM tarefas WHERE descricao = ?)", DESCRICAO_TAREFA);
            jdbc.update("DELETE FROM tarefas WHERE descricao = ?", DESCRICAO_TAREFA);
        }
        removerEpisDoTeste();
        jdbc.update("DELETE FROM usuarios WHERE email = ? OR cpf = ?", EMAIL_GESTOR, CPF_GESTOR);
    }

    private void removerEpisDoTeste() {
        List<Map<String, Object>> linhas = jdbc.queryForList(
                "SELECT e.id, e.lote_id, l.modelo_id FROM epis e LEFT JOIN lotes_epi l ON l.id = e.lote_id "
                        + "WHERE e.nome IN ('Luva Isolante 135', 'Viseira de Proteção 135')");
        for (Map<String, Object> linha : linhas) {
            jdbc.update("DELETE FROM tarefas_epis WHERE epi_id = ?", linha.get("id"));
            jdbc.update("DELETE FROM movimentacoes_estoque WHERE epi_id = ?", linha.get("id"));
            jdbc.update("DELETE FROM epis WHERE id = ?", linha.get("id"));
            if (linha.get("lote_id") != null) {
                jdbc.update("DELETE FROM lotes_epi WHERE id = ?", linha.get("lote_id"));
                jdbc.update("DELETE FROM modelos_epi WHERE id = ?", linha.get("modelo_id"));
            }
        }
        epis.clear();
    }

    private Integer novoEpi(String nome, String numeroCa) {
        return epiRepository.salvar(Epi.novo(nome, numeroCa, 10, 2,
                LocalDate.now().plusYears(2), 365, null, null)).getId();
    }

    private static RequestPostProcessor gestor() {
        return user(EMAIL_GESTOR).roles("GESTOR_SEGURANCA");
    }

    private static RequestPostProcessor supervisor() {
        return user("supervisor.135@safeplace.local").roles("SUPERVISOR");
    }

    @Test
    @DisplayName("Gestor associa EPIs obrigatórios à tarefa com persistência real em tarefas_epis")
    void gestorAssociaEpisComPersistenciaReal() throws Exception {
        String json = "{\"epiIds\": [%d, %d]}".formatted(epis.get(0), epis.get(1));

        mockMvc.perform(put("/api/tarefas/{id}/epis", tarefaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarefaId))
                .andExpect(jsonPath("$.episIds", hasSize(2)));

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_epis WHERE tarefa_id = ?", Integer.class, tarefaId);
        assertThat(count).isEqualTo(2);

        mockMvc.perform(get("/api/tarefas/{id}/epis", tarefaId).with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(epis.get(0)))
                .andExpect(jsonPath("$[0].nome").value("Luva Isolante 135"))
                .andExpect(jsonPath("$[1].id").value(epis.get(1)))
                .andExpect(jsonPath("$[1].nome").value("Viseira de Proteção 135"));
    }

    @Test
    @DisplayName("Supervisor não tem autorização para associar EPIs à tarefa (403 Forbidden)")
    void supervisorNaoPodeAssociarEpis() throws Exception {
        String json = "{\"epiIds\": [%d]}".formatted(epis.get(0));

        mockMvc.perform(put("/api/tarefas/{id}/epis", tarefaId).with(supervisor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_epis WHERE tarefa_id = ?", Integer.class, tarefaId);
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Tentativa de associar EPI inexistente retorna 404 e mantém o banco intacto")
    void associarEpiInexistenteRetorna404() throws Exception {
        String json = "{\"epiIds\": [%d]}".formatted(ID_INEXISTENTE);

        mockMvc.perform(put("/api/tarefas/{id}/epis", tarefaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_epis WHERE tarefa_id = ?", Integer.class, tarefaId);
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Gestor pode desassociar todos os EPIs enviando lista vazia")
    void desassociarEpisComListaVazia() throws Exception {
        jdbc.update("INSERT INTO tarefas_epis (tarefa_id, epi_id) VALUES (?, ?)", tarefaId, epis.get(0));

        mockMvc.perform(put("/api/tarefas/{id}/epis", tarefaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"epiIds\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.episIds", hasSize(0)));

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_epis WHERE tarefa_id = ?", Integer.class, tarefaId);
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Gestor lista tarefas cadastradas na matriz de tarefas")
    void gestorListaTarefas() throws Exception {
        mockMvc.perform(get("/api/tarefas").with(gestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("O contrato OpenAPI publica os endpoints /api/tarefas/{id}/epis")
    void contratoOpenApiPublicaEndpointsEpisTarefa() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/tarefas/{id}/epis'].put").exists())
                .andExpect(jsonPath("$.paths['/api/tarefas/{id}/epis'].get").exists());
    }
}

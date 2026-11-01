/*
 * Teste de integração da #114 pela API real e PostgreSQL: histórico de áreas e tarefas com valor
 * anterior, novo e operador, atualização recusada sem perda de dados e contrato OpenAPI publicado.
 * Cada teste grava com commit e remove só as linhas que criou, sem esvaziar tabelas compartilhadas.
 */
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("bancoDisponivel")
@DisplayName("Integração #114: histórico, atualização sem perda e OpenAPI de áreas e tarefas")
class AreaRiscoTarefaApiIntegracaoTest {

    private static final String EMAIL_GESTOR = "gestor.historico@safeplace-teste-114.local";
    private static final String CPF_GESTOR = "11420261088";
    private static final String CODIGO_AREA = "AR-114-HIST";
    private static final String DESCRICAO_TAREFA = "Solda em altura 114";
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
    private Integer setorId;
    private Integer areaId;

    @BeforeEach
    void prepararDados() throws Exception {
        limpar();
        usuarioRepositorio.salvar(GestorDeSeguranca.novo(
                "Gestor Histórico 114", CPF_GESTOR, LocalDate.of(1987, 3, 9), EMAIL_GESTOR, "hash-de-teste"));
        epis.add(novoEpi("Capacete 114", "11401"));
        epis.add(novoEpi("Luva 114", "11402"));
        setorId = jdbc.queryForObject(
                "INSERT INTO setores (nome, status) VALUES ('Usinagem 114', 'ALTO') RETURNING id", Integer.class);

        String criada = mockMvc.perform(post("/api/areas-risco").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo": "%s", "nome": "Solda 114", "nivelPerigo": "MEDIO",
                                 "agentesRisco": ["Ruído"], "limites": "Entre as linhas A e B",
                                 "episObrigatoriosIds": [%d]}
                                """.formatted(CODIGO_AREA, epis.get(0))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        areaId = jdbc.queryForObject("SELECT id FROM areas_risco WHERE codigo = ?", Integer.class, CODIGO_AREA);
        assertThat(criada).contains(CODIGO_AREA);
    }

    @AfterEach
    void limparDados() {
        limpar();
    }

    private void limpar() {
        jdbc.update("DELETE FROM areas_risco_epis WHERE area_risco_id IN "
                + "(SELECT id FROM areas_risco WHERE codigo = ?)", CODIGO_AREA);
        jdbc.update("DELETE FROM areas_risco WHERE codigo = ?", CODIGO_AREA);
        jdbc.update("DELETE FROM tarefas WHERE descricao = ?", DESCRICAO_TAREFA);
        jdbc.update("DELETE FROM setores WHERE nome = 'Usinagem 114'");
        removerEpisDoTeste();
        jdbc.update("DELETE FROM usuarios WHERE email = ? OR cpf = ?", EMAIL_GESTOR, CPF_GESTOR);
    }

    private void removerEpisDoTeste() {
        List<Map<String, Object>> linhas = jdbc.queryForList(
                "SELECT e.id, e.lote_id, l.modelo_id FROM epis e LEFT JOIN lotes_epi l ON l.id = e.lote_id "
                        + "WHERE e.nome IN ('Capacete 114', 'Luva 114')");
        for (Map<String, Object> linha : linhas) {
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

    private String atualizacao(String limites, Integer setor, String episIds, String justificativa) {
        return """
                {"nome": "Solda 114", "nivelPerigo": "ALTO", "setorId": %s,
                 "agentesRisco": ["Ruído", "Calor"], "limites": "%s",
                 "episObrigatoriosIds": [%s], "justificativa": %s}
                """.formatted(setor, limites, episIds, justificativa == null ? "null" : "\"" + justificativa + "\"");
    }

    @Test
    @DisplayName("Atualizar área pela API reflete o novo estado e grava no histórico o anterior, o novo e o operador")
    void atualizacaoGravaHistoricoConsultavelPeloGestor() throws Exception {
        mockMvc.perform(put("/api/areas-risco/{id}", areaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao("Entre as linhas A e C", setorId, epis.get(0) + ", " + epis.get(1), "Revisão anual")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/areas-risco/{id}", areaId).with(user("supervisor@safeplace.local").roles("SUPERVISOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelPerigo").value("ALTO"))
                .andExpect(jsonPath("$.limites").value("Entre as linhas A e C"))
                .andExpect(jsonPath("$.setorId").value(setorId))
                .andExpect(jsonPath("$.episObrigatorios", hasSize(2)));

        mockMvc.perform(get("/api/areas-risco/{id}/historico", areaId).with(gestor()))
                .andExpect(status().isOk())
                // RNF05 (#193): o cadastro da área também entra no histórico, como registro mais antigo.
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].tipoOperacao").value("CADASTRO"))
                .andExpect(jsonPath("$[0].tipoOperacao").value("ALTERACAO"))
                .andExpect(jsonPath("$[0].responsavel").value(EMAIL_GESTOR))
                .andExpect(jsonPath("$[0].dataHora").exists())
                .andExpect(jsonPath("$[0].descricao").value(containsString(
                        "limites: de \"Entre as linhas A e B\" para \"Entre as linhas A e C\"")))
                .andExpect(jsonPath("$[0].descricao").value(containsString(
                        "setor: de não informado para \"Usinagem 114\"")))
                .andExpect(jsonPath("$[0].descricao").value(containsString(
                        "EPIs obrigatórios: de \"Capacete 114\" para \"Capacete 114, Luva 114\"")))
                .andExpect(jsonPath("$[0].descricao").value(containsString("Justificativa: Revisão anual")));

        mockMvc.perform(put("/api/areas-risco/{id}", areaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao("Linha amarela", setorId, epis.get(0) + ", " + epis.get(1), null)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/areas-risco/{id}/historico", areaId).with(gestor()))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].descricao").value(
                        "Alteração da área de risco: limites: de \"Entre as linhas A e C\" para \"Linha amarela\"."));
    }

    @Test
    @DisplayName("Atualização sem EPI obrigatório é recusada e não altera a área nem o histórico")
    void semEpiObrigatorioNaoAlteraNada() throws Exception {
        assertarRecusaSemPerda(atualizacao("Outro limite", setorId, "", null), status().isBadRequest());
    }

    @Test
    @DisplayName("Atualização com EPI inexistente é recusada e não altera a área nem o histórico")
    void epiInexistenteNaoAlteraNada() throws Exception {
        assertarRecusaSemPerda(atualizacao("Outro limite", setorId, String.valueOf(ID_INEXISTENTE), null),
                status().isNotFound());
    }

    @Test
    @DisplayName("Atualização com setor inexistente é recusada e não altera a área nem o histórico")
    void setorInexistenteNaoAlteraNada() throws Exception {
        assertarRecusaSemPerda(atualizacao("Outro limite", ID_INEXISTENTE, String.valueOf(epis.get(1)), null),
                status().isBadRequest());
    }

    private void assertarRecusaSemPerda(String corpo, ResultMatcher statusEsperado) throws Exception {
        Map<String, Object> antes = linhaDaArea();
        List<Integer> episAntes = episDaArea();

        mockMvc.perform(put("/api/areas-risco/{id}", areaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(statusEsperado);

        assertThat(linhaDaArea()).isEqualTo(antes);
        assertThat(episDaArea()).isEqualTo(episAntes);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM logs_auditoria WHERE entidade = 'AreaRisco' AND entidade_id = ? AND tipo_operacao = 'ALTERACAO'",
                Integer.class, areaId)).isZero();
    }

    private Map<String, Object> linhaDaArea() {
        return jdbc.queryForMap("SELECT codigo, nome, nivel_perigo, setor_id, agentes_risco, limites "
                + "FROM areas_risco WHERE id = ?", areaId);
    }

    private List<Integer> episDaArea() {
        return jdbc.queryForList("SELECT epi_id FROM areas_risco_epis WHERE area_risco_id = ? ORDER BY epi_id",
                Integer.class, areaId);
    }

    @Test
    @DisplayName("Reclassificar tarefa reflete o novo nível e o histórico preserva anterior, novo e responsável")
    void reclassificacaoDeTarefaPreservaHistorico() throws Exception {
        String criada = mockMvc.perform(post("/api/tarefas").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"%s\", \"nivelPerigo\": \"MEDIO\"}".formatted(DESCRICAO_TAREFA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer tarefaId = jdbc.queryForObject(
                "SELECT id FROM tarefas WHERE descricao = ?", Integer.class, DESCRICAO_TAREFA);
        assertThat(criada).contains("\"nivelPerigo\":\"MEDIO\"");

        mockMvc.perform(patch("/api/tarefas/{id}/classificacao", tarefaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelPerigo\": \"ALTO\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tarefas/{id}", tarefaId).with(gestor()))
                .andExpect(jsonPath("$.nivelPerigo").value("ALTO"))
                .andExpect(jsonPath("$.responsavelClassificacaoNome").value("Gestor Histórico 114"));

        mockMvc.perform(get("/api/tarefas/{id}/historico", tarefaId).with(gestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].responsavel").value(EMAIL_GESTOR))
                .andExpect(jsonPath("$[0].descricao").value(containsString("de MEDIO para ALTO")));

        mockMvc.perform(get("/api/tarefas/{id}/historico", tarefaId)
                        .with(user("supervisor@safeplace.local").roles("SUPERVISOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("O OpenAPI publica as rotas existentes, o histórico e os erros, sem /api/areas-de-risco")
    void contratoOpenApiCorrespondeAApi() throws Exception {
        String contrato = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/areas-risco/{id}/historico'].get").exists())
                .andExpect(jsonPath("$.paths['/api/tarefas/{id}/historico'].get").exists())
                .andExpect(jsonPath("$.paths['/api/areas-risco/{id}'].put.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/areas-risco/{id}'].put.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/areas-risco'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/tarefas/{id}/classificacao'].patch.responses['403']").exists())
                .andReturn().getResponse().getContentAsString();

        assertThat(contrato).doesNotContain("/api/areas-de-risco");
    }
}

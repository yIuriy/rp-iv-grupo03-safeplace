/*
 * Teste de integração da #134 pela API real e PostgreSQL (migrações até a V24): grava inspeções,
 * reconstrói itens e responsável, confere a ordem do histórico, a recusa sem deixar linhas, o 403 do
 * Supervisor e o OpenAPI. Cada teste remove só as linhas que criou.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
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

import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("bancoDisponivel")
@DisplayName("Integração #134: inspeções de áreas de risco persistidas no PostgreSQL")
class InspecaoPersistenciaIntegracaoTest {

    private static final String EMAIL_GESTOR = "gestor.inspecoes@safeplace-teste-134.local";
    private static final String CPF_GESTOR = "13420261012";
    private static final String CODIGO_AREA = "AR-134-INSP";
    private static final String NOME_EPI = "Capacete 134";

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
    private EpiRepositoryPort epiRepositoryPort;

    @Autowired
    private UsuarioRepositorioPorta usuarioRepositorio;

    private Integer areaId;
    private Integer gestorId;

    @BeforeEach
    void prepararDados() throws Exception {
        limpar();
        gestorId = usuarioRepositorio.salvar(GestorDeSeguranca.novo(
                "Gestor Inspeções 134", CPF_GESTOR, LocalDate.of(1986, 5, 20), EMAIL_GESTOR, "hash-de-teste")).getId();
        Integer epiId = epiRepositoryPort.salvar(Epi.novo(NOME_EPI, "13401", 10, 2,
                LocalDate.now().plusYears(2), 365, null, null)).getId();
        mockMvc.perform(post("/api/areas-risco").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo": "%s", "nome": "Caldeiraria 134", "nivelPerigo": "ALTO",
                                 "agentesRisco": ["Calor"], "limites": "Galpão 2 - área demarcada",
                                 "episObrigatoriosIds": [%d]}
                                """.formatted(CODIGO_AREA, epiId)))
                .andExpect(status().isCreated());
        areaId = jdbc.queryForObject("SELECT id FROM areas_risco WHERE codigo = ?", Integer.class, CODIGO_AREA);
    }

    @AfterEach
    void limparDados() {
        limpar();
    }

    private void limpar() {
        String inspecoesDaArea = "SELECT i.id FROM inspecoes i JOIN areas_risco a ON a.id = i.area_risco_id WHERE a.codigo = ?";
        jdbc.update("DELETE FROM itens_verificados WHERE inspecao_id IN (" + inspecoesDaArea + ")", CODIGO_AREA);
        jdbc.update("DELETE FROM inspecoes WHERE id IN (" + inspecoesDaArea + ")", CODIGO_AREA);
        jdbc.update("DELETE FROM areas_risco_epis WHERE area_risco_id IN (SELECT id FROM areas_risco WHERE codigo = ?)",
                CODIGO_AREA);
        jdbc.update("DELETE FROM areas_risco WHERE codigo = ?", CODIGO_AREA);
        List<Map<String, Object>> epis = jdbc.queryForList(
                "SELECT e.id, e.lote_id, l.modelo_id FROM epis e LEFT JOIN lotes_epi l ON l.id = e.lote_id WHERE e.nome = ?",
                NOME_EPI);
        for (Map<String, Object> epi : epis) {
            jdbc.update("DELETE FROM movimentacoes_estoque WHERE epi_id = ?", epi.get("id"));
            jdbc.update("DELETE FROM epis WHERE id = ?", epi.get("id"));
            if (epi.get("lote_id") != null) {
                jdbc.update("DELETE FROM lotes_epi WHERE id = ?", epi.get("lote_id"));
                jdbc.update("DELETE FROM modelos_epi WHERE id = ?", epi.get("modelo_id"));
            }
        }
        jdbc.update("DELETE FROM usuarios WHERE email = ? OR cpf = ?", EMAIL_GESTOR, CPF_GESTOR);
    }

    private static RequestPostProcessor gestor() {
        return user(EMAIL_GESTOR).roles("GESTOR_SEGURANCA");
    }

    private void registrar(String corpo, int statusEsperado) throws Exception {
        mockMvc.perform(post("/api/areas-risco/{id}/inspecoes", areaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().is(statusEsperado));
    }

    private int contar(String tabela) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela + " WHERE "
                + ("inspecoes".equals(tabela) ? "area_risco_id = ?"
                        : "inspecao_id IN (SELECT id FROM inspecoes WHERE area_risco_id = ?)"), Integer.class, areaId);
    }

    @Test
    @DisplayName("Grava inspeções pela API e o histórico as reconstrói com itens, responsável e ordem por data")
    void gravaEReconstroiHistorico() throws Exception {
        registrar("""
                {"dataInspecao": "2025-09-01", "itens": [{"descricao": "NR-12: proteções fixas", "conformidade": true}]}
                """, 201);
        registrar("""
                {"dataInspecao": "2025-09-30", "itens": [
                  {"descricao": "NR-12: proteções fixas", "conformidade": true},
                  {"descricao": "NR-26: sinalização", "conformidade": false, "observacoes": "Faixa apagada"}]}
                """, 201);

        assertThat(contar("inspecoes")).isEqualTo(2);
        assertThat(contar("itens_verificados")).isEqualTo(3);
        assertThat(jdbc.queryForList("SELECT DISTINCT gestor_id FROM inspecoes WHERE area_risco_id = ?",
                Integer.class, areaId)).containsExactly(gestorId);

        mockMvc.perform(get("/api/areas-risco/{id}/inspecoes", areaId).with(gestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dataInspecao").value("2025-09-30"))
                .andExpect(jsonPath("$[0].responsavelNome").value("Gestor Inspeções 134"))
                .andExpect(jsonPath("$[0].naoConformidades").value(1))
                .andExpect(jsonPath("$[0].itens", hasSize(2)))
                .andExpect(jsonPath("$[0].itens[0].descricao").value("NR-12: proteções fixas"))
                .andExpect(jsonPath("$[0].itens[1].conformidade").value(false))
                .andExpect(jsonPath("$[0].itens[1].observacoes").value("Faixa apagada"))
                .andExpect(jsonPath("$[1].dataInspecao").value("2025-09-01"))
                .andExpect(jsonPath("$[1].naoConformidades").value(0));
    }

    @Test
    @DisplayName("Registro recusado não deixa linha em inspecoes nem em itens_verificados")
    void registroRecusadoNaoGrava() throws Exception {
        registrar("""
                {"dataInspecao": "%s", "itens": [{"descricao": "NR-26", "conformidade": true}]}
                """.formatted(LocalDate.now().plusDays(1)), 400);
        registrar("""
                {"dataInspecao": "2025-09-01", "itens": [{"descricao": "NR-26"}]}
                """, 400);
        registrar("""
                {"dataInspecao": "2025-09-01", "itens": []}
                """, 400);
        mockMvc.perform(post("/api/areas-risco/{id}/inspecoes", 999_999).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataInspecao\": \"2025-09-01\", \"itens\": [{\"descricao\": \"NR-26\", \"conformidade\": true}]}"))
                .andExpect(status().isNotFound());

        assertThat(contar("inspecoes")).isZero();
        assertThat(contar("itens_verificados")).isZero();
        mockMvc.perform(get("/api/areas-risco/{id}/inspecoes", areaId).with(gestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Supervisor recebe 403 no registro e na consulta, e a consulta comum da área continua liberada")
    void supervisorSemAcesso() throws Exception {
        RequestPostProcessor supervisor = user("supervisor@safeplace-teste-134.local").roles("SUPERVISOR");
        mockMvc.perform(post("/api/areas-risco/{id}/inspecoes", areaId).with(supervisor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataInspecao\": \"2025-09-01\", \"itens\": [{\"descricao\": \"NR-26\", \"conformidade\": true}]}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/areas-risco/{id}/inspecoes", areaId).with(supervisor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/areas-risco/{id}", areaId).with(supervisor))
                .andExpect(status().isOk());
        assertThat(contar("inspecoes")).isZero();
    }

    @Test
    @DisplayName("O OpenAPI publica as rotas de inspeção sob /api/areas-risco, com as respostas de erro")
    void contratoOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/areas-risco/{areaRiscoId}/inspecoes'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/areas-risco/{areaRiscoId}/inspecoes'].post.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/areas-risco/{areaRiscoId}/inspecoes'].get.responses['403']").exists());
    }
}

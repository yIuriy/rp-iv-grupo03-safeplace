/*
 * Teste de integração de API ponta a ponta para Empréstimo e Devolução de EPIs (RF11, UC12, US11, issue #120).
 * Valida o ciclo completo HTTP com PostgreSQL ativo: autorização RBAC, validações de saldo, CA, duplicidade,
 * capacitação, devolução com destino aprovado e saldo persistido, e histórico auditável.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnabledIf("bancoDisponivel")
@DisplayName("API Integrada de Empréstimos e Devoluções no PostgreSQL (UC12 / Issue #120)")
class EmprestimoApiIntegracaoTest {

    private static final String CPF_SUPERVISOR = "94837261501";
    private static final String CPF_COLABORADOR = "73625140982";
    private static final String EMAIL_SUPERVISOR = "supervisor.api.test@safeplace.test";

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
    private GerenciarEpiUseCase gerenciarEpiUseCase;

    @Autowired
    private UsuarioRepositorioPorta usuarioRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer colaboradorId;
    private Integer capaceteId;
    private Integer luvaSemSaldoId;
    private Integer oculosCaVencidoId;
    private Integer respiradorComExigenciaId;

    @BeforeEach
    void setupAmbiente() {
        limparDados();

        usuarioRepository.salvar(Supervisor.novo("Supervisor Teste API", CPF_SUPERVISOR,
                LocalDate.of(1985, 4, 12), EMAIL_SUPERVISOR, "hash"));

        Colaborador colab = usuarioRepository.salvar(Colaborador.novo("Colaborador Beneficiario API",
                CPF_COLABORADOR, LocalDate.of(1992, 8, 24), null));
        colaboradorId = colab.getId();

        capaceteId = gerenciarEpiUseCase.cadastrarEpi(new CadastrarEpiInputDTO(
                "Capacete de Seguranca API", "11223", 5, 1,
                LocalDate.now().plusYears(1), 365, "Capacete", ClassificacaoEPI.PROTECAO_DE_CABECA)).id();

        luvaSemSaldoId = gerenciarEpiUseCase.cadastrarEpi(new CadastrarEpiInputDTO(
                "Luva de Seguranca Zerada API", "44556", 0, 1,
                LocalDate.now().plusYears(1), 180, "Luva", ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES)).id();

        oculosCaVencidoId = gerenciarEpiUseCase.cadastrarEpi(new CadastrarEpiInputDTO(
                "Oculos com CA Vencido API", "77889", 3, 1,
                LocalDate.now().plusMonths(6), 365, "Oculos", ClassificacaoEPI.PROTECAO_DE_OLHOS)).id();
        jdbc.update("UPDATE modelos_epi SET validade_ca = ? WHERE id IN "
                + "(SELECT l.modelo_id FROM lotes_epi l JOIN epis e ON e.lote_id = l.id WHERE e.id = ?)",
                LocalDate.now().minusDays(1), oculosCaVencidoId);

        respiradorComExigenciaId = gerenciarEpiUseCase.cadastrarEpi(new CadastrarEpiInputDTO(
                "Respirador Facial API", "99001", 2, 1,
                LocalDate.now().plusYears(1), 365, "Respirador", ClassificacaoEPI.PROTECAO_RESPIRATORIA)).id();

        jdbc.update("INSERT INTO exigencias_capacitacao (alvo_id, nome_capacitacao, tipo_capacitacao, tipo_alvo) "
                + "VALUES (?, ?, 'CERTIFICACAO', 'EPI')", respiradorComExigenciaId, "Treinamento Protecao Respiratoria");
    }

    @AfterEach
    void tearDownAmbiente() {
        limparDados();
    }

    private void limparDados() {
        jdbc.update("DELETE FROM emprestimos");
        jdbc.update("DELETE FROM movimentacoes_estoque");
        jdbc.update("DELETE FROM exigencias_capacitacao");
        jdbc.update("DELETE FROM certificacoes");
        jdbc.update("DELETE FROM treinamentos_colaboradores");
        jdbc.update("DELETE FROM treinamentos");
        jdbc.update("DELETE FROM epis WHERE nome LIKE '%API%'");
        // Decisão #124: o CA é reaproveitado entre cadastros; modelos órfãos não podem vazar para o próximo teste.
        jdbc.update("DELETE FROM lotes_epi l WHERE NOT EXISTS (SELECT 1 FROM epis e WHERE e.lote_id = l.id) "
                + "AND NOT EXISTS (SELECT 1 FROM movimentacoes_estoque m WHERE m.lote_id = l.id)");
        jdbc.update("DELETE FROM modelos_epi o WHERE NOT EXISTS (SELECT 1 FROM lotes_epi l WHERE l.modelo_id = o.id)");
        jdbc.update("DELETE FROM usuarios WHERE cpf IN (?, ?)", CPF_SUPERVISOR, CPF_COLABORADOR);
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve registrar entrega com débito no estoque e gravação auditável no banco de dados")
    void deveRegistrarEntregaComSucessoEDebitarEstoque() throws Exception {
        String corpo = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(capaceteId, colaboradorId, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("INDISPONIVEL"))
                .andExpect(jsonPath("$.termoCautela").value(containsString("Termo de cautela")))
                .andExpect(jsonPath("$.atrasado").value(false));

        Integer saldoAposEntrega = jdbc.queryForObject(
                "SELECT quantidade FROM epis WHERE id = ?", Integer.class, capaceteId);
        assertThat(saldoAposEntrega).isEqualTo(4);

        Integer logs = jdbc.queryForObject(
                "SELECT count(*) FROM logs_auditoria WHERE entidade = 'Emprestimo' AND tipo_operacao = 'CADASTRO'", Integer.class);
        assertThat(logs).isGreaterThan(0);
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve bloquear entrega quando o EPI está sem saldo disponível em estoque (HTTP 422)")
    void deveBloquearEntregaSemSaldo() throws Exception {
        String corpo = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(luvaSemSaldoId, colaboradorId, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Entrega Impedida"))
                .andExpect(jsonPath("$.message").value(containsString("ESGOTADO")));
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve bloquear entrega quando o CA do EPI está vencido (HTTP 422)")
    void deveBloquearEntregaCaVencido() throws Exception {
        String corpo = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(oculosCaVencidoId, colaboradorId, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Entrega Impedida"))
                .andExpect(jsonPath("$.message").value(containsString("Certificado de Aprovação vencido")));
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve bloquear entrega quando o colaborador não atende às capacitações exigidas pelo EPI (HTTP 422)")
    void deveBloquearEntregaSemCapacitacao() throws Exception {
        String corpo = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(respiradorComExigenciaId, colaboradorId, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Entrega Impedida"))
                .andExpect(jsonPath("$.message").value(containsString("Impedimento normativo")));
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve alertar duplicidade de posse ativa quando tenta emprestar mesmo tipo sem justificativa (HTTP 409)")
    void deveDetectarDuplicidadeDePosse() throws Exception {
        // Primeira entrega
        String corpo1 = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(capaceteId, colaboradorId, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo1))
                .andExpect(status().isCreated());

        // Segunda entrega do mesmo EPI sem justificativa
        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicidade de Posse"))
                .andExpect(jsonPath("$.emprestimoAtivoId").isNumber());
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve registrar devolução com estado APTO, refletir destino ESTOQUE e creditar saldo persistido")
    void deveRegistrarDevolucaoAptoRefletindoDestinoESaldo() throws Exception {
        // Registra empréstimo
        String corpoEntrega = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(capaceteId, colaboradorId, LocalDate.now().plusDays(7));

        MvcResult resultadoEntrega = mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEntrega))
                .andExpect(status().isCreated())
                .andReturn();

        Integer emprestimoId = JsonPath.read(resultadoEntrega.getResponse().getContentAsString(), "$.id");

        // Saldo após entrega: 4
        assertThat(jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, capaceteId)).isEqualTo(4);

        // Registra devolução com APTO
        String corpoDevolucao = "{\"estado\": \"APTO\"}";

        mockMvc.perform(post("/api/emprestimos/" + emprestimoId + "/devolucao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDevolucao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emprestimoId").value(emprestimoId))
                .andExpect(jsonPath("$.estadoDevolucao").value("APTO"))
                .andExpect(jsonPath("$.destinoAprovado").value("ESTOQUE"))
                .andExpect(jsonPath("$.saldoPersistido").value(5))
                .andExpect(jsonPath("$.dataDevolucaoReal").isNotEmpty());

        // Saldo creditado de volta para 5
        Integer saldoAposDevolucao = jdbc.queryForObject(
                "SELECT quantidade FROM epis WHERE id = ?", Integer.class, capaceteId);
        assertThat(saldoAposDevolucao).isEqualTo(5);

        // Auditoria de alteração gravada
        Integer logsAlteracao = jdbc.queryForObject(
                "SELECT count(*) FROM logs_auditoria WHERE entidade = 'Emprestimo' AND tipo_operacao = 'ALTERACAO'", Integer.class);
        assertThat(logsAlteracao).isGreaterThan(0);
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve registrar devolução com estado MANUTENCAO, refletir destino MANUTENCAO e não creditar saldo")
    void deveRegistrarDevolucaoManutencaoSemCreditarSaldo() throws Exception {
        String corpoEntrega = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(capaceteId, colaboradorId, LocalDate.now().plusDays(7));

        MvcResult resultadoEntrega = mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEntrega))
                .andExpect(status().isCreated())
                .andReturn();

        Integer emprestimoId = JsonPath.read(resultadoEntrega.getResponse().getContentAsString(), "$.id");

        // Registra devolução com MANUTENCAO
        String corpoDevolucao = "{\"estado\": \"MANUTENCAO\"}";

        mockMvc.perform(post("/api/emprestimos/" + emprestimoId + "/devolucao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDevolucao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emprestimoId").value(emprestimoId))
                .andExpect(jsonPath("$.estadoDevolucao").value("MANUTENCAO"))
                .andExpect(jsonPath("$.destinoAprovado").value("MANUTENCAO"))
                .andExpect(jsonPath("$.saldoPersistido").value(4));

        // Saldo permanece 4
        Integer saldo = jdbc.queryForObject(
                "SELECT quantidade FROM epis WHERE id = ?", Integer.class, capaceteId);
        assertThat(saldo).isEqualTo(4);
    }

    @Test
    @WithMockUser(username = EMAIL_SUPERVISOR, roles = "SUPERVISOR")
    @DisplayName("Deve consultar itens em posse ativa de colaborador conforme UC12")
    void deveConsultarPosseConformeUC12() throws Exception {
        String corpoEntrega = """
                {
                    "epiId": %d,
                    "colaboradorId": %d,
                    "dataDevolucaoPrevista": "%s"
                }
                """.formatted(capaceteId, colaboradorId, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEntrega))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/emprestimos")
                        .param("colaboradorId", String.valueOf(colaboradorId))
                        .param("situacao", "PENDENTES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].colaboradorId").value(colaboradorId))
                .andExpect(jsonPath("$[0].status").value("INDISPONIVEL"));
    }
}

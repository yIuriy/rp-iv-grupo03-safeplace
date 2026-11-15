package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.util.List;

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

import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("bancoDisponivel")
@DisplayName("Integração RF10/UC02 (#119): consulta, alertas e aptidão de capacitações sobre o PostgreSQL")
class CapacitacaoApiIntegracaoTest {

    private static final String EMAIL_GESTOR = "gestor.capacitacao119@safeplace-teste.local";
    private static final String CPF_GESTOR = "11900000598";
    private static final String CPF_VENCIDO = "11900000164";
    private static final String CPF_PROXIMO = "11900000245";
    private static final String CPF_SEM_HISTORICO = "11900000326";
    private static final String CPF_SO_TREINAMENTO = "11900000679";
    private static final String DESCRICAO_TAREFA = "Montagem de andaime 119";

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
    private UsuarioRepository usuarioRepositorio;

    @Autowired
    private CapacitacaoRepository capacitacaoRepositorio;

    private Integer vencidoId;
    private Integer proximoId;
    private Integer semHistoricoId;
    private Integer soTreinamentoId;
    private Integer tarefaId;

    @BeforeEach
    void prepararDados() throws Exception {
        limpar();
        LocalDate hoje = LocalDate.now();
        usuarioRepositorio.salvar(GestorDeSeguranca.novo(
                "Gestor Capacitação 119", CPF_GESTOR, LocalDate.of(1985, 5, 20), EMAIL_GESTOR, "hash-de-teste"));

        // Colaboradores sem e-mail nem senha: a consulta não exige credencial do beneficiário (RNF03).
        Colaborador vencido = usuarioRepositorio.salvar(Colaborador.novo("Colaborador Vencido 119", CPF_VENCIDO, LocalDate.of(1990, 1, 1), null));
        Colaborador proximo = usuarioRepositorio.salvar(Colaborador.novo("Colaborador Proximo 119", CPF_PROXIMO, LocalDate.of(1991, 1, 1), null));
        Colaborador semHistorico = usuarioRepositorio.salvar(Colaborador.novo("Colaborador Sem Historico 119", CPF_SEM_HISTORICO, LocalDate.of(1992, 1, 1), null));
        Colaborador soTreinamento = usuarioRepositorio.salvar(Colaborador.novo("Colaborador So Treinamento 119", CPF_SO_TREINAMENTO, LocalDate.of(1993, 1, 1), null));

        vencidoId = vencido.getId();
        proximoId = proximo.getId();
        semHistoricoId = semHistorico.getId();
        soTreinamentoId = soTreinamento.getId();

        // Dados de teste explicitamente identificados; o cadastro pela API é coberto nos testes de #192.
        // A NR-35 vence hoje: na data de vencimento ela já está vencida para alertas e para aptidão.
        capacitacaoRepositorio.salvar(new Certificacao(null, "NR-35 Trabalho em Altura", hoje, hoje.minusYears(2), vencido));
        capacitacaoRepositorio.salvar(new Certificacao(null, "NR-10 Eletricidade", hoje.plusDays(30), hoje.minusYears(1), proximo));
        capacitacaoRepositorio.salvar(new Certificacao(null, "NR-33 Espaço Confinado", hoje.plusDays(31), hoje.minusYears(1), proximo));
        capacitacaoRepositorio.salvar(new Treinamento(null, "Integração de Segurança", "Normas internas e rotas de fuga",
                "Eng. Paula", hoje.minusDays(15), 8, List.of(vencido)));
        capacitacaoRepositorio.salvar(new Treinamento(null, "Integração de Segurança 119", "Normas internas",
                "Eng. Paula", hoje.minusDays(5), 4, List.of(soTreinamento)));

        mockMvc.perform(post("/api/tarefas").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"%s\", \"nivelPerigo\": \"ALTO\"}".formatted(DESCRICAO_TAREFA)))
                .andExpect(status().isCreated());
        tarefaId = jdbc.queryForObject("SELECT id FROM tarefas WHERE descricao = ?", Integer.class, DESCRICAO_TAREFA);
        jdbc.update("INSERT INTO exigencias_capacitacao (nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo) "
                + "VALUES ('NR-35 Trabalho em Altura', 'CERTIFICACAO', ?, 'TAREFA')", tarefaId);
    }

    @AfterEach
    void limparDados() {
        limpar();
    }

    private void limpar() {
        List<Integer> tarefas = jdbc.queryForList("SELECT id FROM tarefas WHERE descricao = ?", Integer.class, DESCRICAO_TAREFA);
        for (Integer id : tarefas) {
            jdbc.update("DELETE FROM exigencias_capacitacao WHERE tipo_alvo = 'TAREFA' AND alvo_id = ?", id);
            jdbc.update("DELETE FROM tarefas_alocacoes WHERE tarefa_id = ?", id);
            jdbc.update("DELETE FROM tarefas WHERE id = ?", id);
        }
        List<Integer> usuarios = jdbc.queryForList("SELECT id FROM usuarios WHERE cpf IN (?, ?, ?, ?, ?) OR email = ?",
                Integer.class, CPF_GESTOR, CPF_VENCIDO, CPF_PROXIMO, CPF_SEM_HISTORICO, CPF_SO_TREINAMENTO, EMAIL_GESTOR);
        for (Integer id : usuarios) {
            // V29 (#193): os vínculos restringem a exclusão; removê-los antes dos treinamentos.
            List<Integer> treinamentos = jdbc.queryForList(
                    "SELECT treinamento_id FROM treinamentos_colaboradores WHERE colaborador_id = ?", Integer.class, id);
            jdbc.update("DELETE FROM treinamentos_colaboradores WHERE colaborador_id = ?", id);
            for (Integer treinamentoId : treinamentos) {
                jdbc.update("DELETE FROM treinamentos WHERE id = ? AND NOT EXISTS "
                        + "(SELECT 1 FROM treinamentos_colaboradores WHERE treinamento_id = ?)", treinamentoId, treinamentoId);
            }
            jdbc.update("DELETE FROM certificacoes WHERE colaborador_id = ?", id);
            jdbc.update("DELETE FROM usuarios WHERE id = ?", id);
        }
    }

    @Test
    @DisplayName("UC02 cenário IV: filtro por situação lê a situação consolidada persistida")
    void deveFiltrarColaboradoresPorSituacao() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/colaboradores").param("situacao", "VENCIDA").with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].colaboradorId", hasItem(vencidoId)))
                .andExpect(jsonPath("$[*].colaboradorId", not(hasItem(proximoId))))
                .andExpect(jsonPath("$[*].colaboradorId", not(hasItem(semHistoricoId))));

        mockMvc.perform(get("/api/capacitacoes/colaboradores").param("situacao", "PROXIMA_DO_VENCIMENTO").with(gestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].colaboradorId", hasItem(proximoId)))
                .andExpect(jsonPath("$[*].colaboradorId", not(hasItem(vencidoId))));

        mockMvc.perform(get("/api/capacitacoes/colaboradores").with(gestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.colaboradorId == %d)].semCertificacoes".formatted(semHistoricoId), contains(true)))
                .andExpect(jsonPath("$[?(@.colaboradorId == %d)].pendenteIntegracaoAdmissional".formatted(semHistoricoId), contains(true)));
    }

    @Test
    @DisplayName("UC02 passos 4 e 5: detalhes reconstroem atributos de certificação e treinamento do PostgreSQL")
    void deveDetalharCapacitacoesPersistidas() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/colaboradores/{id}", vencidoId).with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colaboradorNome").value("Colaborador Vencido 119"))
                .andExpect(jsonPath("$.situacaoCertificacoes").value("VENCIDA"))
                .andExpect(jsonPath("$.certificacoes[0].nome").value("NR-35 Trabalho em Altura"))
                .andExpect(jsonPath("$.certificacoes[0].dataVencimento").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.certificacoesVencidas[0].nome").value("NR-35 Trabalho em Altura"))
                .andExpect(jsonPath("$.treinamentos[0].conteudo").value("Normas internas e rotas de fuga"))
                .andExpect(jsonPath("$.treinamentos[0].instrutor").value("Eng. Paula"))
                .andExpect(jsonPath("$.treinamentos[0].cargaHoraria").value(8))
                .andExpect(jsonPath("$.treinamentos[0].status").value("REALIZADO"));

        mockMvc.perform(get("/api/capacitacoes/colaboradores/{id}", semHistoricoId).with(gestor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificacoes", empty()))
                .andExpect(jsonPath("$.pendenteIntegracaoAdmissional").value(true))
                .andExpect(jsonPath("$.pendencias[0]").value("Pendência da trilha inicial de integração"));

        mockMvc.perform(get("/api/capacitacoes/colaboradores/{id}", 999_999).with(gestor()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("UC02 regra 1: alertas aplicam a fronteira de 30 dias e listam vencidas e pendências")
    void deveConsultarAlertas() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/alertas").with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasAntecedenciaAlerta").value(30))
                .andExpect(jsonPath("$.proximasDoVencimento[?(@.colaboradorId == %d)].nome".formatted(proximoId),
                        contains("NR-10 Eletricidade")))
                .andExpect(jsonPath("$.vencidas[?(@.colaboradorId == %d)].nome".formatted(vencidoId),
                        contains("NR-35 Trabalho em Altura")))
                .andExpect(jsonPath("$.pendencias[*].colaboradorId", hasItem(semHistoricoId)))
                // Ausência de certificações é pendência mesmo com treinamento registrado (UC02, cenário III).
                .andExpect(jsonPath("$.pendencias[*].colaboradorId", hasItem(soTreinamentoId)))
                .andExpect(jsonPath("$.pendencias[*].colaboradorId", not(hasItem(vencidoId))));
    }

    @Test
    @DisplayName("#117: no dia do vencimento a aptidão informa o impedimento e a alocação é recusada sem gravar")
    void deveConsultarAptidaoSemAutorizarAlocacao() throws Exception {
        mockMvc.perform(get("/api/capacitacoes/colaboradores/{id}/aptidao", vencidoId)
                        .param("tarefaId", tarefaId.toString()).with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apto").value(false))
                .andExpect(jsonPath("$.impedimentos[0]").value("certificação obrigatória 'NR-35 Trabalho em Altura' ausente ou vencida"));

        mockMvc.perform(post("/api/tarefas/{id}/alocacoes", tarefaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"colaboradorId\": %d}".formatted(vencidoId)))
                .andExpect(status().isUnprocessableEntity());

        Integer alocacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_alocacoes WHERE tarefa_id = ?", Integer.class, tarefaId);
        assertThat(alocacoes).isZero();
    }

    @Test
    @DisplayName("#192: Gestor cadastra certificação e treinamento com validade, e a consulta os reflete")
    void gestorDeveCadastrarCertificacaoETreinamentoPelaApi() throws Exception {
        LocalDate hoje = LocalDate.now();
        mockMvc.perform(post("/api/capacitacoes/certificacoes").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"NR-18 Obras\", \"dataVencimento\": \"%s\", \"dataEmissao\": \"%s\", \"colaboradorId\": %d}"
                                .formatted(hoje.plusDays(10), hoje.minusYears(1), semHistoricoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PROXIMA_DO_VENCIMENTO"));
        mockMvc.perform(post("/api/capacitacoes/treinamentos").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Primeiros Socorros\", \"dataRealizacao\": \"%s\", \"cargaHoraria\": 8, \"validadeMeses\": 12, \"colaboradoresIds\": [%d]}"
                                .formatted(hoje.minusMonths(13), semHistoricoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("VENCIDO"))
                .andExpect(jsonPath("$.dataVencimento").value(hoje.minusMonths(1).toString()));

        mockMvc.perform(get("/api/capacitacoes/colaboradores/{id}", semHistoricoId).with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificacoes[0].nome").value("NR-18 Obras"))
                .andExpect(jsonPath("$.treinamentos[0].validadeMeses").value(12))
                .andExpect(jsonPath("$.treinamentos[0].status").value("VENCIDO"));

        Integer logs = jdbc.queryForObject("SELECT COUNT(*) FROM logs_auditoria WHERE entidade IN ('Certificacao', 'Treinamento') "
                + "AND justificativa IN ('Cadastro de certificação: NR-18 Obras', 'Cadastro de treinamento: Primeiros Socorros')", Integer.class);
        assertThat(logs).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("#192: vencimento de treinamento cadastrado pela API aparece nos alertas")
    void deveAlertarVencimentoDeTreinamentoCadastrado() throws Exception {
        LocalDate hoje = LocalDate.now();
        String corpo = "{\"nome\": \"%s\", \"dataRealizacao\": \"%s\", \"validadeMeses\": 12, \"colaboradoresIds\": [%d]}";
        mockMvc.perform(post("/api/capacitacoes/treinamentos").with(gestor()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted("Brigada Próxima", hoje.minusMonths(12).plusDays(30), proximoId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/capacitacoes/treinamentos").with(gestor()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo.formatted("Brigada Vencida", hoje.minusMonths(12), vencidoId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/capacitacoes/alertas").with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.treinamentosProximosDoVencimento[?(@.colaboradorId == %d)].nome".formatted(proximoId),
                        contains("Brigada Próxima")))
                .andExpect(jsonPath("$.treinamentosVencidos[?(@.colaboradorId == %d)].nome".formatted(vencidoId),
                        contains("Brigada Vencida")))
                .andExpect(jsonPath("$.treinamentosVencidos[?(@.colaboradorId == %d)].dataVencimento".formatted(vencidoId),
                        contains(hoje.toString())));
    }

    @Test
    @DisplayName("#192: cadastro com dados inválidos é recusado com 400 e nada é gravado")
    void deveRecusarCadastroInvalido() throws Exception {
        mockMvc.perform(post("/api/capacitacoes/certificacoes").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"NR-35\", \"dataVencimento\": \"%s\", \"dataEmissao\": \"%s\", \"colaboradorId\": %d}"
                                .formatted(LocalDate.now().minusDays(1), LocalDate.now().minusYears(1), semHistoricoId)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/capacitacoes/treinamentos").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"NR-35\", \"dataRealizacao\": \"%s\", \"colaboradoresIds\": [%d]}"
                                .formatted(LocalDate.now().plusDays(1), semHistoricoId)))
                .andExpect(status().isBadRequest());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM certificacoes WHERE colaborador_id = ?", Integer.class, semHistoricoId)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM treinamentos_colaboradores WHERE colaborador_id = ?", Integer.class, semHistoricoId)).isZero();
    }

    @Test
    @DisplayName("#192: Supervisor não cadastra capacitações")
    void supervisorNaoDeveCadastrar() throws Exception {
        mockMvc.perform(post("/api/capacitacoes/certificacoes").with(supervisor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"NR-35\", \"dataVencimento\": \"%s\", \"dataEmissao\": \"%s\", \"colaboradorId\": %d}"
                                .formatted(LocalDate.now().plusYears(1), LocalDate.now().minusYears(1), semHistoricoId)))
                .andExpect(status().isForbidden());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM certificacoes WHERE colaborador_id = ?", Integer.class, semHistoricoId)).isZero();
    }

    @Test
    @DisplayName("#192: exigência cadastrada pela API com treinamento vencido bloqueia a alocação com a data de vencimento")
    void deveBloquearAlocacaoPorTreinamentoVencido() throws Exception {
        mockMvc.perform(post("/api/capacitacoes/exigencias").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeCapacitacao\": \"Primeiros Socorros\", \"tipoCapacitacao\": \"TREINAMENTO\", \"alvo\": \"TAREFA\", \"alvoId\": %d}"
                                .formatted(tarefaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alvo").value("TAREFA"));
        mockMvc.perform(post("/api/capacitacoes/treinamentos").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Primeiros Socorros\", \"dataRealizacao\": \"%s\", \"validadeMeses\": 12, \"colaboradoresIds\": [%d]}"
                                .formatted(LocalDate.now().minusMonths(13), semHistoricoId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/capacitacoes/colaboradores/{id}/aptidao", semHistoricoId)
                        .param("tarefaId", tarefaId.toString()).with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apto").value(false))
                .andExpect(jsonPath("$.impedimentos[*]", hasItem(containsString("treinamento obrigatório 'Primeiros Socorros' vencido em"))));
        mockMvc.perform(post("/api/tarefas/{id}/alocacoes", tarefaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"colaboradorId\": %d}".formatted(semHistoricoId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Alocação Bloqueada"))
                .andExpect(jsonPath("$.impedimentos[*]", hasItem(containsString("vencido em"))));
    }

    @Test
    @DisplayName("#192: a alocação aceita só colaboradores; gestor e supervisor são recusados")
    void deveRecusarAlocacaoDeGestor() throws Exception {
        Integer gestorId = jdbc.queryForObject("SELECT id FROM usuarios WHERE cpf = ?", Integer.class, CPF_GESTOR);

        mockMvc.perform(post("/api/tarefas/{id}/alocacoes", tarefaId).with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"colaboradorId\": %d}".formatted(gestorId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Colaborador Não Alocável"));
        mockMvc.perform(get("/api/capacitacoes/colaboradores/{id}/aptidao", gestorId)
                        .param("tarefaId", tarefaId.toString()).with(supervisor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apto").value(false))
                .andExpect(jsonPath("$.impedimentos[0]", containsString("Somente colaboradores")));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tarefas_alocacoes WHERE tarefa_id = ?", Integer.class, tarefaId)).isZero();
    }

    @Test
    @DisplayName("O contrato OpenAPI publica as consultas e o cadastro de capacitações")
    void contratoOpenApiPublicaConsultasECadastro() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/colaboradores'].get").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/colaboradores/{colaboradorId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/alertas'].get").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/colaboradores/{colaboradorId}/aptidao'].get").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/certificacoes'].post").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/treinamentos'].post").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/exigencias'].post").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/colaboradores'].post").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.TreinamentoOutputDTO.properties.cargaHoraria.type").value("integer"))
                .andExpect(jsonPath("$.components.schemas.CertificacaoOutputDTO.properties.dataVencimento.format").value("date"));
    }

    private static RequestPostProcessor gestor() {
        return user(EMAIL_GESTOR).roles("GESTOR_SEGURANCA");
    }

    private static RequestPostProcessor supervisor() {
        return user("supervisor.capacitacao119@safeplace-teste.local").roles("SUPERVISOR");
    }
}

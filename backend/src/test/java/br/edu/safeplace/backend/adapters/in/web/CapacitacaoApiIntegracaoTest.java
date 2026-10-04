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

import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepositoryPort;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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
    private UsuarioRepositorioPorta usuarioRepositorio;

    @Autowired
    private CapacitacaoRepositoryPort capacitacaoRepositorio;

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
        vencidoId = usuarioRepositorio.salvar(Colaborador.novo("Colaborador Vencido 119", CPF_VENCIDO, LocalDate.of(1990, 1, 1), null)).getId();
        proximoId = usuarioRepositorio.salvar(Colaborador.novo("Colaborador Proximo 119", CPF_PROXIMO, LocalDate.of(1991, 1, 1), null)).getId();
        semHistoricoId = usuarioRepositorio.salvar(Colaborador.novo("Colaborador Sem Historico 119", CPF_SEM_HISTORICO, LocalDate.of(1992, 1, 1), null)).getId();
        soTreinamentoId = usuarioRepositorio.salvar(Colaborador.novo("Colaborador So Treinamento 119", CPF_SO_TREINAMENTO, LocalDate.of(1993, 1, 1), null)).getId();

        // Dados de teste explicitamente identificados (#116): a escrita não é exposta pela API.
        // A NR-35 vence hoje: na data de vencimento ela já está vencida para alertas e para aptidão.
        capacitacaoRepositorio.salvarCertificacao(new Certificacao(null, "NR-35 Trabalho em Altura", hoje, hoje.minusYears(2), vencidoId));
        capacitacaoRepositorio.salvarCertificacao(new Certificacao(null, "NR-10 Eletricidade", hoje.plusDays(30), hoje.minusYears(1), proximoId));
        capacitacaoRepositorio.salvarCertificacao(new Certificacao(null, "NR-33 Espaço Confinado", hoje.plusDays(31), hoje.minusYears(1), proximoId));
        capacitacaoRepositorio.salvarTreinamento(new Treinamento(null, "Integração de Segurança", "Normas internas e rotas de fuga",
                "Eng. Paula", hoje.minusDays(15), 8, List.of(vencidoId)));
        capacitacaoRepositorio.salvarTreinamento(new Treinamento(null, "Integração de Segurança 119", "Normas internas",
                "Eng. Paula", hoje.minusDays(5), 4, List.of(soTreinamentoId)));

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
            jdbc.update("DELETE FROM logs_auditoria WHERE entidade IN ('Tarefa', 'AlocacaoTarefa') AND entidade_id = ?", id);
            jdbc.update("DELETE FROM tarefas WHERE id = ?", id);
        }
        List<Integer> usuarios = jdbc.queryForList("SELECT id FROM usuarios WHERE cpf IN (?, ?, ?, ?, ?) OR email = ?",
                Integer.class, CPF_GESTOR, CPF_VENCIDO, CPF_PROXIMO, CPF_SEM_HISTORICO, CPF_SO_TREINAMENTO, EMAIL_GESTOR);
        for (Integer id : usuarios) {
            jdbc.update("DELETE FROM treinamentos WHERE id IN (SELECT treinamento_id FROM treinamentos_colaboradores WHERE colaborador_id = ?)", id);
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
    @DisplayName("Escrita de capacitações não é exposta a nenhum perfil")
    void deveNegarEscrita() throws Exception {
        mockMvc.perform(post("/api/capacitacoes/certificacoes").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"NR-35\", \"colaboradorId\": %d}".formatted(semHistoricoId)))
                .andExpect(status().isForbidden());

        Integer certificacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM certificacoes WHERE colaborador_id = ?", Integer.class, semHistoricoId);
        assertThat(certificacoes).isZero();
    }

    @Test
    @DisplayName("O contrato OpenAPI publica as consultas de capacitação e nenhuma operação de escrita")
    void contratoOpenApiPublicaSomenteConsultas() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/colaboradores'].get").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/colaboradores/{colaboradorId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/alertas'].get").exists())
                .andExpect(jsonPath("$.paths['/api/capacitacoes/colaboradores/{colaboradorId}/aptidao'].get").exists())
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

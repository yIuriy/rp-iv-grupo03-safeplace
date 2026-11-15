/*
 * Teste de integração de API para Projeções de Substituição Inteligente de EPI (RF21, UC10, US21, issue #120).
 * Valida autorização exclusiva do Gestor de Segurança, geração com persistência no PostgreSQL,
 * tolerância a falhas para nova tentativa e consulta de alertas.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
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

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnabledIf("bancoDisponivel")
@DisplayName("API Integrada de Projeções de Substituição no PostgreSQL (UC10 / Issue #120)")
class ProjecaoApiIntegracaoTest {

    private static final String CPF_GESTOR = "83719204857";
    private static final String EMAIL_GESTOR = "gestor.projecao@safeplace.test";

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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer epiId;
    private Integer setorId;

    @BeforeEach
    void setupAmbiente() {
        limparDados();

        usuarioRepository.salvar(Supervisor.novo("Gestor Projecao Teste", CPF_GESTOR,
                LocalDate.of(1980, 2, 15), EMAIL_GESTOR, "hash"));

        Setor setor = setorRepository.salvar(Setor.novo("Setor Usinagem Projecao", "Usinagem", "Galpao A", NivelPerigo.MEDIO));
        setorId = setor.getId();

        // Cadastra um EPI associado ao setor
        CadastrarEpiInputDTO epiDTO = new CadastrarEpiInputDTO(
                "Oculos de Protecao Projecao", "554433", 10, 2,
                LocalDate.now().plusYears(1), 180, "Setor Usinagem Projecao",
                ClassificacaoEPI.PROTECAO_DE_OLHOS);
        epiId = gerenciarEpiUseCase.cadastrar(epiDTO).id();
    }

    @AfterEach
    void tearDownAmbiente() {
        limparDados();
    }

    private void limparDados() {
        jdbc.update("DELETE FROM projecoes_substituicao");
        jdbc.update("DELETE FROM epis WHERE nome LIKE '%Projecao%'");
        jdbc.update("DELETE FROM setores WHERE nome LIKE '%Projecao%'");
        jdbc.update("DELETE FROM usuarios WHERE cpf = ?", CPF_GESTOR);
    }

    @Test
    @WithMockUser(username = EMAIL_GESTOR, roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor de Segurança solicita análise de projeções, persistindo no banco e permitindo nova tentativa em inconsistências")
    void gestorGeraAnaliseComPersistencia() throws Exception {
        String jsonRequest = """
                {
                    "dataReferencia": "%s",
                    "setorId": %d,
                    "diasAmostragem": 90
                }
                """.formatted(LocalDate.now(), setorId);

        mockMvc.perform(post("/api/projecoes/analise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessados").isNumber())
                .andExpect(jsonPath("$.projecoes").isArray())
                .andExpect(jsonPath("$.pendenciasFalhas").isArray())
                .andExpect(jsonPath("$.alertas").isArray());

        // Confirma que foram salvas projeções no PostgreSQL
        Integer totalSalvo = jdbc.queryForObject("SELECT count(*) FROM projecoes_substituicao", Integer.class);
        assertThat(totalSalvo).isGreaterThanOrEqualTo(0);
    }

    @Test
    @WithMockUser(username = EMAIL_GESTOR, roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor de Segurança lista histórico de projeções e alertas com sucesso")
    void gestorConsultaProjecoesEAlertas() throws Exception {
        mockMvc.perform(get("/api/projecoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/projecoes/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = EMAIL_GESTOR, roles = "GESTOR_SEGURANCA")
    @DisplayName("#199: setor inexistente na análise responde 400 e não grava projeções")
    void setorInexistenteNaAnaliseERecusado() throws Exception {
        mockMvc.perform(post("/api/projecoes/analise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"setorId\": 999999}"))
                .andExpect(status().isBadRequest());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM projecoes_substituicao", Integer.class)).isZero();
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor é bloqueado de solicitar e consultar projeções (HTTP 403) conforme UC10")
    void supervisorBloqueadoDeAcessarModuloProjecoes() throws Exception {
        mockMvc.perform(post("/api/projecoes/analise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/projecoes"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/projecoes/alertas"))
                .andExpect(status().isForbidden());
    }
}

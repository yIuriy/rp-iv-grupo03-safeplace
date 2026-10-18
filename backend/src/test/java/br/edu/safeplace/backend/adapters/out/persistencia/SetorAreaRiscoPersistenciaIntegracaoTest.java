package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.AtualizarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Testes de Integração de Persistência de Setores, Áreas de Risco e Auditoria")
class SetorAreaRiscoPersistenciaIntegracaoTest {

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private GerenciarSetorUseCase setorUseCase;

    @Autowired
    private GerenciarAreaRiscoUseCase areaRiscoUseCase;

    @Autowired
    private EpiRepositoryPort epiRepositoryPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer epiId1;
    private Integer epiId2;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM notificacoes_ocorrencia");
        jdbcTemplate.execute("DELETE FROM ocorrencias");
        jdbcTemplate.execute("DELETE FROM areas_risco_epis");
        jdbcTemplate.execute("DELETE FROM areas_risco");
        jdbcTemplate.execute("DELETE FROM setores");
        jdbcTemplate.execute("DELETE FROM movimentacoes_estoque");
        jdbcTemplate.execute("DELETE FROM epis");

        Epi epi1 = new Epi(null, "Capacete H-700", "CA-12345", 20, 5,
                StatusEpi.DISPONIVEL, LocalDate.now().plusYears(2), 180, "Capacete", null, null);
        Epi epi2 = new Epi(null, "Óculos de Proteção", "CA-54321", 15, 3,
                StatusEpi.DISPONIVEL, LocalDate.now().plusYears(2), 180, "Óculos", null, null);

        epiId1 = epiRepositoryPort.salvar(epi1).getId();
        epiId2 = epiRepositoryPort.salvar(epi2).getId();
    }

    @Test
    @DisplayName("Deve persistir setor físico e associar a uma nova área de risco com agentes e limites")
    void devePersistirSetorEAssociarAreaRisco() {
        SetorOutputDTO setor = setorUseCase.cadastrarSetor(new CadastrarSetorInputDTO(
                "Usinagem Pesada", "Setor com tornos CNC", "Galpão 3", NivelPerigo.ALTO));
        assertThat(setor.id()).isNotNull();

        CadastrarAreaRiscoInputDTO areaInput = new CadastrarAreaRiscoInputDTO(
                "AR-USI-01",
                "Área dos Tornos CNC",
                "Operação de tornos de grande porte",
                NivelPerigo.ALTO,
                setor.id(),
                List.of("Ruído excessivo", "Projeção de cavacos"),
                "Linha amarela ao redor das máquinas",
                List.of(epiId1, epiId2)
        );

        AreaRiscoOutputDTO areaSalva = areaRiscoUseCase.cadastrarAreaRisco(areaInput);

        assertThat(areaSalva.id()).isNotNull();
        assertThat(areaSalva.codigo()).isEqualTo("AR-USI-01");
        assertThat(areaSalva.setorId()).isEqualTo(setor.id());
        assertThat(areaSalva.agentesRisco()).containsExactly("Ruído excessivo", "Projeção de cavacos");
        assertThat(areaSalva.limites()).isEqualTo("Linha amarela ao redor das máquinas");
        assertThat(areaSalva.episObrigatorios()).hasSize(2);
    }

    @Test
    @DisplayName("Deve rejeitar cadastro de área de risco com código duplicado")
    void deveRejeitarCodigoDuplicado() {
        CadastrarAreaRiscoInputDTO area1 = new CadastrarAreaRiscoInputDTO(
                "AR-DUP-01", "Área 1", "Desc", NivelPerigo.MEDIO, null, List.of("Ruído"), "Galpão 1", List.of(epiId1));
        areaRiscoUseCase.cadastrarAreaRisco(area1, "gestor@safeplace.com");

        CadastrarAreaRiscoInputDTO area2 = new CadastrarAreaRiscoInputDTO(
                "ar-dup-01", "Área 2", "Outra Desc", NivelPerigo.ALTO, null, List.of("Calor"), "Galpão 2", List.of(epiId2));

        assertThatThrownBy(() -> areaRiscoUseCase.cadastrarAreaRisco(area2, "gestor@safeplace.com"))
                .isInstanceOf(CodigoAreaRiscoDuplicadoException.class);
    }

    @Test
    @DisplayName("Deve rejeitar cadastro de área de risco sem EPIs obrigatórios")
    void deveRejeitarAreaSemEpisObrigatorios() {
        CadastrarAreaRiscoInputDTO areaSemEpis = new CadastrarAreaRiscoInputDTO(
                "AR-NO-EPI", "Área sem EPI", "Desc", NivelPerigo.BAIXO, null, List.of("Poeira"), "Pátio", List.of());

        assertThatThrownBy(() -> areaRiscoUseCase.cadastrarAreaRisco(areaSemEpis))
                .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class);
    }

    @Test
    @DisplayName("Deve atualizar área de risco e registrar log de auditoria")
    void deveAtualizarAreaERegistrarLogAuditoria() {
        CadastrarAreaRiscoInputDTO areaInput = new CadastrarAreaRiscoInputDTO(
                "AR-AUDIT-01", "Área Original", "Desc", NivelPerigo.MEDIO, null,
                List.of("Ruído"), "Limite original", List.of(epiId1));
        AreaRiscoOutputDTO criada = areaRiscoUseCase.cadastrarAreaRisco(areaInput);

        AtualizarAreaRiscoInputDTO atualizacao = new AtualizarAreaRiscoInputDTO(
                "Área Atualizada",
                "Nova Descrição",
                NivelPerigo.ALTO,
                null,
                List.of("Ruído", "Calor"),
                "Novo Limite demarcado",
                List.of(epiId1, epiId2),
                "Revisão de risco anual pelo gestor"
        );

        AreaRiscoOutputDTO atualizada = areaRiscoUseCase.atualizarAreaRisco(criada.id(), atualizacao, "gestor@safeplace.com");

        assertThat(atualizada.nome()).isEqualTo("Área Atualizada");
        assertThat(atualizada.nivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(atualizada.agentesRisco()).containsExactly("Ruído", "Calor");
        assertThat(atualizada.limites()).isEqualTo("Novo Limite demarcado");
        assertThat(atualizada.episObrigatorios()).hasSize(2);

        // Verifica se o log de auditoria foi gravado na tabela
        Integer countAudit = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM logs_auditoria WHERE entidade = 'AreaRisco' AND entidade_id = ? AND tipo_operacao = 'ALTERACAO'",
                Integer.class,
                criada.id()
        );
        assertThat(countAudit).isEqualTo(1);
    }
}

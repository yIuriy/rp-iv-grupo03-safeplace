package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercita os módulos de áreas de risco e de tarefas de ponta a ponta contra um PostgreSQL real:
 * migrações Flyway, mapeamento JPA, tabela de junção e ida e volta das enumerações.
 *
 * <p>Cada teste roda em transação com rollback, então nada é deixado no banco.</p>
 */
@SpringBootTest
@Transactional
@EnabledIf("bancoDisponivel")
class AreaRiscoTarefaPersistenciaTest {

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private GerenciarAreaRiscoUseCase areaRiscoUseCase;

    @Autowired
    private GerenciarSetorUseCase setorUseCase;

    @Autowired
    private ClassificarTarefaUseCase tarefaUseCase;

    @Autowired
    private EpiRepositoryPort epiRepositoryPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Gestor de Segurança criado dentro da transação de cada teste (e desfeito no rollback); é o
     * responsável técnico das classificações. Não se usa a conta de demonstração da V17, que
     * outros testes de integração podem remover do banco compartilhado.
     */
    private static final String EMAIL_GESTOR = "gestor.tarefas@safeplace-teste-112.local";
    private static final String CPF_GESTOR = "73194628500";

    @Autowired
    private UsuarioRepositorioPorta usuarioRepositorio;

    private Integer idGestor;

    @BeforeEach
    void autenticarGestor() {
        idGestor = usuarioRepositorio.salvar(GestorDeSeguranca.novo(
                "Gestor Tarefas 112", CPF_GESTOR, LocalDate.of(1985, 1, 15), EMAIL_GESTOR, "hash-de-teste")).getId();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EMAIL_GESTOR, null, List.of(new SimpleGrantedAuthority("ROLE_GESTOR_SEGURANCA"))));
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private Integer novoEpi(String nome, String numeroCa) {
        Epi salvo = epiRepositoryPort.salvar(Epi.novo(nome, numeroCa, 10, 2,
                LocalDate.now().plusYears(2), 365, null, null));
        return salvo.getId();
    }

    /**
     * Força a leitura a bater no banco, e não no contexto de persistência.
     */
    private void reiniciarContexto() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("As migrações V6 e V7 devem ter criado as tabelas dos dois módulos")
    void migracoesDevemTerCriadoAsTabelas() {
        assertThat(tabelaExiste("areas_risco")).isTrue();
        assertThat(tabelaExiste("areas_risco_epis")).isTrue();
        assertThat(tabelaExiste("tarefas")).isTrue();

        Integer restricoesUnicas = jdbcTemplate.queryForObject("""
                SELECT count(*) FROM information_schema.table_constraints
                WHERE table_name = 'areas_risco' AND constraint_type = 'UNIQUE'
                """, Integer.class);
        assertThat(restricoesUnicas).isPositive();
    }

    private boolean tabelaExiste(String tabela) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = ?",
                Integer.class, tabela);
        return total != null && total > 0;
    }

    @Test
    @DisplayName("Deve persistir área de risco com os EPIs obrigatórios na tabela de junção")
    void devePersistirAreaRiscoComEpisNaTabelaDeJuncao() {
        Integer capacete = novoEpi("Capacete", "91001");
        Integer luva = novoEpi("Luva térmica", "91002");

        AreaRiscoOutputDTO salva = areaRiscoUseCase.cadastrarAreaRisco(new CadastrarAreaRiscoInputDTO(
                "IT-SET-01", "Linha de Produção", "Solda e montagem", NivelPerigo.ALTO,
                List.of(capacete, luva)));

        reiniciarContexto();

        AreaRiscoOutputDTO lida = areaRiscoUseCase.buscarPorId(salva.id());
        assertThat(lida.codigo()).isEqualTo("IT-SET-01");
        assertThat(lida.nivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(lida.episObrigatorios()).extracting(dto -> dto.nome())
                .containsExactlyInAnyOrder("Capacete", "Luva térmica");

        Integer vinculos = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM areas_risco_epis WHERE area_risco_id = ?",
                Integer.class, salva.id());
        assertThat(vinculos).isEqualTo(2);
    }

    @Test
    @DisplayName("UC03 exceção I: código duplicado deve ser barrado com o banco real")
    void codigoDuplicadoDeveSerBarrado() {
        Integer capacete = novoEpi("Capacete", "91003");

        areaRiscoUseCase.cadastrarAreaRisco(new CadastrarAreaRiscoInputDTO(
                "IT-SET-02", "Caldeiras", null, NivelPerigo.CRITICO, List.of(capacete)));

        reiniciarContexto();

        assertThatThrownBy(() -> areaRiscoUseCase.cadastrarAreaRisco(new CadastrarAreaRiscoInputDTO(
                "it-set-02", "Outro setor", null, NivelPerigo.BAIXO, List.of(capacete))))
                .isInstanceOf(CodigoAreaRiscoDuplicadoException.class);
    }

    @Test
    @DisplayName("UC03 alternativo I: filtro por grau de perigo deve consultar o banco")
    void filtroPorGrauDePerigoDeveConsultarOBanco() {
        Integer capacete = novoEpi("Capacete", "91004");

        areaRiscoUseCase.cadastrarAreaRisco(new CadastrarAreaRiscoInputDTO(
                "IT-SET-03", "Pintura", null, NivelPerigo.MEDIO, List.of(capacete)));
        areaRiscoUseCase.cadastrarAreaRisco(new CadastrarAreaRiscoInputDTO(
                "IT-SET-04", "Solda", null, NivelPerigo.CRITICO, List.of(capacete)));

        reiniciarContexto();

        assertThat(areaRiscoUseCase.listarPorNivelPerigo(NivelPerigo.MEDIO))
                .extracting(AreaRiscoOutputDTO::codigo)
                .contains("IT-SET-03")
                .doesNotContain("IT-SET-04");
    }

    @Test
    @DisplayName("Deve persistir tarefa sem classificação e classificá-la depois")
    void devePersistirTarefaSemClassificacaoEClassificarDepois() {
        TarefaOutputDTO semClassificacao = tarefaUseCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Inspeção visual de andaimes", null));

        reiniciarContexto();

        TarefaOutputDTO lida = tarefaUseCase.buscarPorId(semClassificacao.id());
        assertThat(lida.classificada()).isFalse();
        assertThat(lida.nivelPerigo()).isNull();
        assertThat(lida.dataClassificacao()).isNull();

        tarefaUseCase.classificar(lida.id(), NivelPerigo.CRITICO);

        reiniciarContexto();

        TarefaOutputDTO classificada = tarefaUseCase.buscarPorId(lida.id());
        assertThat(classificada.nivelPerigo()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(classificada.classificada()).isTrue();
        assertThat(classificada.dataClassificacao()).isNotNull();

        Integer linhas = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tarefas WHERE id = ? AND nivel_perigo = 'CRITICO'",
                Integer.class, lida.id());
        assertThat(linhas).isEqualTo(1);
    }

    @Test
    @DisplayName("A reclassificação não deve duplicar linhas na tabela de tarefas")
    void reclassificacaoNaoDeveDuplicarLinhas() {
        TarefaOutputDTO tarefa = tarefaUseCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Trabalho em espaço confinado", NivelPerigo.ALTO));

        tarefaUseCase.classificar(tarefa.id(), NivelPerigo.BAIXO);
        tarefaUseCase.classificar(tarefa.id(), NivelPerigo.CRITICO);

        reiniciarContexto();

        Integer linhas = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tarefas WHERE descricao = ?",
                Integer.class, "Trabalho em espaço confinado");
        assertThat(linhas).isEqualTo(1);
        assertThat(tarefaUseCase.buscarPorId(tarefa.id()).nivelPerigo()).isEqualTo(NivelPerigo.CRITICO);
    }

    @Test
    @DisplayName("#112: classificação e reavaliação gravam responsável técnico e histórico com valor anterior")
    void classificacaoGravaResponsavelEHistorico() {
        TarefaOutputDTO tarefa = tarefaUseCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Manutenção de painel elétrico", NivelPerigo.CRITICO));
        tarefaUseCase.classificar(tarefa.id(), NivelPerigo.ALTO);

        reiniciarContexto();

        TarefaOutputDTO lida = tarefaUseCase.buscarPorId(tarefa.id());
        assertThat(lida.nivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(lida.responsavelClassificacaoId()).isEqualTo(idGestor);
        assertThat(lida.responsavelClassificacaoNome()).isEqualTo("Gestor Tarefas 112");

        Integer responsavelGravado = jdbcTemplate.queryForObject(
                "SELECT responsavel_classificacao_id FROM tarefas WHERE id = ?", Integer.class, tarefa.id());
        assertThat(responsavelGravado).isEqualTo(idGestor);

        List<String> historico = jdbcTemplate.queryForList("""
                SELECT justificativa FROM logs_auditoria
                WHERE entidade = 'Tarefa' AND entidade_id = ? AND responsavel = ?
                  AND justificativa LIKE 'Classificação de periculosidade%'
                ORDER BY id
                """, String.class, tarefa.id(), EMAIL_GESTOR);
        assertThat(historico).satisfiesExactly(
                primeira -> assertThat(primeira).contains("de sem classificação para CRITICO"),
                segunda -> assertThat(segunda).contains("de CRITICO para ALTO"));
    }

    @Test
    @DisplayName("#112: tarefa classificada antes do registro de autoria é lida sem responsável inventado")
    void classificacaoLegadaSemResponsavel() {
        jdbcTemplate.update("""
                INSERT INTO tarefas (descricao, nivel_perigo, data_classificacao)
                VALUES ('Tarefa legada', 'MEDIO', TIMESTAMP '2026-09-01 09:00:00')
                """);
        Integer id = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefas WHERE descricao = 'Tarefa legada'", Integer.class);

        TarefaOutputDTO lida = tarefaUseCase.buscarPorId(id);

        assertThat(lida.classificada()).isTrue();
        assertThat(lida.nivelPerigo()).isEqualTo(NivelPerigo.MEDIO);
        assertThat(lida.responsavelClassificacaoId()).isNull();
    }

    @Test
    @DisplayName("#113: tarefa reconstrói área e setores vinculados após leitura do PostgreSQL")
    void tarefaDeveReconstruirAreaESetoresVinculados() {
        Integer epiId = novoEpi("Protetor auricular", "91005");
        var setorA = setorUseCase.cadastrarSetor(new CadastrarSetorInputDTO(
                "Prensa", "Prensas hidráulicas", "Galpão A", NivelPerigo.ALTO));
        var setorB = setorUseCase.cadastrarSetor(new CadastrarSetorInputDTO(
                "Expedição", "Área de carga", "Galpão B", NivelPerigo.MEDIO));
        AreaRiscoOutputDTO area = areaRiscoUseCase.cadastrarAreaRisco(new CadastrarAreaRiscoInputDTO(
                "AR-TAR-113", "Prensas", null, NivelPerigo.ALTO, setorA.id(), List.of("Ruído"),
                "Faixa de segurança", List.of(epiId)));

        TarefaOutputDTO salva = tarefaUseCase.cadastrarTarefa(new CadastrarTarefaInputDTO(
                "Operar prensa hidráulica", null, area.id(), List.of(setorA.id(), setorB.id())));

        reiniciarContexto();

        TarefaOutputDTO reconstruida = tarefaUseCase.buscarPorId(salva.id());
        assertThat(reconstruida.areaRiscoId()).isEqualTo(area.id());
        assertThat(reconstruida.setoresIds()).containsExactlyInAnyOrder(setorA.id(), setorB.id());
    }

    @Test
    @DisplayName("#113: setor ou área inexistente não grava tarefa")
    void relacaoInexistenteNaoDeveGravarTarefa() {
        assertThatThrownBy(() -> tarefaUseCase.cadastrarTarefa(new CadastrarTarefaInputDTO(
                "Tarefa sem vínculo válido", null, 999999, List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Área de risco não encontrada");

        assertThatThrownBy(() -> tarefaUseCase.cadastrarTarefa(new CadastrarTarefaInputDTO(
                "Tarefa sem setor válido", null, null, List.of(999999))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Setor não encontrado");

        Integer gravadas = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tarefas WHERE descricao IN ('Tarefa sem vínculo válido', 'Tarefa sem setor válido')",
                Integer.class);
        assertThat(gravadas).isZero();
    }
}

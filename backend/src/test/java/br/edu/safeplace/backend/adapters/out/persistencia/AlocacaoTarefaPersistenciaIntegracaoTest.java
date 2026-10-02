package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.AlocarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Alocação de Tarefas Persistida: Integridade, Bloqueio e Auditoria no PostgreSQL (Issue #117)")
class AlocacaoTarefaPersistenciaIntegracaoTest {

    private static final String CPF_GESTOR = "44455566677";
    private static final String CPF_COLABORADOR = "77788899900";
    private static final String EMAIL_GESTOR = "gestor.alocacao@safeplace.test";

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private AlocarColaboradorTarefaUseCase alocacaoUseCase;

    @Autowired
    private TarefaRepositoryPort tarefas;

    @Autowired
    private UsuarioRepositorioPorta usuarios;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer colaboradorId;
    private Integer tarefaClassificadaId;
    private Integer tarefaSemClassificacaoId;

    @BeforeEach
    void prepararBanco() {
        jdbc.update("DELETE FROM tarefas_alocacoes");
        jdbc.update("DELETE FROM exigencias_capacitacao WHERE tipo_alvo = 'TAREFA'");
        jdbc.update("DELETE FROM certificacoes");
        jdbc.update("DELETE FROM treinamentos_colaboradores");
        jdbc.update("DELETE FROM treinamentos");
        jdbc.update("DELETE FROM logs_auditoria WHERE entidade = 'AlocacaoTarefa'");
        jdbc.update("DELETE FROM tarefas WHERE descricao LIKE '%Teste Alocacao%'");
        jdbc.update("DELETE FROM usuarios WHERE cpf IN (?, ?)", CPF_GESTOR, CPF_COLABORADOR);

        GestorDeSeguranca gestor = (GestorDeSeguranca) usuarios.salvar(
                GestorDeSeguranca.novo("Gestor Teste", CPF_GESTOR, LocalDate.of(1980, 1, 1), EMAIL_GESTOR, "hash"));
        Colaborador colab = usuarios.salvar(
                Colaborador.novo("Colaborador Teste", CPF_COLABORADOR, LocalDate.of(1995, 5, 15), null));
        colaboradorId = colab.getId();

        Tarefa classificada = tarefas.salvar(
                new Tarefa(null, "Tarefa Classificada - Teste Alocacao", NivelPerigo.ALTO, LocalDateTime.now(), gestor));
        tarefaClassificadaId = classificada.getId();

        Tarefa semClassificacao = tarefas.salvar(Tarefa.cadastrar("Tarefa Sem Grau - Teste Alocacao"));
        tarefaSemClassificacaoId = semClassificacao.getId();
    }

    @Test
    @DisplayName("Alocação com sucesso grava em tarefas_alocacoes e gera log de auditoria")
    void alocacaoSucessoPersisteNoBancoEGeraAuditoria() {
        AlocacaoTarefaOutputDTO resultado = alocacaoUseCase.alocar(
                tarefaClassificadaId,
                new AlocarColaboradorInputDTO(colaboradorId, "Alocação normal sem exigências"),
                EMAIL_GESTOR
        );

        assertThat(resultado.id()).isNotNull();

        int totalAlocacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_alocacoes WHERE tarefa_id = ? AND colaborador_id = ?",
                Integer.class, tarefaClassificadaId, colaboradorId);
        assertThat(totalAlocacoes).isEqualTo(1);

        int totalAuditoria = jdbc.queryForObject(
                "SELECT COUNT(*) FROM logs_auditoria WHERE entidade = 'AlocacaoTarefa' AND entidade_id = ?",
                Integer.class, resultado.id());
        assertThat(totalAuditoria).isEqualTo(1);
    }

    @Test
    @DisplayName("Tarefa sem classificação bloqueia alocação e NÃO grava nada no banco")
    void tarefaSemClassificacaoNaoGravaNada() {
        assertThatThrownBy(() -> alocacaoUseCase.alocar(
                tarefaSemClassificacaoId,
                new AlocarColaboradorInputDTO(colaboradorId, "Tentativa indevida"),
                EMAIL_GESTOR
        )).isInstanceOf(TarefaSemClassificacaoException.class);

        int totalAlocacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_alocacoes WHERE tarefa_id = ?",
                Integer.class, tarefaSemClassificacaoId);
        assertThat(totalAlocacoes).isZero();

        int totalAuditoria = jdbc.queryForObject(
                "SELECT COUNT(*) FROM logs_auditoria WHERE entidade = 'AlocacaoTarefa'",
                Integer.class);
        assertThat(totalAuditoria).isZero();
    }

    @Test
    @DisplayName("Certificação exigida vencida bloqueia alocação e NÃO grava nada no banco")
    void certificacaoVencidaNaoGravaNada() {
        jdbc.update("INSERT INTO exigencias_capacitacao (nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo) " +
                "VALUES ('NR-35 Altura', 'CERTIFICACAO', ?, 'TAREFA')", tarefaClassificadaId);
        jdbc.update("INSERT INTO certificacoes (nome, data_vencimento, data_emissao, colaborador_id) " +
                "VALUES ('NR-35 Altura', ?, ?, ?)", LocalDate.now().minusDays(1), LocalDate.now().minusYears(1), colaboradorId);

        assertThatThrownBy(() -> alocacaoUseCase.alocar(
                tarefaClassificadaId,
                new AlocarColaboradorInputDTO(colaboradorId, "Tentativa com cert vencida"),
                EMAIL_GESTOR
        )).isInstanceOf(AlocacaoBloqueadaException.class)
          .hasMessageContaining("NR-35 Altura");

        int totalAlocacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_alocacoes WHERE tarefa_id = ?",
                Integer.class, tarefaClassificadaId);
        assertThat(totalAlocacoes).isZero();

        // Renovando a certificação, a alocação passa a gravar
        jdbc.update("UPDATE certificacoes SET data_vencimento = ? WHERE colaborador_id = ?",
                LocalDate.now().plusYears(1), colaboradorId);

        AlocacaoTarefaOutputDTO resultado = alocacaoUseCase.alocar(
                tarefaClassificadaId,
                new AlocarColaboradorInputDTO(colaboradorId, "Agora apto"),
                EMAIL_GESTOR
        );
        assertThat(resultado.id()).isNotNull();

        int totalAposRenovacao = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_alocacoes WHERE tarefa_id = ?",
                Integer.class, tarefaClassificadaId);
        assertThat(totalAposRenovacao).isEqualTo(1);
    }

    @Test
    @DisplayName("Treinamento exigido pendente/não realizado bloqueia alocação e NÃO grava nada")
    void treinamentoPendenteNaoGravaNada() {
        jdbc.update("INSERT INTO exigencias_capacitacao (nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo) " +
                "VALUES ('NR-33 Confinado', 'TREINAMENTO', ?, 'TAREFA')", tarefaClassificadaId);

        assertThatThrownBy(() -> alocacaoUseCase.alocar(
                tarefaClassificadaId,
                new AlocarColaboradorInputDTO(colaboradorId, "Tentativa sem treino"),
                EMAIL_GESTOR
        )).isInstanceOf(AlocacaoBloqueadaException.class)
          .hasMessageContaining("NR-33 Confinado");

        int totalAlocacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tarefas_alocacoes WHERE tarefa_id = ?",
                Integer.class, tarefaClassificadaId);
        assertThat(totalAlocacoes).isZero();
    }
}

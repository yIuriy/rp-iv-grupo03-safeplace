package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

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

import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.CadastrarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepositoryPort;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Capacitações Persistidas: Integridade Relacional, UC02, Datas e Auditoria #121 no PostgreSQL")
class CapacitacaoPersistenciaIntegracaoTest {

    private static final String CPF_COLAB_1 = "11122233344";
    private static final String CPF_COLAB_2 = "22233344455";
    private static final String CPF_COLAB_3 = "33344455566";
    private static final String CPF_COLAB_SEM_DADOS = "44455566600";

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private CadastrarCapacitacaoUseCase cadastroService;

    @Autowired
    private ConsultarCapacitacaoUseCase consultaService;

    @Autowired
    private CapacitacaoRepositoryPort repositoryPort;

    @Autowired
    private UsuarioRepositorioPorta usuarioRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer colab1Id;
    private Integer colab2Id;
    private Integer colab3Id;
    private Integer colabSemDadosId;

    @BeforeEach
    void prepararAmbiente() {
        jdbc.update("DELETE FROM tarefas_alocacoes");
        jdbc.update("DELETE FROM exigencias_capacitacao");
        jdbc.update("DELETE FROM certificacoes");
        jdbc.update("DELETE FROM treinamentos_colaboradores");
        jdbc.update("DELETE FROM treinamentos");
        jdbc.update("DELETE FROM usuarios WHERE cpf IN (?, ?, ?, ?)", CPF_COLAB_1, CPF_COLAB_2, CPF_COLAB_3, CPF_COLAB_SEM_DADOS);

        // Criação de Colaboradores operacionais: sem senha/conta conforme RF23
        Colaborador c1 = usuarioRepository.salvar(Colaborador.novo("Colaborador Um", CPF_COLAB_1, LocalDate.of(1990, 1, 1), null));
        Colaborador c2 = usuarioRepository.salvar(Colaborador.novo("Colaborador Dois", CPF_COLAB_2, LocalDate.of(1992, 2, 2), null));
        Colaborador c3 = usuarioRepository.salvar(Colaborador.novo("Colaborador Tres", CPF_COLAB_3, LocalDate.of(1994, 3, 3), null));
        Colaborador cSemDados = usuarioRepository.salvar(Colaborador.novo("Colaborador Sem Dados", CPF_COLAB_SEM_DADOS, LocalDate.of(1996, 4, 4), null));

        colab1Id = c1.getId();
        colab2Id = c2.getId();
        colab3Id = c3.getId();
        colabSemDadosId = cSemDados.getId();
    }

    @Test
    @DisplayName("Critério 1 e 2: Persistir e reconstruir Certificação com vínculo a Colaborador sem conta/senha")
    void devePersistirEReconstruirCertificacaoSemExigirSenha() {
        LocalDate hoje = LocalDate.now();
        LocalDate vencimento = hoje.plusMonths(12);
        LocalDate emissao = hoje.minusMonths(1);

        Certificacao salva = cadastroService.cadastrarCertificacao("NR-35 Trabalho em Altura", vencimento, emissao, colab1Id);

        assertThat(salva.getId()).isNotNull();

        // 1. Reconstrução completa dos atributos
        Certificacao recuperada = repositoryPort.buscarCertificacaoPorId(salva.getId()).orElseThrow();
        assertThat(recuperada.getNome()).isEqualTo("NR-35 Trabalho em Altura");
        assertThat(recuperada.getDataVencimento()).isEqualTo(vencimento);
        assertThat(recuperada.getDataEmissao()).isEqualTo(emissao);
        assertThat(recuperada.getColaboradorId()).isEqualTo(colab1Id);

        // 2. Colaborador não possui senha no banco
        String senha = jdbc.queryForObject("SELECT senha FROM usuarios WHERE id = ?", String.class, colab1Id);
        assertThat(senha).isNull();

        // 3. Auditoria integrada (#121)
        int logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM logs_auditoria WHERE entidade = 'Certificacao' AND entidade_id = ? AND tipo_operacao = 'CADASTRO'",
                Integer.class, salva.getId());
        assertThat(logs).isEqualTo(1);
    }

    @Test
    @DisplayName("Critério 1: Persistir e reconstruir Treinamento com conteúdo, instrutor, carga horária e relação N:N")
    void devePersistirEReconstruirTreinamentoComTodosAtributos() {
        LocalDate hoje = LocalDate.now();
        LocalDate realizacao = hoje.minusDays(10);

        Treinamento salvo = cadastroService.cadastrarTreinamento(
                "Treinamento de Espaço Confinado",
                "Conteúdo completo sobre ventilação, detecção de gases e resgate",
                "Eng. Carlos Silva",
                realizacao,
                16,
                List.of(colab1Id, colab2Id)
        );

        assertThat(salvo.getId()).isNotNull();

        // Reconstrução integral
        Treinamento recuperado = repositoryPort.buscarTreinamentoPorId(salvo.getId()).orElseThrow();
        assertThat(recuperado.getNome()).isEqualTo("Treinamento de Espaço Confinado");
        assertThat(recuperado.getConteudo()).isEqualTo("Conteúdo completo sobre ventilação, detecção de gases e resgate");
        assertThat(recuperado.getInstrutor()).isEqualTo("Eng. Carlos Silva");
        assertThat(recuperado.getDataRealizacao()).isEqualTo(realizacao);
        assertThat(recuperado.getCargaHoraria()).isEqualTo(16);
        assertThat(recuperado.getColaboradoresIds()).containsExactlyInAnyOrder(colab1Id, colab2Id);

        // Tabela associativa treinamentos_colaboradores no PostgreSQL
        int associacoes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM treinamentos_colaboradores WHERE treinamento_id = ?",
                Integer.class, salvo.getId());
        assertThat(associacoes).isEqualTo(2);

        // Auditoria integrada (#121)
        int logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM logs_auditoria WHERE entidade = 'Treinamento' AND entidade_id = ? AND tipo_operacao = 'CADASTRO'",
                Integer.class, salvo.getId());
        assertThat(logs).isEqualTo(1);
    }

    @Test
    @DisplayName("Critério 3: Consultas distinguem ausência, vencido e próximo de vencer conforme UC02")
    void consultasDistinguemAusenciaVencidoEProximoDeVencerConformeUC02() {
        LocalDate hoje = LocalDate.now();

        // 1. Cenário III: Ausência de histórico
        PendenciasColaboradorDTO pendenciaAusente = consultaService.consultarPendenciasColaborador(colabSemDadosId);
        assertThat(pendenciaAusente.pendenteIntegracaoAdmissional()).isTrue();
        assertThat(pendenciaAusente.totalCertificacoes()).isZero();
        assertThat(pendenciaAusente.totalTreinamentosRealizados()).isZero();
        assertThat(pendenciaAusente.obterMensagensAvisos())
                .contains("Pendência da trilha inicial de integração", "Pendência de treinamentos admissionais");

        // 2. Cenário I: Vencida (inserindo via SQL direto para simular certificação antiga que venceu)
        jdbc.update("INSERT INTO certificacoes (nome, data_vencimento, data_emissao, colaborador_id) VALUES (?, ?, ?, ?)",
                "NR-10 Segurança em Instalações Elétricas", java.sql.Date.valueOf(hoje.minusDays(5)), java.sql.Date.valueOf(hoje.minusYears(2)), colab1Id);

        PendenciasColaboradorDTO pendenciaVencida = consultaService.consultarPendenciasColaborador(colab1Id);
        assertThat(pendenciaVencida.certificacoesVencidas()).isEqualTo(1);
        assertThat(pendenciaVencida.possuiPendenciasImpeditivas()).isTrue();
        assertThat(pendenciaVencida.obterMensagensAvisos())
                .anyMatch(msg -> msg.contains("vencida(s) necessitando de renovação imediata"));

        // 3. Regra 1: Próxima do vencimento em 30 dias (ex.: vence em 15 dias)
        cadastroService.cadastrarCertificacao("NR-35 Alerta", hoje.plusDays(15), hoje.minusMonths(11), colab2Id);
        PendenciasColaboradorDTO pendenciaProxima = consultaService.consultarPendenciasColaborador(colab2Id);
        assertThat(pendenciaProxima.certificacoesProximasVencimento()).isEqualTo(1);
        assertThat(pendenciaProxima.possuiPendenciasImpeditivas()).isFalse();
        assertThat(pendenciaProxima.obterMensagensAvisos())
                .anyMatch(msg -> msg.contains("próxima(s) do vencimento (30 dias)"));

        // 4. Em dia (vence em 180 dias)
        cadastroService.cadastrarCertificacao("NR-33 Espaço Confinado", hoje.plusDays(180), hoje.minusMonths(1), colab3Id);
        PendenciasColaboradorDTO pendenciaEmDia = consultaService.consultarPendenciasColaborador(colab3Id);
        assertThat(pendenciaEmDia.certificacoesEmDia()).isEqualTo(1);
        assertThat(pendenciaEmDia.certificacoesVencidas()).isZero();
        assertThat(pendenciaEmDia.certificacoesProximasVencimento()).isZero();

        // 5. Cenário IV: Filtragem de colaboradores por status
        List<Integer> vencidos = consultaService.listarColaboradoresPorStatusCertificacao(StatusCertificacao.VENCIDA);
        List<Integer> proximos = consultaService.listarColaboradoresPorStatusCertificacao(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
        List<Integer> emDia = consultaService.listarColaboradoresPorStatusCertificacao(StatusCertificacao.EM_DIA);

        assertThat(vencidos).contains(colab1Id);
        assertThat(proximos).contains(colab2Id);
        assertThat(emDia).contains(colab3Id);
    }

    @Test
    @DisplayName("Critério 5: Gravação de Exigências de Capacitação para Tarefa e EPI gera logs de auditoria (#121)")
    void definicaoDeExigenciasGeraPersistenciaEAuditoria() {
        ExigenciaCapacitacao exTarefa = cadastroService.definirExigenciaTarefa("NR-35", ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 10);
        ExigenciaCapacitacao exEpi = cadastroService.definirExigenciaEpi("Treinamento de Cinto", ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO, 20);

        assertThat(exTarefa.getId()).isNotNull();
        assertThat(exEpi.getId()).isNotNull();

        List<ExigenciaCapacitacao> exigenciasTarefa = repositoryPort.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.TAREFA, 10);
        assertThat(exigenciasTarefa).hasSize(1);
        assertThat(exigenciasTarefa.get(0).getNomeCapacitacao()).isEqualTo("NR-35");

        List<ExigenciaCapacitacao> exigenciasEpi = repositoryPort.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.EPI, 20);
        assertThat(exigenciasEpi).hasSize(1);
        assertThat(exigenciasEpi.get(0).getNomeCapacitacao()).isEqualTo("Treinamento de Cinto");

        // RNF05 (#193): logs_auditoria não aceita DELETE; conta só os registros destas exigências.
        int logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM logs_auditoria WHERE entidade = 'ExigenciaCapacitacao' AND entidade_id IN (?, ?)",
                Integer.class, exTarefa.getId(), exEpi.getId());
        assertThat(logs).isEqualTo(2);
    }
}

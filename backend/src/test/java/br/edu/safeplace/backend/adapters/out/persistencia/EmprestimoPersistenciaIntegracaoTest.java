/*
 * Teste de integração com PostgreSQL do fluxo transacional e concorrência de empréstimo (issue #107):
 * concorrência de entregas e devoluções, rollback transacional, preservação de dados e validação
 * de constraints/FKs pelo PostgreSQL. Ignorado sem banco, como os demais testes que usam PostgresDeTeste.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarDevolucaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarEntregaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase.Situacao;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Empréstimo persistido: transacionalidade, concorrência e constraints (issue #107)")
class EmprestimoPersistenciaIntegracaoTest {

    private static final String CPF_SUPERVISOR = "52998224725";
    private static final String CPF_COLABORADOR = "12345678909";
    private static final String CPF_COLABORADOR_2 = "98765432100";
    private static final String EMAIL_SUPERVISOR = "supervisor.emprestimo@safeplace.test";

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private ControlarEmprestimoUseCase emprestimos;

    @Autowired
    private GerenciarEpiUseCase epis;

    @Autowired
    private UsuarioRepositorioPorta usuarios;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer colaboradorId;
    private Integer colaborador2Id;
    private Integer capaceteId;

    @BeforeEach
    void prepararBanco() {
        limparRegistrosDoTeste();

        usuarios.salvar(Supervisor.novo("Bruno Empréstimo", CPF_SUPERVISOR, LocalDate.of(1985, 3, 10), EMAIL_SUPERVISOR, "hash"));
        colaboradorId = usuarios.salvar(Colaborador.novo("Carlos Empréstimo", CPF_COLABORADOR, LocalDate.of(1992, 6, 18), null)).getId();
        colaborador2Id = usuarios.salvar(Colaborador.novo("Maria Empréstimo", CPF_COLABORADOR_2, LocalDate.of(1995, 8, 20), null)).getId();
        capaceteId = epis.cadastrarEpi(new CadastrarEpiInputDTO("Capacete", "12345", 3, 1,
                LocalDate.now().plusYears(1), 365, "Capacete classe B", ClassificacaoEPI.PROTECAO_DE_CABECA)).id();
    }

    /**
     * emprestimos referencia epis e usuarios, e os demais testes de integração limpam essas tabelas
     * ou contam pessoas por nome sem conhecer este teste. Por isso ele remove tudo o que criou
     * (empréstimos, capacitações, movimentações, EPIs e as pessoas) antes e depois de cada caso.
     */
    @AfterEach
    void limparRegistrosDoTeste() {
        jdbc.execute("DELETE FROM emprestimos");
        jdbc.execute("DELETE FROM projecoes_substituicao");
        jdbc.execute("DELETE FROM exigencias_capacitacao");
        jdbc.execute("DELETE FROM certificacoes");
        jdbc.execute("DELETE FROM treinamentos_colaboradores");
        jdbc.execute("DELETE FROM treinamentos");
        jdbc.execute("DELETE FROM movimentacoes_estoque WHERE epi_id IN (SELECT e.id FROM epis e JOIN lotes_epi l ON l.id = e.lote_id JOIN modelos_epi m ON m.id = l.modelo_id WHERE m.ca IN (12345, 54321, 99999, 88888))");
        jdbc.execute("DELETE FROM areas_risco_epis WHERE epi_id IN (SELECT e.id FROM epis e JOIN lotes_epi l ON l.id = e.lote_id JOIN modelos_epi m ON m.id = l.modelo_id WHERE m.ca IN (12345, 54321, 99999, 88888))");
        jdbc.execute("DELETE FROM epis WHERE id IN (SELECT e.id FROM epis e JOIN lotes_epi l ON l.id = e.lote_id JOIN modelos_epi m ON m.id = l.modelo_id WHERE m.ca IN (12345, 54321, 99999, 88888))");
        // Decisão #124: o CA é reaproveitado entre cadastros; modelos órfãos não podem vazar para o próximo teste.
        jdbc.execute("DELETE FROM lotes_epi l WHERE NOT EXISTS (SELECT 1 FROM epis e WHERE e.lote_id = l.id) "
                + "AND NOT EXISTS (SELECT 1 FROM movimentacoes_estoque m WHERE m.lote_id = l.id)");
        jdbc.execute("DELETE FROM modelos_epi o WHERE NOT EXISTS (SELECT 1 FROM lotes_epi l WHERE l.modelo_id = o.id)");
        jdbc.update("DELETE FROM usuarios WHERE cpf IN (?, ?, ?)", CPF_SUPERVISOR, CPF_COLABORADOR, CPF_COLABORADOR_2);
    }

    private RegistrarEntregaInputDTO entrega() {
        return new RegistrarEntregaInputDTO(capaceteId, colaboradorId, LocalDate.now().plusDays(7), false, null);
    }

    @Test
    @DisplayName("Entrega grava emprestimos, debita o saldo e registra SAIDA com operador; devolução APTO credita")
    void entregaEDevolucaoAptaPersistidas() {
        EmprestimoOutputDTO entregue = emprestimos.registrarEntrega(entrega(), EMAIL_SUPERVISOR);

        assertThat(entregue.id()).isNotNull();
        assertThat(saldo()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT status FROM emprestimos WHERE id = ?", String.class, entregue.id()))
                .isEqualTo("INDISPONIVEL");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM movimentacoes_estoque WHERE epi_id = ? AND tipo = 'SAIDA' AND responsavel_id IS NOT NULL",
                Integer.class, capaceteId)).isEqualTo(1);
        assertThat(emprestimos.listar(Situacao.PENDENTES, colaboradorId)).singleElement()
                .satisfies(pendente -> assertThat(pendente.colaboradorNome()).isEqualTo("Carlos Empréstimo"));

        EmprestimoOutputDTO devolvido = emprestimos.registrarDevolucao(entregue.id(),
                new RegistrarDevolucaoInputDTO(EstadoDevolucao.APTO), EMAIL_SUPERVISOR);

        assertThat(devolvido.status()).isEqualTo(ClassificacaoEmprestimo.DISPONIVEL);
        assertThat(saldo()).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT estado_devolucao FROM emprestimos WHERE id = ?", String.class, entregue.id()))
                .isEqualTo("APTO");
        assertThat(epis.buscarHistorico(capaceteId)).hasSize(2);
        assertThat(emprestimos.listar(Situacao.PENDENTES, colaboradorId)).isEmpty();
    }

    @Test
    @DisplayName("Devolução para MANUTENCAO encerra o empréstimo sem creditar o saldo disponível")
    void devolucaoParaManutencaoNaoCredita() {
        EmprestimoOutputDTO entregue = emprestimos.registrarEntrega(entrega(), EMAIL_SUPERVISOR);

        emprestimos.registrarDevolucao(entregue.id(), new RegistrarDevolucaoInputDTO(EstadoDevolucao.MANUTENCAO), EMAIL_SUPERVISOR);

        assertThat(saldo()).isEqualTo(2);
        assertThat(epis.buscarHistorico(capaceteId)).hasSize(1);
        assertThat(emprestimos.buscarPorId(entregue.id()).estadoDevolucao()).isEqualTo(EstadoDevolucao.MANUTENCAO);
    }

    @Test
    @DisplayName("Certificação exigida pelo EPI vencida impede a entrega; renovada, libera")
    void certificacaoVencidaImpedeEntrega() {
        jdbc.update("INSERT INTO exigencias_capacitacao (nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo) VALUES (?, 'CERTIFICACAO', ?, 'EPI')",
                "NR-35 Trabalho em altura", capaceteId);
        jdbc.update("INSERT INTO certificacoes (nome, data_vencimento, data_emissao, colaborador_id) VALUES (?, ?, ?, ?)",
                "NR-35 Trabalho em altura", LocalDate.now().minusDays(1), LocalDate.now().minusYears(2), colaboradorId);

        assertThatThrownBy(() -> emprestimos.registrarEntrega(entrega(), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("NR-35");
        assertThat(saldo()).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM emprestimos", Integer.class)).isZero();

        jdbc.update("INSERT INTO certificacoes (nome, data_vencimento, data_emissao, colaborador_id) VALUES (?, ?, ?, ?)",
                "NR-35 Trabalho em altura", LocalDate.now().plusYears(1), LocalDate.now(), colaboradorId);

        assertThat(emprestimos.registrarEntrega(entrega(), EMAIL_SUPERVISOR).status())
                .isEqualTo(ClassificacaoEmprestimo.INDISPONIVEL);
        assertThat(saldo()).isEqualTo(2);
    }

    @Test
    @DisplayName("Treinamento obrigatório exigido pelo EPI pendente impede a entrega e NÃO movimenta estoque (UC12)")
    void treinamentoPendenteImpedeEntregaENaoMovimentaEstoque() {
        jdbc.update("INSERT INTO exigencias_capacitacao (nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo) VALUES (?, 'TREINAMENTO', ?, 'EPI')",
                "Treinamento de Proteção Respiratória", capaceteId);

        assertThatThrownBy(() -> emprestimos.registrarEntrega(entrega(), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("Proteção Respiratória");

        assertThat(saldo()).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM emprestimos", Integer.class)).isZero();

        // Concluindo o treinamento, a entrega é liberada e debita 1 do estoque
        Integer treinoId = jdbc.queryForObject(
                "INSERT INTO treinamentos (nome, conteudo, instrutor, data_realizacao, carga_horaria) VALUES (?, ?, ?, ?, ?) RETURNING id",
                Integer.class, "Treinamento de Proteção Respiratória", "Uso correto", "Instrutor", LocalDate.now().minusDays(1), 8);
        jdbc.update("INSERT INTO treinamentos_colaboradores (treinamento_id, colaborador_id) VALUES (?, ?)", treinoId, colaboradorId);

        assertThat(emprestimos.registrarEntrega(entrega(), EMAIL_SUPERVISOR).status())
                .isEqualTo(ClassificacaoEmprestimo.INDISPONIVEL);
        assertThat(saldo()).isEqualTo(2);
    }

    @Test
    @DisplayName("Duas entregas concorrentes não consomem a mesma disponibilidade: apenas uma confirma e saldo final é zero")
    void duasEntregasConcorrentesNaoConsomemMesmaDisponibilidade() throws Exception {
        Integer epiUnicoId = epis.cadastrarEpi(new CadastrarEpiInputDTO("Luva de Raspa", "54321", 1, 1,
                LocalDate.now().plusYears(1), 180, "Luva proteção", ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES)).id();

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhasImpedidas = new AtomicInteger(0);
        List<Throwable> outrasFalhas = Collections.synchronizedList(new ArrayList<>());

        Callable<Void> tarefa1 = () -> {
            latch.await();
            try {
                emprestimos.registrarEntrega(
                        new RegistrarEntregaInputDTO(epiUnicoId, colaboradorId, LocalDate.now().plusDays(5), false, null),
                        EMAIL_SUPERVISOR);
                sucessos.incrementAndGet();
            } catch (EntregaImpedidaException e) {
                falhasImpedidas.incrementAndGet();
            } catch (Throwable t) {
                outrasFalhas.add(t);
            }
            return null;
        };

        Callable<Void> tarefa2 = () -> {
            latch.await();
            try {
                emprestimos.registrarEntrega(
                        new RegistrarEntregaInputDTO(epiUnicoId, colaborador2Id, LocalDate.now().plusDays(5), false, null),
                        EMAIL_SUPERVISOR);
                sucessos.incrementAndGet();
            } catch (EntregaImpedidaException e) {
                falhasImpedidas.incrementAndGet();
            } catch (Throwable t) {
                outrasFalhas.add(t);
            }
            return null;
        };

        Future<Void> f1 = executor.submit(tarefa1);
        Future<Void> f2 = executor.submit(tarefa2);

        latch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(outrasFalhas).isEmpty();
        assertThat(sucessos.get()).isEqualTo(1);
        assertThat(falhasImpedidas.get()).isEqualTo(1);

        int saldoFinal = jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, epiUnicoId);
        assertThat(saldoFinal).isZero();

        int totalEmprestimos = jdbc.queryForObject("SELECT COUNT(*) FROM emprestimos WHERE epi_id = ?",
                Integer.class, epiUnicoId);
        assertThat(totalEmprestimos).isEqualTo(1);

        int totalMovimentacoes = jdbc.queryForObject("SELECT COUNT(*) FROM movimentacoes_estoque WHERE epi_id = ?",
                Integer.class, epiUnicoId);
        assertThat(totalMovimentacoes).isEqualTo(1);
    }

    @Test
    @DisplayName("Devolução repetida concorrente não duplica crédito de estoque: apenas uma confirma e saldo final é consistente")
    void devolucaoRepetidaConcorrenteNaoDuplicaCredito() throws Exception {
        Integer epiUnicoId = epis.cadastrarEpi(new CadastrarEpiInputDTO("Óculos de Proteção", "99999", 1, 1,
                LocalDate.now().plusYears(1), 180, "Óculos", ClassificacaoEPI.PROTECAO_DE_OLHOS)).id();

        EmprestimoOutputDTO entregue = emprestimos.registrarEntrega(
                new RegistrarEntregaInputDTO(epiUnicoId, colaboradorId, LocalDate.now().plusDays(5), false, null),
                EMAIL_SUPERVISOR);

        int saldoAposEntrega = jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, epiUnicoId);
        assertThat(saldoAposEntrega).isZero();

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhasJaEncerrado = new AtomicInteger(0);
        List<Throwable> outrasFalhas = Collections.synchronizedList(new ArrayList<>());

        Callable<Void> tarefaDevolucao = () -> {
            latch.await();
            try {
                emprestimos.registrarDevolucao(entregue.id(),
                        new RegistrarDevolucaoInputDTO(EstadoDevolucao.APTO), EMAIL_SUPERVISOR);
                sucessos.incrementAndGet();
            } catch (EmprestimoJaEncerradoException e) {
                falhasJaEncerrado.incrementAndGet();
            } catch (Throwable t) {
                outrasFalhas.add(t);
            }
            return null;
        };

        Future<Void> f1 = executor.submit(tarefaDevolucao);
        Future<Void> f2 = executor.submit(tarefaDevolucao);

        latch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(outrasFalhas).isEmpty();
        assertThat(sucessos.get()).isEqualTo(1);
        assertThat(falhasJaEncerrado.get()).isEqualTo(1);

        int saldoFinal = jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, epiUnicoId);
        assertThat(saldoFinal).isEqualTo(1);

        int entradas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM movimentacoes_estoque WHERE epi_id = ? AND tipo = 'ENTRADA'",
                Integer.class, epiUnicoId);
        assertThat(entradas).isEqualTo(1);

        String statusFinal = jdbc.queryForObject("SELECT status FROM emprestimos WHERE id = ?", String.class, entregue.id());
        assertThat(statusFinal).isEqualTo("DISPONIVEL");
    }

    @Test
    @DisplayName("Falha desfaz empréstimo, movimento e saldo juntos (atomicidade transacional)")
    void falhaDesfazEmprestimoMovimentoESaldoJuntos() {
        int saldoInicial = saldo();

        assertThatThrownBy(() -> emprestimos.registrarEntrega(
                new RegistrarEntregaInputDTO(capaceteId, 999999, LocalDate.now().plusDays(7), false, null),
                EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class);

        assertThat(saldo()).isEqualTo(saldoInicial);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM emprestimos", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimentacoes_estoque WHERE epi_id = ?",
                Integer.class, capaceteId)).isZero();
    }

    @Test
    @DisplayName("PostgreSQL valida constraints relacionais (FKs) e de consistência da tabela emprestimos")
    void postgresValidaChavesEstrangeirasEReconstrucao() {
        assertThatThrownBy(() -> jdbc.execute(
                "INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista, status, termo_cautela) " +
                "VALUES (999999, " + colaboradorId + ", (SELECT id FROM usuarios WHERE email = '" + EMAIL_SUPERVISOR + "'), NOW(), CURRENT_DATE + 7, 'INDISPONIVEL', 'Termo')"))
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbc.execute(
                "INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista, status, termo_cautela) " +
                "VALUES (" + capaceteId + ", 999999, (SELECT id FROM usuarios WHERE email = '" + EMAIL_SUPERVISOR + "'), NOW(), CURRENT_DATE + 7, 'INDISPONIVEL', 'Termo')"))
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbc.execute(
                "INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista, status, termo_cautela) " +
                "VALUES (" + capaceteId + ", " + colaboradorId + ", 999999, NOW(), CURRENT_DATE + 7, 'INDISPONIVEL', 'Termo')"))
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbc.execute(
                "INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista, status, termo_cautela) " +
                "VALUES (" + capaceteId + ", " + colaboradorId + ", (SELECT id FROM usuarios WHERE email = '" + EMAIL_SUPERVISOR + "'), NOW(), CURRENT_DATE + 7, 'INVALIDO', 'Termo')"))
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbc.execute(
                "INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista, status, termo_cautela) " +
                "VALUES (" + capaceteId + ", " + colaboradorId + ", (SELECT id FROM usuarios WHERE email = '" + EMAIL_SUPERVISOR + "'), NOW(), CURRENT_DATE + 7, 'DISPONIVEL', 'Termo')"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("Empréstimo conserva equipamento, beneficiário, operadores e datas ao consultar novamente")
    void emprestimoConservaEquipamentoBeneficiarioOperadorEDatas() {
        EmprestimoOutputDTO entregue = emprestimos.registrarEntrega(entrega(), EMAIL_SUPERVISOR);
        emprestimos.registrarDevolucao(entregue.id(),
                new RegistrarDevolucaoInputDTO(EstadoDevolucao.APTO), EMAIL_SUPERVISOR);

        EmprestimoOutputDTO consultado = emprestimos.buscarPorId(entregue.id());

        assertThat(consultado.epiId()).isEqualTo(capaceteId);
        assertThat(consultado.epiNome()).isEqualTo("Capacete");
        assertThat(consultado.colaboradorId()).isEqualTo(colaboradorId);
        assertThat(consultado.colaboradorNome()).isEqualTo("Carlos Empréstimo");
        assertThat(consultado.responsavelEntregaId()).isNotNull();
        assertThat(consultado.responsavelDevolucaoId()).isNotNull();
        assertThat(consultado.dataRetirada()).isNotNull();
        assertThat(consultado.dataDevolucaoPrevista()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(consultado.dataDevolucaoReal()).isNotNull();
        assertThat(consultado.estadoDevolucao()).isEqualTo(EstadoDevolucao.APTO);
        assertThat(consultado.status()).isEqualTo(ClassificacaoEmprestimo.DISPONIVEL);
        assertThat(consultado.termoCautela()).contains("Termo de cautela", "Carlos Empréstimo", "Capacete");
    }

    @Test
    @DisplayName("Itens devolvidos para manutenção não aparecem automaticamente disponíveis")
    void itensDevolvidosParaManutencaoNaoAparecemAutomaticamenteDisponiveis() {
        Integer epiUnicoId = epis.cadastrarEpi(new CadastrarEpiInputDTO("Abafador", "88888", 1, 1,
                LocalDate.now().plusYears(1), 180, "Abafador auricular", ClassificacaoEPI.PROTECAO_AUDITIVA)).id();

        EmprestimoOutputDTO entregue = emprestimos.registrarEntrega(
                new RegistrarEntregaInputDTO(epiUnicoId, colaboradorId, LocalDate.now().plusDays(5), false, null),
                EMAIL_SUPERVISOR);

        emprestimos.registrarDevolucao(entregue.id(),
                new RegistrarDevolucaoInputDTO(EstadoDevolucao.MANUTENCAO), EMAIL_SUPERVISOR);

        int saldoAposManutencao = jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, epiUnicoId);
        assertThat(saldoAposManutencao).isZero();

        List<EpiOutputDTO> disponiveis = emprestimos.listarEpisDisponiveisParaEmprestimo();
        assertThat(disponiveis).extracting(EpiOutputDTO::id).doesNotContain(epiUnicoId);
    }

    private Integer saldo() {
        return jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, capaceteId);
    }
}

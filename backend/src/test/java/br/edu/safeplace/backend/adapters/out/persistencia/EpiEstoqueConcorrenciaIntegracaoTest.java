package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.usecase.EpiUseCase;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Testes de Integração de Concorrência e Transacionalidade do Estoque de EPIs")
class EpiEstoqueConcorrenciaIntegracaoTest {

    /** Gestor semeado pela V17; US03 exige o responsável em toda movimentação. */
    private static final String GESTOR_DEMO = "gestor.demo@safeplace.com";

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private EpiUseCase epiUseCase;

    @Autowired
    private EpiRepositoryPort epiRepositoryPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limparTabelas() {
        jdbcTemplate.execute("DELETE FROM movimentacoes_estoque");
        jdbcTemplate.execute("DELETE FROM epis");
        // Decisão #124: o CA é reaproveitado entre cadastros; modelos órfãos não podem vazar para o próximo teste.
        jdbcTemplate.execute("DELETE FROM lotes_epi l WHERE NOT EXISTS (SELECT 1 FROM epis e WHERE e.lote_id = l.id) "
                + "AND NOT EXISTS (SELECT 1 FROM movimentacoes_estoque m WHERE m.lote_id = l.id)");
        jdbcTemplate.execute("DELETE FROM modelos_epi o WHERE NOT EXISTS (SELECT 1 FROM lotes_epi l WHERE l.modelo_id = o.id)");
    }

    private Integer criarEpi(String nome, String numeroCa, int quantidadeInicial) {
        CadastrarEpiInputDTO dto = new CadastrarEpiInputDTO(
                nome,
                numeroCa,
                quantidadeInicial,
                5,
                LocalDate.of(2028, 12, 31),
                365,
                "Descrição de teste",
                ClassificacaoEPI.PROTECAO_DE_CABECA
        );
        EpiOutputDTO output = epiUseCase.cadastrarEpi(dto);
        return output.id();
    }

    @Test
    @DisplayName("Disputa de saldo insuficiente sob concorrência: apenas uma saída é confirmada e saldo final é consistente")
    void disputaDeSaldoInsuficienteSobConcorrencia() throws Exception {
        Integer epiId = criarEpi("Capacete de Segurança", "CA-11111", 10);

        int numeroThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhasSaldoInsuficiente = new AtomicInteger(0);
        List<Throwable> outrasFalhas = Collections.synchronizedList(new ArrayList<>());

        Callable<Void> tarefa = () -> {
            latch.await();
            try {
                epiUseCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, 8, "Saída concorrente", GESTOR_DEMO);
                sucessos.incrementAndGet();
            } catch (SaldoInsuficienteException e) {
                falhasSaldoInsuficiente.incrementAndGet();
            } catch (Throwable t) {
                outrasFalhas.add(t);
            }
            return null;
        };

        Future<Void> f1 = executor.submit(tarefa);
        Future<Void> f2 = executor.submit(tarefa);

        latch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(outrasFalhas).isEmpty();
        assertThat(sucessos.get()).isEqualTo(1);
        assertThat(falhasSaldoInsuficiente.get()).isEqualTo(1);

        Epi epiAtualizado = epiRepositoryPort.buscarPorId(epiId).orElseThrow();
        assertThat(epiAtualizado.getQuantidade()).isEqualTo(2);

        var movimentacoes = epiRepositoryPort.listarMovimentacoesPorEpi(epiId);
        assertThat(movimentacoes).hasSize(1);
        assertThat(movimentacoes.get(0).getQuantidade()).isEqualTo(8);
        assertThat(movimentacoes.get(0).getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
    }

    @Test
    @DisplayName("Múltiplas entradas e saídas concorrentes: saldo final e histórico correspondem exatamente às operações")
    void multiplasOperacoesConcorrentesComSaldoSuficiente() throws Exception {
        int saldoInicial = 100;
        Integer epiId = criarEpi("Luva de Proteção", "CA-22222", saldoInicial);

        int totalEntradas = 10;
        int qtdEntrada = 10;
        int totalSaidas = 10;
        int qtdSaida = 5;
        int totalOperacoes = totalEntradas + totalSaidas;

        ExecutorService executor = Executors.newFixedThreadPool(totalOperacoes);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger sucessos = new AtomicInteger(0);
        List<Throwable> falhas = Collections.synchronizedList(new ArrayList<>());

        List<Callable<Void>> tarefas = new ArrayList<>();

        for (int i = 0; i < totalEntradas; i++) {
            tarefas.add(() -> {
                latch.await();
                try {
                    epiUseCase.registrarMovimentacao(epiId, TipoMovimentacao.ENTRADA, qtdEntrada, "Reposição concorrente", GESTOR_DEMO);
                    sucessos.incrementAndGet();
                } catch (Throwable t) {
                    falhas.add(t);
                }
                return null;
            });
        }

        for (int i = 0; i < totalSaidas; i++) {
            tarefas.add(() -> {
                latch.await();
                try {
                    epiUseCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, qtdSaida, "Retirada concorrente", GESTOR_DEMO);
                    sucessos.incrementAndGet();
                } catch (Throwable t) {
                    falhas.add(t);
                }
                return null;
            });
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> tarefa : tarefas) {
            futures.add(executor.submit(tarefa));
        }

        latch.countDown();

        for (Future<Void> future : futures) {
            future.get(15, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(falhas).isEmpty();
        assertThat(sucessos.get()).isEqualTo(totalOperacoes);

        int saldoEsperado = saldoInicial + (totalEntradas * qtdEntrada) - (totalSaidas * qtdSaida);
        Epi epiAtualizado = epiRepositoryPort.buscarPorId(epiId).orElseThrow();
        assertThat(epiAtualizado.getQuantidade()).isEqualTo(saldoEsperado);

        var movimentacoes = epiRepositoryPort.listarMovimentacoesPorEpi(epiId);
        assertThat(movimentacoes).hasSize(totalOperacoes);
    }

    @Test
    @DisplayName("Busca por CA suporta múltiplos EPIs cadastrados com o mesmo CA sem estourar exceção")
    void buscaPorCaSuportaMultiplosEpisComMesmoCa() {
        String caCompartilhado = "CA-998877";
        criarEpi("Óculos de Proteção Transparente", caCompartilhado, 25);
        criarEpi("Óculos de Proteção Fumê", caCompartilhado, 30);

        List<Epi> episEncontrados = epiRepositoryPort.listarPorCA(caCompartilhado);
        assertThat(episEncontrados).hasSize(2);

        Optional<Epi> primeiroEpi = epiRepositoryPort.buscarPorCA(caCompartilhado);
        assertThat(primeiroEpi).isPresent();
        assertThat(primeiroEpi.get().getNumeroCa()).isIn("998877", caCompartilhado);
    }

    @Test
    @DisplayName("Atomicidade transacional: tentativa de saída com saldo insuficiente não grava movimentação")
    void tentativaDeSaidaInvalidaNaoGravaMovimentacao() {
        Integer epiId = criarEpi("Protetor Auricular", "CA-33333", 5);

        try {
            epiUseCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, 10, "Tentativa excessiva", GESTOR_DEMO);
        } catch (SaldoInsuficienteException ignored) {
        }

        Epi epi = epiRepositoryPort.buscarPorId(epiId).orElseThrow();
        assertThat(epi.getQuantidade()).isEqualTo(5);

        var movimentacoes = epiRepositoryPort.listarMovimentacoesPorEpi(epiId);
        assertThat(movimentacoes).isEmpty();
    }
}

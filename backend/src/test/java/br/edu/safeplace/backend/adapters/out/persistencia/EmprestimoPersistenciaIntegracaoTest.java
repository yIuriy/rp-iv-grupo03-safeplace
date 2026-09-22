/*
 * Teste de integração com PostgreSQL do fluxo completo de empréstimo (issue #106): entrega debita
 * o saldo persistido e grava movimentação com operador; devolução APTO credita; MANUTENCAO não
 * credita; exigência de capacitação vencida (tabelas da V15) impede a entrega; a migração V19 cria
 * a tabela emprestimos. Ignorado sem banco, como os demais testes que usam PostgresDeTeste.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarDevolucaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarEntregaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase.Situacao;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Empréstimo persistido: entrega, devolução e impedimento por capacitação (issue #106)")
class EmprestimoPersistenciaIntegracaoTest {

    private static final String CPF_SUPERVISOR = "52998224725";
    private static final String CPF_COLABORADOR = "12345678909";
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
    private Integer capaceteId;

    @BeforeEach
    void prepararBanco() {
        limparRegistrosDoTeste();

        usuarios.salvar(Supervisor.novo("Bruno Empréstimo", CPF_SUPERVISOR, LocalDate.of(1985, 3, 10), EMAIL_SUPERVISOR, "hash"));
        colaboradorId = usuarios.salvar(Colaborador.novo("Carlos Empréstimo", CPF_COLABORADOR, LocalDate.of(1992, 6, 18), null)).getId();
        capaceteId = epis.cadastrarEpi(new CadastrarEpiInputDTO("Capacete", "12345", 3, 1,
                LocalDate.now().plusYears(1), 365, "Capacete classe B", ClassificacaoEPI.PROTECAO_DE_CABECA)).id();
    }

    /**
     * emprestimos referencia epis e usuarios, e os demais testes de integração limpam essas tabelas
     * ou contam pessoas por nome sem conhecer este teste. Por isso ele remove tudo o que criou
     * (empréstimos, capacitações, movimentações, EPIs e as duas pessoas) antes e depois de cada caso.
     */
    @AfterEach
    void limparRegistrosDoTeste() {
        jdbc.execute("DELETE FROM emprestimos");
        jdbc.execute("DELETE FROM exigencias_capacitacao");
        jdbc.execute("DELETE FROM certificacoes");
        jdbc.execute("DELETE FROM movimentacoes_estoque");
        jdbc.execute("DELETE FROM epis");
        jdbc.update("DELETE FROM usuarios WHERE cpf IN (?, ?)", CPF_SUPERVISOR, CPF_COLABORADOR);
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

    private Integer saldo() {
        return jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, capaceteId);
    }
}

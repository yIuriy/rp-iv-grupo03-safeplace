/*
 * Teste de integração com PostgreSQL para a issue #123: o valor exibido na consulta corresponde ao
 * saldo persistido, e a situação crítica (saldo <= quantidade mínima da especificação) muda ao
 * cruzar o mínimo por saída e por entrada. Ignorado quando não há banco disponível, como os
 * demais testes que usam PostgresDeTeste.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

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
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Estoque crítico persistido: cruzamento do mínimo com saldo 10 (issue #123)")
class EstoqueCriticoPersistenciaIntegracaoTest {

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private GerenciarEpiUseCase useCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer epiId;

    @BeforeEach
    void prepararEpiComSaldoDez() {
        jdbcTemplate.execute("DELETE FROM movimentacoes_estoque");
        jdbcTemplate.execute("DELETE FROM epis");
        EpiOutputDTO salvo = useCase.cadastrarEpi(new CadastrarEpiInputDTO(
                "Luva nitrílica", "12345", 10, 5, LocalDate.now().plusYears(1), 180,
                "Luva de proteção química", ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES));
        epiId = salvo.id();
    }

    @Test
    @DisplayName("Saída de 5 deixa saldo 5 no banco e a consulta passa a crítica; entrada de 1 encerra")
    void cruzarOMinimoPorSaidaEEntrada() {
        assertThat(useCase.buscarPorId(epiId).estoqueCritico()).isFalse();

        useCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, 5, "Entrega semanal", null);

        assertThat(saldoNoBanco()).isEqualTo(5);
        EpiOutputDTO critico = useCase.buscarPorId(epiId);
        assertThat(critico.quantidade()).isEqualTo(5);
        assertThat(critico.estoqueCritico()).isTrue();

        useCase.registrarMovimentacao(epiId, TipoMovimentacao.ENTRADA, 1, "Reposição", null);

        assertThat(saldoNoBanco()).isEqualTo(6);
        assertThat(useCase.buscarPorId(epiId).estoqueCritico()).isFalse();
        assertThat(useCase.buscarHistorico(epiId)).hasSize(2);
    }

    @Test
    @DisplayName("Saída de 11 sobre saldo 10 falha, mantém as 10 unidades e não grava histórico")
    void saidaAcimaDoSaldoNaoAlteraBanco() {
        assertThatThrownBy(() -> useCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, 11, "Indevida", null))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(saldoNoBanco()).isEqualTo(10);
        assertThat(useCase.buscarPorId(epiId).estoqueCritico()).isFalse();
        assertThat(useCase.buscarHistorico(epiId)).isEmpty();
    }

    private Integer saldoNoBanco() {
        return jdbcTemplate.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, epiId);
    }
}

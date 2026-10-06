/*
 * Teste de integração com PostgreSQL para a issue #197 (RF04, UC01, decisão #124): a manutenção
 * concluída é gravada em manutencoes_epi com o responsável autenticado, o histórico é relido do banco
 * e a situação do EPI segue o resultado. Ignorado quando não há banco disponível, como os demais
 * testes que usam PostgresDeTeste.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarManutencaoEpiUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Manutenção de EPI persistida no PostgreSQL (issue #197)")
class ManutencaoEpiPersistenciaIntegracaoTest {

    /** Gestor semeado pela V17. */
    private static final String GESTOR_DEMO = "gestor.demo@safeplace.com";
    private static final int CA_TESTE = 919191;

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private GerenciarEpiUseCase epis;

    @Autowired
    private ControlarManutencaoEpiUseCase manutencoes;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer epiId;

    @BeforeEach
    void cadastrarEpi() {
        limparRegistrosDoTeste();
        epiId = epis.cadastrarEpi(new CadastrarEpiInputDTO(
                "Cinto paraquedista", Integer.toString(CA_TESTE), 4, 1, LocalDate.now().plusYears(1), 730,
                "Cinto para trabalho em altura", ClassificacaoEPI.PROTECAO_CONTRA_QUEDAS)).id();
    }

    @AfterEach
    void limparRegistrosDoTeste() {
        String episDoTeste = "SELECT e.id FROM epis e JOIN lotes_epi l ON l.id = e.lote_id "
                + "JOIN modelos_epi m ON m.id = l.modelo_id WHERE m.ca = " + CA_TESTE;
        jdbc.execute("DELETE FROM manutencoes_epi WHERE epi_id IN (" + episDoTeste + ")");
        jdbc.execute("DELETE FROM movimentacoes_estoque WHERE epi_id IN (" + episDoTeste + ")");
        jdbc.execute("DELETE FROM epis WHERE id IN (" + episDoTeste + ")");
        jdbc.execute("DELETE FROM lotes_epi l WHERE l.modelo_id IN (SELECT id FROM modelos_epi WHERE ca = " + CA_TESTE
                + ") AND NOT EXISTS (SELECT 1 FROM epis e WHERE e.lote_id = l.id)");
        jdbc.execute("DELETE FROM modelos_epi o WHERE o.ca = " + CA_TESTE
                + " AND NOT EXISTS (SELECT 1 FROM lotes_epi l WHERE l.modelo_id = o.id)");
    }

    @Test
    @DisplayName("Manutenção aprovada fica no banco com o responsável e devolve o EPI à disponibilidade")
    void manutencaoAprovadaPersisteHistoricoEResponsavel() {
        manutencoes.concluirManutencao(manutencao(ResultadoManutencao.APROVADO), GESTOR_DEMO);

        List<ManutencaoEpiOutputDTO> historico = manutencoes.listarHistoricoPorEpi(epiId);
        assertThat(historico).singleElement().satisfies(registro -> {
            assertThat(registro.id()).isNotNull();
            assertThat(registro.resultado()).isEqualTo(ResultadoManutencao.APROVADO);
            assertThat(registro.responsavelId()).isEqualTo(idDoGestorDemo());
            assertThat(registro.statusAtualEpi()).isEqualTo(StatusEpi.DISPONIVEL);
        });
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM manutencoes_epi WHERE epi_id = ? AND responsavel_id = ?",
                Integer.class, epiId, idDoGestorDemo())).isEqualTo(1);
        assertThat(statusNoBanco()).isEqualTo(StatusEpi.DISPONIVEL.name());
    }

    @Test
    @DisplayName("Manutenção reprovada fica no banco e mantém o EPI indisponível, sem descarte")
    void manutencaoReprovadaMantemEpiEmManutencao() {
        manutencoes.concluirManutencao(manutencao(ResultadoManutencao.REPROVADO), GESTOR_DEMO);

        assertThat(manutencoes.listarHistoricoPorEpi(epiId)).singleElement()
                .satisfies(registro -> assertThat(registro.resultado()).isEqualTo(ResultadoManutencao.REPROVADO));
        assertThat(statusNoBanco()).isEqualTo(StatusEpi.EM_MANUTENCAO.name());
        assertThat(jdbc.queryForObject("SELECT quantidade FROM epis WHERE id = ?", Integer.class, epiId))
                .isEqualTo(4);
    }

    private ConcluirManutencaoInputDTO manutencao(ResultadoManutencao resultado) {
        return new ConcluirManutencaoInputDTO(epiId, LocalDateTime.now(), TipoManutencao.PREVENTIVA,
                "Inspeção de costuras e fivelas", resultado, null);
    }

    private Integer idDoGestorDemo() {
        return jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", Integer.class, GESTOR_DEMO);
    }

    private String statusNoBanco() {
        return jdbc.queryForObject("SELECT status FROM epis WHERE id = ?", String.class, epiId);
    }
}

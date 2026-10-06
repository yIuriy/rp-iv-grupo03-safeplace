/*
 * Teste de integração com PostgreSQL para a issue #193 (RNF05): a V29 torna logs_auditoria somente
 * inclusão (UPDATE, DELETE e TRUNCATE recusados pelo banco) e troca o ON DELETE CASCADE do histórico
 * de capacitações e alocações por restrição. Ignorado quando não há banco disponível, como os demais
 * testes que usam PostgresDeTeste.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Trilha de auditoria imutável e histórico preservado no PostgreSQL (issue #193)")
class LogAuditoriaImutabilidadeIntegracaoTest {

    private static final String CPF_COLABORADOR = "70419311050";

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private LogAuditoriaRepositoryPort auditoria;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM certificacoes WHERE colaborador_id IN (SELECT id FROM usuarios WHERE cpf = ?)",
                CPF_COLABORADOR);
        jdbc.update("DELETE FROM usuarios WHERE cpf = ?", CPF_COLABORADOR);
    }

    @Test
    @DisplayName("UPDATE, DELETE e TRUNCATE em logs_auditoria são recusados e o registro permanece intacto")
    void trilhaDeAuditoriaSomenteInclusao() {
        LogAuditoria gravado = auditoria.salvar(LogAuditoria.registrarCadastro(
                "gestor.demo@safeplace.com", "Teste193", 193, "Registro original"));
        Integer id = gravado.getId();

        assertThatThrownBy(() -> jdbc.update(
                "UPDATE logs_auditoria SET justificativa = 'adulterado' WHERE id = ?", id))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("somente inclusão");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM logs_auditoria WHERE id = ?", id))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("somente inclusão");
        assertThatThrownBy(() -> jdbc.execute("TRUNCATE logs_auditoria"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("somente inclusão");

        assertThat(jdbc.queryForObject("SELECT justificativa FROM logs_auditoria WHERE id = ?", String.class, id))
                .isEqualTo("Registro original");
        assertThat(auditoria.listarPorEntidade("Teste193", 193))
                .extracting(LogAuditoria::getId)
                .contains(id);
    }

    @Test
    @DisplayName("Excluir pessoa com certificação é recusado e a certificação continua no banco")
    void exclusaoDePessoaNaoApagaCertificacaoEmCascata() {
        Integer colaboradorId = jdbc.queryForObject("""
                INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil, ativo)
                VALUES (?, 'Colaborador 193', ?, NULL, NULL, 'COLABORADOR', TRUE)
                RETURNING id
                """, Integer.class, CPF_COLABORADOR, Date.valueOf(LocalDate.of(1991, 4, 2)));
        jdbc.update("INSERT INTO certificacoes (nome, data_vencimento, data_emissao, colaborador_id) "
                + "VALUES ('NR-35', ?, ?, ?)", Date.valueOf(LocalDate.now().plusYears(1)),
                Date.valueOf(LocalDate.now().minusYears(1)), colaboradorId);

        assertThatThrownBy(() -> jdbc.update("DELETE FROM usuarios WHERE id = ?", colaboradorId))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM certificacoes WHERE colaborador_id = ?",
                Integer.class, colaboradorId)).isEqualTo(1);
    }
}

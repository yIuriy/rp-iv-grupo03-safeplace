package br.edu.safeplace.backend.domain.comum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de Unidade - LogAuditoria")
class LogAuditoriaTest {

    @Test
    @DisplayName("Deve criar log de auditoria com parâmetros válidos")
    void deveCriarLogAuditoriaValido() {
        LocalDateTime agora = LocalDateTime.now();
        LogAuditoria log = new LogAuditoria(
                1,
                "CADASTRO",
                agora,
                "gestor@safeplace.com",
                "Criação de usuário para o setor fabril",
                "Usuario",
                42
        );

        assertThat(log.getId()).isEqualTo(1);
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getDataHora()).isEqualTo(agora);
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getJustificativa()).isEqualTo("Criação de usuário para o setor fabril");
        assertThat(log.getEntidade()).isEqualTo("Usuario");
        assertThat(log.getEntidadeId()).isEqualTo(42);
    }

    @Test
    @DisplayName("Construtor monta o log sem validar; a recusa ficou no LogAuditoriaValidador (#297)")
    void construtorNaoValida() {
        LogAuditoria semTipoNemResponsavel = new LogAuditoria(null, null, LocalDateTime.now(), null, null, "Entidade", 1);
        LogAuditoria emBranco = new LogAuditoria(null, "   ", LocalDateTime.now(), "   ", null, "Entidade", 1);

        assertThat(semTipoNemResponsavel.getTipoOperacao()).isNull();
        assertThat(semTipoNemResponsavel.getResponsavel()).isNull();
        assertThat(emBranco.getTipoOperacao()).isEmpty();
        assertThat(emBranco.getResponsavel()).isEmpty();
    }

    @Test
    @DisplayName("Deve criar log de cadastro via método de fábrica")
    void deveCriarLogViaRegistrarCadastro() {
        LogAuditoria log = LogAuditoria.registrarCadastro(
                "supervisor@safeplace.com",
                "Epi",
                10,
                "Novo lote de capacetes"
        );

        assertThat(log.getId()).isNull();
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getResponsavel()).isEqualTo("supervisor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Epi");
        assertThat(log.getEntidadeId()).isEqualTo(10);
        assertThat(log.getJustificativa()).isEqualTo("Novo lote de capacetes");
        assertThat(log.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar log de alteração via método de fábrica")
    void deveCriarLogViaRegistrarAlteracao() {
        LogAuditoria log = LogAuditoria.registrarAlteracao(
                "gestor@safeplace.com",
                "AreaRisco",
                5,
                "Atualização de nível de risco"
        );

        assertThat(log.getId()).isNull();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("AreaRisco");
        assertThat(log.getEntidadeId()).isEqualTo(5);
        assertThat(log.getJustificativa()).isEqualTo("Atualização de nível de risco");
        assertThat(log.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar log de remoção via métodos de fábrica")
    void deveCriarLogViaRegistrarRemocao() {
        LocalDateTime data = LocalDateTime.of(2026, 9, 20, 10, 0);
        LogAuditoria log1 = LogAuditoria.registrarRemocao(15, "admin@safeplace.com", data, "Exclusão solicitada");

        assertThat(log1.getTipoOperacao()).isEqualTo("REMOCAO");
        assertThat(log1.getResponsavel()).isEqualTo("admin@safeplace.com");
        assertThat(log1.getEntidadeId()).isEqualTo(15);
        assertThat(log1.getDataHora()).isEqualTo(data);

        LogAuditoria log2 = LogAuditoria.registrarRemocao("admin@safeplace.com", "Tarefa", 20, "Remoção de tarefa");
        assertThat(log2.getTipoOperacao()).isEqualTo("REMOCAO");
        assertThat(log2.getResponsavel()).isEqualTo("admin@safeplace.com");
        assertThat(log2.getEntidade()).isEqualTo("Tarefa");
        assertThat(log2.getEntidadeId()).isEqualTo(20);
        assertThat(log2.getJustificativa()).isEqualTo("Remoção de tarefa");
    }
}

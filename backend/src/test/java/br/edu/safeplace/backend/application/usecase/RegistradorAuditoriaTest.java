package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

/** Facade da auditoria (#223): uma chamada monta o LogAuditoria, resolve o operador e grava pela porta. */
class RegistradorAuditoriaTest {

    private final TrilhaAuditoriaEmMemoria trilha = new TrilhaAuditoriaEmMemoria();
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.autenticado("gestor@safeplace.com");
    private final RegistradorAuditoria registrador =
            new RegistradorAuditoria(trilha, operador.operadorAtual(mock(UsuarioRepository.class)));

    @Test
    void cadastroGravaOTipoAEntidadeEOOperadorAutenticado() {
        registrador.cadastro("Acidente", 7, "Registro de Acidente de Trabalho");

        LogAuditoria log = unicoRegistro();
        assertThat(log.getTipoOperacao()).isEqualTo(LogAuditoria.CADASTRO);
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(7);
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getJustificativa()).isEqualTo("Registro de Acidente de Trabalho");
        assertThat(log.getDataHora()).isNotNull();
    }

    @Test
    void alteracaoERemocaoGravamOsTiposCorrespondentes() {
        registrador.alteracao("Tarefa", 1, "Atualização");
        registrador.remocao("Tarefa", 1, "Remoção");

        assertThat(trilha.registros()).extracting(LogAuditoria::getTipoOperacao)
                .containsExactly(LogAuditoria.ALTERACAO, LogAuditoria.REMOCAO);
    }

    @Test
    void semSessaoOResponsavelEhOSistema() {
        operador.encerrarSessao();

        registrador.alteracao("Incidente", 2, "Arquivamento de ocorrência");

        assertThat(unicoRegistro().getResponsavel()).isEqualTo(OperadorAtual.SISTEMA);
    }

    @Test
    void operadorInformadoPelaBordaTemPrecedencia() {
        registrador.por("  supervisor@safeplace.com ").cadastro("Colaborador", 3, "Cadastro de Colaborador");

        assertThat(unicoRegistro().getResponsavel()).isEqualTo("supervisor@safeplace.com");
    }

    @Test
    void operadorInformadoEmBrancoCaiNoAutenticado() {
        registrador.por(" ").alteracao("Tarefa", 4, "Atualização de Tarefa: Solda");

        assertThat(unicoRegistro().getResponsavel()).isEqualTo("gestor@safeplace.com");
    }

    @Test
    void instanteInformadoEhGravadoNoRegistro() {
        LocalDateTime classificacao = LocalDateTime.of(2026, 11, 6, 8, 15);

        registrador.por("gestor@safeplace.com").em(classificacao).alteracao("Tarefa", 5, "Classificação");

        assertThat(unicoRegistro().getDataHora()).isEqualTo(classificacao);
    }

    @Test
    void recusaDependenciasNulas() {
        assertThatThrownBy(() -> new RegistradorAuditoria(null, operador.operadorAtual(mock(UsuarioRepository.class))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Porta de auditoria é obrigatória (RNF05).");
        assertThatThrownBy(() -> new RegistradorAuditoria(trilha, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Operador atual é obrigatório.");
    }

    private LogAuditoria unicoRegistro() {
        assertThat(trilha.registros()).hasSize(1);
        return trilha.registros().get(0);
    }
}

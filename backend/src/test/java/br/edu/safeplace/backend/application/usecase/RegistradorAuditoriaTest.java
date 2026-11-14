package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;
import br.edu.safeplace.backend.validacao.Validador;

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

    @Test
    @DisplayName("Registro sem tipo de operação é recusado pelo validador antes de gravar (#297)")
    void registroInvalidoNaoEhGravado() {
        assertThatThrownBy(() -> registrador.operacao("   ", "Relatorio", null, "Exportação"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de operação é obrigatório.");

        assertThat(trilha.registros()).isEmpty();
    }

    @Test
    @DisplayName("O registrador usa o validador recebido antes de chamar o repository (#297)")
    void usaOValidadorRecebidoAntesDoRepository() {
        RegistradorAuditoria comValidadorQueRecusa = new RegistradorAuditoria(
                trilha, operador.operadorAtual(mock(UsuarioRepository.class)), new Validador<>() {
                    @Override
                    public ResultadoValidacao verificar(LogAuditoria log) {
                        return new ResultadoValidacao();
                    }

                    @Override
                    public void validar(LogAuditoria log) {
                        throw new IllegalStateException("recusado pelo validador");
                    }
                });

        assertThatThrownBy(() -> comValidadorQueRecusa.cadastro("Epi", 1, "Cadastro"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("recusado pelo validador");
        assertThat(trilha.registros()).isEmpty();
    }

    @Test
    void recusaValidadorNulo() {
        assertThatThrownBy(() -> new RegistradorAuditoria(trilha, operador.operadorAtual(mock(UsuarioRepository.class)), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Validador do registro de auditoria é obrigatório.");
    }

    @Test
    @DisplayName("Nenhum caso de uso chama LogAuditoria.registrar* diretamente (#237)")
    void casosDeUsoNaoChamamLogAuditoriaDiretamente() throws IOException {
        Path usecases = Path.of("src", "main", "java", "br", "edu", "safeplace", "backend", "application", "usecase");
        try (Stream<Path> stream = Files.walk(usecases)) {
            List<String> violacoes = stream
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().equals("RegistradorAuditoria.java"))
                    .filter(p -> {
                        try {
                            String conteudo = Files.readString(p, StandardCharsets.UTF_8);
                            return conteudo.contains("LogAuditoria.registrar");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(p -> p.getFileName().toString())
                    .toList();
            assertThat(violacoes).isEmpty();
        }
    }

    @Test
    @DisplayName("Construtor simplificado para testes registra auditoria com operador padrão (#237)")
    void construtorSimplificadoParaTestesRegistraAuditoria() {
        RegistradorAuditoria registradorSimplificado = new RegistradorAuditoria(trilha);
        registradorSimplificado.cadastro("Epi", 123, "Cadastro de EPI");

        LogAuditoria log = unicoRegistro();
        assertThat(log.getTipoOperacao()).isEqualTo(LogAuditoria.CADASTRO);
        assertThat(log.getEntidade()).isEqualTo("Epi");
        assertThat(log.getEntidadeId()).isEqualTo(123);
        assertThat(log.getResponsavel()).isEqualTo(OperadorAtual.SISTEMA);
    }

    private LogAuditoria unicoRegistro() {
        assertThat(trilha.registros()).hasSize(1);
        return trilha.registros().get(0);
    }
}

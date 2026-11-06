/*
 * Testes da issue #241 (Null Object, parte 2): as dependências antes opcionais dos casos de uso são
 * obrigatórias. Montar o serviço com null falha na hora, com mensagem clara; a ausência de operador
 * é representada pelo OperadorAutenticadoNulo, que leva a auditoria ao responsável "SISTEMA".
 */
package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.adapters.out.seguranca.OperadorAutenticadoNulo;
import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.EmprestimoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.VerificadorImpedimentoCapacitacao;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

@DisplayName("Dependências obrigatórias dos casos de uso (Null Object, #241)")
class DependenciasObrigatoriasCasosDeUsoTest {

    private final LogAuditoriaRepository auditoria = new TrilhaAuditoriaEmMemoria();

    /** Sem sessão: a fachada sobre o Null Object da porta (#236). */
    private static OperadorAtual operadorAtual() {
        return new OperadorAtual(new OperadorAutenticadoNulo(), mock(UsuarioRepository.class));
    }

    @Test
    @DisplayName("UsuarioService recusa operador null: a ausência é OperadorAutenticadoNulo")
    void usuarioServiceRecusaOperadorNulo() {
        assertThatThrownBy(() -> new UsuarioService(mock(UsuarioRepository.class), mock(CodificadorSenha.class),
                () -> "Senha#1", auditoria, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Porta do operador autenticado é obrigatória.");
    }

    @Test
    @DisplayName("NullObject: sem operador autenticado, a auditoria do cadastro registra SISTEMA")
    void operadorNuloLevaAuditoriaAoSistema() {
        TrilhaAuditoriaEmMemoria trilha = new TrilhaAuditoriaEmMemoria();
        UsuarioService servico = usuarioServiceCom(trilha, new OperadorAutenticadoNulo());

        servico.cadastrarColaborador(colaborador());

        assertThat(trilha.registros()).extracting(LogAuditoria::getResponsavel).containsExactly("SISTEMA");
    }

    @Test
    @DisplayName("RealObject: com operador identificado, a auditoria registra o e-mail dele")
    void operadorRealLevaAuditoriaAoEmail() {
        TrilhaAuditoriaEmMemoria trilha = new TrilhaAuditoriaEmMemoria();
        UsuarioService servico = usuarioServiceCom(trilha, new OperadorFixo("supervisor@safeplace.com"));

        servico.cadastrarColaborador(colaborador());

        assertThat(trilha.registros()).extracting(LogAuditoria::getResponsavel)
                .containsExactly("supervisor@safeplace.com");
    }

    @Test
    @DisplayName("EmprestimoService recusa repositório de manutenções null")
    void emprestimoServiceRecusaManutencoesNulo() {
        assertThatThrownBy(() -> new EmprestimoService(mock(EmprestimoRepository.class), mock(EpiRepository.class),
                mock(UsuarioRepository.class), mock(VerificadorImpedimentoCapacitacao.class), auditoria, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Repositório de manutenções é obrigatório.");
    }

    @Test
    @DisplayName("TarefaService recusa repositório de EPIs null")
    void tarefaServiceRecusaEpisNulo() {
        assertThatThrownBy(() -> new TarefaService(mock(TarefaRepository.class), auditoria,
                operadorAtual(), mock(AreaRiscoRepository.class), mock(SetorRepository.class), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Repositório de EPIs é obrigatório.");
    }

    @Test
    @DisplayName("PlanoDeAcaoService recusa consulta de ocorrências null")
    void planoDeAcaoServiceRecusaConsultaNula() {
        assertThatThrownBy(() -> new PlanoDeAcaoService(mock(PlanoDeAcaoRepository.class),
                mock(OcorrenciaRepository.class), mock(UsuarioRepository.class), mock(SetorRepository.class),
                mock(NotificadorAcao.class), null, operadorAtual()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Consulta de ocorrências é obrigatória (visibilidade do plano, issue #100).");
    }

    @Test
    @DisplayName("PlanoDeAcaoService monta com todas as portas informadas")
    void planoDeAcaoServiceMontaComPortas() {
        assertThat(new PlanoDeAcaoService(mock(PlanoDeAcaoRepository.class), mock(OcorrenciaRepository.class),
                mock(UsuarioRepository.class), mock(SetorRepository.class), mock(NotificadorAcao.class),
                mock(GerenciarOcorrenciaUseCase.class), operadorAtual())).isNotNull();
    }

    private static UsuarioService usuarioServiceCom(LogAuditoriaRepository trilha, OperadorAutenticado operador) {
        UsuarioRepository repositorio = mock(UsuarioRepository.class);
        when(repositorio.salvar(any(Colaborador.class))).thenReturn(new Colaborador(20, "52998224725", "Ana Silva",
                LocalDate.of(1990, 5, 10), "ana@safeplace.com", true, LocalDateTime.now(), LocalDateTime.now()));
        return new UsuarioService(repositorio, mock(CodificadorSenha.class), () -> "Senha#1", trilha, operador);
    }

    private static CadastrarColaboradorInputDTO colaborador() {
        return new CadastrarColaboradorInputDTO("Ana Silva", "52998224725", LocalDate.of(1990, 5, 10),
                "ana@safeplace.com");
    }

    /** Dublê de operador identificado (papel RealObject), escrito à mão. */
    private record OperadorFixo(String email) implements OperadorAutenticado {
        @Override
        public Optional<String> obterIdentificadorOperador() {
            return Optional.of(email);
        }

        @Override
        public boolean possuiPapel(String papel) {
            return false;
        }
    }
}

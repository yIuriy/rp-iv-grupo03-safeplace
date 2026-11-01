package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.adapters.out.seguranca.OperadorAutenticadoNulo;
import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.GeradorSenha;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Auditoria - UsuarioService")
class UsuarioServiceAuditoriaTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodificadorSenha codificadorSenha;

    @Mock
    private GeradorSenha geradorSenha;

    @Mock
    private LogAuditoriaRepository logAuditoriaRepository;

    private UsuarioService casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new UsuarioService(
                usuarioRepository,
                codificadorSenha,
                geradorSenha,
                logAuditoriaRepository,
                new OperadorAutenticadoNulo()
        );
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao cadastrar Supervisor sem expor credenciais")
    void deveRegistrarAuditoriaAoCadastrarSupervisor() {
        CadastrarSupervisorInputDTO entrada = new CadastrarSupervisorInputDTO(
                "Carlos Gestor", "52998224725", LocalDate.of(1985, 1, 1), "carlos@safeplace.com");

        when(geradorSenha.gerar()).thenReturn("SenhaInicial#123");
        when(codificadorSenha.codificar("SenhaInicial#123")).thenReturn("$2a$10$hashSeguro");

        Supervisor salvo = new Supervisor(
                10, "52998224725", "Carlos Gestor", LocalDate.of(1985, 1, 1),
                "carlos@safeplace.com", "$2a$10$hashSeguro", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(usuarioRepository.salvar(any(Supervisor.class))).thenReturn(salvo);

        UsuarioOutputDTO resultado = casoDeUso.cadastrarSupervisor(entrada, "admin@safeplace.com");

        assertThat(resultado.id()).isEqualTo(10);
        assertThat(resultado.senhaInicial()).isEqualTo("SenhaInicial#123");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getResponsavel()).isEqualTo("admin@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Supervisor");
        assertThat(log.getEntidadeId()).isEqualTo(10);
        // Garantir que senha/hash não é armazenada no payload/justificativa
        assertThat(log.getJustificativa()).doesNotContain("SenhaInicial#123");
        assertThat(log.getJustificativa()).doesNotContain("$2a$10$hashSeguro");
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao cadastrar Colaborador")
    void deveRegistrarAuditoriaAoCadastrarColaborador() {
        CadastrarColaboradorInputDTO entrada = new CadastrarColaboradorInputDTO(
                "Ana Silva", "52998224725", LocalDate.of(1990, 5, 10), "ana@safeplace.com");

        Colaborador salvo = new Colaborador(
                20, "52998224725", "Ana Silva", LocalDate.of(1990, 5, 10),
                "ana@safeplace.com", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(usuarioRepository.salvar(any(Colaborador.class))).thenReturn(salvo);

        UsuarioOutputDTO resultado = casoDeUso.cadastrarColaborador(entrada, "supervisor@safeplace.com");

        assertThat(resultado.id()).isEqualTo(20);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getResponsavel()).isEqualTo("supervisor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Colaborador");
        assertThat(log.getEntidadeId()).isEqualTo(20);
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao atualizar Colaborador")
    void deveRegistrarAuditoriaAoAtualizarColaborador() {
        Colaborador existente = new Colaborador(
                30, "52998224725", "Nome Antigo", LocalDate.of(1990, 1, 1),
                "antigo@safeplace.com", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(usuarioRepository.buscarPorId(30)).thenReturn(Optional.of(existente));

        Colaborador atualizado = new Colaborador(
                30, "52998224725", "Nome Novo", LocalDate.of(1990, 1, 1),
                "novo@safeplace.com", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(usuarioRepository.salvar(any(Colaborador.class))).thenReturn(atualizado);

        AtualizarPessoaInputDTO entrada = new AtualizarPessoaInputDTO(
                "Nome Novo", LocalDate.of(1990, 1, 1), "novo@safeplace.com");

        UsuarioOutputDTO resultado = casoDeUso.atualizarColaborador(30, entrada, "supervisor@safeplace.com");

        assertThat(resultado.nome()).isEqualTo("Nome Novo");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("supervisor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Colaborador");
        assertThat(log.getEntidadeId()).isEqualTo(30);
    }

    @Test
    @DisplayName("Falha de negócio não deve gerar log de auditoria")
    void falhaDeNegocioNaoGeraLogAuditoria() {
        when(usuarioRepository.buscarPorCpf("52998224725"))
                .thenReturn(Optional.of(mock(Colaborador.class)));

        CadastrarColaboradorInputDTO entrada = new CadastrarColaboradorInputDTO(
                "Teste", "52998224725", LocalDate.of(1990, 1, 1), "teste@safeplace.com");

        assertThatThrownBy(() -> casoDeUso.cadastrarColaborador(entrada, "op@safeplace.com"))
                .isInstanceOf(CpfJaCadastradoException.class);

        verifyNoInteractions(logAuditoriaRepository);
    }

    @Test
    @DisplayName("Atualização de usuário inexistente não deve gerar log de auditoria")
    void atualizacaoInexistenteNaoGeraLogAuditoria() {
        when(usuarioRepository.buscarPorId(999)).thenReturn(Optional.empty());

        AtualizarPessoaInputDTO entrada = new AtualizarPessoaInputDTO(
                "Nome", LocalDate.of(1990, 1, 1), "email@safeplace.com");

        assertThatThrownBy(() -> casoDeUso.atualizarColaborador(999, entrada, "op@safeplace.com"))
                .isInstanceOf(UsuarioNaoEncontradoException.class);

        verifyNoInteractions(logAuditoriaRepository);
    }
}

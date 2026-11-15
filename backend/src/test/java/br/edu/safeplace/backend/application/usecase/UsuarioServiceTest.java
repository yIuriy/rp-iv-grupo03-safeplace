package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.adapters.out.seguranca.OperadorAutenticadoNulo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioInputDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.GeradorSenha;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    /** RNF05: a porta de auditoria é obrigatória nos casos de uso. */
    private final TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();

    @Mock
    private UsuarioRepository repositorioPorta;

    @Mock
    private CodificadorSenha codificadorSenha;

    @Mock
    private GeradorSenha geradorSenha;

    private UsuarioService casoDeUso;

    private final String cpfValido = "52998224725";

    @BeforeEach
    void setUp() {
        casoDeUso = new UsuarioService(repositorioPorta, codificadorSenha, geradorSenha, auditoria,
                new OperadorAutenticadoNulo());
    }

    @Test
    void deveCadastrarColaboradorComSucesso() {
        CadastrarUsuarioInputDTO entrada = new CadastrarUsuarioInputDTO(
                "Carlos Souza", cpfValido, LocalDate.of(1995, 3, 10),
                "carlos@empresa.com", null, null
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());
        when(repositorioPorta.buscarPorEmail("carlos@empresa.com")).thenReturn(Optional.empty());
        when(repositorioPorta.salvar(any(Colaborador.class))).thenAnswer(inv -> {
            Colaborador c = inv.getArgument(0);
            return new Colaborador(1, c.getCpf(), c.getNome(), c.getDataNascimento(), c.getEmail(), true, LocalDateTime.now(), LocalDateTime.now());
        });

        UsuarioOutputDTO saida = casoDeUso.cadastrar(entrada);

        assertNotNull(saida);
        assertEquals(1, saida.id());
        assertEquals("Carlos Souza", saida.nome());
        assertEquals(cpfValido, saida.cpf());
        assertNull(saida.perfil());
        verify(codificadorSenha, never()).codificar(any());
        verify(repositorioPorta).salvar(any(Colaborador.class));
    }

    @Test
    void deveCadastrarSupervisorComHashDeSenha() {
        CadastrarUsuarioInputDTO entrada = new CadastrarUsuarioInputDTO(
                "Ana Lima", cpfValido, LocalDate.of(1985, 7, 20),
                "ana@empresa.com", "senha123", Perfil.SUPERVISOR
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());
        when(repositorioPorta.buscarPorEmail("ana@empresa.com")).thenReturn(Optional.empty());
        when(geradorSenha.gerar()).thenReturn("senha123"); // issue #91: senha do Supervisor passa a ser gerada
        when(codificadorSenha.codificar("senha123")).thenReturn("$2a$10$hashedPassword");
        when(repositorioPorta.salvar(any(Supervisor.class))).thenAnswer(inv -> {
            Supervisor s = inv.getArgument(0);
            return new Supervisor(2, s.getCpf(), s.getNome(), s.getDataNascimento(), s.getEmail(), s.getSenha(), true, LocalDateTime.now(), LocalDateTime.now());
        });

        UsuarioOutputDTO saida = casoDeUso.cadastrar(entrada);

        assertNotNull(saida);
        assertEquals(2, saida.id());
        assertEquals(Perfil.SUPERVISOR, saida.perfil());
        verify(codificadorSenha).codificar("senha123");
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        CadastrarUsuarioInputDTO entrada = new CadastrarUsuarioInputDTO(
                "Carlos", cpfValido, LocalDate.of(1995, 3, 10), "carlos@empresa.com", null, null
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.of(mock(Colaborador.class)));

        assertThrows(CpfJaCadastradoException.class, () -> casoDeUso.cadastrar(entrada));
        verify(repositorioPorta, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        CadastrarUsuarioInputDTO entrada = new CadastrarUsuarioInputDTO(
                "Carlos", cpfValido, LocalDate.of(1995, 3, 10), "carlos@empresa.com", null, null
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());
        when(repositorioPorta.buscarPorEmail("carlos@empresa.com")).thenReturn(Optional.of(mock(Colaborador.class)));

        assertThrows(EmailJaCadastradoException.class, () -> casoDeUso.cadastrar(entrada));
        verify(repositorioPorta, never()).salvar(any());
    }

    @Test
    void deveListarUsuarios() {
        Colaborador c = new Colaborador(1, cpfValido, "Carlos", LocalDate.of(1990, 1, 1), "carlos@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
        when(repositorioPorta.listar()).thenReturn(List.of(c));

        List<UsuarioOutputDTO> lista = casoDeUso.listar();

        assertEquals(1, lista.size());
        assertEquals("Carlos", lista.getFirst().nome());
    }

    @Test
    void deveBuscarPorIdComSucessoELancarExcecaoQuandoNaoEncontrado() {
        Colaborador c = new Colaborador(1, cpfValido, "Carlos", LocalDate.of(1990, 1, 1), "carlos@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
        when(repositorioPorta.buscarPorId(1)).thenReturn(Optional.of(c));
        when(repositorioPorta.buscarPorId(99)).thenReturn(Optional.empty());

        assertNotNull(casoDeUso.buscarPorId(1));
        assertThrows(UsuarioNaoEncontradoException.class, () -> casoDeUso.buscarPorId(99));
    }

    @Test
    void supervisorBuscandoSupervisorPorIdDeveLancarAcessoNegado() {
        Supervisor s = new Supervisor(2, cpfValido, "Ana Lima", LocalDate.of(1985, 7, 20), "ana@empresa.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
        when(repositorioPorta.buscarPorId(2)).thenReturn(Optional.of(s));

        OperadorAutenticado operadorPorta = mock(OperadorAutenticado.class);
        when(operadorPorta.possuiPapel("ROLE_GESTOR_SEGURANCA")).thenReturn(false);
        when(operadorPorta.possuiPapel("ROLE_SUPERVISOR")).thenReturn(true);

        UsuarioService casoDeUsoComOperador = new UsuarioService(
                repositorioPorta, codificadorSenha, geradorSenha, auditoria, operadorPorta);

        assertThrows(AcessoNegadoException.class, () -> casoDeUsoComOperador.buscarPorId(2));
    }

    @Test
    void supervisorBuscandoColaboradorPorIdDeveTerSucesso() {
        Colaborador c = new Colaborador(3, cpfValido, "Carlos", LocalDate.of(1990, 1, 1), "carlos@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
        when(repositorioPorta.buscarPorId(3)).thenReturn(Optional.of(c));

        OperadorAutenticado operadorPorta = mock(OperadorAutenticado.class);
        when(operadorPorta.possuiPapel("ROLE_GESTOR_SEGURANCA")).thenReturn(false);
        when(operadorPorta.possuiPapel("ROLE_SUPERVISOR")).thenReturn(true);

        UsuarioService casoDeUsoComOperador = new UsuarioService(
                repositorioPorta, codificadorSenha, geradorSenha, auditoria, operadorPorta);

        UsuarioOutputDTO saida = casoDeUsoComOperador.buscarPorId(3);
        assertNotNull(saida);
        assertEquals(3, saida.id());
        assertNull(saida.perfil());
    }

    @Test
    void gestorBuscandoSupervisorPorIdDeveTerSucesso() {
        Supervisor s = new Supervisor(2, cpfValido, "Ana Lima", LocalDate.of(1985, 7, 20), "ana@empresa.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
        when(repositorioPorta.buscarPorId(2)).thenReturn(Optional.of(s));

        OperadorAutenticado operadorPorta = mock(OperadorAutenticado.class);
        when(operadorPorta.possuiPapel("ROLE_GESTOR_SEGURANCA")).thenReturn(true);

        UsuarioService casoDeUsoComOperador = new UsuarioService(
                repositorioPorta, codificadorSenha, geradorSenha, auditoria, operadorPorta);

        UsuarioOutputDTO saida = casoDeUsoComOperador.buscarPorId(2);
        assertNotNull(saida);
        assertEquals(2, saida.id());
        assertEquals(Perfil.SUPERVISOR, saida.perfil());
    }
}

package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.GeradorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
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
class UsuarioCasoDeUsoTest {

    @Mock
    private UsuarioRepositorioPorta repositorioPorta;

    @Mock
    private CodificadorSenhaPorta codificadorSenhaPorta;

    @Mock
    private GeradorSenhaPorta geradorSenhaPorta;

    private UsuarioCasoDeUso casoDeUso;

    private final String cpfValido = "52998224725";

    @BeforeEach
    void setUp() {
        casoDeUso = new UsuarioCasoDeUso(repositorioPorta, codificadorSenhaPorta, geradorSenhaPorta);
    }

    @Test
    void deveCadastrarColaboradorComSucesso() {
        CadastrarUsuarioEntradaDTO entrada = new CadastrarUsuarioEntradaDTO(
                "Carlos Souza", cpfValido, LocalDate.of(1995, 3, 10),
                "carlos@empresa.com", null, Perfil.COLABORADOR
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());
        when(repositorioPorta.buscarPorEmail("carlos@empresa.com")).thenReturn(Optional.empty());
        when(repositorioPorta.salvar(any(Colaborador.class))).thenAnswer(inv -> {
            Colaborador c = inv.getArgument(0);
            return new Colaborador(1, c.getCpf(), c.getNome(), c.getDataNascimento(), c.getEmail(), true, LocalDateTime.now(), LocalDateTime.now());
        });

        UsuarioSaidaDTO saida = casoDeUso.cadastrarUsuario(entrada);

        assertNotNull(saida);
        assertEquals(1, saida.id());
        assertEquals("Carlos Souza", saida.nome());
        assertEquals(cpfValido, saida.cpf());
        assertEquals(Perfil.COLABORADOR, saida.perfil());
        verify(codificadorSenhaPorta, never()).codificar(any());
        verify(repositorioPorta).salvar(any(Colaborador.class));
    }

    @Test
    void deveCadastrarSupervisorComHashDeSenha() {
        CadastrarUsuarioEntradaDTO entrada = new CadastrarUsuarioEntradaDTO(
                "Ana Lima", cpfValido, LocalDate.of(1985, 7, 20),
                "ana@empresa.com", "senha123", Perfil.SUPERVISOR
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());
        when(repositorioPorta.buscarPorEmail("ana@empresa.com")).thenReturn(Optional.empty());
        when(geradorSenhaPorta.gerar()).thenReturn("senha123"); // issue #91: senha do Supervisor passa a ser gerada
        when(codificadorSenhaPorta.codificar("senha123")).thenReturn("$2a$10$hashedPassword");
        when(repositorioPorta.salvar(any(Supervisor.class))).thenAnswer(inv -> {
            Supervisor s = inv.getArgument(0);
            return new Supervisor(2, s.getCpf(), s.getNome(), s.getDataNascimento(), s.getEmail(), s.getSenha(), true, LocalDateTime.now(), LocalDateTime.now());
        });

        UsuarioSaidaDTO saida = casoDeUso.cadastrarUsuario(entrada);

        assertNotNull(saida);
        assertEquals(2, saida.id());
        assertEquals(Perfil.SUPERVISOR, saida.perfil());
        verify(codificadorSenhaPorta).codificar("senha123");
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        CadastrarUsuarioEntradaDTO entrada = new CadastrarUsuarioEntradaDTO(
                "Carlos", cpfValido, LocalDate.of(1995, 3, 10), "carlos@empresa.com", null, Perfil.COLABORADOR
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.of(mock(Colaborador.class)));

        assertThrows(CpfJaCadastradoException.class, () -> casoDeUso.cadastrarUsuario(entrada));
        verify(repositorioPorta, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        CadastrarUsuarioEntradaDTO entrada = new CadastrarUsuarioEntradaDTO(
                "Carlos", cpfValido, LocalDate.of(1995, 3, 10), "carlos@empresa.com", null, Perfil.COLABORADOR
        );

        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());
        when(repositorioPorta.buscarPorEmail("carlos@empresa.com")).thenReturn(Optional.of(mock(Colaborador.class)));

        assertThrows(EmailJaCadastradoException.class, () -> casoDeUso.cadastrarUsuario(entrada));
        verify(repositorioPorta, never()).salvar(any());
    }

    @Test
    void deveListarUsuarios() {
        Colaborador c = new Colaborador(1, cpfValido, "Carlos", LocalDate.of(1990, 1, 1), "carlos@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
        when(repositorioPorta.listarTodos()).thenReturn(List.of(c));

        List<UsuarioSaidaDTO> lista = casoDeUso.listarUsuarios();

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
}

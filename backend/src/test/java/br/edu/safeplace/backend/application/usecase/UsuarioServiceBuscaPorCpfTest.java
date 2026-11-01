package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.adapters.out.seguranca.OperadorAutenticadoNulo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceBuscaPorCpfTest {

    /** RNF05: a porta de auditoria é obrigatória nos casos de uso. */
    private final TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();

    @Mock
    private UsuarioRepository repositorioPorta;

    @Mock
    private CodificadorSenha codificadorSenha;

    @Mock
    private br.edu.safeplace.backend.application.port.out.GeradorSenha geradorSenha;

    private UsuarioService casoDeUso;

    private final String cpfValido = "52998224725";

    @BeforeEach
    void setUp() {
        casoDeUso = new UsuarioService(repositorioPorta, codificadorSenha, geradorSenha, auditoria,
                new OperadorAutenticadoNulo());
    }

    @Test
    void deveBuscarPorCpfSanitizandoAMascaraAntesDeConsultarORepositorio() {
        Colaborador c = new Colaborador(1, cpfValido, "Carlos", LocalDate.of(1990, 1, 1), null, true,
                LocalDateTime.now(), LocalDateTime.now());
        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.of(c));

        UsuarioOutputDTO saida = casoDeUso.buscarPorCpf("529.982.247-25");

        assertEquals(1, saida.id());
        assertEquals(cpfValido, saida.cpf());
        verify(repositorioPorta).buscarPorCpf(cpfValido);
    }

    @Test
    void deveLancarUsuarioNaoEncontradoQuandoCpfNaoExiste() {
        when(repositorioPorta.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class, () -> casoDeUso.buscarPorCpf(cpfValido));
    }

    @Test
    void deveRejeitarCpfInvalidoSemConsultarORepositorio() {
        assertThrows(CpfInvalidoException.class, () -> casoDeUso.buscarPorCpf("123"));

        verify(repositorioPorta, never()).buscarPorCpf(any());
    }
}

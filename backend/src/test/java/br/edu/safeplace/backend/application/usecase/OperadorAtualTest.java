package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperadorAtualTest {

    @Mock
    private OperadorAutenticado operadorPorta;

    @Mock
    private UsuarioRepository usuarioRepository;

    private OperadorAtual operadorAtual;

    @BeforeEach
    void setUp() {
        operadorAtual = new OperadorAtual(operadorPorta, usuarioRepository);
    }

    @Test
    void construtorDeveRejeitarDependenciasNulas() {
        assertThrows(NullPointerException.class, () -> new OperadorAtual(null, usuarioRepository));
        assertThrows(NullPointerException.class, () -> new OperadorAtual(operadorPorta, null));
    }

    @Test
    void emailOuSistemaDeveRetornarSistemaQuandoOperadorVazio() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.empty());

        assertEquals("SISTEMA", operadorAtual.emailOuSistema());
    }

    @Test
    void emailOuSistemaDeveRetornarEmailQuandoOperadorPresente() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.of("operador@empresa.com"));

        assertEquals("operador@empresa.com", operadorAtual.emailOuSistema());
    }

    @Test
    void obterIdentificadorOperadorDeveDelegarParaPorta() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.of("teste@empresa.com"));

        Optional<String> resultado = operadorAtual.obterIdentificadorOperador();
        assertTrue(resultado.isPresent());
        assertEquals("teste@empresa.com", resultado.get());
    }

    @Test
    void buscarGestorDeveRetornarVazioQuandoOperadorNaoAutenticado() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.empty());

        Optional<GestorDeSeguranca> gestor = operadorAtual.buscarGestor();
        assertTrue(gestor.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void buscarGestorDeveRetornarVazioQuandoUsuarioNaoEncontradoNoRepositorio() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.of("gestor@empresa.com"));
        when(usuarioRepository.buscarPorEmail("gestor@empresa.com")).thenReturn(Optional.empty());

        Optional<GestorDeSeguranca> gestor = operadorAtual.buscarGestor();
        assertTrue(gestor.isEmpty());
    }

    @Test
    void buscarGestorDeveRetornarVazioQuandoUsuarioNaoForGestorDeSeguranca() {
        Colaborador colaboradorComum = new Colaborador(
                1,
                "52998224725",
                "Carlos Silva",
                LocalDate.of(1990, 1, 1),
                "carlos@empresa.com",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.of("carlos@empresa.com"));
        when(usuarioRepository.buscarPorEmail("carlos@empresa.com")).thenReturn(Optional.of(colaboradorComum));

        Optional<GestorDeSeguranca> gestor = operadorAtual.buscarGestor();
        assertTrue(gestor.isEmpty(), "Colaborador comum não deve ser aceito como GestorDeSeguranca");
    }

    @Test
    void buscarGestorDeveRetornarGestorQuandoAutenticadoComoGestorDeSeguranca() {
        GestorDeSeguranca gestorEsperado = new GestorDeSeguranca(
                2,
                "52998224725",
                "Gestor Santos",
                LocalDate.of(1985, 5, 10),
                "gestor@empresa.com",
                "hash",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.of("gestor@empresa.com"));
        when(usuarioRepository.buscarPorEmail("gestor@empresa.com")).thenReturn(Optional.of(gestorEsperado));

        Optional<GestorDeSeguranca> gestor = operadorAtual.buscarGestor();
        assertTrue(gestor.isPresent());
        assertSame(gestorEsperado, gestor.get());
    }

    @Test
    void gestorObrigatorioDeveRetornarGestorQuandoExistir() {
        GestorDeSeguranca gestorEsperado = new GestorDeSeguranca(
                2,
                "52998224725",
                "Gestor Santos",
                LocalDate.of(1985, 5, 10),
                "gestor@empresa.com",
                "hash",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.of("gestor@empresa.com"));
        when(usuarioRepository.buscarPorEmail("gestor@empresa.com")).thenReturn(Optional.of(gestorEsperado));

        GestorDeSeguranca retorno = operadorAtual.gestorObrigatorio();
        assertSame(gestorEsperado, retorno);
    }

    @Test
    void gestorObrigatorioSemMensagemDeveLancarAcessoNegadoComMensagemPadrao() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.empty());

        AcessoNegadoException ex = assertThrows(AcessoNegadoException.class, () -> operadorAtual.gestorObrigatorio());
        assertEquals("Operação exige um Gestor de Segurança autenticado.", ex.getMessage());
    }

    @Test
    void gestorObrigatorioComMensagemCustomizadaDeveLancarAcessoNegadoComMensagemInformada() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.empty());

        String mensagemCustom = "A classificação de periculosidade exige um Gestor de Segurança autenticado como responsável técnico.";
        AcessoNegadoException ex = assertThrows(AcessoNegadoException.class, () -> operadorAtual.gestorObrigatorio(mensagemCustom));
        assertEquals(mensagemCustom, ex.getMessage());
    }

    @Test
    void gestorObrigatorioComMensagemNulaOuVaziaDeveUsarMensagemPadrao() {
        when(operadorPorta.obterIdentificadorOperador()).thenReturn(Optional.empty());

        AcessoNegadoException exNulo = assertThrows(AcessoNegadoException.class, () -> operadorAtual.gestorObrigatorio(null));
        assertEquals("Operação exige um Gestor de Segurança autenticado.", exNulo.getMessage());

        AcessoNegadoException exVazio = assertThrows(AcessoNegadoException.class, () -> operadorAtual.gestorObrigatorio("   "));
        assertEquals("Operação exige um Gestor de Segurança autenticado.", exVazio.getMessage());
    }

    @Test
    void possuiPapelDeveDelegarParaPorta() {
        when(operadorPorta.possuiPapel("GESTOR_SEGURANCA")).thenReturn(true);
        when(operadorPorta.possuiPapel("SUPERVISOR")).thenReturn(false);

        assertTrue(operadorAtual.possuiPapel("GESTOR_SEGURANCA"));
        assertFalse(operadorAtual.possuiPapel("SUPERVISOR"));
    }
}

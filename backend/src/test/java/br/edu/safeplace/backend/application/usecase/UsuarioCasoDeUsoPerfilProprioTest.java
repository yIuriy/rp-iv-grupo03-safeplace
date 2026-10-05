/*
 * Testes da consulta do próprio perfil (tela de perfil do Gestor e do Supervisor): o caso de uso
 * encontra o cadastro pelo e-mail do login, normalizado como na autenticação, e informa quando
 * ele não existe.
 */

package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.GeradorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioCasoDeUsoPerfilProprioTest {

    @Mock
    private UsuarioRepositorioPorta repositorioPorta;

    @Mock
    private CodificadorSenhaPorta codificadorSenhaPorta;

    @Mock
    private GeradorSenhaPorta geradorSenhaPorta;

    private UsuarioCasoDeUso casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new UsuarioCasoDeUso(repositorioPorta, codificadorSenhaPorta, geradorSenhaPorta);
    }

    @Test
    void deveBuscarOProprioCadastroPeloEmailNormalizado() {
        Supervisor supervisor = Supervisor.novo("Sara Lima", "52998224725", LocalDate.of(1990, 4, 12),
                "sara@safeplace.com", "hash");
        when(repositorioPorta.buscarPorEmail("sara@safeplace.com")).thenReturn(Optional.of(supervisor));

        UsuarioSaidaDTO saida = casoDeUso.buscarPorEmail("  Sara@SafePlace.com ");

        assertEquals("Sara Lima", saida.nome());
        assertEquals("52998224725", saida.cpf());
        assertEquals(LocalDate.of(1990, 4, 12), saida.dataNascimento());
        assertEquals(Perfil.SUPERVISOR, saida.perfil());
        assertNull(saida.senhaInicial());
    }

    @Test
    void deveLancarUsuarioNaoEncontradoQuandoEmailNaoTemCadastro() {
        when(repositorioPorta.buscarPorEmail("ausente@safeplace.com")).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class, () -> casoDeUso.buscarPorEmail("ausente@safeplace.com"));
    }
}

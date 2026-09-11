package br.edu.safeplace.backend.domain.usuario;

import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ColaboradorTest {

    private final String cpfValido = "52998224725";

    @Test
    void deveCriarColaboradorComSucessoSemSenha() {
        Colaborador colaborador = Colaborador.novo("Carlos Souza", cpfValido, LocalDate.of(1995, 3, 10), "carlos@empresa.com");

        assertNull(colaborador.getId());
        assertEquals("Carlos Souza", colaborador.getNome());
        assertEquals(cpfValido, colaborador.getCpf());
        assertEquals(LocalDate.of(1995, 3, 10), colaborador.getDataNascimento());
        assertEquals("carlos@empresa.com", colaborador.getEmail());
        assertEquals(Perfil.COLABORADOR, colaborador.getPerfil());
        assertTrue(colaborador.isAtivo());
    }

    @Test
    void deveCriarSupervisorComSucessoComSenha() {
        Supervisor supervisor = Supervisor.novo("Ana Lima", cpfValido, LocalDate.of(1985, 7, 20), "ana@empresa.com", "hash_senha");

        assertEquals(Perfil.SUPERVISOR, supervisor.getPerfil());
        assertEquals("hash_senha", supervisor.getSenha());
        assertEquals("ana@empresa.com", supervisor.getEmail());
    }

    @Test
    void deveRejeitarSupervisorSemSenhaOuSemEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                Supervisor.novo("Ana Lima", cpfValido, LocalDate.of(1985, 7, 20), "ana@empresa.com", null));

        assertThrows(IllegalArgumentException.class, () ->
                Supervisor.novo("Ana Lima", cpfValido, LocalDate.of(1985, 7, 20), "", "hash_senha"));
    }

    @Test
    void deveCriarGestorDeSegurancaComSucesso() {
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Roberto Silva", cpfValido, LocalDate.of(1980, 1, 15), "roberto@empresa.com", "hash_senha");

        assertEquals(Perfil.GESTOR_SEGURANCA, gestor.getPerfil());
        assertEquals("hash_senha", gestor.getSenha());
    }

    @Test
    void deveRejeitarColaboradorComDataNoFuturoOuCpfInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                Colaborador.novo("Carlos", cpfValido, LocalDate.now().plusDays(1), "carlos@empresa.com"));

        assertThrows(CpfInvalidoException.class, () ->
                Colaborador.novo("Carlos", "11111111111", LocalDate.of(1990, 1, 1), "carlos@empresa.com"));
    }
}

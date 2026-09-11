package br.edu.safeplace.backend.domain.usuario;

import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SupervisorTest {

    private static final String CPF = "52998224725";
    private static final LocalDate NASCIMENTO = LocalDate.of(1985, 7, 20);

    @Test
    void deveCriarSupervisorAtivoComPerfilDeSupervisor() {
        Supervisor supervisor = Supervisor.novo("Ana Lima", CPF, NASCIMENTO, "ana@empresa.com", "$2a$10$hash");

        assertNull(supervisor.getId());
        assertTrue(supervisor.isAtivo());
        assertEquals(Perfil.SUPERVISOR, supervisor.getPerfil());
        assertTrue(supervisor.getPerfil().exigeCredenciais());
    }

    @Test
    void deveNormalizarEmailParaMinusculasSemEspacos() {
        Supervisor supervisor = Supervisor.novo("Ana Lima", CPF, NASCIMENTO, "  Ana@Empresa.COM ", "$2a$10$hash");

        assertEquals("ana@empresa.com", supervisor.getEmail());
    }

    @Test
    void deveSanitizarCpfComMascara() {
        Supervisor supervisor = Supervisor.novo("Ana Lima", "529.982.247-25", NASCIMENTO, "ana@empresa.com", "$2a$10$hash");

        assertEquals(CPF, supervisor.getCpf());
    }

    @Test
    void deveHerdarInvariantesDeIdentificacao() {
        assertThrows(IllegalArgumentException.class, () ->
                Supervisor.novo("", CPF, NASCIMENTO, "ana@empresa.com", "$2a$10$hash"));
        assertThrows(IllegalArgumentException.class, () ->
                Supervisor.novo("Ana Lima", CPF, LocalDate.now().plusDays(1), "ana@empresa.com", "$2a$10$hash"));
        assertThrows(CpfInvalidoException.class, () ->
                Supervisor.novo("Ana Lima", "123", NASCIMENTO, "ana@empresa.com", "$2a$10$hash"));
    }
}

package br.edu.safeplace.backend.domain.usuario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerfilTest {

    @Test
    void gestorESupervisorDevemExigirCredenciais() {
        assertTrue(Perfil.GESTOR_SEGURANCA.exigeCredenciais());
        assertTrue(Perfil.SUPERVISOR.exigeCredenciais());
    }

    @Test
    void colaboradorNaoDeveExigirCredenciais() {
        assertFalse(Perfil.COLABORADOR.exigeCredenciais());
    }
}

package br.edu.safeplace.backend.domain.usuario;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GestorDeSegurancaTest {

    private static final String CPF = "12345678909";
    private static final LocalDate NASCIMENTO = LocalDate.of(1980, 2, 15);

    @Test
    void deveCriarGestorAtivoComPerfilDeGestorEGuardarOHashRecebido() {
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Marina Costa", CPF, NASCIMENTO, "marina@empresa.com", "$2a$10$hash");

        assertNull(gestor.getId());
        assertTrue(gestor.isAtivo());
        assertEquals(Perfil.GESTOR_SEGURANCA, gestor.getPerfil());
        assertTrue(gestor.getPerfil().exigeCredenciais());
        assertEquals("$2a$10$hash", gestor.getSenha());
    }
}

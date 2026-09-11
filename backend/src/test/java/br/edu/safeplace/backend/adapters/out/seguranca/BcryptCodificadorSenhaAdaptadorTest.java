package br.edu.safeplace.backend.adapters.out.seguranca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BcryptCodificadorSenhaAdaptadorTest {

    private final BcryptCodificadorSenhaAdaptador codificador = new BcryptCodificadorSenhaAdaptador();

    @Test
    void deveGerarHashBcryptDiferenteDaSenhaPura() {
        String hash = codificador.codificar("Segredo@123");

        assertNotEquals("Segredo@123", hash);
        assertTrue(hash.startsWith("$2a$"), "prefixo BCrypt esperado, obtido: " + hash);
        assertEquals(60, hash.length(), "hash BCrypt tem 60 caracteres");
    }

    @Test
    void deveValidarSenhaCorretaERejeitarSenhaErrada() {
        String hash = codificador.codificar("Segredo@123");

        assertTrue(codificador.validar("Segredo@123", hash));
        assertFalse(codificador.validar("segredo@123", hash));
        assertFalse(codificador.validar("Outra@456", hash));
    }

    @Test
    void mesmaSenhaDeveGerarHashesDiferentesPorCausaDoSaltEAmbosDevemValidar() {
        String primeiro = codificador.codificar("Segredo@123");
        String segundo = codificador.codificar("Segredo@123");

        assertNotEquals(primeiro, segundo);
        assertTrue(codificador.validar("Segredo@123", primeiro));
        assertTrue(codificador.validar("Segredo@123", segundo));
    }

    @Test
    void deveTratarNulosSemLancarExcecao() {
        assertNull(codificador.codificar(null));
        assertFalse(codificador.validar(null, "$2a$10$qualquer"));
        assertFalse(codificador.validar("Segredo@123", null));
    }
}

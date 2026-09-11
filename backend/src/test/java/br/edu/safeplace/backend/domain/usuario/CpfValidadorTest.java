package br.edu.safeplace.backend.domain.usuario;

import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidadorTest {

    @Test
    void deveValidarCpfValidoComMascaraESemMascara() {
        assertDoesNotThrow(() -> CpfValidador.validar("52998224725"));
        assertDoesNotThrow(() -> CpfValidador.validar("529.982.247-25"));
        assertDoesNotThrow(() -> CpfValidador.validar("12345678901"));
        assertDoesNotThrow(() -> CpfValidador.validar("123.456.789-01"));
    }

    @Test
    void deveRejeitarCpfNuloOuComTamanhoInvalido() {
        assertThrows(CpfInvalidoException.class, () -> CpfValidador.validar(null));
        assertThrows(CpfInvalidoException.class, () -> CpfValidador.validar("123456789"));
        assertThrows(CpfInvalidoException.class, () -> CpfValidador.validar("123456789012"));
    }

    @Test
    void deveRejeitarCpfComDigitosRepetidos() {
        assertThrows(CpfInvalidoException.class, () -> CpfValidador.validar("11111111111"));
        assertThrows(CpfInvalidoException.class, () -> CpfValidador.validar("00000000000"));
        assertThrows(CpfInvalidoException.class, () -> CpfValidador.validar("99999999999"));
    }

    @Test
    void deveSanitizarCpfRemovendoPontosETracos() {
        assertEquals("52998224725", CpfValidador.sanitizar("529.982.247-25"));
        assertNull(CpfValidador.sanitizar(null));
    }
}

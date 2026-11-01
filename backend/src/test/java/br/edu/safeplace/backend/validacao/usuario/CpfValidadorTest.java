package br.edu.safeplace.backend.validacao.usuario;

import br.edu.safeplace.backend.domain.usuario.Cpf;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Vieram do antigo CpfValidador estático do domínio, com as mesmas asserções (issue #263). */
class CpfValidadorTest {

    private final CpfValidador validador = new CpfValidador();

    @Test
    void deveValidarCpfValidoComMascaraESemMascara() {
        assertDoesNotThrow(() -> validador.validar("52998224725"));
        assertDoesNotThrow(() -> validador.validar("529.982.247-25"));
        assertDoesNotThrow(() -> validador.validar("12345678901"));
        assertDoesNotThrow(() -> validador.validar("123.456.789-01"));
    }

    @Test
    void deveRejeitarCpfNuloOuComTamanhoInvalido() {
        assertThrows(CpfInvalidoException.class, () -> validador.validar(null));
        assertThrows(CpfInvalidoException.class, () -> validador.validar("123456789"));
        assertThrows(CpfInvalidoException.class, () -> validador.validar("123456789012"));
    }

    @Test
    void deveRejeitarCpfComDigitosRepetidos() {
        assertThrows(CpfInvalidoException.class, () -> validador.validar("11111111111"));
        assertThrows(CpfInvalidoException.class, () -> validador.validar("00000000000"));
        assertThrows(CpfInvalidoException.class, () -> validador.validar("99999999999"));
    }

    @Test
    void deveSanitizarCpfRemovendoPontosETracos() {
        assertEquals("52998224725", Cpf.sanitizar("529.982.247-25"));
        assertNull(Cpf.sanitizar(null));
    }
}

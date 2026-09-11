package br.edu.safeplace.backend.domain.usuario;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Garante, de forma executável, o critério de aceite da issue #90:
 * "O modelo de Colaborador não possui senha, token ou qualquer dado de login".
 */
class ColaboradorSemCredenciaisTest {

    private static final List<String> TERMOS_DE_CREDENCIAL = List.of("senha", "password", "hash", "token", "login");

    @Test
    void colaboradorNaoDeveDeclararCampoRelacionadoACredencial() {
        List<String> campos = Arrays.stream(Colaborador.class.getDeclaredFields()).map(Field::getName).toList();

        for (String termo : TERMOS_DE_CREDENCIAL) {
            assertTrue(campos.stream().noneMatch(c -> c.toLowerCase(Locale.ROOT).contains(termo)),
                    "Colaborador não pode ter campo relacionado a '" + termo + "'. Campos: " + campos);
        }
    }

    @Test
    void colaboradorNaoDeveDeclararMetodoRelacionadoACredencial() {
        List<String> metodos = Arrays.stream(Colaborador.class.getDeclaredMethods()).map(Method::getName).toList();

        for (String termo : TERMOS_DE_CREDENCIAL) {
            assertTrue(metodos.stream().noneMatch(m -> m.toLowerCase(Locale.ROOT).contains(termo)),
                    "Colaborador não pode ter método relacionado a '" + termo + "'. Métodos: " + metodos);
        }
    }

    @Test
    void perfilDoColaboradorNaoDeveExigirCredenciais() {
        Colaborador colaborador = Colaborador.novo("Carlos Souza", "52998224725", LocalDate.of(1995, 3, 10), null);

        assertFalse(colaborador.getPerfil().exigeCredenciais());
    }

    @Test
    void colaboradorDeveSerCriadoSemEmailPoisNaoPossuiConta() {
        Colaborador colaborador = Colaborador.novo("Carlos Souza", "52998224725", LocalDate.of(1995, 3, 10), "  ");

        assertNull(colaborador.getEmail());
    }
}

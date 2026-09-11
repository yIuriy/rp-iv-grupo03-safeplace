package br.edu.safeplace.backend.adapters.out.seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenAdaptadorTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 604800000L; // 7 dias

    private JwtTokenAdaptador adaptador;

    @BeforeEach
    void setUp() {
        adaptador = new JwtTokenAdaptador(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido e extrair claims de email e perfil")
    void deveGerarETrazerClaimsValidas() {
        String email = "gestor@safeplace.com";
        String perfil = "GESTOR_SEGURANCA";

        String token = adaptador.gerarToken(email, perfil);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(adaptador.validarToken(token));
        assertEquals(email, adaptador.extrairEmail(token));
        assertEquals(perfil, adaptador.extrairPerfil(token));
    }

    @Test
    @DisplayName("Deve rejeitar token inválido ou adulterado")
    void deveRejeitarTokenInvalido() {
        assertFalse(adaptador.validarToken("token.totalmente.invalido"));
        assertFalse(adaptador.validarToken(null));
        assertFalse(adaptador.validarToken(""));

        String tokenValido = adaptador.gerarToken("supervisor@safeplace.com", "SUPERVISOR");
        String tokenAdulterado = tokenValido + "tampered";
        assertFalse(adaptador.validarToken(tokenAdulterado));
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void deveInvalidarTokenExpirado() {
        // Adaptador com expiração negativa para gerar token já expirado
        JwtTokenAdaptador adaptadorExpirado = new JwtTokenAdaptador(SECRET, -1000L);
        String token = adaptadorExpirado.gerarToken("gestor@safeplace.com", "GESTOR_SEGURANCA");

        assertFalse(adaptador.validarToken(token));
    }
}

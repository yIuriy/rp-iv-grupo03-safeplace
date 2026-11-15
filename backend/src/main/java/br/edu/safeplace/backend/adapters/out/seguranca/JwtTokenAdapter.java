package br.edu.safeplace.backend.adapters.out.seguranca;

import br.edu.safeplace.backend.application.port.out.GerenciadorToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenAdapter implements GerenciadorToken {

    /** O HS256 exige chave de 256 bits (32 bytes) ou mais. */
    static final int TAMANHO_MINIMO_CHAVE_BYTES = 32;

    private final SecretKey chaveAssinatura;
    private final long jwtExpirationMs;

    /**
     * Issue #309: a chave vem só da variável de ambiente {@code JWT_SECRET}, sem valor padrão no código.
     * A chave é montada aqui para que uma chave ausente ou curta impeça o backend de subir.
     */
    public JwtTokenAdapter(
            @Value("${jwt.secret:}") String jwtSecret,
            @Value("${jwt.expiration-ms:604800000}") long jwtExpirationMs) {
        this.chaveAssinatura = criarChaveAssinatura(jwtSecret);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Override
    public String gerar(String email, String perfil) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("perfil", perfil)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(chaveAssinatura, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String extrairEmail(String token) {
        return obterClaims(token).getSubject();
    }

    @Override
    public String extrairPerfil(String token) {
        return obterClaims(token).get("perfil", String.class);
    }

    @Override
    public boolean validar(String token) {
        try {
            Jwts.parser()
                    .verifyWith(chaveAssinatura)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims obterClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveAssinatura)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey criarChaveAssinatura(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("A variável de ambiente JWT_SECRET não foi definida. "
                    + "Gere uma chave com 'openssl rand -base64 32' e defina JWT_SECRET (veja o .env.example).");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (DecodingException e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < TAMANHO_MINIMO_CHAVE_BYTES) {
            throw new IllegalStateException("JWT_SECRET tem " + keyBytes.length + " bytes, e o mínimo é "
                    + TAMANHO_MINIMO_CHAVE_BYTES + " bytes (256 bits). Gere uma chave com 'openssl rand -base64 32'.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

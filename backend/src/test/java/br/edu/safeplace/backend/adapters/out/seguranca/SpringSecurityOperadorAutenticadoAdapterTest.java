package br.edu.safeplace.backend.adapters.out.seguranca;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpringSecurityOperadorAutenticadoAdapterTest {

    private SpringSecurityOperadorAutenticadoAdapter adaptador;

    @BeforeEach
    void setUp() {
        adaptador = new SpringSecurityOperadorAutenticadoAdapter();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obterIdentificadorOperadorDeveRetornarVazioQuandoNaoHouverAutenticacao() {
        Optional<String> operador = adaptador.obterIdentificador();
        assertTrue(operador.isEmpty());
    }

    @Test
    void obterIdentificadorOperadorDeveRetornarVazioQuandoNomeForNuloOuEmBranco() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("   ", "senha");
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Optional<String> operador = adaptador.obterIdentificador();
        assertTrue(operador.isEmpty());
    }

    @Test
    void obterIdentificadorOperadorDeveDescartarAnonymousUser() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new AnonymousAuthenticationToken(
                "chave",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Optional<String> operador = adaptador.obterIdentificador();
        assertTrue(operador.isEmpty(), "anonymousUser deve ser filtrado retornando vazio");
    }

    @Test
    void obterIdentificadorOperadorDeveDescartarAnonymousUserMesmoComEspacosOuMaiusculas() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("  ANONYMOUSUSER  ", "senha");
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Optional<String> operador = adaptador.obterIdentificador();
        assertTrue(operador.isEmpty());
    }

    @Test
    void obterIdentificadorOperadorDeveRetornarNomeQuandoAutenticado() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("  gestor@empresa.com  ", "senha");
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        Optional<String> operador = adaptador.obterIdentificador();
        assertTrue(operador.isPresent());
        assertEquals("gestor@empresa.com", operador.get());
    }

    @Test
    void possuiPapelDeveRetornarFalseParaPapelNuloOuEmBranco() {
        assertFalse(adaptador.possuiPapel(null));
        assertFalse(adaptador.possuiPapel(""));
        assertFalse(adaptador.possuiPapel("   "));
    }

    @Test
    void possuiPapelDeveRetornarFalseSemAutenticacao() {
        assertFalse(adaptador.possuiPapel("GESTOR_SEGURANCA"));
    }

    @Test
    void possuiPapelDeveRetornarFalseQuandoNaoAutenticado() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "gestor@empresa.com",
                "senha"
        );
        // auth com construtor de 2 argumentos tem isAuthenticated = false
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertFalse(adaptador.possuiPapel("GESTOR_SEGURANCA"));
    }

    @Test
    void possuiPapelDeveRetornarFalseParaAnonymousUser() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new AnonymousAuthenticationToken(
                "chave",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertFalse(adaptador.possuiPapel("ANONYMOUS"));
        assertFalse(adaptador.possuiPapel("ROLE_ANONYMOUS"));
    }

    @Test
    void possuiPapelDeveValidarPapelComOuSemPrefixoRole() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "gestor@empresa.com",
                "senha",
                List.of(new SimpleGrantedAuthority("ROLE_GESTOR_SEGURANCA"))
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertTrue(adaptador.possuiPapel("GESTOR_SEGURANCA"));
        assertTrue(adaptador.possuiPapel("ROLE_GESTOR_SEGURANCA"));
        assertFalse(adaptador.possuiPapel("SUPERVISOR"));
        assertFalse(adaptador.possuiPapel("ROLE_SUPERVISOR"));
    }
}

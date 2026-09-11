package br.edu.safeplace.backend.adapters.out.seguranca;

import br.edu.safeplace.backend.application.port.out.TokenPorta;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final TokenPorta tokenPorta;

    public JwtFiltroAutenticacao(TokenPorta tokenPorta) {
        this.tokenPorta = tokenPorta;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (tokenPorta.validarToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = tokenPorta.extrairEmail(token);
                String perfil = tokenPorta.extrairPerfil(token);

                if (email != null && perfil != null) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + perfil);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

package br.edu.safeplace.backend.adapters.out.seguranca;

import br.edu.safeplace.backend.application.port.out.OperadorAutenticadoPorta;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityOperadorAutenticadoAdaptador implements OperadorAutenticadoPorta {

    private static final String USUARIO_ANONIMO = "anonymousUser";

    @Override
    public Optional<String> obterIdentificadorOperador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            String identificador = auth.getName().trim();
            if (USUARIO_ANONIMO.equalsIgnoreCase(identificador)) {
                return Optional.empty();
            }
            return Optional.of(identificador);
        }
        return Optional.empty();
    }

    @Override
    public boolean possuiPapel(String papel) {
        if (papel == null || papel.isBlank()) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getName() != null && USUARIO_ANONIMO.equalsIgnoreCase(auth.getName().trim())) {
            return false;
        }
        String papelNormalizado = papel.startsWith("ROLE_") ? papel : "ROLE_" + papel;
        return auth.getAuthorities().stream()
                .anyMatch(authority -> papelNormalizado.equals(authority.getAuthority()));
    }
}

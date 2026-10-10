package br.edu.safeplace.backend.adapters.out.seguranca;

import br.edu.safeplace.backend.application.port.out.OperadorAutenticadoPorta;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityOperadorAutenticadoAdaptador implements OperadorAutenticadoPorta {

    @Override
    public Optional<String> obterIdentificadorOperador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return Optional.of(auth.getName().trim());
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
        String papelNormalizado = papel.startsWith("ROLE_") ? papel : "ROLE_" + papel;
        return auth.getAuthorities().stream()
                .anyMatch(authority -> papelNormalizado.equals(authority.getAuthority()));
    }
}

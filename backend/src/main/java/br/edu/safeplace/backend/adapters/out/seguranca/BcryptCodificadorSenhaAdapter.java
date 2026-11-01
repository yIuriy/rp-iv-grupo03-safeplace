package br.edu.safeplace.backend.adapters.out.seguranca;

import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptCodificadorSenhaAdapter implements CodificadorSenha {
    private final PasswordEncoder passwordEncoder;

    public BcryptCodificadorSenhaAdapter() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public BcryptCodificadorSenhaAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String senha) {
        if (senha == null) {
            return null;
        }
        return passwordEncoder.encode(senha);
    }

    @Override
    public boolean validar(String senhaPura, String senhaCodificada) {
        if (senhaPura == null || senhaCodificada == null) {
            return false;
        }
        return passwordEncoder.matches(senhaPura, senhaCodificada);
    }
}

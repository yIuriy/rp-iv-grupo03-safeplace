package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.LoginEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.TokenSaidaDTO;

public interface AutenticarUsuarioCasoDeUso {
    TokenSaidaDTO autenticar(LoginEntradaDTO entrada);
}

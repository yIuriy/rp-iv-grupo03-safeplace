package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.LoginInputDTO;
import br.edu.safeplace.backend.application.dto.output.TokenOutputDTO;

public interface AutenticarUsuarioUseCase {
    TokenOutputDTO autenticar(LoginInputDTO entrada);
}

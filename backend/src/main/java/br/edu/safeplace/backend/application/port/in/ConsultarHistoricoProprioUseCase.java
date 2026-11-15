/*
 * Porta de entrada do histórico do próprio perfil (RNF05): lista as ações que a pessoa autenticada
 * registrou, das mais recentes para as mais antigas.
 */
package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.output.AcaoUsuarioOutputDTO;

public interface ConsultarHistoricoProprioUseCase {

    int LIMITE_PADRAO = 20;
    int LIMITE_MAXIMO = 100;

    /**
     * Ações registradas com o e-mail informado como responsável. O limite fica entre 1 e
     * {@link #LIMITE_MAXIMO}; valores fora dessa faixa são ajustados para o mais próximo.
     */
    List<AcaoUsuarioOutputDTO> listar(String email, int limite);
}

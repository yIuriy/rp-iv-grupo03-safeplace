package br.edu.safeplace.backend.validacao.ocorrencia;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida a ocorrência antes de gravar a notificação do novo relato ao Gestor (UC09). Antes da issue
 * #266, ficava em {@code NotificacaoOcorrencia.deNovoRelato}. É chamado pelo adaptador de
 * notificação, que monta e grava a notificação.
 */
@Component
public class NotificacaoOcorrenciaValidador extends ValidadorEmCadeia<Ocorrencia> {

    public NotificacaoOcorrenciaValidador() {
        super(exigir(o -> o == null || o.getId() == null, "A notificação exige uma ocorrência já registrada."));
    }
}

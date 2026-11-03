package br.edu.safeplace.backend.validacao.ocorrencia;

import static br.edu.safeplace.backend.validacao.RegraElo.exigirComMensagem;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import br.edu.safeplace.backend.validacao.RegraElo;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o formato do protocolo CAT guardado no acidente (issue #230). O elo do formato é o mesmo
 * da cadeia do {@link OcorrenciaValidador} (issue #266); este validador o usa sozinho onde só o
 * protocolo muda, como na consolidação da CAT de acidentes antigos, sem reaplicar as regras do
 * relato a registros que já estão no banco.
 */
@Component
public class ProtocoloCATValidador extends ValidadorEmCadeia<Acidente> {

    public ProtocoloCATValidador() {
        super(eloFormato());
    }

    /**
     * Elo "Formato de protocolo CAT inválido". O protocolo é opcional até ser emitido; quando existe,
     * precisa seguir CAT-AAAA-MM-NNNN. Consulta o Value Object guardado no acidente, sem recriá-lo a
     * partir do texto. Só se aplica a acidentes; incidentes não têm protocolo.
     */
    public static <T extends Ocorrencia> RegraElo<T> eloFormato() {
        return exigirComMensagem(ProtocoloCATValidador::protocoloInvalido,
                o -> "Formato de protocolo CAT inválido: " + ((Acidente) o).getNumeroProtocoloCAT(),
                ProtocoloCATInvalidoException::new);
    }

    private static boolean protocoloInvalido(Ocorrencia ocorrencia) {
        if (!(ocorrencia instanceof Acidente acidente)) {
            return false;
        }
        NumeroProtocoloCAT protocolo = acidente.getProtocoloCAT();
        return protocolo != null && !protocolo.formatoValido();
    }
}

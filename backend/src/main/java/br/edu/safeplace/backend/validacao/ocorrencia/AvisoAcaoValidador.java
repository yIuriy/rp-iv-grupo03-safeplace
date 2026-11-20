package br.edu.safeplace.backend.validacao.ocorrencia;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.RegraElo;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o aviso de uma ação antes de gravá-lo como notificação (UC07, regra 2 e alternativo II).
 * Antes da issue #266, ficava em {@code NotificacaoAcao.deAviso}. É chamado pelo adaptador de
 * notificação, que monta e grava a notificação.
 */
@Component
public class AvisoAcaoValidador extends ValidadorEmCadeia<AvisoAcao> {

    public AvisoAcaoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<AvisoAcao> montarCadeia() {
        EloValidacao<AvisoAcao> primeiro = RegraElo.<AvisoAcao>exigir(
                        a -> a == null || a.acao() == null || a.idAcao() == null,
                        "O aviso exige uma ação já registrada.").interrompendo();
        primeiro.encadear(exigir(a -> a.prazo() == null || a.dataAviso() == null,
                        "O aviso exige o prazo da ação e a data do aviso."))
                .encadear(exigir(a -> (a.responsavelColaborador() == null) == (a.responsavelSetor() == null),
                        "O aviso exige um único responsável: colaborador ou setor."));
        return primeiro;
    }
}

package br.edu.safeplace.backend.validacao.aptidao;

import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida se o colaborador pode ser alocado na tarefa (RF06, RF10, UC02, UC11, US06, US10).
 *
 * <p>A mesma cadeia atende a consulta de aptidão ({@link #verificar}, que lista os impedimentos)
 * e a alocação ({@link #validar}, que bloqueia). Antes da issue #228, as duas usavam cópias
 * diferentes das mesmas regras.</p>
 *
 * <p>Pendências de capacitação são reunidas em {@link AlocacaoBloqueadaException}. Colaborador sem
 * perfil alocável e tarefa sem classificação têm exceção própria, lançada primeiro.</p>
 */
@Component
public class AptidaoColaboradorValidador extends ValidadorEmCadeia<AptidaoParaTarefa> {

    public AptidaoColaboradorValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<AptidaoParaTarefa> montarCadeia() {
        EloValidacao<AptidaoParaTarefa> primeiro = new DadosInformadosElo();
        primeiro.encadear(new ColaboradorAlocavelElo())
                .encadear(new TarefaClassificadaElo())
                .encadear(new ExigenciasCapacitacaoElo());
        return primeiro;
    }

    @Override
    protected RuntimeException excecaoPadrao(List<String> impedimentos) {
        return new AlocacaoBloqueadaException(impedimentos);
    }
}

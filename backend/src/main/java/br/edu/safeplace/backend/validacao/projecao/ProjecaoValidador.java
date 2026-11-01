package br.edu.safeplace.backend.validacao.projecao;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Pré-condições da projeção de substituição (UC10, exceção I; DS10, 2.1 e 2.3). Antes da issue
 * #267, eram 4 blocos de {@code if} em sequência dentro de {@code calcularProjecao}. A ordem dos
 * elos repete a ordem antiga, então a primeira pendência lançada é a mesma.
 */
@Component
public class ProjecaoValidador extends ValidadorEmCadeia<ContextoProjecao> {

    public ProjecaoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<ContextoProjecao> montarCadeia() {
        EloValidacao<ContextoProjecao> primeiro = new EpiInformadoElo();
        primeiro.encadear(new DurabilidadeNominalElo())
                .encadear(new PericulosidadeSetorElo())
                .encadear(new CertificadoAprovacaoElo())
                .encadear(new HistoricoEmprestimosElo());
        return primeiro;
    }
}

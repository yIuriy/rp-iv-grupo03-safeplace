package br.edu.safeplace.backend.validacao.relatorio;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o período de um relatório estatístico (RF18, issue #299), na mesma cadeia usada pelos demais
 * validadores (Chain of Responsibility, #228). Sem as duas datas a cadeia é interrompida, porque as
 * regras seguintes comparam as datas.
 *
 * <p>Um ano é o máximo: os relatórios do requisito são mensais ou anuais, e o relatório de acidentes
 * tem uma linha por mês.</p>
 */
@Component
public class PeriodoRelatorioValidador extends ValidadorEmCadeia<PeriodoRelatorio> {

    public PeriodoRelatorioValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<PeriodoRelatorio> montarCadeia() {
        EloValidacao<PeriodoRelatorio> primeiro = exigir(
                (PeriodoRelatorio p) -> p.inicio() == null || p.fim() == null,
                "Informe as datas inicial e final do período do relatório.").interrompendo();
        primeiro.encadear(exigir(p -> p.inicio().isAfter(p.fim()),
                        "A data inicial do período não pode ser posterior à data final."))
                .encadear(exigir(p -> p.fim().isAfter(p.inicio().plusYears(1).minusDays(1)),
                        "O período do relatório pode ter no máximo um ano."));
        return primeiro;
    }
}

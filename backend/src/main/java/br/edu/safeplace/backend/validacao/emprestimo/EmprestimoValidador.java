package br.edu.safeplace.backend.validacao.emprestimo;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o empréstimo antes de gravá-lo (RF11, UC12). Antes da issue #265, as 11 regras ficavam no
 * construtor de {@code Emprestimo}. A devolução continua sendo regra da operação
 * {@code Emprestimo.devolver}, que encerra um empréstimo ativo.
 */
@Component
public class EmprestimoValidador extends ValidadorEmCadeia<Emprestimo> {

    public EmprestimoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<Emprestimo> montarCadeia() {
        EloValidacao<Emprestimo> primeiro = exigir(e -> e.getEpiId() == null, "EPI é obrigatório.");
        primeiro.encadear(exigir(e -> e.getColaboradorId() == null, "Colaborador é obrigatório."))
                .encadear(exigir(e -> e.getResponsavelEntregaId() == null, "Responsável pela entrega é obrigatório."))
                .encadear(exigir(e -> e.getDataRetirada() == null, "Data e hora da retirada são obrigatórias."))
                .encadear(exigir(e -> e.getDataDevolucaoPrevista() == null, "Data prevista de devolução é obrigatória."))
                .encadear(exigir(e -> e.getDataRetirada() != null && e.getDataDevolucaoPrevista() != null
                                && e.getDataDevolucaoPrevista().isBefore(e.getDataRetirada().toLocalDate()),
                        "Data prevista de devolução não pode ser anterior à retirada."))
                .encadear(exigir(e -> isBlankOrNull(e.getTermoCautela()), "Termo de cautela é obrigatório."))
                .encadear(exigir(e -> e.getStatus() == null, "Status do empréstimo é obrigatório."))
                .encadear(exigir(e -> e.getStatus() != null
                                && devolvido(e) != (e.getStatus() == ClassificacaoEmprestimo.DISPONIVEL),
                        "Empréstimo devolvido deve estar DISPONIVEL; empréstimo ativo não pode ter devolução."))
                .encadear(exigir(e -> devolvido(e) && e.getEstadoDevolucao() == null, "Estado na devolução é obrigatório."))
                .encadear(exigir(e -> devolvido(e) && e.getResponsavelDevolucaoId() == null,
                        "Responsável pela devolução é obrigatório."));
        return primeiro;
    }

    private static boolean devolvido(Emprestimo emprestimo) {
        return emprestimo.getDataDevolucaoReal() != null;
    }
}

package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;

/**
 * Dados de entrada da projeção de substituição de um EPI (DS10.1). Substitui os 5 parâmetros
 * soltos de {@code calcularProjecao} (issue #267).
 *
 * @param periculosidadeSetor grau de perigo do setor onde o EPI opera
 * @param emprestimos histórico de empréstimos do EPI; nulo vale como lista vazia
 * @param dataReferencia data base da projeção; nula vale como hoje
 * @param diasAmostragem período informado pelo Gestor, em dias; nulo ou não positivo usa o período
 *                       da primeira retirada até a data de referência
 */
public record ContextoProjecao(Epi epi, NivelPerigo periculosidadeSetor, List<Emprestimo> emprestimos,
                               LocalDate dataReferencia, Integer diasAmostragem) {

    public ContextoProjecao {
        emprestimos = emprestimos != null ? List.copyOf(emprestimos) : List.of();
        dataReferencia = dataReferencia != null ? dataReferencia : LocalDate.now();
    }

    /** Identificador usado nas pendências e na projeção. */
    public Integer epiId() {
        return epi.getId() != null ? epi.getId() : epi.getCodigoEPI();
    }

    /** Empréstimos com retirada registrada até a data de referência. */
    public List<Emprestimo> historico() {
        return emprestimos.stream()
                .filter(emp -> emp.getDataRetirada() != null
                        && !emp.getDataRetirada().toLocalDate().isAfter(dataReferencia))
                .toList();
    }
}

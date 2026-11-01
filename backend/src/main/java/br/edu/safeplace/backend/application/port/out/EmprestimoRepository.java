/*
 * Porta de saída de persistência de empréstimos (RF11, UC12). O adaptador JPA guarda o status base
 * (INDISPONIVEL ou DISPONIVEL); o atraso é calculado pelo domínio na consulta.
 */
package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;

public interface EmprestimoRepository {

    Emprestimo salvar(Emprestimo emprestimo);

    Optional<Emprestimo> buscarPorId(Integer id);

    default Optional<Emprestimo> buscarPorIdComLock(Integer id) {
        return buscarPorId(id);
    }

    List<Emprestimo> listarTodos();

    List<Emprestimo> listarPorColaborador(Integer colaboradorId);

    /** Empréstimos ainda não devolvidos do colaborador, para a verificação de duplicidade (UC12, exceção III). */
    List<Emprestimo> listarAtivosPorColaborador(Integer colaboradorId);

    /** Lista todos os empréstimos (ativos e encerrados) vinculados a um EPI para cálculo de frequência de uso (RF21, UC10). */
    default List<Emprestimo> listarPorEpi(Integer epiId) {
        if (epiId == null) {
            return List.of();
        }
        return listarTodos().stream().filter(e -> epiId.equals(e.getEpiId())).toList();
    }
}

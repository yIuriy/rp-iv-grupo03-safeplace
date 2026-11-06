/*
 * Adaptador JPA da porta EmprestimoRepository: delega ao repositório JPA da própria entidade de domínio
 * Emprestimo (#260), sem classe espelhada.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.EmprestimoRepository;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;

@Repository
public class EmprestimoJpaAdapter implements EmprestimoRepository {
    private final EmprestimoJpaRepository repositorio;

    public EmprestimoJpaAdapter(EmprestimoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Emprestimo salvar(Emprestimo emprestimo) {
        return repositorio.save(emprestimo);
    }

    @Override
    public Optional<Emprestimo> buscarPorId(Integer id) {
        return repositorio.findById(id);
    }

    @Override
    public Optional<Emprestimo> buscarPorIdComLock(Integer id) {
        return repositorio.findByIdWithLock(id);
    }

    @Override
    public List<Emprestimo> listarTodos() {
        return repositorio.findAllByOrderByDataRetiradaDesc();
    }

    @Override
    public List<Emprestimo> listarPorColaborador(Integer colaboradorId) {
        return repositorio.listarPorColaborador(colaboradorId);
    }

    @Override
    public List<Emprestimo> listarAtivosPorColaborador(Integer colaboradorId) {
        return repositorio.listarPorColaboradorEStatus(colaboradorId, ClassificacaoEmprestimo.INDISPONIVEL);
    }

    @Override
    public List<Emprestimo> listarPorEpi(Integer epiId) {
        return repositorio.listarPorEpi(epiId);
    }
}

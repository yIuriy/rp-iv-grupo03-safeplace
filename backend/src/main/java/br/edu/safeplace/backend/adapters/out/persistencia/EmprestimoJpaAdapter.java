/*
 * Adaptador JPA da porta EmprestimoRepositoryPort: converte entre Emprestimo (domínio) e
 * EmprestimoEntity (tabela emprestimos).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.EmprestimoRepositoryPort;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;

@Repository
public class EmprestimoJpaAdapter implements EmprestimoRepositoryPort {
    private final EmprestimoJpaRepository repositorio;

    public EmprestimoJpaAdapter(EmprestimoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Emprestimo salvar(Emprestimo emprestimo) {
        return paraDominio(repositorio.save(paraEntidade(emprestimo)));
    }

    @Override
    public Optional<Emprestimo> buscarPorId(Integer id) {
        return repositorio.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Emprestimo> listarTodos() {
        return repositorio.findAllByOrderByDataRetiradaDesc().stream().map(this::paraDominio).toList();
    }

    @Override
    public List<Emprestimo> listarPorColaborador(Integer colaboradorId) {
        return repositorio.findByColaboradorIdOrderByDataRetiradaDesc(colaboradorId).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public List<Emprestimo> listarAtivosPorColaborador(Integer colaboradorId) {
        return repositorio.findByColaboradorIdAndStatus(colaboradorId, ClassificacaoEmprestimo.INDISPONIVEL.name())
                .stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public List<Emprestimo> listarPorEpi(Integer epiId) {
        return repositorio.findByEpiIdOrderByDataRetiradaDesc(epiId)
                .stream()
                .map(this::paraDominio)
                .toList();
    }

    private EmprestimoEntity paraEntidade(Emprestimo dominio) {
        return new EmprestimoEntity(
                dominio.getId(),
                dominio.getEpiId(),
                dominio.getColaboradorId(),
                dominio.getResponsavelEntregaId(),
                dominio.getDataRetirada(),
                dominio.getDataDevolucaoPrevista(),
                dominio.getDataDevolucaoReal(),
                dominio.getResponsavelDevolucaoId(),
                dominio.getEstadoDevolucao() != null ? dominio.getEstadoDevolucao().name() : null,
                dominio.getStatus().name(),
                dominio.getJustificativaSubstituicao(),
                dominio.getTermoCautela());
    }

    private Emprestimo paraDominio(EmprestimoEntity entidade) {
        return new Emprestimo(
                entidade.getId(),
                entidade.getEpiId(),
                entidade.getColaboradorId(),
                entidade.getResponsavelEntregaId(),
                entidade.getDataRetirada(),
                entidade.getDataDevolucaoPrevista(),
                entidade.getTermoCautela(),
                entidade.getJustificativaSubstituicao(),
                entidade.getDataDevolucaoReal(),
                entidade.getResponsavelDevolucaoId(),
                entidade.getEstadoDevolucao() != null ? EstadoDevolucao.valueOf(entidade.getEstadoDevolucao()) : null,
                ClassificacaoEmprestimo.valueOf(entidade.getStatus()));
    }
}

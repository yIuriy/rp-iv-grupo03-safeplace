package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;

public interface CertificacaoJpaRepository extends JpaRepository<Certificacao, Integer> {

    @Override
    @EntityGraph(attributePaths = {"colaborador"})
    Optional<Certificacao> findById(Integer id);

    @EntityGraph(attributePaths = {"colaborador"})
    List<Certificacao> findByColaborador_IdOrderByDataVencimentoAsc(Integer colaboradorId);

    @EntityGraph(attributePaths = {"colaborador"})
    List<Certificacao> findAllByOrderByDataVencimentoAsc();
}

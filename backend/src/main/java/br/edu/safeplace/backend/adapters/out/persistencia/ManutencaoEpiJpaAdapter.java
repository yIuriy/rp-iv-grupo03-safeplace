package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;

/**
 * Adaptador JPA da porta ManutencaoEpiRepository: delega ao repositório JPA da própria entidade de
 * domínio ManutencaoEpi (#259), sem classe espelhada.
 */
@Repository
public class ManutencaoEpiJpaAdapter implements ManutencaoEpiRepository {
    private final ManutencaoEpiJpaRepository repository;

    public ManutencaoEpiJpaAdapter(ManutencaoEpiJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ManutencaoEpi salvar(ManutencaoEpi manutencao) {
        return repository.save(manutencao);
    }

    @Override
    public Optional<ManutencaoEpi> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<ManutencaoEpi> listarPorEpi(Integer epiId) {
        return repository.listarPorEpi(epiId);
    }
}

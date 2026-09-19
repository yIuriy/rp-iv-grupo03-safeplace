package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ManutencaoEpiJpaAdapter implements ManutencaoEpiRepositoryPort {
    private final ManutencaoEpiJpaRepository repository;

    public ManutencaoEpiJpaAdapter(ManutencaoEpiJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ManutencaoEpi salvar(ManutencaoEpi manutencao) {
        return toDomain(repository.save(toEntity(manutencao)));
    }

    @Override
    public Optional<ManutencaoEpi> buscarPorId(Integer id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ManutencaoEpi> listarPorEpiId(Integer epiId) {
        return repository.findByEpiIdOrderByDataManutencaoDesc(epiId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ManutencaoEpiEntity toEntity(ManutencaoEpi manutencao) {
        return new ManutencaoEpiEntity(manutencao.getId(), manutencao.getEpiId(),
                manutencao.getDataManutencao(), manutencao.getTipoManutencao() == null
                        ? null : manutencao.getTipoManutencao().name(),
                manutencao.getDescricao(), manutencao.getResultado().name(),
                manutencao.getResponsavelManutencao(), manutencao.getResponsavelId());
    }

    private ManutencaoEpi toDomain(ManutencaoEpiEntity entity) {
        return new ManutencaoEpi(entity.getId(), entity.getEpiId(), entity.getDataManutencao(),
                entity.getTipoManutencao() == null ? null : TipoManutencao.valueOf(entity.getTipoManutencao()),
                entity.getDescricao(),
                ResultadoManutencao.valueOf(entity.getResultado()), entity.getResponsavelManutencao(),
                entity.getResponsavelId());
    }
}

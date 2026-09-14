package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;

@Repository
public class AreaRiscoJpaAdapter implements AreaRiscoRepositoryPort {

    private final AreaRiscoJpaRepository areaRiscoRepository;
    private final EpiJpaRepository epiRepository;

    public AreaRiscoJpaAdapter(AreaRiscoJpaRepository areaRiscoRepository, EpiJpaRepository epiRepository) {
        this.areaRiscoRepository = areaRiscoRepository;
        this.epiRepository = epiRepository;
    }

    @Override
    public AreaRisco salvar(AreaRisco areaRisco) {
        return toDomain(areaRiscoRepository.save(toEntity(areaRisco)));
    }

    @Override
    public Optional<AreaRisco> buscarPorId(Integer id) {
        return areaRiscoRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<AreaRisco> buscarPorCodigo(String codigo) {
        return areaRiscoRepository.findByCodigo(codigo).map(this::toDomain);
    }

    @Override
    public List<AreaRisco> listarTodas() {
        return areaRiscoRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<AreaRisco> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        return areaRiscoRepository.findByNivelPerigo(nivelPerigo.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private AreaRiscoEntity toEntity(AreaRisco domain) {
        return new AreaRiscoEntity(
                domain.getId(),
                domain.getCodigo(),
                domain.getNome(),
                domain.getDescricao(),
                domain.getNivelPerigo() == null ? null : domain.getNivelPerigo().name(),
                carregarEpisGerenciados(domain));
    }

    /**
     * Os EPIs vinculados já existem no catálogo: carrega as instâncias gerenciadas pelo id em vez
     * de reconstruí-las, para que a gravação da área toque apenas a tabela de junção.
     */
    private List<EpiEntity> carregarEpisGerenciados(AreaRisco domain) {
        List<Integer> ids = domain.verificarEpisObrigatoriosArea().stream()
                .map(Epi::getId)
                .filter(Objects::nonNull)
                .toList();
        return ids.isEmpty() ? List.of() : epiRepository.findAllById(ids);
    }

    private AreaRisco toDomain(AreaRiscoEntity entity) {
        return new AreaRisco(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getNivelPerigo() == null ? null : NivelPerigo.valueOf(entity.getNivelPerigo()),
                entity.getEpisObrigatorios().stream()
                        .map(EpiEntityMapper::toDomain)
                        .toList());
    }
}

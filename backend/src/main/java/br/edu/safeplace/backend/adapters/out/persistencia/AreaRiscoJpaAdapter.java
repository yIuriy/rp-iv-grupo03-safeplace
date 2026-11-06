package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;

@Repository
public class AreaRiscoJpaAdapter implements AreaRiscoRepository {

    private final AreaRiscoJpaRepository areaRiscoRepository;
    private final EpiJpaRepository epiRepository;

    public AreaRiscoJpaAdapter(AreaRiscoJpaRepository areaRiscoRepository, EpiJpaRepository epiRepository) {
        this.areaRiscoRepository = areaRiscoRepository;
        this.epiRepository = epiRepository;
    }

    @Override
    public AreaRisco salvar(AreaRisco areaRisco) {
        List<Epi> epis = areaRisco.getEpisObrigatorios();
        AreaRisco salva = areaRiscoRepository.save(areaRisco);
        if (epis != null && !epis.isEmpty()) {
            salva.definirEpisObrigatorios(epis);
            return salva;
        }
        return hidratarEpis(salva);
    }

    @Override
    public AreaRisco atualizar(AreaRisco areaRisco) {
        return salvar(areaRisco);
    }

    @Override
    public Optional<AreaRisco> buscarPorId(Integer id) {
        return areaRiscoRepository.findById(id).map(this::hidratarEpis);
    }

    @Override
    public Optional<AreaRisco> buscarPorCodigo(String codigo) {
        return areaRiscoRepository.findByCodigo(codigo).map(this::hidratarEpis);
    }

    @Override
    public List<AreaRisco> listarTodas() {
        return areaRiscoRepository.findAll().stream().map(this::hidratarEpis).toList();
    }

    @Override
    public List<AreaRisco> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        return areaRiscoRepository.findByNivelPerigo(nivelPerigo).stream()
                .map(this::hidratarEpis)
                .toList();
    }

    /** Ponte transicional até a issue #258 tornar Epi uma @Entity. */
    private AreaRisco hidratarEpis(AreaRisco area) {
        if (area == null || epiRepository == null) {
            return area;
        }
        Set<Integer> ids = area.getEpisObrigatoriosIds();
        if (ids == null || ids.isEmpty()) {
            area.definirEpisObrigatorios(List.of());
            return area;
        }
        List<Epi> epis = epiRepository.findAllById(ids);
        area.definirEpisObrigatorios(epis);
        return area;
    }
}

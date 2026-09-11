package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@ConditionalOnMissingBean(name = "manutencaoEpiJpaAdapter")
public class InMemoryManutencaoEpiRepository implements ManutencaoEpiRepositoryPort {
    private final Map<Integer, ManutencaoEpi> storage = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public ManutencaoEpi salvar(ManutencaoEpi manutencao) {
        Integer id = manutencao.getId() != null ? manutencao.getId() : sequence.getAndIncrement();
        ManutencaoEpi salvo = new ManutencaoEpi(
                id,
                manutencao.getEpiId(),
                manutencao.getDataManutencao(),
                manutencao.getTipoManutencao(),
                manutencao.getDescricao(),
                manutencao.getResultado(),
                manutencao.getResponsavelManutencao()
        );
        storage.put(id, salvo);
        return salvo;
    }

    @Override
    public Optional<ManutencaoEpi> buscarPorId(Integer id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ManutencaoEpi> listarPorEpiId(Integer epiId) {
        if (epiId == null) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(m -> epiId.equals(m.getEpiId()))
                .toList();
    }

    public void clear() {
        storage.clear();
        sequence.set(1);
    }
}

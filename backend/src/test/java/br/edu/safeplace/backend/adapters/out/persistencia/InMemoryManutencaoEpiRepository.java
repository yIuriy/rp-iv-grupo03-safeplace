package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dublê de teste da porta de manutenção. Não comprova persistência: a gravação real é coberta por
 * ManutencaoEpiPersistenciaIntegracaoTest (issue #197).
 */
public class InMemoryManutencaoEpiRepository implements ManutencaoEpiRepository {
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
                manutencao.getResponsavelManutencao(),
                manutencao.getResponsavelId()
        );
        storage.put(id, salvo);
        return salvo;
    }

    @Override
    public Optional<ManutencaoEpi> buscarPorId(Integer id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ManutencaoEpi> listarPorEpi(Integer epiId) {
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

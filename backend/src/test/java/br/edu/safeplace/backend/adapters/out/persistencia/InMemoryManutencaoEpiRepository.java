package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.test.util.ReflectionTestUtils;

import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;

/**
 * Dublê de teste da porta de manutenção. Não comprova persistência: a gravação real é coberta por
 * ManutencaoEpiPersistenciaIntegracaoTest (issue #197).
 */
public class InMemoryManutencaoEpiRepository implements ManutencaoEpiRepository {
    private final Map<Integer, ManutencaoEpi> storage = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public ManutencaoEpi salvar(ManutencaoEpi manutencao) {
        // Como o JPA, o banco em memória atribui o id gerado à própria entidade.
        if (manutencao.getId() == null) {
            ReflectionTestUtils.setField(manutencao, "id", sequence.getAndIncrement());
        }
        storage.put(manutencao.getId(), manutencao);
        return manutencao;
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

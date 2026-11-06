package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.domain.ocorrencia.SequenciaProtocoloCat;
import br.edu.safeplace.backend.domain.ocorrencia.SequenciaProtocoloCatId;

@Component
public class SequenciaProtocoloCatInicializador {

    private final SequenciaProtocoloCatSpringDataRepository repository;

    public SequenciaProtocoloCatInicializador(SequenciaProtocoloCatSpringDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Garante que a linha (ano, mes) existe na tabela sequencias_protocolo_cat.
     * Roda em transação isolada (REQUIRES_NEW) para que eventual concorrência no primeiro insert
     * não marque a transação principal como rollback-only.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void garantirLinhaCriada(Short ano, Short mes) {
        if (!repository.existsById(new SequenciaProtocoloCatId(ano, mes))) {
            try {
                repository.saveAndFlush(new SequenciaProtocoloCat(ano, mes, 0L));
            } catch (DataIntegrityViolationException ignored) {
                // Outra transação concorrente já criou o registro inicial no mesmo instante.
            }
        }
    }
}

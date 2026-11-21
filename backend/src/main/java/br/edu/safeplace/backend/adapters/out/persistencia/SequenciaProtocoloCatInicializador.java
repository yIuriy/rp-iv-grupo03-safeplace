package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.domain.ocorrencia.SequenciaProtocoloCat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class SequenciaProtocoloCatInicializador {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Cria a linha (ano, mes) da tabela sequencias_protocolo_cat com contador zero.
     * Roda em transação isolada (REQUIRES_NEW) para que eventual concorrência no primeiro insert
     * não marque a transação principal como rollback-only.
     *
     * <p>Usa {@code persist}, que sempre gera INSERT. O {@code save} do Spring Data faria {@code merge},
     * porque a chave é atribuída, e sobrescreveria com zero uma linha criada por outra transação no
     * mesmo instante. Com INSERT, essa corrida viola a chave primária, a transação isolada é desfeita e
     * o chamador ignora a exceção.</p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void garantirLinhaCriada(Short ano, Short mes) {
        entityManager.persist(new SequenciaProtocoloCat(ano, mes, 0L));
        entityManager.flush();
    }
}

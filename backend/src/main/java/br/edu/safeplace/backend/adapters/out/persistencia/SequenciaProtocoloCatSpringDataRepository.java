package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.ocorrencia.SequenciaProtocoloCat;
import br.edu.safeplace.backend.domain.ocorrencia.SequenciaProtocoloCatId;
import jakarta.persistence.LockModeType;

public interface SequenciaProtocoloCatSpringDataRepository
        extends JpaRepository<SequenciaProtocoloCat, SequenciaProtocoloCatId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SequenciaProtocoloCat s WHERE s.ano = :ano AND s.mes = :mes")
    Optional<SequenciaProtocoloCat> findByAnoAndMesComLock(@Param("ano") Short ano, @Param("mes") Short mes);
}

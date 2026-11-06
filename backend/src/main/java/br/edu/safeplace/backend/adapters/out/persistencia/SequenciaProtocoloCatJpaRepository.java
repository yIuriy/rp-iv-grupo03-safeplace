package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.domain.ocorrencia.SequenciaProtocoloCat;

/**
 * Emite o sequencial do protocolo CAT de um par ano/mês usando Spring Data JPA com bloqueio pessimista (#261).
 * Não utiliza JdbcTemplate nem SQL nativo: transações concorrentes são serializadas por @Lock(PESSIMISTIC_WRITE).
 */
@Repository
public class SequenciaProtocoloCatJpaRepository {

    private final SequenciaProtocoloCatSpringDataRepository springDataRepository;
    private final SequenciaProtocoloCatInicializador inicializador;

    public SequenciaProtocoloCatJpaRepository(
            SequenciaProtocoloCatSpringDataRepository springDataRepository,
            SequenciaProtocoloCatInicializador inicializador) {
        this.springDataRepository = springDataRepository;
        this.inicializador = inicializador;
    }

    /**
     * Reserva e devolve o próximo sequencial do par ano/mês informado com bloqueio pessimista de escrita.
     */
    @Transactional
    public long reservarProximoSequencial(int ano, int mes) {
        Short a = (short) ano;
        Short m = (short) mes;

        var sequenciaOpt = springDataRepository.findByAnoAndMesComLock(a, m);
        if (sequenciaOpt.isEmpty()) {
            inicializador.garantirLinhaCriada(a, m);
            sequenciaOpt = springDataRepository.findByAnoAndMesComLock(a, m);
        }

        SequenciaProtocoloCat sequencia = sequenciaOpt.orElseThrow(() -> new IllegalStateException(
                "Não foi possível reservar o sequencial do protocolo CAT para " + ano + "/" + mes + "."));

        long proximo = sequencia.incrementar();
        springDataRepository.save(sequencia);
        return proximo;
    }
}

package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Emite o sequencial do protocolo CAT de um par ano/mês.
 *
 * <p>A reserva é feita por um único {@code INSERT ... ON CONFLICT DO UPDATE ... RETURNING}, que o
 * PostgreSQL executa de forma atômica sobre a linha do par ano/mês: transações concorrentes ficam
 * serializadas nessa linha e cada uma recebe um número distinto. Contar ocorrências e somar um não
 * oferece essa garantia, pois duas transações simultâneas leem a mesma contagem.</p>
 */
@Repository
public class SequenciaProtocoloCatJpaRepository {

    private static final String RESERVAR_PROXIMO_SEQUENCIAL = """
            INSERT INTO sequencias_protocolo_cat (ano, mes, ultimo_sequencial)
            VALUES (?, ?, 1)
            ON CONFLICT (ano, mes)
            DO UPDATE SET ultimo_sequencial = sequencias_protocolo_cat.ultimo_sequencial + 1
            RETURNING ultimo_sequencial
            """;

    private final JdbcTemplate jdbcTemplate;

    public SequenciaProtocoloCatJpaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Reserva e devolve o próximo sequencial do par ano/mês informado.
     */
    public long reservarProximoSequencial(int ano, int mes) {
        Long sequencial = jdbcTemplate.queryForObject(RESERVAR_PROXIMO_SEQUENCIAL, Long.class, ano, mes);
        if (sequencial == null) {
            throw new IllegalStateException(
                    "Não foi possível reservar o sequencial do protocolo CAT para " + ano + "/" + mes + ".");
        }
        return sequencial;
    }
}

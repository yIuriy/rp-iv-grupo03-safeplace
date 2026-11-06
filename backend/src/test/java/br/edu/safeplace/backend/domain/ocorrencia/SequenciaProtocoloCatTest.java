package br.edu.safeplace.backend.domain.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testes de Unidade - SequenciaProtocoloCat")
class SequenciaProtocoloCatTest {

    @Test
    @DisplayName("Deve inicializar a entidade com ano, mês e último sequencial")
    void deveInicializarEntidade() {
        SequenciaProtocoloCat seq = new SequenciaProtocoloCat(2026, 11, 0L);

        assertThat(seq.getAno()).isEqualTo((short) 2026);
        assertThat(seq.getMes()).isEqualTo((short) 11);
        assertThat(seq.getUltimoSequencial()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Deve incrementar o sequencial atômico de forma consistente")
    void deveIncrementarSequencial() {
        SequenciaProtocoloCat seq = new SequenciaProtocoloCat(2026, 11, 4L);

        long proximo1 = seq.incrementar();
        long proximo2 = seq.incrementar();

        assertThat(proximo1).isEqualTo(5L);
        assertThat(proximo2).isEqualTo(6L);
        assertThat(seq.getUltimoSequencial()).isEqualTo(6L);
    }

    @Test
    @DisplayName("Chave primária composta deve respeitar igualdade e hashCode")
    void chaveCompostaDeveRespeitarIgualdade() {
        SequenciaProtocoloCatId id1 = new SequenciaProtocoloCatId((short) 2026, (short) 11);
        SequenciaProtocoloCatId id2 = new SequenciaProtocoloCatId((short) 2026, (short) 11);
        SequenciaProtocoloCatId idOutro = new SequenciaProtocoloCatId((short) 2026, (short) 12);

        assertThat(id1).isEqualTo(id2).hasSameHashCodeAs(id2);
        assertThat(id1).isNotEqualTo(idOutro);
    }
}

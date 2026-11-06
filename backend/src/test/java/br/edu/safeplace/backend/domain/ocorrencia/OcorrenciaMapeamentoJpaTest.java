package br.edu.safeplace.backend.domain.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * A hierarquia de ocorrências é a própria entidade JPA com JOINED (#261), sem classes espelhadas.
 */
@DisplayName("Testes de Unidade - Mapeamento JPA da Hierarquia de Ocorrências")
class OcorrenciaMapeamentoJpaTest {

    @Test
    @DisplayName("Ocorrencia é a raiz da herança JOINED")
    void ocorrenciaEhRaizJoined() {
        assertThat(Ocorrencia.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(Ocorrencia.class.getAnnotation(Table.class).name()).isEqualTo("ocorrencias");
        assertThat(Ocorrencia.class.getAnnotation(Inheritance.class).strategy())
                .isEqualTo(InheritanceType.JOINED);
    }

    @Test
    @DisplayName("Acidente e Incidente mapeiam suas respectivas tabelas JOINED")
    void subclassesMapeiamTabelasEspecificas() {
        assertThat(Acidente.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(Acidente.class.getAnnotation(Table.class).name()).isEqualTo("acidentes");

        assertThat(Incidente.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(Incidente.class.getAnnotation(Table.class).name()).isEqualTo("incidentes");
    }

    @Test
    @DisplayName("Identidade é pelo id quando ele existe (#254)")
    void identidadeEhPeloId() {
        LocalDateTime agora = LocalDateTime.now();
        Acidente a1 = new Acidente(50, "Desc", agora, agora, StatusOcorrencia.ABERTA, null, null, null, null, null,
                CausaRaizAcidente.FALHA_EPI, "Tipo", "Dano", null, "Destino", null);
        Acidente a2 = new Acidente(50, "Outra desc", agora, agora, StatusOcorrencia.EM_TRIAGEM, null, null, null, null, null,
                CausaRaizAcidente.OUTRO, "Outro tipo", "Outro dano", null, "Outro destino", null);
        Acidente a3 = new Acidente(51, "Desc", agora, agora, StatusOcorrencia.ABERTA, null, null, null, null, null,
                CausaRaizAcidente.FALHA_EPI, "Tipo", "Dano", null, "Destino", null);

        assertThat(a1).isEqualTo(a2).hasSameHashCodeAs(a2);
        assertThat(a1).isNotEqualTo(a3);
    }
}

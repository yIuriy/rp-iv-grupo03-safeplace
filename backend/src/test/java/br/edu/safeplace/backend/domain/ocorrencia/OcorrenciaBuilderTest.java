package br.edu.safeplace.backend.domain.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@DisplayName("Builder (#240): OcorrenciaBuilder na superclasse Ocorrencia")
class OcorrenciaBuilderTest {

    private static class OcorrenciaConcretaBuilder extends OcorrenciaBuilder<OcorrenciaConcretaBuilder> {
        public Ocorrencia build() {
            return new Ocorrencia(this) {
                @Override
                public TipoOcorrencia getTipoOcorrencia() {
                    return TipoOcorrencia.INCIDENTE;
                }
            };
        }
    }

    @Test
    @DisplayName("Inicializa todos os atributos da superclasse Ocorrencia via builder")
    void inicializaSuperclasseViaBuilder() {
        Colaborador colaborador = mock(Colaborador.class);
        Colaborador relator = mock(Colaborador.class);
        AreaRisco area = mock(AreaRisco.class);
        PlanoDeAcao plano = mock(PlanoDeAcao.class);
        LocalDateTime agora = LocalDateTime.of(2026, 11, 16, 10, 0);

        Ocorrencia ocorrencia = new OcorrenciaConcretaBuilder()
                .idOcorrencia(99)
                .descricao("Descrição da ocorrência base")
                .dataOcorrencia(agora)
                .dataRegistro(agora.plusMinutes(10))
                .statusOcorrencia(StatusOcorrencia.EM_TRIAGEM)
                .colaborador(colaborador)
                .area(area)
                .relator(relator)
                .localInformado("Almoxarifado")
                .planoDeAcao(plano)
                .build();

        assertThat(ocorrencia.getIdOcorrencia()).isEqualTo(99);
        assertThat(ocorrencia.getDescricao()).isEqualTo("Descrição da ocorrência base");
        assertThat(ocorrencia.getDataOcorrencia()).isEqualTo(agora);
        assertThat(ocorrencia.getDataRegistro()).isEqualTo(agora.plusMinutes(10));
        assertThat(ocorrencia.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.EM_TRIAGEM);
        assertThat(ocorrencia.getColaborador()).isSameAs(colaborador);
        assertThat(ocorrencia.getArea()).isSameAs(area);
        assertThat(ocorrencia.getRelator()).isSameAs(relator);
        assertThat(ocorrencia.getLocalInformado()).isEqualTo("Almoxarifado");
        assertThat(ocorrencia.getPlanoDeAcao()).isSameAs(plano);
    }

    @Test
    @DisplayName("Superclasse Ocorrencia não possui construtores de 9 ou 10 parâmetros")
    void superclasseSemConstrutoresTelescopicos() {
        long construtoresComMuitosParametros = java.util.Arrays.stream(Ocorrencia.class.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() >= 5)
                .count();

        assertThat(construtoresComMuitosParametros)
                .as("Ocorrencia não deve possuir construtores com 5 ou mais parâmetros")
                .isZero();
    }
}

package br.edu.safeplace.backend.domain.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@DisplayName("Builder (#240): IncidenteBuilder em código de produção")
class IncidenteBuilderTest {

    @Test
    @DisplayName("Monta Incidente com todos os atributos nomeados")
    void montaIncidenteComTodosAtributos() {
        Colaborador colaborador = mock(Colaborador.class);
        Colaborador relator = mock(Colaborador.class);
        AreaRisco area = mock(AreaRisco.class);
        PlanoDeAcao plano = mock(PlanoDeAcao.class);
        LocalDateTime dataFato = LocalDateTime.of(2026, 11, 7, 14, 0);
        LocalDateTime dataReg = LocalDateTime.of(2026, 11, 7, 14, 15);

        Incidente incidente = Incidente.builder()
                .idOcorrencia(25)
                .descricao("  Quase colisão com empilhadeira  ")
                .dataOcorrencia(dataFato)
                .dataRegistro(dataReg)
                .statusOcorrencia(StatusOcorrencia.EM_TRIAGEM)
                .colaborador(colaborador)
                .area(area)
                .relator(relator)
                .localInformado("Galpão 2")
                .planoDeAcao(plano)
                .situacaoRisco("Ponto cego na curva")
                .potencialDano("Atropelamento grave")
                .build();

        assertThat(incidente.getIdOcorrencia()).isEqualTo(25);
        assertThat(incidente.getDescricao()).isEqualTo("Quase colisão com empilhadeira");
        assertThat(incidente.getDataOcorrencia()).isEqualTo(dataFato);
        assertThat(incidente.getDataRegistro()).isEqualTo(dataReg);
        assertThat(incidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.EM_TRIAGEM);
        assertThat(incidente.getColaborador()).isSameAs(colaborador);
        assertThat(incidente.getArea()).isSameAs(area);
        assertThat(incidente.getRelator()).isSameAs(relator);
        assertThat(incidente.getLocalInformado()).isEqualTo("Galpão 2");
        assertThat(incidente.getPlanoDeAcao()).isSameAs(plano);
        assertThat(incidente.getSituacaoRisco()).isEqualTo("Ponto cego na curva");
        assertThat(incidente.getPotencialDano()).isEqualTo("Atropelamento grave");
    }

    @Test
    @DisplayName("Aplica valores padrão de status e data de registro quando não informados")
    void aplicaValoresPadrao() {
        Incidente incidente = new IncidenteBuilder()
                .descricao("Quase queda")
                .build();

        assertThat(incidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(incidente.getDataRegistro()).isNotNull();
    }

    @Test
    @DisplayName("Builder não valida: cria incidente mesmo sem campos obrigatórios")
    void builderNaoValida() {
        Incidente incidente = new IncidenteBuilder()
                .descricao(null)
                .situacaoRisco(null)
                .potencialDano(null)
                .build();

        assertThat(incidente.getDescricao()).isNull();
        assertThat(incidente.getSituacaoRisco()).isNull();
        assertThat(incidente.getPotencialDano()).isNull();
    }
}

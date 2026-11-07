package br.edu.safeplace.backend.domain.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@DisplayName("Builder (#240): AcidenteBuilder em código de produção")
class AcidenteBuilderTest {

    @Test
    @DisplayName("Monta Acidente com todos os atributos nomeados")
    void montaAcidenteComTodosAtributos() {
        Colaborador colaborador = mock(Colaborador.class);
        Colaborador relator = mock(Colaborador.class);
        AreaRisco area = mock(AreaRisco.class);
        PlanoDeAcao plano = mock(PlanoDeAcao.class);
        LocalDateTime dataFato = LocalDateTime.of(2026, 11, 7, 8, 30);
        LocalDateTime dataReg = LocalDateTime.of(2026, 11, 7, 9, 0);
        NumeroProtocoloCAT cat = NumeroProtocoloCAT.gerar(dataFato, 12345L);

        Acidente acidente = Acidente.builder()
                .idOcorrencia(10)
                .descricao("  Queda de mesmo nível  ")
                .dataOcorrencia(dataFato)
                .dataRegistro(dataReg)
                .statusOcorrencia(StatusOcorrencia.EM_TRIAGEM)
                .colaborador(colaborador)
                .area(area)
                .relator(relator)
                .localInformado("Corredor A")
                .planoDeAcao(plano)
                .causaRaiz(CausaRaizAcidente.FATOR_HUMANO)
                .tipo("Típico")
                .dano("Escoriações")
                .numeroProtocoloCAT(cat)
                .destino("Ambulatório")
                .gravidade(NivelPerigo.MEDIO)
                .build();

        assertThat(acidente.getIdOcorrencia()).isEqualTo(10);
        assertThat(acidente.getDescricao()).isEqualTo("Queda de mesmo nível");
        assertThat(acidente.getDataOcorrencia()).isEqualTo(dataFato);
        assertThat(acidente.getDataRegistro()).isEqualTo(dataReg);
        assertThat(acidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.EM_TRIAGEM);
        assertThat(acidente.getColaborador()).isSameAs(colaborador);
        assertThat(acidente.getArea()).isSameAs(area);
        assertThat(acidente.getRelator()).isSameAs(relator);
        assertThat(acidente.getLocalInformado()).isEqualTo("Corredor A");
        assertThat(acidente.getPlanoDeAcao()).isSameAs(plano);
        assertThat(acidente.getCausaRaiz()).isEqualTo(CausaRaizAcidente.FATOR_HUMANO);
        assertThat(acidente.getTipo()).isEqualTo("Típico");
        assertThat(acidente.getDano()).isEqualTo("Escoriações");
        assertThat(acidente.getProtocoloCAT()).isEqualTo(cat);
        assertThat(acidente.getDestino()).isEqualTo("Ambulatório");
        assertThat(acidente.getGravidade()).isEqualTo(NivelPerigo.MEDIO);
    }

    @Test
    @DisplayName("Aplica valores padrão de status e data de registro quando não informados")
    void aplicaValoresPadrao() {
        Acidente acidente = new AcidenteBuilder()
                .descricao("Impacto")
                .build();

        assertThat(acidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(acidente.getDataRegistro()).isNotNull();
    }

    @Test
    @DisplayName("Builder não valida: cria acidente mesmo sem campos obrigatórios")
    void builderNaoValida() {
        Acidente acidente = new AcidenteBuilder()
                .descricao(null)
                .gravidade(null)
                .causaRaiz(null)
                .build();

        assertThat(acidente.getDescricao()).isNull();
        assertThat(acidente.getGravidade()).isNull();
        assertThat(acidente.getCausaRaiz()).isNull();
    }
}

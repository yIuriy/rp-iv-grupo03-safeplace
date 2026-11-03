package br.edu.safeplace.backend.validacao.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;

/**
 * Elo do formato do protocolo CAT usado sozinho (issue #230). As mesmas recusas valem dentro da
 * cadeia do {@code OcorrenciaValidador}, conferidas em {@code OcorrenciaValidadorTest}.
 */
@DisplayName("ProtocoloCATValidador: elo do formato do protocolo CAT")
class ProtocoloCATValidadorTest {

    private final ProtocoloCATValidador validador = new ProtocoloCATValidador();

    private static Acidente acidente(CausaRaizAcidente causa, String descricao, NumeroProtocoloCAT protocolo) {
        return new Acidente(1, descricao, LocalDateTime.of(2026, 9, 10, 8, 0), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, null, null, null, null, null, causa, null, null, protocolo, null,
                NivelPerigo.BAIXO);
    }

    @Test
    @DisplayName("Aceita protocolo no formato e acidente ainda sem protocolo")
    void aceitaProtocoloValidoOuAusente() {
        assertThatCode(() -> validador.validar(acidente(CausaRaizAcidente.OUTRO, "Corte",
                NumeroProtocoloCAT.gerar(LocalDateTime.of(2026, 9, 10, 8, 0), 1)))).doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(acidente(CausaRaizAcidente.OUTRO, "Corte", null)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALIDO", "OC-2026-09-0001", "CAT-26-9-1"})
    @DisplayName("Recusa formato inválido com a mesma exceção e mensagem do OcorrenciaValidador")
    void recusaFormatoInvalido(String invalido) {
        assertThatThrownBy(() -> validador.validar(acidente(CausaRaizAcidente.OUTRO, "Corte",
                NumeroProtocoloCAT.de(invalido))))
                .isInstanceOf(ProtocoloCATInvalidoException.class)
                .hasMessage("Formato de protocolo CAT inválido: " + invalido.toUpperCase());
    }

    @Test
    @DisplayName("Só confere o protocolo: não reaplica as regras do relato (causa raiz, descrição)")
    void naoReaplicaAsRegrasDoRelato() {
        Acidente antigoIncompleto = acidente(null, null, NumeroProtocoloCAT.de("CAT-2026-09-0001"));

        assertThat(validador.verificar(antigoIncompleto).valido()).isTrue();
        assertThat(new OcorrenciaValidador().verificar(antigoIncompleto).valido()).isFalse();
    }
}

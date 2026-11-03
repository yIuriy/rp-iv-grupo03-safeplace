package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Papéis do Value Object (issue #230): {@link Acidente} (Entity) guarda o {@link NumeroProtocoloCAT}
 * (Value Object) como atributo, e a {@link NotificacaoOcorrencia} usa o mesmo objeto, sem recriá-lo
 * a partir do texto. O getter em texto continua existindo para o contrato HTTP e o banco.
 */
@DisplayName("Acidente guarda o protocolo CAT como Value Object (#230)")
class AcidenteProtocoloCATTest {

    private static final LocalDateTime FATO = LocalDateTime.of(2026, 9, 10, 10, 0);

    private static Acidente acidente(Integer id, NumeroProtocoloCAT protocolo) {
        return new Acidente(id, "Queimadura na bancada", FATO, FATO, StatusOcorrencia.ABERTA, null, null, null,
                null, null, CausaRaizAcidente.FALHA_EPI, null, null, protocolo, null, NivelPerigo.ALTO);
    }

    @Test
    @DisplayName("Guarda o próprio Value Object recebido; o getter em texto devolve o valor dele")
    void guardaOValueObjectRecebido() {
        NumeroProtocoloCAT protocolo = NumeroProtocoloCAT.de("CAT-2026-09-0003");

        Acidente acidente = acidente(1, protocolo);

        assertThat(acidente.getProtocoloCAT()).isSameAs(protocolo);
        assertThat(acidente.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0003");
        assertThat(acidente.getNumeroProtocolo()).isEqualTo("CAT-2026-09-0003");
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROTOCOLO-123", "CAT-26-9-1"})
    @DisplayName("Não revalida na criação: formato inválido fica guardado para o OcorrenciaValidador recusar")
    void naoRevalidaNaCriacao(String invalido) {
        Acidente acidente = acidente(1, NumeroProtocoloCAT.de(invalido));

        assertThat(acidente.getProtocoloCAT()).isEqualTo(NumeroProtocoloCAT.de(invalido));
        assertThat(acidente.getProtocoloCAT().formatoValido()).isFalse();
    }

    @Test
    @DisplayName("Protocolo ausente ou sem texto significa CAT ainda não emitida")
    void protocoloSemValorFicaNulo() {
        assertThat(acidente(1, null).getProtocoloCAT()).isNull();
        assertThat(acidente(1, null).getNumeroProtocoloCAT()).isNull();
        assertThat(acidente(1, NumeroProtocoloCAT.de("   ")).getProtocoloCAT()).isNull();
        assertThat(acidente(1, NumeroProtocoloCAT.de(null)).getNumeroProtocoloCAT()).isNull();
    }

    @Test
    @DisplayName("Consolidar a CAT guarda o Value Object gerado pela sequência")
    void consolidarGuardaOValueObjectGerado() {
        Acidente consolidado = acidente(1, null).consolidarCAT(15);

        assertThat(consolidado.getProtocoloCAT())
                .isEqualTo(NumeroProtocoloCAT.gerar(FATO, 15))
                .isEqualTo(NumeroProtocoloCAT.de("CAT-2026-09-0015"));
        assertThat(consolidado.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0015");
    }

    @Test
    @DisplayName("Dois acidentes com o mesmo número têm protocolos iguais, mesmo vindos de instâncias diferentes")
    void acidentesComMesmoNumeroTemProtocolosIguais() {
        Acidente lidoDoBanco = acidente(1, NumeroProtocoloCAT.de("cat-2026-09-0001"));
        Acidente gerado = acidente(1, NumeroProtocoloCAT.gerar(FATO, 1));

        assertThat(lidoDoBanco.getProtocoloCAT()).isEqualTo(gerado.getProtocoloCAT());
        assertThat(lidoDoBanco.getProtocoloCAT()).isNotSameAs(gerado.getProtocoloCAT());
    }

    @Test
    @DisplayName("A notificação usa o mesmo Value Object do acidente, sem recriar nem revalidar")
    void notificacaoUsaOMesmoValueObject() {
        Acidente acidente = acidente(7, NumeroProtocoloCAT.de("CAT-2026-09-0003"));

        NotificacaoOcorrencia notificacao = NotificacaoOcorrencia.deNovoRelato(acidente);

        assertThat(notificacao.numeroProtocoloCAT()).isSameAs(acidente.getProtocoloCAT());
        assertThat(notificacao.mensagem()).contains("(protocolo CAT-2026-09-0003)");
    }
}

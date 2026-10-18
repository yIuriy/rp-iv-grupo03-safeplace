package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificacaoOcorrenciaTest {

    private static final LocalDateTime FATO = LocalDateTime.of(2026, 9, 10, 10, 0);

    private static AreaRisco area() {
        return new AreaRisco(4, "SOL-01", "Solda", "Bancada de solda", NivelPerigo.ALTO, 1, List.of(), null, List.of());
    }

    @Test
    @DisplayName("Notificação de acidente traz tipo, protocolo e área (DS09, VisaoNotificacao)")
    void deveMontarNotificacaoDeAcidente() {
        Acidente acidente = new Acidente(7, "Queimadura", FATO, FATO, StatusOcorrencia.ABERTA, null, area(), null,
                null, null, CausaRaizAcidente.FALHA_EPI, null, null, "CAT-2026-09-0003", null, NivelPerigo.ALTO);

        NotificacaoOcorrencia notificacao = NotificacaoOcorrencia.deNovoRelato(acidente);

        assertThat(notificacao.ocorrenciaId()).isEqualTo(7);
        assertThat(notificacao.tipoOcorrencia()).isEqualTo(TipoOcorrencia.ACIDENTE);
        assertThat(notificacao.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0003");
        assertThat(notificacao.areaNome()).isEqualTo("Solda");
        assertThat(notificacao.mensagem())
                .isEqualTo("Novo acidente registrado (protocolo CAT-2026-09-0003) na área Solda. Aguarda triagem.");
        assertThat(notificacao.criadaEm()).isNotNull();
    }

    @Test
    @DisplayName("Notificação de incidente não tem protocolo (DS09, seção 2.1)")
    void deveMontarNotificacaoDeIncidente() {
        Incidente incidente = new Incidente(8, "Piso molhado", FATO, FATO, StatusOcorrencia.ABERTA, null, area(), null,
                null, null, "Vazamento", "Queda");

        NotificacaoOcorrencia notificacao = NotificacaoOcorrencia.deNovoRelato(incidente);

        assertThat(notificacao.tipoOcorrencia()).isEqualTo(TipoOcorrencia.INCIDENTE);
        assertThat(notificacao.numeroProtocoloCAT()).isNull();
        assertThat(notificacao.mensagem()).isEqualTo("Novo incidente registrado na área Solda. Aguarda triagem.");
    }

    @Test
    @DisplayName("Ocorrência ainda não gravada não gera notificação")
    void deveRecusarOcorrenciaSemId() {
        Incidente naoGravado = new Incidente(null, "Piso molhado", FATO, FATO, StatusOcorrencia.ABERTA, null, area(),
                null, null, null, "Vazamento", "Queda");

        assertThatThrownBy(() -> NotificacaoOcorrencia.deNovoRelato(naoGravado))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

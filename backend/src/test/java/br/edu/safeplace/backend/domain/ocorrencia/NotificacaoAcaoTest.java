package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificacaoAcaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 18);

    private final Colaborador maria = new Colaborador(7, "52998224725", "Maria Souza", LocalDate.of(1990, 1, 1),
            null, true, LocalDateTime.now(), LocalDateTime.now());
    private final Setor manutencao = Setor.reconstituir(3, "Manutenção", null, "Bloco B", NivelPerigo.MEDIO);

    @Test
    @DisplayName("Alerta de prazo identifica a ação, a ocorrência, o prazo e o colaborador responsável")
    void alertaDePrazoComColaborador() {
        NotificacaoAcao notificacao = NotificacaoAcao.deAviso(new AvisoAcao(
                12, 5, MotivoAvisoAcao.PRAZO, maria, null, HOJE.plusDays(2), HOJE));

        assertThat(notificacao.idAcao()).isEqualTo(12);
        assertThat(notificacao.idOcorrencia()).isEqualTo(5);
        assertThat(notificacao.motivo()).isEqualTo(MotivoAvisoAcao.PRAZO);
        assertThat(notificacao.responsavelTipo()).isEqualTo(NotificacaoAcao.RESPONSAVEL_COLABORADOR);
        assertThat(notificacao.responsavelId()).isEqualTo(7);
        assertThat(notificacao.responsavelNome()).isEqualTo("Maria Souza");
        assertThat(notificacao.prazo()).isEqualTo(HOJE.plusDays(2));
        assertThat(notificacao.dataAviso()).isEqualTo(HOJE);
        assertThat(notificacao.mensagem())
                .isEqualTo("Ação #12 da ocorrência #5 com prazo em 20/10/2026. Responsável: Maria Souza (colaborador).");
    }

    @Test
    @DisplayName("Alerta depois do prazo informa o atraso")
    void alertaDePrazoEmAtraso() {
        NotificacaoAcao notificacao = NotificacaoAcao.deAviso(new AvisoAcao(
                12, 5, MotivoAvisoAcao.PRAZO, null, manutencao, HOJE.minusDays(1), HOJE));

        assertThat(notificacao.responsavelTipo()).isEqualTo(NotificacaoAcao.RESPONSAVEL_SETOR);
        assertThat(notificacao.responsavelId()).isEqualTo(3);
        assertThat(notificacao.mensagem()).isEqualTo("Ação #12 da ocorrência #5 em atraso: o prazo venceu em 17/10/2026 "
                + "e a execução não foi confirmada. Responsável: setor Manutenção.");
    }

    @Test
    @DisplayName("Reatribuição pede ao Gestor que comunique o novo responsável")
    void avisoDeReatribuicao() {
        NotificacaoAcao notificacao = NotificacaoAcao.deAviso(new AvisoAcao(
                12, 5, MotivoAvisoAcao.REATRIBUICAO, null, manutencao, HOJE.plusDays(4), HOJE));

        assertThat(notificacao.motivo()).isEqualTo(MotivoAvisoAcao.REATRIBUICAO);
        assertThat(notificacao.mensagem()).isEqualTo("Ação #12 da ocorrência #5 reatribuída a setor Manutenção. "
                + "Prazo: 22/10/2026. Comunique o novo responsável.");
    }

    @Test
    @DisplayName("Aviso sem ação gravada, sem prazo ou sem responsável único é recusado")
    void avisoIncompletoRecusado() {
        assertThatThrownBy(() -> NotificacaoAcao.deAviso(new AvisoAcao(
                null, 5, MotivoAvisoAcao.PRAZO, maria, null, HOJE, HOJE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NotificacaoAcao.deAviso(new AvisoAcao(
                12, 5, MotivoAvisoAcao.PRAZO, maria, null, null, HOJE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NotificacaoAcao.deAviso(new AvisoAcao(
                12, 5, MotivoAvisoAcao.PRAZO, null, null, HOJE, HOJE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NotificacaoAcao.deAviso(new AvisoAcao(
                12, 5, MotivoAvisoAcao.PRAZO, maria, manutencao, HOJE, HOJE)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

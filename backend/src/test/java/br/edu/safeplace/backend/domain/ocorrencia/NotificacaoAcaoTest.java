package br.edu.safeplace.backend.domain.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

class NotificacaoAcaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 18);

    private final Colaborador maria = new Colaborador(7, "52998224725", "Maria Souza", LocalDate.of(1990, 1, 1),
            null, true, LocalDateTime.now(), LocalDateTime.now());
    private final Setor manutencao = Setor.reconstituir(3, "Manutenção", null, "Bloco B", NivelPerigo.MEDIO);
    private final Ocorrencia ocorrencia5 = Incidente.builder()
            .idOcorrencia(5)
            .descricao("Piso molhado")
            .dataOcorrencia(HOJE.atStartOfDay())
            .build();
    private final PlanoDeAcao acao12 = new PlanoDeAcaoBuilder()
            .id(12)
            .ocorrencia(ocorrencia5)
            .medidasCorretivas("Sinalizar piso")
            .prazo(HOJE.plusDays(2))
            .responsavelColaborador(maria)
            .build();

    @Test
    @DisplayName("Alerta de prazo identifica a ação, a ocorrência, o prazo e o colaborador responsável")
    void alertaDePrazoComColaborador() {
        NotificacaoAcao notificacao = new NotificacaoAcao(new AvisoAcao(
                acao12, ocorrencia5, MotivoAvisoAcao.PRAZO, maria, null, HOJE.plusDays(2), HOJE));

        assertThat(notificacao.getAcao()).isSameAs(acao12);
        assertThat(notificacao.getOcorrencia()).isSameAs(ocorrencia5);
        assertThat(notificacao.getIdAcao()).isEqualTo(12);
        assertThat(notificacao.getIdOcorrencia()).isEqualTo(5);
        assertThat(notificacao.getMotivo()).isEqualTo(MotivoAvisoAcao.PRAZO);
        assertThat(notificacao.getResponsavelTipo()).isEqualTo(NotificacaoAcao.RESPONSAVEL_COLABORADOR);
        assertThat(notificacao.getResponsavelId()).isEqualTo(7);
        assertThat(notificacao.getResponsavelNome()).isEqualTo("Maria Souza");
        assertThat(notificacao.getPrazo()).isEqualTo(HOJE.plusDays(2));
        assertThat(notificacao.getDataAviso()).isEqualTo(HOJE);
        assertThat(notificacao.getCriadaEm()).isNotNull();
        assertThat(notificacao.getMensagem())
                .isEqualTo("Ação #12 da ocorrência #5 com prazo em 20/10/2026. Responsável: Maria Souza (colaborador).");
    }

    @Test
    @DisplayName("Alerta depois do prazo informa o atraso")
    void alertaDePrazoEmAtraso() {
        NotificacaoAcao notificacao = new NotificacaoAcao(new AvisoAcao(
                acao12, ocorrencia5, MotivoAvisoAcao.PRAZO, null, manutencao, HOJE.minusDays(1), HOJE));

        assertThat(notificacao.getResponsavelTipo()).isEqualTo(NotificacaoAcao.RESPONSAVEL_SETOR);
        assertThat(notificacao.getResponsavelId()).isEqualTo(3);
        assertThat(notificacao.getMensagem()).isEqualTo("Ação #12 da ocorrência #5 em atraso: o prazo venceu em 17/10/2026 "
                + "e a execução não foi confirmada. Responsável: setor Manutenção.");
    }

    @Test
    @DisplayName("Reatribuição pede ao Gestor que comunique o novo responsável")
    void avisoDeReatribuicao() {
        NotificacaoAcao notificacao = new NotificacaoAcao(new AvisoAcao(
                acao12, ocorrencia5, MotivoAvisoAcao.REATRIBUICAO, null, manutencao, HOJE.plusDays(4), HOJE));

        assertThat(notificacao.getMotivo()).isEqualTo(MotivoAvisoAcao.REATRIBUICAO);
        assertThat(notificacao.getMensagem()).isEqualTo("Ação #12 da ocorrência #5 reatribuída a setor Manutenção. "
                + "Prazo: 22/10/2026. Comunique o novo responsável.");
    }
}

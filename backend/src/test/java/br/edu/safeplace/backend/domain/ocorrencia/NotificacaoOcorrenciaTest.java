package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoOcorrenciaTest {

    private static final LocalDateTime FATO = LocalDateTime.of(2026, 9, 10, 10, 0);
    private static final Setor SETOR = new Setor(1, "Solda", null, "Bloco 1", NivelPerigo.ALTO);

    private static AreaRisco area() {
        return new AreaRisco(4, "SOL-01", "Solda", "Bancada de solda", NivelPerigo.ALTO, SETOR, List.of(), null, List.of());
    }

    @Test
    @DisplayName("Notificação de acidente traz tipo, protocolo e área (DS09, VisaoNotificacao)")
    void deveMontarNotificacaoDeAcidente() {
        Acidente acidente = Acidente.builder()
                .idOcorrencia(7)
                .descricao("Queimadura")
                .dataOcorrencia(FATO)
                .dataRegistro(FATO)
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .area(area())
                .causaRaiz(CausaRaizAcidente.FALHA_EPI)
                .numeroProtocoloCAT(NumeroProtocoloCAT.de("CAT-2026-09-0003"))
                .gravidade(NivelPerigo.ALTO)
                .build();

        NotificacaoOcorrencia notificacao = new FabricaRelatoAcidente().criarNotificacao(acidente);

        assertThat(notificacao.ocorrenciaId()).isEqualTo(7);
        assertThat(notificacao.tipoOcorrencia()).isEqualTo(TipoOcorrencia.ACIDENTE);
        assertThat(notificacao.numeroProtocoloCAT()).isEqualTo(NumeroProtocoloCAT.de("CAT-2026-09-0003"));
        assertThat(notificacao.areaNome()).isEqualTo("Solda");
        assertThat(notificacao.mensagem())
                .isEqualTo("Novo acidente registrado (protocolo CAT-2026-09-0003) na área Solda. Aguarda triagem.");
        assertThat(notificacao.criadaEm()).isNotNull();
    }

    @Test
    @DisplayName("Notificação de incidente não tem protocolo (DS09, seção 2.1)")
    void deveMontarNotificacaoDeIncidente() {
        Incidente incidente = Incidente.builder()
                .idOcorrencia(8)
                .descricao("Piso molhado")
                .dataOcorrencia(FATO)
                .dataRegistro(FATO)
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .area(area())
                .situacaoRisco("Vazamento")
                .potencialDano("Queda")
                .build();

        NotificacaoOcorrencia notificacao = new FabricaRelatoIncidente().criarNotificacao(incidente);

        assertThat(notificacao.tipoOcorrencia()).isEqualTo(TipoOcorrencia.INCIDENTE);
        assertThat(notificacao.numeroProtocoloCAT()).isNull();
        assertThat(notificacao.mensagem()).isEqualTo("Novo incidente registrado na área Solda. Aguarda triagem.");
    }

    @Test
    @DisplayName("Deve possuir anotações JPA e Immutable no domínio (#261)")
    void devePossuirAnotacoesJpa() {
        assertThat(NotificacaoOcorrencia.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(NotificacaoOcorrencia.class.getAnnotation(Table.class).name()).isEqualTo("notificacoes_ocorrencia");
        assertThat(NotificacaoOcorrencia.class.getAnnotation(Immutable.class)).isNotNull();
    }

    @Test
    @DisplayName("Identidade é pelo id gerado (#254)")
    void identidadeEhPeloId() {
        NotificacaoOcorrencia n1 = new NotificacaoOcorrencia(10, 1, TipoOcorrencia.ACIDENTE, null, "Área", "Msg", LocalDateTime.now());
        NotificacaoOcorrencia n2 = new NotificacaoOcorrencia(10, 2, TipoOcorrencia.INCIDENTE, null, "Outra", "Msg 2", LocalDateTime.now());
        NotificacaoOcorrencia n3 = new NotificacaoOcorrencia(11, 1, TipoOcorrencia.ACIDENTE, null, "Área", "Msg", LocalDateTime.now());

        assertThat(n1).isEqualTo(n2).hasSameHashCodeAs(n2);
        assertThat(n1).isNotEqualTo(n3);
    }
}

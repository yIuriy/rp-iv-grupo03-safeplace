package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcaoBuilder;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - NotificacaoAcaoGravador (deduplicação sem SQL nativo, #262)")
class NotificacaoAcaoGravadorTest {

    @Mock
    private NotificacaoAcaoJpaRepository repository;

    private NotificacaoAcaoGravador gravador;
    private final LocalDate hoje = LocalDate.of(2026, 10, 18);

    @BeforeEach
    void setUp() {
        gravador = new NotificacaoAcaoGravador(repository);
    }

    private NotificacaoAcao notificacao(MotivoAvisoAcao motivo) {
        Colaborador maria = new Colaborador(7, "52998224725", "Maria Souza", LocalDate.of(1990, 1, 1),
                null, true, LocalDateTime.now(), LocalDateTime.now());
        Ocorrencia ocorrencia = Incidente.builder()
                .idOcorrencia(5)
                .descricao("Piso molhado")
                .dataOcorrencia(hoje.atStartOfDay())
                .build();
        PlanoDeAcao acao = new PlanoDeAcaoBuilder()
                .id(12)
                .ocorrencia(ocorrencia)
                .medidasCorretivas("Sinalizar piso")
                .prazo(hoje.plusDays(2))
                .responsavelColaborador(maria)
                .build();
        return new NotificacaoAcao(new AvisoAcao(acao, ocorrencia, motivo, maria, null, hoje.plusDays(2), hoje));
    }

    @Test
    @DisplayName("Ignora alerta de prazo quando já existe registro para a mesma ação e prazo")
    void ignoraQuandoJaExiste() {
        NotificacaoAcao notificacao = notificacao(MotivoAvisoAcao.PRAZO);
        when(repository.existsByAcaoIdAndPrazoAndMotivo(12, hoje.plusDays(2), MotivoAvisoAcao.PRAZO))
                .thenReturn(true);

        boolean gravado = gravador.gravarAlertaPrazoUnico(notificacao);

        assertThat(gravado).isFalse();
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Trata violação concorrente do índice único como já notificado")
    void trataViolacaoIndiceUnicoComoJaNotificado() {
        NotificacaoAcao notificacao = notificacao(MotivoAvisoAcao.PRAZO);
        when(repository.existsByAcaoIdAndPrazoAndMotivo(12, hoje.plusDays(2), MotivoAvisoAcao.PRAZO))
                .thenReturn(false);
        when(repository.saveAndFlush(notificacao))
                .thenThrow(new DataIntegrityViolationException("ux_notificacoes_acao_prazo"));

        boolean gravado = gravador.gravarAlertaPrazoUnico(notificacao);

        assertThat(gravado).isFalse();
    }

    @Test
    @DisplayName("Grava alerta de prazo inédito via saveAndFlush")
    void gravaAlertaPrazoInedito() {
        NotificacaoAcao notificacao = notificacao(MotivoAvisoAcao.PRAZO);
        when(repository.existsByAcaoIdAndPrazoAndMotivo(12, hoje.plusDays(2), MotivoAvisoAcao.PRAZO))
                .thenReturn(false);

        boolean gravado = gravador.gravarAlertaPrazoUnico(notificacao);

        assertThat(gravado).isTrue();
        verify(repository).saveAndFlush(notificacao);
    }

    @Test
    @DisplayName("Grava notificação de reatribuição via save")
    void gravaReatribuicao() {
        NotificacaoAcao notificacao = notificacao(MotivoAvisoAcao.REATRIBUICAO);

        boolean gravado = gravador.gravar(notificacao);

        assertThat(gravado).isTrue();
        verify(repository).save(notificacao);
    }
}

package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verificação periódica dos alertas de prazo das ações do plano (UC07, regra 2; issue #196). */
class NotificacaoAcaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 18);

    private final Colaborador carlos = new Colaborador(10, "11122233344", "Carlos", LocalDate.of(1990, 1, 1),
            null, true, LocalDateTime.now(), LocalDateTime.now());

    private PlanoDeAcaoRepository planoDeAcaoRepository;
    private NotificadorAcao notificadorAcao;
    private NotificacaoAcaoService useCase;

    @BeforeEach
    void configurar() {
        planoDeAcaoRepository = mock(PlanoDeAcaoRepository.class);
        notificadorAcao = mock(NotificadorAcao.class);
        useCase = new NotificacaoAcaoService(planoDeAcaoRepository, notificadorAcao);
    }

    private PlanoDeAcao acao(int id, LocalDate prazo, int antecedenciaDias) {
        return new PlanoDeAcao(id, 5, "Medida", null, prazo, "ABERTA", carlos, null, true, antecedenciaDias, null);
    }

    @Test
    @DisplayName("Emite o alerta das ações que chegaram à antecedência, inclusive as vencidas, e ignora as demais")
    void emiteSoAsAcoesDevidas() {
        PlanoDeAcao naAntecedencia = acao(1, HOJE.plusDays(3), 3);
        PlanoDeAcao vencida = acao(2, HOJE.minusDays(2), 1);
        PlanoDeAcao aindaNao = acao(3, HOJE.plusDays(10), 3);
        when(planoDeAcaoRepository.listarAbertasComAlerta()).thenReturn(List.of(naAntecedencia, vencida, aindaNao));
        when(notificadorAcao.notificar(any(AvisoAcao.class))).thenReturn(true);

        int emitidos = useCase.emitir(HOJE);

        assertThat(emitidos).isEqualTo(2);
        ArgumentCaptor<AvisoAcao> avisos = ArgumentCaptor.forClass(AvisoAcao.class);
        verify(notificadorAcao, times(2)).notificar(avisos.capture());
        assertThat(avisos.getAllValues()).extracting(AvisoAcao::idAcao).containsExactly(1, 2);
        assertThat(avisos.getAllValues()).allSatisfy(aviso -> {
            assertThat(aviso.motivo()).isEqualTo(MotivoAvisoAcao.PRAZO);
            assertThat(aviso.dataAviso()).isEqualTo(HOJE);
            assertThat(aviso.responsavelColaborador()).isSameAs(carlos);
        });
    }

    @Test
    @DisplayName("Alerta já emitido para o mesmo prazo não é contado de novo (deduplicação)")
    void naoContaAlertaRepetido() {
        when(planoDeAcaoRepository.listarAbertasComAlerta()).thenReturn(List.of(acao(1, HOJE, 0)));
        when(notificadorAcao.notificar(any(AvisoAcao.class))).thenReturn(true, false);

        assertThat(useCase.emitir(HOJE)).isEqualTo(1);
        assertThat(useCase.emitir(HOJE)).isZero();
    }

    @Test
    @DisplayName("Ação concluída não recebe alerta, mesmo se a consulta a devolver")
    void acaoConcluidaNaoRecebeAlerta() {
        PlanoDeAcao concluida = new PlanoDeAcao(4, 5, "Medida", null, HOJE.minusDays(1), "CONCLUIDA", carlos, null,
                true, 1, null);
        when(planoDeAcaoRepository.listarAbertasComAlerta()).thenReturn(List.of(concluida));

        assertThat(useCase.emitir(HOJE)).isZero();
        verify(notificadorAcao, never()).notificar(any(AvisoAcao.class));
    }

    @Test
    @DisplayName("A data de referência é obrigatória")
    void exigeDataDeReferencia() {
        assertThatThrownBy(() -> useCase.emitir(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A consulta devolve os avisos gravados pela porta")
    void listaAvisos() {
        NotificacaoAcao aviso = new NotificacaoAcao(1, 1, 5, MotivoAvisoAcao.PRAZO, "COLABORADOR", 10, "Carlos",
                HOJE, HOJE, "Ação #1 da ocorrência #5 com prazo em 18/10/2026. Responsável: Carlos (colaborador).",
                LocalDateTime.of(2026, 10, 18, 7, 0));
        when(notificadorAcao.listar()).thenReturn(List.of(aviso));

        assertThat(useCase.listar()).containsExactly(aviso);
    }
}

/*
 * Consolidação da CAT com o Value Object do protocolo (issue #230): o formato do protocolo gerado é
 * verificado pelo elo de #266 (ProtocoloCATValidador) antes do repository, sem reaplicar as regras do
 * relato a acidentes antigos.
 */
package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.adapters.out.notificacao.NotificadorOcorrenciaNulo;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;
import br.edu.safeplace.backend.validacao.RegraElo;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;
import br.edu.safeplace.backend.validacao.ocorrencia.OcorrenciaValidador;
import br.edu.safeplace.backend.validacao.ocorrencia.ProtocoloCATValidador;

@DisplayName("Consolidação da CAT valida o formato do protocolo antes do repository (#230)")
class OcorrenciaServiceConsolidacaoCATTest {

    private static final LocalDateTime FATO = LocalDateTime.of(2026, 9, 12, 9, 0);

    /** Acidente antigo, sem protocolo e sem causa raiz (o banco aceita causa_raiz nula, V32). */
    private final Acidente legadoSemCausaRaiz = new Acidente(5, "Queimadura leve por respingo", FATO, FATO,
            StatusOcorrencia.EM_TRIAGEM, null, null, null, "Posto antigo", null,
            null, null, null, null, null, NivelPerigo.MEDIO);

    private OcorrenciaRepository repository;

    @BeforeEach
    void configurar() {
        repository = mock(OcorrenciaRepository.class);
        when(repository.buscarAcidentePorId(5)).thenReturn(Optional.of(legadoSemCausaRaiz));
        when(repository.proximoSequencialCAT(2026, 9)).thenReturn(3L);
        when(repository.salvarAcidente(any(Acidente.class))).thenAnswer(chamada -> chamada.getArgument(0));
    }

    private OcorrenciaService servico(ValidadorEmCadeia<Acidente> protocoloValidador) {
        return new OcorrenciaService(repository, mock(AreaRiscoRepository.class), mock(UsuarioRepository.class),
                new TrilhaAuditoriaEmMemoria(), new NotificadorOcorrenciaNulo(), new OcorrenciaValidador(),
                protocoloValidador);
    }

    @Test
    @DisplayName("O validador do protocolo recebe o acidente já com o Value Object gerado, antes de salvar")
    void validaOAcidenteConsolidadoAntesDeSalvar() {
        List<NumeroProtocoloCAT> recebidos = new ArrayList<>();
        ValidadorEmCadeia<Acidente> registrador = new ValidadorEmCadeia<>(RegraElo.<Acidente>exigir(a -> {
            recebidos.add(a.getProtocoloCAT());
            verify(repository, never()).salvarAcidente(any(Acidente.class));
            return false;
        }, "não usado")) { };

        OcorrenciaOutputDTO consolidado = servico(registrador).consolidarCAT(5);

        assertThat(recebidos).containsExactly(NumeroProtocoloCAT.de("CAT-2026-09-0003"));
        assertThat(consolidado.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0003");
    }

    @Test
    @DisplayName("Se o formato for recusado, nada é salvo e a exceção do elo chega ao cliente")
    void nadaESalvoQuandoOFormatoEhRecusado() {
        ValidadorEmCadeia<Acidente> recusa = new ValidadorEmCadeia<>(RegraElo.<Acidente>exigir(a -> true,
                "Formato de protocolo CAT inválido: X", ProtocoloCATInvalidoException::new)) { };

        assertThatThrownBy(() -> servico(recusa).consolidarCAT(5))
                .isInstanceOf(ProtocoloCATInvalidoException.class)
                .hasMessage("Formato de protocolo CAT inválido: X");
        verify(repository, never()).salvarAcidente(any(Acidente.class));
    }

    @Test
    @DisplayName("Acidente antigo sem causa raiz continua sendo consolidado: só o formato é conferido")
    void acidenteAntigoSemCausaRaizContinuaConsolidavel() {
        OcorrenciaOutputDTO consolidado = servico(new ProtocoloCATValidador()).consolidarCAT(5);

        assertThat(consolidado.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0003");
        verify(repository).salvarAcidente(any(Acidente.class));
    }
}

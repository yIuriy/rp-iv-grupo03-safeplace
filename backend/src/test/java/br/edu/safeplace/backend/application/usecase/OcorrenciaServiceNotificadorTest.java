/*
 * Testes dos papéis do Null Object no OcorrenciaService (issue #227): a porta NotificadorOcorrencia
 * é obrigatória, o caso de uso a chama sempre (dublê que registra a chamada) e funciona igual com o
 * objeto nulo NotificadorOcorrenciaNulo, sem nenhum if != null no fluxo do relato.
 */
package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.adapters.out.notificacao.NotificadorOcorrenciaNulo;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

@DisplayName("OcorrenciaService com a porta de notificação obrigatória (Null Object)")
class OcorrenciaServiceNotificadorTest {

    private final AreaRisco area = new AreaRisco(10, "AR-10", "Galpão 1", null, NivelPerigo.ALTO, 1,
            List.of(), null, List.of());
    private final Colaborador colaborador = new Colaborador(20, "11122233344", "Carlos",
            LocalDate.of(1995, 2, 2), null, true, null, null);
    private final Incidente salvo = new Incidente(2, "Fagulha perto de inflamáveis",
            LocalDateTime.of(2026, 9, 20, 11, 0), LocalDateTime.of(2026, 9, 20, 11, 5), StatusOcorrencia.ABERTA,
            colaborador, area, null, null, null, "Vazamento de faíscas", "Incêndio");

    private OcorrenciaRepository repository;
    private AreaRiscoRepository areaRepo;
    private UsuarioRepository usuarioRepo;

    @BeforeEach
    void configurar() {
        repository = mock(OcorrenciaRepository.class);
        areaRepo = mock(AreaRiscoRepository.class);
        usuarioRepo = mock(UsuarioRepository.class);
        when(areaRepo.buscarPorId(10)).thenReturn(Optional.of(area));
        when(usuarioRepo.buscarPorId(20)).thenReturn(Optional.of(colaborador));
        when(repository.salvarIncidente(any(Incidente.class))).thenReturn(salvo);
    }

    private OcorrenciaService servicoCom(NotificadorOcorrencia notificador) {
        return new OcorrenciaService(repository, areaRepo, usuarioRepo, new TrilhaAuditoriaEmMemoria(), notificador);
    }

    private static RegistrarIncidenteInputDTO relato() {
        return new RegistrarIncidenteInputDTO(LocalDateTime.of(2026, 9, 20, 11, 0), "Fagulha perto de inflamáveis",
                "Vazamento de faíscas", "Incêndio", 10, 20);
    }

    @Test
    @DisplayName("Sem notificador o serviço não é montado: a ausência vira NotificadorOcorrenciaNulo, não null")
    void deveRecusarNotificadorNulo() {
        assertThatThrownBy(() -> servicoCom(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Notificador de ocorrência é obrigatório.");
    }

    @Test
    @DisplayName("RealObject: o caso de uso chama a porta uma vez com a ocorrência gravada")
    void deveChamarPortaComOcorrenciaGravada() {
        NotificadorQueRegistra dubleQueRegistra = new NotificadorQueRegistra();

        servicoCom(dubleQueRegistra).registrarIncidente(relato());

        assertThat(dubleQueRegistra.notificadas).containsExactly(salvo);
    }

    @Test
    @DisplayName("NullObject: o relato é gravado e devolvido normalmente, sem notificação")
    void deveRegistrarRelatoComNotificadorNulo() {
        NotificadorOcorrencia nulo = new NotificadorOcorrenciaNulo();

        var resposta = servicoCom(nulo).registrarIncidente(relato());

        assertThat(resposta.idOcorrencia()).isEqualTo(2);
        assertThat(new NotificacaoOcorrenciaService(nulo).listar()).isEmpty();
    }

    /** Dublê escrito à mão: guarda cada ocorrência recebida, para o teste conferir a chamada. */
    private static final class NotificadorQueRegistra implements NotificadorOcorrencia {
        private final List<Ocorrencia> notificadas = new ArrayList<>();

        @Override
        public void notificarGestor(Ocorrencia ocorrencia) {
            notificadas.add(ocorrencia);
        }

        @Override
        public List<NotificacaoOcorrencia> listar() {
            return List.of();
        }
    }
}

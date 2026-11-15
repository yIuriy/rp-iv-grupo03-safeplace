package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.adapters.out.notificacao.NotificadorOcorrenciaNulo;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Auditoria - OcorrenciaService")
class OcorrenciaUseCaseAuditoriaTest {

    @Mock
    private OcorrenciaRepository repository;

    @Mock
    private AreaRiscoRepository areaRiscoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LogAuditoriaRepository logAuditoriaRepository;

    private OcorrenciaService useCase;

    private final Setor setor = new Setor(1, "Galpão", null, "Bloco 1", NivelPerigo.ALTO);
    private final AreaRisco area = new AreaRisco(10, "AR-10", "Galpão 1", null, NivelPerigo.ALTO, setor,
            List.of(), null, List.of());
    private final Colaborador colaborador = new Colaborador(20, "11122233344", "Carlos",
            LocalDate.of(1995, 2, 2), null, true, null, null);

    private void stubAreaEColaborador() {
        when(areaRiscoRepository.buscarPorId(10)).thenReturn(Optional.of(area));
        when(usuarioRepository.buscarPorId(20)).thenReturn(Optional.of(colaborador));
    }

    @BeforeEach
    void setUp() {
        // Dublê da porta do operador (#236), no lugar do SecurityContext do Spring.
        OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.autenticado("gestor@safeplace.com");
        useCase = new OcorrenciaService(repository, areaRiscoRepository, usuarioRepository, logAuditoriaRepository,
                new NotificadorOcorrenciaNulo(), operador.operadorAtual(usuarioRepository));
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao registrar Acidente")
    void deveRegistrarAuditoriaAoRegistrarAcidente() {
        stubAreaEColaborador();
        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                "Queda de nível",
                "FALHA_EPI",
                "ALTO",
                "Fratura",
                "Fratura no braço",
                "Hospital",
                10,
                20
        );

        Acidente salvo = Acidente.builder()
                .idOcorrencia(1)
                .descricao("Queda de nível")
                .dataOcorrencia(LocalDateTime.of(2026, 9, 20, 10, 0))
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .causaRaiz(CausaRaizAcidente.FALHA_EPI)
                .tipo("Fratura")
                .dano("Fratura no braço")
                .destino("Hospital")
                .gravidade(NivelPerigo.ALTO)
                .build();
        when(repository.salvar(any(Acidente.class))).thenReturn(salvo);
        when(repository.proximoSequencialCAT(anyInt(), anyInt())).thenReturn(1L);

        OcorrenciaOutputDTO dto = useCase.registrar(input);

        assertThat(dto.idOcorrencia()).isEqualTo(1);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao registrar Incidente")
    void deveRegistrarAuditoriaAoRegistrarIncidente() {
        stubAreaEColaborador();
        RegistrarIncidenteInputDTO input = new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 20, 11, 0),
                "Quase acidente por fagulha",
                "Vazamento de faíscas próximo a inflamáveis",
                "Incêndio",
                10,
                20
        );

        Incidente salvo = Incidente.builder()
                .idOcorrencia(2)
                .descricao("Quase acidente por fagulha")
                .dataOcorrencia(LocalDateTime.of(2026, 9, 20, 11, 0))
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .situacaoRisco("Vazamento de faíscas")
                .potencialDano("Incêndio")
                .build();
        when(repository.salvar(any(Incidente.class))).thenReturn(salvo);

        OcorrenciaOutputDTO dto = useCase.registrar(input);

        assertThat(dto.idOcorrencia()).isEqualTo(2);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Incidente");
        assertThat(log.getEntidadeId()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao enviar ocorrencia para triagem")
    void deveRegistrarAuditoriaAoEnviarParaTriagem() {
        Acidente acidente = Acidente.builder()
                .idOcorrencia(3)
                .descricao("Descricao")
                .dataOcorrencia(LocalDateTime.now())
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .causaRaiz(CausaRaizAcidente.OUTRO)
                .gravidade(NivelPerigo.BAIXO)
                .build();
        when(repository.buscarPorId(3)).thenReturn(Optional.of(acidente));
        when(repository.salvar(any(Acidente.class))).thenReturn(acidente);

        useCase.enviarParaTriagem(3);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(3);
        assertThat(log.getJustificativa()).isEqualTo("Envio de ocorrência para triagem");
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao arquivar ocorrencia")
    void deveRegistrarAuditoriaAoArquivar() {
        Acidente acidente = Acidente.builder()
                .idOcorrencia(4)
                .descricao("Descricao")
                .dataOcorrencia(LocalDateTime.now())
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.EM_TRIAGEM)
                .colaborador(colaborador)
                .area(area)
                .causaRaiz(CausaRaizAcidente.OUTRO)
                .gravidade(NivelPerigo.BAIXO)
                .build();
        when(repository.buscarPorId(4)).thenReturn(Optional.of(acidente));
        when(repository.salvar(any(Acidente.class))).thenReturn(acidente);

        useCase.arquivar(4);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(4);
        assertThat(log.getJustificativa()).isEqualTo("Arquivamento de ocorrência");
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao consolidar CAT")
    void deveRegistrarAuditoriaAoConsolidarCAT() {
        Acidente acidente = Acidente.builder()
                .idOcorrencia(5)
                .descricao("Descricao")
                .dataOcorrencia(LocalDateTime.now())
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .causaRaiz(CausaRaizAcidente.OUTRO)
                .gravidade(NivelPerigo.BAIXO)
                .build();
        when(repository.buscarAcidentePorId(5)).thenReturn(Optional.of(acidente));
        when(repository.proximoSequencialCAT(anyInt(), anyInt())).thenReturn(1L);
        when(repository.salvar(any(Acidente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.consolidarCAT(5);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(5);
        assertThat(log.getJustificativa()).contains("Consolidação de CAT com protocolo");
    }

    @Test
    @DisplayName("Falha ao buscar ocorrencia nao deve gerar log de auditoria")
    void falhaNaoGeraLogAuditoria() {
        when(repository.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.arquivar(99))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);

        verifyNoInteractions(logAuditoriaRepository);
    }
}

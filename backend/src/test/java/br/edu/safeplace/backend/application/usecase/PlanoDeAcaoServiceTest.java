package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.AtualizarResponsavelInputDTO;
import br.edu.safeplace.backend.application.dto.input.AtualizarStatusPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ConfigurarAlertaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ProrrogarPrazoInputDTO;
import br.edu.safeplace.backend.application.dto.output.PlanoDeAcaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProrrogacaoPrazoOutputDTO;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPlanoNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - PlanoDeAcaoService")
class PlanoDeAcaoServiceTest {

    @Mock
    private PlanoDeAcaoRepository planoDeAcaoRepository;

    @Mock
    private OcorrenciaRepository ocorrenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SetorRepository setorRepository;

    @Mock
    private NotificadorAcao notificadorAcao;

    private PlanoDeAcaoService useCase;

    private Colaborador colaborador;
    private Setor setor;
    private Acidente ocorrenciaGrave;
    private Acidente ocorrenciaLeve;

    @BeforeEach
    void setUp() {
        useCase = new PlanoDeAcaoService(planoDeAcaoRepository, ocorrenciaRepository, usuarioRepository, setorRepository,
                notificadorAcao);

        colaborador = new Colaborador(10, "11122233344", "Carlos", LocalDate.of(1990, 1, 1), "carlos@safeplace.com", true, LocalDateTime.now(), LocalDateTime.now());
        setor = new Setor(20, "Manutenção", "Setor de Manutenção", "Bloco M", NivelPerigo.MEDIO);

        ocorrenciaGrave = new Acidente(
                1, "Amputacao de dedo", LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, colaborador, null, null, "Prensa",
                null, CausaRaizAcidente.FALHA_EPI, "Amputação", "Grave", "CAT-2026-09-0001", "Hospital", NivelPerigo.CRITICO
        );

        ocorrenciaLeve = new Acidente(
                2, "Escoriacao no braco", LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, colaborador, null, null, "Pátio",
                null, CausaRaizAcidente.OUTRO, "Escoriação", "Leve", "CAT-2026-09-0002", "Ambulatório", NivelPerigo.BAIXO
        );
    }

    @Test
    @DisplayName("Deve cadastrar ação com sucesso vinculada à ocorrência")
    void deveCadastrarAcaoComSucesso() {
        when(ocorrenciaRepository.buscarPorId(2)).thenReturn(Optional.of(ocorrenciaLeve));
        when(usuarioRepository.buscarPorId(10)).thenReturn(Optional.of(colaborador));
        when(planoDeAcaoRepository.listarPorOcorrencia(2)).thenReturn(List.of());
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> {
            PlanoDeAcao p = inv.getArgument(0);
            return new PlanoDeAcao(100, p.getIdOcorrencia(), p.getMedidasCorretivas(), p.getMedidasPreventivas(),
                    p.getPrazo(), p.getStatus(), p.getResponsavelColaborador(), p.getResponsavelSetor(),
                    p.isAlertaAtivo(), p.getAntecedenciaAlertaDias(), p.getGestor());
        });

        CriarAcaoPlanoInputDTO input = new CriarAcaoPlanoInputDTO(
                "Instalar protecao na esteira",
                null,
                LocalDate.now().plusDays(10),
                10,
                null,
                true,
                3
        );

        PlanoDeAcaoOutputDTO output = useCase.cadastrarAcao(2, input);

        assertThat(output.id()).isEqualTo(100);
        assertThat(output.medidasCorretivas()).isEqualTo("Instalar protecao na esteira");
        assertThat(output.responsavelTipo()).isEqualTo("COLABORADOR");
        assertThat(output.responsavelId()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve exigir ação preventiva ao cadastrar plano para ocorrência grave")
    void deveExigirAcaoPreventivaEmOcorrenciaGrave() {
        when(ocorrenciaRepository.buscarPorId(1)).thenReturn(Optional.of(ocorrenciaGrave));
        when(usuarioRepository.buscarPorId(10)).thenReturn(Optional.of(colaborador));
        when(planoDeAcaoRepository.listarPorOcorrencia(1)).thenReturn(List.of());

        CriarAcaoPlanoInputDTO input = new CriarAcaoPlanoInputDTO(
                "Socorro imediato ao colaborador",
                null, // Sem medida preventiva
                LocalDate.now().plusDays(5),
                10,
                null,
                false,
                null
        );

        assertThatThrownBy(() -> useCase.cadastrarAcao(1, input))
                .isInstanceOf(AcaoPreventivaObrigatoriaException.class);
    }

    @Test
    @DisplayName("Deve permitir cadastrar ação em ocorrência grave se contiver medida preventiva")
    void devePermitirCadastroComMedidaPreventivaEmOcorrenciaGrave() {
        when(ocorrenciaRepository.buscarPorId(1)).thenReturn(Optional.of(ocorrenciaGrave));
        when(usuarioRepository.buscarPorId(10)).thenReturn(Optional.of(colaborador));
        when(planoDeAcaoRepository.listarPorOcorrencia(1)).thenReturn(List.of());
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        CriarAcaoPlanoInputDTO input = new CriarAcaoPlanoInputDTO(
                "Troca de sensor quebrado",
                "Instalacao de redundancia eletromecanica e novo protocolo de trava",
                LocalDate.now().plusDays(5),
                10,
                null,
                true,
                2
        );

        PlanoDeAcaoOutputDTO output = useCase.cadastrarAcao(1, input);
        assertThat(output.medidasPreventivas()).contains("Instalacao de redundancia");
    }

    @Test
    @DisplayName("Deve rejeitar prazo anterior à data atual")
    void deveRejeitarPrazoPassado() {
        when(ocorrenciaRepository.buscarPorId(2)).thenReturn(Optional.of(ocorrenciaLeve));
        when(usuarioRepository.buscarPorId(10)).thenReturn(Optional.of(colaborador));

        CriarAcaoPlanoInputDTO input = new CriarAcaoPlanoInputDTO(
                "Ajuste de painel",
                null,
                LocalDate.now().minusDays(1), // prazo retroativo
                10,
                null,
                false,
                null
        );

        assertThatThrownBy(() -> useCase.cadastrarAcao(2, input))
                .isInstanceOf(PrazoAcaoInvalidoException.class);
    }

    @Test
    @DisplayName("Deve prorrogar prazo com justificativa")
    void deveProrrogarPrazoComJustificativa() {
        LocalDate prazoAtual = LocalDate.now().plusDays(5);
        PlanoDeAcao acao = new PlanoDeAcao(
                50, 2, "Conserto", null, prazoAtual, "ABERTA", colaborador, null, true, 3, null
        );
        when(planoDeAcaoRepository.buscarPorId(50)).thenReturn(Optional.of(acao));
        when(planoDeAcaoRepository.salvarProrrogacao(any(ProrrogacaoPrazo.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDate novoPrazo = LocalDate.now().plusDays(15);
        ProrrogarPrazoInputDTO input = new ProrrogarPrazoInputDTO(novoPrazo, "Atraso no fornecimento de peças pelo fabricante");

        ProrrogacaoPrazoOutputDTO dto = useCase.prorrogarPrazo(50, input);

        assertThat(dto.prazoAnterior()).isEqualTo(prazoAtual);
        assertThat(dto.novoPrazo()).isEqualTo(novoPrazo);
        assertThat(dto.justificativa()).contains("Atraso no fornecimento");
        assertThat(acao.getPrazo()).isEqualTo(novoPrazo);
    }

    @Test
    @DisplayName("Deve reatribuir responsável da ação para setor")
    void deveReatribuirResponsavelParaSetor() {
        PlanoDeAcao acao = new PlanoDeAcao(
                60, 2, "Manutenção geral", null, LocalDate.now().plusDays(5), "ABERTA", colaborador, null, false, null, null
        );
        when(planoDeAcaoRepository.buscarPorId(60)).thenReturn(Optional.of(acao));
        when(setorRepository.buscarPorId(20)).thenReturn(Optional.of(setor));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarResponsavelInputDTO input = new AtualizarResponsavelInputDTO(null, 20);
        PlanoDeAcaoOutputDTO output = useCase.atualizarResponsavel(60, input);

        assertThat(output.responsavelTipo()).isEqualTo("SETOR");
        assertThat(output.responsavelId()).isEqualTo(20);
        assertThat(output.responsavelNome()).isEqualTo("Manutenção");

        // UC07, alternativo II: o aviso ao novo encarregado é entregue, não descartado (issue #196).
        ArgumentCaptor<AvisoAcao> aviso = ArgumentCaptor.forClass(AvisoAcao.class);
        verify(notificadorAcao).notificar(aviso.capture());
        assertThat(aviso.getValue().motivo()).isEqualTo(MotivoAvisoAcao.REATRIBUICAO);
        assertThat(aviso.getValue().idAcao()).isEqualTo(60);
        assertThat(aviso.getValue().idOcorrencia()).isEqualTo(2);
        assertThat(aviso.getValue().responsavelSetor()).isSameAs(setor);
        assertThat(aviso.getValue().responsavelColaborador()).isNull();
        assertThat(aviso.getValue().dataAviso()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Reatribuição a colaborador avisa o novo colaborador responsável (issue #196)")
    void deveAvisarNovoColaboradorNaReatribuicao() {
        Colaborador novo = new Colaborador(11, "55566677788", "Beatriz", LocalDate.of(1992, 2, 2), null, true,
                LocalDateTime.now(), LocalDateTime.now());
        PlanoDeAcao acao = new PlanoDeAcao(
                61, 2, "Troca de guarda-corpo", null, LocalDate.now().plusDays(8), "ABERTA", null, setor, false, null, null
        );
        when(planoDeAcaoRepository.buscarPorId(61)).thenReturn(Optional.of(acao));
        when(usuarioRepository.buscarPorId(11)).thenReturn(Optional.of(novo));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.atualizarResponsavel(61, new AtualizarResponsavelInputDTO(11, null));

        ArgumentCaptor<AvisoAcao> aviso = ArgumentCaptor.forClass(AvisoAcao.class);
        verify(notificadorAcao).notificar(aviso.capture());
        assertThat(aviso.getValue().motivo()).isEqualTo(MotivoAvisoAcao.REATRIBUICAO);
        assertThat(aviso.getValue().responsavelColaborador()).isSameAs(novo);
        assertThat(aviso.getValue().responsavelSetor()).isNull();
        assertThat(aviso.getValue().prazo()).isEqualTo(LocalDate.now().plusDays(8));
    }

    @Test
    @DisplayName("Reatribuição recusada não grava nem avisa")
    void reatribuicaoRecusadaNaoAvisa() {
        PlanoDeAcao acao = new PlanoDeAcao(
                62, 2, "Manutenção geral", null, LocalDate.now().plusDays(5), "ABERTA", null, setor, false, null, null
        );
        when(planoDeAcaoRepository.buscarPorId(62)).thenReturn(Optional.of(acao));
        when(setorRepository.buscarPorId(20)).thenReturn(Optional.of(setor));

        assertThatThrownBy(() -> useCase.atualizarResponsavel(62, new AtualizarResponsavelInputDTO(null, 20)))
                .isInstanceOf(PlanoDeAcaoInvalidoException.class);

        verify(planoDeAcaoRepository, never()).salvar(any(PlanoDeAcao.class));
        verifyNoInteractions(notificadorAcao);
    }

    @Test
    @DisplayName("Ação inexistente responde não encontrada em todas as operações (404, issue #196)")
    void acaoInexistenteRespondeNaoEncontrada() {
        when(planoDeAcaoRepository.buscarPorId(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.buscarPorId(999))
                .isInstanceOf(AcaoPlanoNaoEncontradaException.class)
                .hasMessageContaining("999");
        assertThatThrownBy(() -> useCase.atualizarStatus(999, new AtualizarStatusPlanoInputDTO("CONCLUIDA")))
                .isInstanceOf(AcaoPlanoNaoEncontradaException.class);
        assertThatThrownBy(() -> useCase.prorrogarPrazo(999, new ProrrogarPrazoInputDTO(LocalDate.now().plusDays(3), "Peças")))
                .isInstanceOf(AcaoPlanoNaoEncontradaException.class);
        assertThatThrownBy(() -> useCase.listarProrrogacoes(999))
                .isInstanceOf(AcaoPlanoNaoEncontradaException.class);
        assertThatThrownBy(() -> useCase.atualizarResponsavel(999, new AtualizarResponsavelInputDTO(10, null)))
                .isInstanceOf(AcaoPlanoNaoEncontradaException.class);
        assertThatThrownBy(() -> useCase.configurarAlerta(999, new ConfigurarAlertaInputDTO(true, 2)))
                .isInstanceOf(AcaoPlanoNaoEncontradaException.class);
        verifyNoInteractions(notificadorAcao);
    }

    @Test
    @DisplayName("Alerta devido e atraso são calculados no servidor (issue #196)")
    void alertaEAtrasoCalculadosNoServidor() {
        PlanoDeAcao vencida = new PlanoDeAcao(
                80, 2, "Sinalizar rampa", null, LocalDate.now().minusDays(1), "ABERTA", colaborador, null, true, 2, null
        );
        PlanoDeAcao futura = new PlanoDeAcao(
                81, 2, "Revisar extintores", null, LocalDate.now().plusDays(10), "ABERTA", colaborador, null, true, 2, null
        );
        when(planoDeAcaoRepository.buscarPorId(80)).thenReturn(Optional.of(vencida));
        when(planoDeAcaoRepository.buscarPorId(81)).thenReturn(Optional.of(futura));
        when(ocorrenciaRepository.buscarPorId(2)).thenReturn(Optional.of(ocorrenciaLeve));

        PlanoDeAcaoOutputDTO atrasada = useCase.buscarPorId(80);
        PlanoDeAcaoOutputDTO noPrazo = useCase.buscarPorId(81);

        assertThat(atrasada.alertaDevido()).isTrue();
        assertThat(atrasada.emAtraso()).isTrue();
        assertThat(noPrazo.alertaDevido()).isFalse();
        assertThat(noPrazo.emAtraso()).isFalse();
        assertThat(noPrazo.dataAlerta()).isEqualTo(LocalDate.now().plusDays(8));
    }

    @Test
    @DisplayName("Deve atualizar status da ação para CONCLUIDA")
    void deveAtualizarStatusDaAcao() {
        PlanoDeAcao acao = new PlanoDeAcao(
                70, 2, "Treinamento", null, LocalDate.now().plusDays(10), "ABERTA", colaborador, null, false, null, null
        );
        when(planoDeAcaoRepository.buscarPorId(70)).thenReturn(Optional.of(acao));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarStatusPlanoInputDTO input = new AtualizarStatusPlanoInputDTO("CONCLUIDA");
        PlanoDeAcaoOutputDTO output = useCase.atualizarStatus(70, input);

        assertThat(output.status()).isEqualTo("CONCLUIDA");
    }

    @Test
    @DisplayName("Consulta do plano segue a visibilidade do relato: relato alheio ao Supervisor responde não encontrada")
    void consultaDoPlanoRespeitaVisibilidadeDaOcorrencia() {
        br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase consulta =
                org.mockito.Mockito.mock(br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase.class);
        when(consulta.buscarPorId(9)).thenThrow(
                new br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException(9));
        PlanoDeAcaoService comVisibilidade = new PlanoDeAcaoService(
                planoDeAcaoRepository, ocorrenciaRepository, usuarioRepository, setorRepository, notificadorAcao, consulta);

        assertThatThrownBy(() -> comVisibilidade.listarPorOcorrencia(9))
                .isInstanceOf(br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException.class);
        org.mockito.Mockito.verifyNoInteractions(planoDeAcaoRepository);
    }
}

package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.edu.safeplace.backend.application.dto.input.AtualizarResponsavelInputDTO;
import br.edu.safeplace.backend.application.dto.input.AtualizarStatusPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ConfigurarAlertaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ProrrogarPrazoInputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcaoBuilder;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

@ExtendWith(MockitoExtension.class)
@DisplayName("Facade (#238): auditoria de PlanoDeAcao via RegistradorAuditoria")
class PlanoDeAcaoAuditoriaTest {

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

    @Mock
    private GerenciarOcorrenciaUseCase consultaOcorrencia;

    private final TrilhaAuditoriaEmMemoria trilha = new TrilhaAuditoriaEmMemoria();
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.semSessao();
    private RegistradorAuditoria registradorAuditoria;
    private PlanoDeAcaoService service;

    private final LocalDate hoje = LocalDate.now();
    private Colaborador colaborador;
    private Setor setor;
    private Acidente ocorrencia;

    @BeforeEach
    void setUp() {
        OperadorAtual operadorAtual = operador.operadorAtual(usuarioRepository);
        registradorAuditoria = new RegistradorAuditoria(trilha, operadorAtual);
        service = new PlanoDeAcaoService(
                planoDeAcaoRepository,
                ocorrenciaRepository,
                usuarioRepository,
                setorRepository,
                notificadorAcao,
                consultaOcorrencia,
                operadorAtual,
                registradorAuditoria
        );

        colaborador = new Colaborador(1, "87455877074", "Roberto", LocalDate.of(1992, 3, 10),
                "roberto@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
        setor = Setor.reconstituir(5, "Usinagem", "Setor de usinagem", "Bloco B", NivelPerigo.ALTO);

        ocorrencia = Acidente.builder()
                .idOcorrencia(10)
                .descricao("Corte na mão")
                .dataOcorrencia(hoje.atStartOfDay())
                .dataRegistro(hoje.atStartOfDay())
                .statusOcorrencia(br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia.ABERTA)
                .causaRaiz(CausaRaizAcidente.FATOR_HUMANO)
                .gravidade(NivelPerigo.MEDIO)
                .build();
    }

    @Test
    @DisplayName("Cadastrar ação grava auditoria CADASTRO com vínculo à ocorrência")
    void deveAuditarCadastroDeAcao() {
        CriarAcaoPlanoInputDTO input = new CriarAcaoPlanoInputDTO(
                "Isolar prensa",
                "Treinar equipe",
                hoje.plusDays(5),
                1,
                null,
                true,
                3
        );

        when(ocorrenciaRepository.buscarPorId(10)).thenReturn(Optional.of(ocorrencia));
        when(usuarioRepository.buscarPorId(1)).thenReturn(Optional.of(colaborador));
        when(planoDeAcaoRepository.listarPorOcorrencia(10)).thenReturn(List.of());
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> {
            PlanoDeAcao a = inv.getArgument(0);
            ReflectionTestUtils.setField(a, "id", 100);
            return a;
        });

        service.cadastrar(10, input);

        assertThat(trilha.registros()).hasSize(1);
        LogAuditoria log = trilha.registros().get(0);
        assertThat(log.getTipoOperacao()).isEqualTo(LogAuditoria.CADASTRO);
        assertThat(log.getEntidade()).isEqualTo("PlanoDeAcao");
        assertThat(log.getEntidadeId()).isEqualTo(100);
        assertThat(log.getJustificativa()).isEqualTo("Criação de ação vinculada à ocorrência 10");
        assertThat(log.getResponsavel()).isEqualTo(OperadorAtual.SISTEMA);
    }

    @Test
    @DisplayName("Atualizar status grava auditoria ALTERACAO descrevendo status anterior e novo")
    void deveAuditarAlteracaoDeStatus() {
        PlanoDeAcao existente = new PlanoDeAcaoBuilder()
                .id(100).ocorrencia(ocorrencia).medidasCorretivas("Medida").prazo(hoje.plusDays(5))
                .responsavelColaborador(colaborador).build();
        when(planoDeAcaoRepository.buscarPorId(100)).thenReturn(Optional.of(existente));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizarStatus(100, new AtualizarStatusPlanoInputDTO("CONCLUIDA"));

        assertThat(trilha.registros()).hasSize(1);
        LogAuditoria log = trilha.registros().get(0);
        assertThat(log.getTipoOperacao()).isEqualTo(LogAuditoria.ALTERACAO);
        assertThat(log.getEntidade()).isEqualTo("PlanoDeAcao");
        assertThat(log.getEntidadeId()).isEqualTo(100);
        assertThat(log.getJustificativa()).isEqualTo("Alteração de situação da ação: de ABERTA para CONCLUIDA");
    }

    @Test
    @DisplayName("Atualizar responsável grava auditoria ALTERACAO descrevendo responsável anterior e novo")
    void deveAuditarReatribuicaoDeResponsavel() {
        PlanoDeAcao existente = new PlanoDeAcaoBuilder()
                .id(100).ocorrencia(ocorrencia).medidasCorretivas("Medida").prazo(hoje.plusDays(5))
                .responsavelColaborador(colaborador).build();
        when(planoDeAcaoRepository.buscarPorId(100)).thenReturn(Optional.of(existente));
        when(setorRepository.buscarPorId(5)).thenReturn(Optional.of(setor));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizarResponsavel(100, new AtualizarResponsavelInputDTO(null, 5));

        assertThat(trilha.registros()).hasSize(1);
        LogAuditoria log = trilha.registros().get(0);
        assertThat(log.getTipoOperacao()).isEqualTo(LogAuditoria.ALTERACAO);
        assertThat(log.getEntidade()).isEqualTo("PlanoDeAcao");
        assertThat(log.getEntidadeId()).isEqualTo(100);
        assertThat(log.getJustificativa()).isEqualTo("Reatribuição de responsável da ação: de colaborador id 1 para setor id 5");
    }

    @Test
    @DisplayName("Configurar alerta grava auditoria ALTERACAO quando houver mudança")
    void deveAuditarConfiguracaoDeAlerta() {
        PlanoDeAcao existente = new PlanoDeAcaoBuilder()
                .id(100).ocorrencia(ocorrencia).medidasCorretivas("Medida").prazo(hoje.plusDays(5))
                .responsavelColaborador(colaborador).build();
        when(planoDeAcaoRepository.buscarPorId(100)).thenReturn(Optional.of(existente));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.configurarAlerta(100, new ConfigurarAlertaInputDTO(true, 3));

        assertThat(trilha.registros()).hasSize(1);
        LogAuditoria log = trilha.registros().get(0);
        assertThat(log.getTipoOperacao()).isEqualTo(LogAuditoria.ALTERACAO);
        assertThat(log.getEntidade()).isEqualTo("PlanoDeAcao");
        assertThat(log.getEntidadeId()).isEqualTo(100);
        assertThat(log.getJustificativa()).isEqualTo(
                "Configuração de alerta da ação: de ativo=false, antecedencia=null dias para ativo=true, antecedencia=3 dias"
        );
    }

    @Test
    @DisplayName("Prorrogar prazo grava auditoria PRORROGAR_PRAZO com justificativa")
    void deveAuditarProrrogacaoDePrazo() {
        PlanoDeAcao existente = new PlanoDeAcaoBuilder()
                .id(100).ocorrencia(ocorrencia).medidasCorretivas("Medida").prazo(hoje.plusDays(2))
                .responsavelColaborador(colaborador).build();
        when(planoDeAcaoRepository.buscarPorId(100)).thenReturn(Optional.of(existente));
        when(planoDeAcaoRepository.salvar(any(ProrrogacaoPrazo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.prorrogarPrazo(100, new ProrrogarPrazoInputDTO(hoje.plusDays(10), "Atraso na entrega de peças"));

        assertThat(trilha.registros()).hasSize(1);
        LogAuditoria log = trilha.registros().get(0);
        assertThat(log.getTipoOperacao()).isEqualTo("PRORROGAR_PRAZO");
        assertThat(log.getEntidade()).isEqualTo("PlanoDeAcao");
        assertThat(log.getEntidadeId()).isEqualTo(100);
        assertThat(log.getJustificativa()).isEqualTo("Atraso na entrega de peças");
    }

    @Test
    @DisplayName("Auditoria registra o operador autenticado lido pela porta")
    void deveRegistrarOperadorAutenticadoNaAuditoria() {
        operador.autenticar("gestora@safeplace.com", "GESTOR_SEGURANCA");

        PlanoDeAcao existente = new PlanoDeAcaoBuilder()
                .id(100).ocorrencia(ocorrencia).medidasCorretivas("Medida").prazo(hoje.plusDays(5))
                .responsavelColaborador(colaborador).build();
        when(planoDeAcaoRepository.buscarPorId(100)).thenReturn(Optional.of(existente));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizarStatus(100, new AtualizarStatusPlanoInputDTO("CONCLUIDA"));

        assertThat(trilha.registros()).hasSize(1);
        assertThat(trilha.registros().get(0).getResponsavel()).isEqualTo("gestora@safeplace.com");
    }

    @Test
    @DisplayName("Sem sessão, auditoria registra e-mail do gestor associado ao plano")
    void semSessaoRegistraEmailDoGestor() {
        GestorDeSeguranca gestor = GestorDeSeguranca.novo(
                "Gestor Responsavel", "12345678901", LocalDate.of(1980, 1, 1), "gestor.plano@safeplace.com", "hash"
        );
        PlanoDeAcao existente = new PlanoDeAcaoBuilder()
                .id(100).ocorrencia(ocorrencia).medidasCorretivas("Medida").prazo(hoje.plusDays(5))
                .responsavelColaborador(colaborador).gestor(gestor).build();
        when(planoDeAcaoRepository.buscarPorId(100)).thenReturn(Optional.of(existente));
        when(planoDeAcaoRepository.salvar(any(PlanoDeAcao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizarStatus(100, new AtualizarStatusPlanoInputDTO("CONCLUIDA"));

        assertThat(trilha.registros()).hasSize(1);
        assertThat(trilha.registros().get(0).getResponsavel()).isEqualTo("gestor.plano@safeplace.com");
    }
}

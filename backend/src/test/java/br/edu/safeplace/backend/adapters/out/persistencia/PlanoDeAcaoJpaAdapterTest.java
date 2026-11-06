package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.usecase.OperadorAtual;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - PlanoDeAcaoJpaAdapter")
class PlanoDeAcaoJpaAdapterTest {

    @Mock
    private PlanoDeAcaoJpaRepository planoRepository;

    @Mock
    private ProrrogacaoPrazoJpaRepository prorrogacaoRepository;

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @Mock
    private SetorJpaRepository setorRepository;

    @Mock
    private LogAuditoriaRepository auditoriaRepository;

    /** Dublê da porta do operador (#236): começa sem sessão. */
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.semSessao();

    private PlanoDeAcaoJpaAdapter adapter;
    private final LocalDate hoje = LocalDate.of(2026, 9, 20);

    @BeforeEach
    void setUp() {
        adapter = new PlanoDeAcaoJpaAdapter(
                planoRepository,
                prorrogacaoRepository,
                usuarioRepository,
                setorRepository,
                auditoriaRepository,
                operador.operadorAtual(mock(UsuarioRepository.class))
        );
    }

    private Colaborador colaborador(Integer id) {
        return new Colaborador(id, "87455877074", "Roberto", LocalDate.of(1992, 3, 10),
                "roberto@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private ColaboradorEntidade colaboradorEntidade(Integer id) {
        return new ColaboradorEntidade(id, "87455877074", "Roberto", LocalDate.of(1992, 3, 10),
                "roberto@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Setor setor(Integer id) {
        return Setor.reconstituir(id, "Usinagem", "Setor de usinagem", "Bloco B", NivelPerigo.ALTO);
    }

    @Test
    @DisplayName("Deve salvar ação nova com colaborador e alerta, registrando auditoria de cadastro")
    void deveSalvarAcaoNovaComColaborador() {
        PlanoDeAcao plano = new PlanoDeAcao(
                null,
                10,
                "Isolar prensa",
                "Treinar equipe",
                hoje.plusDays(5),
                "ABERTA",
                colaborador(1),
                null,
                true,
                3,
                null
        );

        PlanoDeAcaoEntity salva = new PlanoDeAcaoEntity(
                100,
                10,
                "Isolar prensa",
                hoje.plusDays(5),
                "ABERTA",
                "Treinar equipe",
                1,
                null,
                null,
                true,
                3
        );

        when(planoRepository.save(any(PlanoDeAcaoEntity.class))).thenReturn(salva);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(colaboradorEntidade(1)));

        PlanoDeAcao resultado = adapter.salvar(plano);

        assertThat(resultado.getId()).isEqualTo(100);
        assertThat(resultado.getIdOcorrencia()).isEqualTo(10);
        assertThat(resultado.getMedidasCorretivas()).isEqualTo("Isolar prensa");
        assertThat(resultado.getMedidasPreventivas()).isEqualTo("Treinar equipe");
        assertThat(resultado.getStatus()).isEqualTo("ABERTA");
        assertThat(resultado.isAlertaAtivo()).isTrue();
        assertThat(resultado.getAntecedenciaAlertaDias()).isEqualTo(3);
        assertThat(resultado.getResponsavelColaborador()).isNotNull();
        assertThat(resultado.getResponsavelColaborador().getId()).isEqualTo(1);

        ArgumentCaptor<LogAuditoria> auditCaptor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(auditoriaRepository).salvar(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(auditCaptor.getValue().getEntidade()).isEqualTo("PlanoDeAcao");
        assertThat(auditCaptor.getValue().getEntidadeId()).isEqualTo(100);
    }

    @Test
    @DisplayName("Deve salvar ação vinculada a setor como responsável")
    void deveSalvarAcaoComSetor() {
        PlanoDeAcao plano = new PlanoDeAcao(
                null,
                10,
                "Manutenção corretiva",
                null,
                hoje.plusDays(7),
                "ABERTA",
                null,
                setor(5),
                false,
                null,
                null
        );

        PlanoDeAcaoEntity salva = new PlanoDeAcaoEntity(
                101,
                10,
                "Manutenção corretiva",
                hoje.plusDays(7),
                "ABERTA",
                null,
                null,
                5,
                null,
                false,
                null
        );

        when(planoRepository.save(any(PlanoDeAcaoEntity.class))).thenReturn(salva);
        when(setorRepository.findById(5)).thenReturn(Optional.of(setor(5)));

        PlanoDeAcao resultado = adapter.salvar(plano);

        assertThat(resultado.getId()).isEqualTo(101);
        assertThat(resultado.getResponsavelSetor()).isNotNull();
        assertThat(resultado.getResponsavelSetor().getId()).isEqualTo(5);
        assertThat(resultado.getResponsavelColaborador()).isNull();
    }

    @Test
    @DisplayName("Deve rejeitar gravação quando prazo for nulo")
    void deveRejeitarPrazoNulo() {
        PlanoDeAcao plano = new PlanoDeAcao(
                null,
                10,
                "Medida",
                null,
                null,
                "ABERTA",
                colaborador(1),
                null,
                false,
                null,
                null
        );

        assertThatThrownBy(() -> adapter.salvar(plano))
                .isInstanceOf(PrazoAcaoInvalidoException.class)
                .hasMessage("O prazo da ação é obrigatório.");
    }

    @Test
    @DisplayName("Deve registrar auditoria de reatribuição ao mudar responsável")
    void deveAuditarReatribuicao() {
        PlanoDeAcaoEntity existente = new PlanoDeAcaoEntity(
                100, 10, "Medida", hoje.plusDays(5), "ABERTA", null, 1, null, null, false, null
        );
        PlanoDeAcaoEntity atualizada = new PlanoDeAcaoEntity(
                100, 10, "Medida", hoje.plusDays(5), "ABERTA", null, null, 5, null, false, null
        );

        when(planoRepository.findById(100)).thenReturn(Optional.of(existente));
        when(planoRepository.save(any(PlanoDeAcaoEntity.class))).thenReturn(atualizada);
        when(setorRepository.findById(5)).thenReturn(Optional.of(setor(5)));

        PlanoDeAcao plano = new PlanoDeAcao(
                100, 10, "Medida", null, hoje.plusDays(5), "ABERTA", null, setor(5), false, null, null
        );

        adapter.salvar(plano);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(auditoriaRepository).salvar(captor.capture());
        assertThat(captor.getValue().getJustificativa()).isEqualTo("Reatribuição de responsável da ação: de colaborador id 1 para setor id 5");
    }

    @Test
    @DisplayName("Deve registrar auditoria de alteração de situação ao concluir ação")
    void deveAuditarConclusao() {
        PlanoDeAcaoEntity existente = new PlanoDeAcaoEntity(
                100, 10, "Medida", hoje.plusDays(5), "ABERTA", null, 1, null, null, false, null
        );
        PlanoDeAcaoEntity atualizada = new PlanoDeAcaoEntity(
                100, 10, "Medida", hoje.plusDays(5), "CONCLUIDA", null, 1, null, null, false, null
        );

        when(planoRepository.findById(100)).thenReturn(Optional.of(existente));
        when(planoRepository.save(any(PlanoDeAcaoEntity.class))).thenReturn(atualizada);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(colaboradorEntidade(1)));

        PlanoDeAcao plano = new PlanoDeAcao(
                100, 10, "Medida", null, hoje.plusDays(5), "CONCLUIDA", colaborador(1), null, false, null, null
        );

        adapter.salvar(plano);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(auditoriaRepository).salvar(captor.capture());
        assertThat(captor.getValue().getJustificativa()).isEqualTo("Alteração de situação da ação: de ABERTA para CONCLUIDA");
    }

    /** Conclui uma ação de plano sem Gestor e devolve o responsável gravado na auditoria. */
    private String responsavelDaAuditoriaDeConclusao() {
        when(planoRepository.findById(100)).thenReturn(Optional.of(new PlanoDeAcaoEntity(
                100, 10, "Medida", hoje.plusDays(5), "ABERTA", null, 1, null, null, false, null)));
        when(planoRepository.save(any(PlanoDeAcaoEntity.class))).thenReturn(new PlanoDeAcaoEntity(
                100, 10, "Medida", hoje.plusDays(5), "CONCLUIDA", null, 1, null, null, false, null));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(colaboradorEntidade(1)));

        adapter.salvar(new PlanoDeAcao(
                100, 10, "Medida", null, hoje.plusDays(5), "CONCLUIDA", colaborador(1), null, false, null, null));

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(auditoriaRepository).salvar(captor.capture());
        return captor.getValue().getResponsavel();
    }

    @Test
    @DisplayName("Adapter (#236): a auditoria registra o operador autenticado lido pela porta")
    void auditoriaRegistraOperadorDaPorta() {
        operador.autenticar("gestora@safeplace.com", "GESTOR_SEGURANCA");

        assertThat(responsavelDaAuditoriaDeConclusao()).isEqualTo("gestora@safeplace.com");
    }

    @Test
    @DisplayName("Adapter (#236): sem sessão e sem Gestor no plano, a auditoria registra SISTEMA")
    void semSessaoNemGestorAuditoriaRegistraSistema() {
        assertThat(responsavelDaAuditoriaDeConclusao()).isEqualTo(OperadorAtual.SISTEMA);
    }

    @Test
    @DisplayName("Deve salvar prorrogação com justificativa e registrar em log de auditoria")
    void deveSalvarProrrogacao() {
        ProrrogacaoPrazo prorrogacao = new ProrrogacaoPrazo(
                100,
                hoje.plusDays(2),
                hoje.plusDays(10),
                "Atraso na entrega de peças"
        );

        ProrrogacaoPrazo resultado = adapter.salvarProrrogacao(prorrogacao);

        assertThat(resultado).isEqualTo(prorrogacao);
        verify(prorrogacaoRepository).save(any(ProrrogacaoPrazoEntity.class));

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(auditoriaRepository).salvar(captor.capture());
        assertThat(captor.getValue().getTipoOperacao()).isEqualTo("PRORROGAR_PRAZO");
        assertThat(captor.getValue().getJustificativa()).isEqualTo("Atraso na entrega de peças");
        assertThat(captor.getValue().getEntidadeId()).isEqualTo(100);
    }

    @Test
    @DisplayName("Deve listar histórico de prorrogações ordenado")
    void deveListarProrrogacoes() {
        ProrrogacaoPrazoEntity e1 = new ProrrogacaoPrazoEntity(
                1, 100, hoje.plusDays(2), hoje.plusDays(5), "Motivo 1", LocalDateTime.now().minusDays(2)
        );
        ProrrogacaoPrazoEntity e2 = new ProrrogacaoPrazoEntity(
                2, 100, hoje.plusDays(5), hoje.plusDays(10), "Motivo 2", LocalDateTime.now().minusDays(1)
        );

        when(prorrogacaoRepository.findByPlanoDeAcaoIdOrderByCriadoEmAscIdAsc(100))
                .thenReturn(List.of(e1, e2));

        List<ProrrogacaoPrazo> lista = adapter.listarProrrogacoes(100);

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).prazoAnterior()).isEqualTo(hoje.plusDays(2));
        assertThat(lista.get(0).novoPrazo()).isEqualTo(hoje.plusDays(5));
        assertThat(lista.get(1).novoPrazo()).isEqualTo(hoje.plusDays(10));
    }
}

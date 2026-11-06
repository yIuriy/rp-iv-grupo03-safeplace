/*
 * Testes do caso de uso de EPIs para a issue #123: a consulta (listar e buscarPorId) e o retorno
 * de uma movimentação expõem a mesma regra de estoque crítico do domínio (saldo <= mínimo), sem
 * reimplementá-la. Usa um repositório em memória; a persistência real é coberta em
 * EstoqueCriticoPersistenciaIntegracaoTest.
 */
package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

class EstoqueCriticoConsultaUseCaseTest {

    /** RNF05: a porta de auditoria é obrigatória nos casos de uso. */
    private final TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();

    private static final String EMAIL_GESTOR = "gestor@empresa.com";

    private EpiRepositoryEmMemoria repositorio;
    private EpiService useCase;
    private Integer epiId;
    /** Dublê da porta do operador (#236), no lugar do SecurityContext do Spring. */
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.autenticado(EMAIL_GESTOR);

    @BeforeEach
    void cadastrarEpiComSaldoDezEMinimoCinco() {
        repositorio = new EpiRepositoryEmMemoria();
        // US03: toda movimentação identifica o usuário autenticado como responsável.
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(usuarios.buscarPorEmail(EMAIL_GESTOR)).thenReturn(Optional.of(new GestorDeSeguranca(
                7, "49216091040", "Mariana", LocalDate.of(1988, 7, 20), EMAIL_GESTOR, "hash", true,
                LocalDateTime.now(), LocalDateTime.now())));
        useCase = new EpiService(repositorio, usuarios, auditoria, operador.operadorAtual(usuarios));
        EpiOutputDTO salvo = useCase.cadastrarEpi(new CadastrarEpiInputDTO(
                "Luva nitrílica", "12345", 10, 5, LocalDate.now().plusYears(1), 180,
                "Luva de proteção química", ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES));
        epiId = salvo.id();
    }

    @Test
    @DisplayName("Consulta inicial: saldo 10 acima do mínimo 5 não é crítico")
    void consultaInicialNaoEhCritica() {
        assertThat(useCase.buscarPorId(epiId).estoqueCritico()).isFalse();
        assertThat(useCase.listar()).singleElement()
                .satisfies(epi -> assertThat(epi.estoqueCritico()).isFalse());
    }

    @Test
    @DisplayName("Saída de 5 iguala o mínimo: a próxima consulta mostra crítico e o saldo 5")
    void saidaQueIgualaOMinimoApareceCriticaNaConsulta() {
        MovimentacaoEstoqueOutputDTO saida =
                useCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, 5, "Entrega semanal");

        assertThat(saida.saldoAposMovimentacao()).isEqualTo(5);
        EpiOutputDTO consultado = useCase.buscarPorId(epiId);
        assertThat(consultado.quantidade()).isEqualTo(5);
        assertThat(consultado.estoqueMinimo()).isEqualTo(5);
        assertThat(consultado.estoqueCritico()).isTrue();
    }

    @Test
    @DisplayName("Entrada que ultrapassa o mínimo encerra a situação crítica na consulta")
    void entradaAcimaDoMinimoEncerraCritico() {
        useCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, 7, "Entregas");
        assertThat(useCase.buscarPorId(epiId).estoqueCritico()).isTrue();

        useCase.registrarMovimentacao(epiId, TipoMovimentacao.ENTRADA, 3, "Reposição");

        EpiOutputDTO consultado = useCase.buscarPorId(epiId);
        assertThat(consultado.quantidade()).isEqualTo(6);
        assertThat(consultado.estoqueCritico()).isFalse();
    }

    @Test
    @DisplayName("Saída acima do saldo falha e a consulta preserva saldo 10 e situação não crítica")
    void saidaAcimaDoSaldoPreservaConsulta() {
        assertThatThrownBy(() -> useCase.registrarMovimentacao(epiId, TipoMovimentacao.SAIDA, 11, "Indevida"))
                .isInstanceOf(SaldoInsuficienteException.class);

        EpiOutputDTO consultado = useCase.buscarPorId(epiId);
        assertThat(consultado.quantidade()).isEqualTo(10);
        assertThat(consultado.estoqueCritico()).isFalse();
        assertThat(useCase.buscarHistorico(epiId)).isEmpty();
    }

    /** Guarda o EPI pelo estado devolvido do domínio, como faria o adaptador JPA. */
    private static class EpiRepositoryEmMemoria implements EpiRepository {
        private final Map<Integer, Epi> epis = new HashMap<>();
        private final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
        private int proximoEpiId = 1;
        private int proximaMovimentacaoId = 1;

        @Override
        public Epi salvar(Epi epi) {
            Integer id = epi.getId() != null ? epi.getId() : proximoEpiId++;
            Epi salvo = EpiDeTeste.umEpi()
                    .comId(id)
                    .comNome(epi.getNome())
                    .comCa(epi.getNumeroCa())
                    .comQuantidade(epi.getQuantidade())
                    .comEstoqueMinimo(epi.getEstoqueMinimo())
                    .comStatus(epi.getStatus())
                    .comValidadeCa(epi.getDataValidadeCa())
                    .comVidaUtilDias(epi.getVidaUtilDias())
                    .comDescricao(epi.getEspecificacao().getDescricao())
                    .comClassificacao(epi.getEspecificacao().getClassificacao())
                    .comLocalizacao(epi.getLocalizacao())
                    .build();
            epis.put(id, salvo);
            return salvo;
        }

        @Override
        public Optional<Epi> buscarPorId(Integer id) {
            return Optional.ofNullable(epis.get(id));
        }

        @Override
        public Optional<Epi> buscarPorCA(String numeroCa) {
            return epis.values().stream().filter(epi -> epi.getNumeroCa().equals(numeroCa)).findFirst();
        }

        @Override
        public List<Epi> listarTodos() {
            return new ArrayList<>(epis.values());
        }

        @Override
        public Epi atualizarSaldo(Epi epi) {
            return salvar(epi);
        }

        @Override
        public MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao) {
            MovimentacaoEstoque salva = new MovimentacaoEstoque(proximaMovimentacaoId++, movimentacao.getEpi(),
                    movimentacao.getLote(), movimentacao.getResponsavel(), movimentacao.getTipo(),
                    movimentacao.getQuantidade(), movimentacao.getDataHora(), movimentacao.getMotivo());
            movimentacoes.add(salva);
            return salva;
        }

        @Override
        public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
            return movimentacoes.stream().filter(m -> m.getEpiId().equals(epiId)).toList();
        }
    }
}

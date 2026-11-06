package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
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

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TarefaServiceTest {

    private static final String EMAIL_GESTOR = "mariana@empresa.com";
    private static final String EMAIL_SUPERVISOR = "paulo@empresa.com";

    private TarefaRepositoryFake repository;
    private LogAuditoriaFake auditoria;
    private EpiRepositoryFake epiRepository;
    private TarefaService useCase;
    /** Dublê da porta do operador (#236), no lugar do SecurityContext do Spring. */
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.semSessao();

    @BeforeEach
    void setUp() {
        repository = new TarefaRepositoryFake();
        auditoria = new LogAuditoriaFake();
        epiRepository = new EpiRepositoryFake();
        UsuarioRepositorioFake usuarios = new UsuarioRepositorioFake(List.of(
                new GestorDeSeguranca(7, "49216091040", "Mariana", LocalDate.of(1988, 7, 20), EMAIL_GESTOR,
                        "hash", true, LocalDateTime.now(), LocalDateTime.now()),
                new Supervisor(8, "87455877074", "Paulo", LocalDate.of(1990, 1, 10), EMAIL_SUPERVISOR,
                        "hash", true, LocalDateTime.now(), LocalDateTime.now())));
        useCase = new TarefaService(repository, auditoria, operador.operadorAtual(usuarios), null, null, epiRepository);
        autenticarComo(EMAIL_GESTOR);
    }

    private void autenticarComo(String email) {
        operador.autenticar(email);
    }

    private List<LogAuditoria> classificacoesRegistradas(Integer idTarefa) {
        return auditoria.listarPorEntidade("Tarefa", idTarefa).stream()
                .filter(log -> log.getJustificativa().startsWith("Classificação de periculosidade"))
                .toList();
    }

    @Test
    @DisplayName("Deve cadastrar tarefa já classificada com responsável técnico e histórico")
    void deveCadastrarTarefaClassificada() {
        TarefaOutputDTO salva = useCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Solda em altura", NivelPerigo.ALTO));

        assertThat(salva.id()).isEqualTo(1);
        assertThat(salva.descricao()).isEqualTo("Solda em altura");
        assertThat(salva.nivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(salva.classificada()).isTrue();
        assertThat(salva.dataClassificacao()).isNotNull();
        assertThat(salva.responsavelClassificacaoId()).isEqualTo(7);
        assertThat(salva.responsavelClassificacaoNome()).isEqualTo("Mariana");

        assertThat(classificacoesRegistradas(1)).singleElement().satisfies(log -> {
            assertThat(log.getResponsavel()).isEqualTo(EMAIL_GESTOR);
            assertThat(log.getJustificativa()).contains("de sem classificação para ALTO");
            assertThat(log.getDataHora()).isEqualTo(salva.dataClassificacao());
        });
    }

    @Test
    @DisplayName("UC11: deve cadastrar tarefa sem classificação prévia, sem exigir responsável")
    void deveCadastrarTarefaSemClassificacao() {
        operador.encerrarSessao();

        TarefaOutputDTO salva = useCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Inspeção visual", null));

        assertThat(salva.classificada()).isFalse();
        assertThat(salva.nivelPerigo()).isNull();
        assertThat(salva.dataClassificacao()).isNull();
        assertThat(salva.responsavelClassificacaoId()).isNull();
        assertThat(classificacoesRegistradas(salva.id())).isEmpty();
    }

    @Test
    @DisplayName("UC11: deve classificar tarefa existente registrando responsável técnico")
    void deveClassificarTarefaExistente() {
        Integer id = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Inspeção visual", null)).id();

        TarefaOutputDTO classificada = useCase.classificar(id, NivelPerigo.CRITICO);

        assertThat(classificada.nivelPerigo()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(classificada.classificada()).isTrue();
        assertThat(classificada.responsavelClassificacaoNome()).isEqualTo("Mariana");
        assertThat(useCase.buscarPorId(id).nivelPerigo()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(classificacoesRegistradas(id)).singleElement()
                .satisfies(log -> assertThat(log.getJustificativa())
                        .contains("de sem classificação para CRITICO")
                        .contains("responsável técnico: Mariana (id 7)"));
    }

    @Test
    @DisplayName("UC11 alternativo I: reavaliação substitui o grau e preserva o valor anterior no histórico")
    void reavaliacaoSubstituiGrauAnterior() {
        Integer id = useCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Solda em altura", NivelPerigo.CRITICO)).id();

        useCase.classificar(id, NivelPerigo.BAIXO);

        assertThat(useCase.buscarPorId(id).nivelPerigo()).isEqualTo(NivelPerigo.BAIXO);
        assertThat(classificacoesRegistradas(id))
                .extracting(LogAuditoria::getJustificativa)
                .satisfiesExactly(
                        primeira -> assertThat(primeira).contains("de sem classificação para CRITICO"),
                        segunda -> assertThat(segunda).contains("de CRITICO para BAIXO"));
    }

    @Test
    @DisplayName("Classificação sem Gestor de Segurança identificável é recusada sem gravar nada")
    void classificacaoSemGestorERecusada() {
        Integer id = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Inspeção visual", null)).id();

        String mensagem =
                "A classificação de periculosidade exige um Gestor de Segurança autenticado como responsável técnico.";
        autenticarComo(EMAIL_SUPERVISOR);
        assertThatThrownBy(() -> useCase.classificar(id, NivelPerigo.ALTO))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage(mensagem);

        operador.encerrarSessao();
        assertThatThrownBy(() -> useCase.classificar(id, NivelPerigo.ALTO))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage(mensagem);
        assertThatThrownBy(() -> useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Solda", NivelPerigo.ALTO)))
                .isInstanceOf(AcessoNegadoException.class);

        assertThat(useCase.buscarPorId(id).classificada()).isFalse();
        assertThat(classificacoesRegistradas(id)).isEmpty();
        assertThat(useCase.listar()).hasSize(1);
    }

    @Test
    @DisplayName("Deve rejeitar classificação com nível nulo")
    void deveRejeitarClassificacaoComNivelNulo() {
        Integer id = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Inspeção visual", null)).id();

        assertThatThrownBy(() -> useCase.classificar(id, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nível de perigo da tarefa é obrigatório.");
    }

    @Test
    @DisplayName("Deve falhar ao classificar ou buscar tarefa inexistente")
    void deveFalharParaTarefaInexistente() {
        assertThatThrownBy(() -> useCase.classificar(99, NivelPerigo.ALTO))
                .isInstanceOf(TarefaNaoEncontradaException.class)
                .hasMessageContaining("99");

        assertThatThrownBy(() -> useCase.buscarPorId(99))
                .isInstanceOf(TarefaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve filtrar a matriz de periculosidade pelo grau de perigo")
    void deveFiltrarPorNivelPerigo() {
        useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Solda em altura", NivelPerigo.ALTO));
        useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Pintura", NivelPerigo.BAIXO));
        useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Trabalho em espaço confinado", NivelPerigo.ALTO));

        assertThat(useCase.listarPorNivelPerigo(NivelPerigo.ALTO)).hasSize(2);
        assertThat(useCase.listarPorNivelPerigo(NivelPerigo.BAIXO)).hasSize(1);
        assertThat(useCase.listarPorNivelPerigo(null)).hasSize(3);
        assertThat(useCase.listar()).hasSize(3);
    }

    private static Epi epi(Integer id, String nome) {
        return EpiDeTeste.umEpi()
                .comId(id)
                .comNome(nome)
                .comCa("CA-" + id)
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.now().plusYears(1))
                .comVidaUtilDias(365)
                .build();
    }

    @Test
    @DisplayName("RF12 / UC08: deve associar EPIs a uma tarefa e listar EPIs obrigatórios")
    void deveAssociarEpisEListarObrigatorios() {
        Integer tarefaId = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Trabalho em altura", null)).id();
        Epi capacete = epi(1, "Capacete de Segurança");
        Epi cinto = epi(2, "Cinto Paraquedista");
        epiRepository.adicionar(capacete);
        epiRepository.adicionar(cinto);

        TarefaOutputDTO resultado = useCase.associarEpis(tarefaId, List.of(1, 2));

        assertThat(resultado.episIds()).containsExactly(1, 2);

        List<EpiOutputDTO> epis = useCase.listarEpisObrigatorios(tarefaId);
        assertThat(epis).hasSize(2);
        assertThat(epis.stream().map(EpiOutputDTO::id)).containsExactly(1, 2);
        assertThat(epis.stream().map(EpiOutputDTO::nome)).containsExactly("Capacete de Segurança", "Cinto Paraquedista");
    }

    @Test
    @DisplayName("RF12 / UC08: deve falhar ao associar EPI inexistente")
    void deveFalharAoAssociarEpiInexistente() {
        Integer tarefaId = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Solda", null)).id();

        assertThatThrownBy(() -> useCase.associarEpis(tarefaId, List.of(999)))
                .isInstanceOf(EpiNaoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("RF12 / UC08: deve falhar ao associar com id de EPI nulo")
    void deveFalharAoAssociarEpiNulo() {
        Integer tarefaId = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Solda", null)).id();
        List<Integer> ids = new ArrayList<>();
        ids.add(null);

        assertThatThrownBy(() -> useCase.associarEpis(tarefaId, ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Identificador de EPI não pode ser nulo.");
    }

    @Test
    @DisplayName("RF12 / UC08: deve falhar ao associar EPIs a tarefa inexistente")
    void deveFalharAoAssociarEpisATarefaInexistente() {
        assertThatThrownBy(() -> useCase.associarEpis(999, List.of(1)))
                .isInstanceOf(TarefaNaoEncontradaException.class);

        assertThatThrownBy(() -> useCase.listarEpisObrigatorios(999))
                .isInstanceOf(TarefaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("RF12 / UC08: deve permitir associar lista vazia de EPIs desvinculando anteriores")
    void devePermitirListaVaziaDeEpis() {
        Integer tarefaId = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Inspeção", null)).id();
        Epi luva = epi(5, "Luva");
        epiRepository.adicionar(luva);

        useCase.associarEpis(tarefaId, List.of(5));
        assertThat(useCase.listarEpisObrigatorios(tarefaId)).hasSize(1);

        TarefaOutputDTO resultado = useCase.associarEpis(tarefaId, List.of());
        assertThat(resultado.episIds()).isEmpty();
        assertThat(useCase.listarEpisObrigatorios(tarefaId)).isEmpty();
    }

    @Test
    @DisplayName("RNF05 (#193): o vínculo de EPIs registra em LogAuditoria os EPIs anteriores e os novos")
    void vinculoDeEpisRegistraAuditoriaComValorAnteriorENovo() {
        Integer tarefaId = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Inspeção de telhado", null)).id();
        epiRepository.adicionar(epi(5, "Luva"));
        epiRepository.adicionar(epi(6, "Cinto"));

        useCase.associarEpis(tarefaId, List.of(5));
        useCase.associarEpis(tarefaId, List.of(6));

        assertThat(auditoria.listarPorEntidade("Tarefa", tarefaId))
                .extracting(LogAuditoria::getJustificativa)
                .filteredOn(texto -> texto.startsWith("EPIs obrigatórios da tarefa"))
                .containsExactlyInAnyOrder(
                        "EPIs obrigatórios da tarefa: de nenhum para [Luva (id 5)]",
                        "EPIs obrigatórios da tarefa: de [Luva (id 5)] para [Cinto (id 6)]");
    }

    @Test
    @DisplayName("Deve atualizar descrição, área de risco e setores da tarefa gravando LogAuditoria de ALTERACAO")
    void deveAtualizarTarefa() {
        TarefaOutputDTO cadastrada = useCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Solda simples", null));

        TarefaOutputDTO atualizada = useCase.atualizarTarefa(cadastrada.id(),
                new br.edu.safeplace.backend.application.dto.input.AtualizarTarefaInputDTO(
                        "Solda complexa em altura", null, List.of()), EMAIL_GESTOR);

        assertThat(atualizada.descricao()).isEqualTo("Solda complexa em altura");

        assertThat(auditoria.listarPorEntidade("Tarefa", cadastrada.id()))
                .filteredOn(log -> "ALTERACAO".equals(log.getTipoOperacao()))
                .singleElement()
                .satisfies(log -> {
                    assertThat(log.getResponsavel()).isEqualTo(EMAIL_GESTOR);
                    assertThat(log.getJustificativa()).contains("Solda complexa em altura");
                });
    }

    private static class TarefaRepositoryFake implements TarefaRepository {
        private final Map<Integer, Tarefa> storage = new HashMap<>();
        private int nextId = 1;

        @Override
        public Tarefa salvar(Tarefa tarefa) {
            Integer id = tarefa.getId() != null ? tarefa.getId() : nextId++;
            Tarefa salva = new Tarefa(id, tarefa.getDescricao(), tarefa.getNivelPerigo(),
                    tarefa.getDataClassificacao(), tarefa.getResponsavelClassificacao(),
                    tarefa.getAreaRiscoId(), tarefa.getSetoresIds(), tarefa.getEpisObrigatorios());
            storage.put(id, salva);
            return salva;
        }

        @Override
        public Optional<Tarefa> buscarPorId(Integer id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Tarefa> listarTodas() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<Tarefa> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
            return storage.values().stream()
                    .filter(tarefa -> tarefa.getNivelPerigo() == nivelPerigo)
                    .toList();
        }
    }

    private static class EpiRepositoryFake implements EpiRepository {
        private final Map<Integer, Epi> storage = new HashMap<>();

        void adicionar(Epi epi) {
            storage.put(epi.getId(), epi);
        }

        @Override
        public Epi salvar(Epi epi) {
            storage.put(epi.getId(), epi);
            return epi;
        }

        @Override
        public Optional<Epi> buscarPorId(Integer id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Epi> buscarPorCA(String numeroCa) {
            return storage.values().stream().filter(e -> numeroCa.equals(e.getNumeroCa())).findFirst();
        }

        @Override
        public List<Epi> listarTodos() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Epi atualizarSaldo(Epi epi) {
            return salvar(epi);
        }

        @Override
        public MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao) {
            return movimentacao;
        }

        @Override
        public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
            return List.of();
        }
    }

    private static class LogAuditoriaFake implements LogAuditoriaRepository {
        private final List<LogAuditoria> logs = new ArrayList<>();

        @Override
        public LogAuditoria salvar(LogAuditoria log) {
            logs.add(log);
            return log;
        }

        @Override
        public List<LogAuditoria> listarPorEntidade(String entidade, Integer entidadeId) {
            return logs.stream()
                    .filter(log -> entidade.equals(log.getEntidade()) && entidadeId.equals(log.getEntidadeId()))
                    .toList();
        }
    }

    private static class UsuarioRepositorioFake implements UsuarioRepository {
        private final List<Colaborador> pessoas;

        UsuarioRepositorioFake(List<Colaborador> pessoas) {
            this.pessoas = pessoas;
        }

        @Override
        public Colaborador salvar(Colaborador colaborador) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Colaborador> buscarPorId(Integer id) {
            return pessoas.stream().filter(p -> id.equals(p.getId())).findFirst();
        }

        @Override
        public Optional<Colaborador> buscarPorCpf(String cpf) {
            return pessoas.stream().filter(p -> cpf.equals(p.getCpf())).findFirst();
        }

        @Override
        public Optional<Colaborador> buscarPorEmail(String email) {
            return pessoas.stream().filter(p -> email.equals(p.getEmail())).findFirst();
        }

        @Override
        public List<Colaborador> listarTodos() {
            return pessoas;
        }

        @Override
        public List<Colaborador> listarColaboradores(String nome, String cpf) {
            return List.of();
        }
    }
}

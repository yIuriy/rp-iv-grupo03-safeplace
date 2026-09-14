package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AreaRiscoUseCaseTest {

    private AreaRiscoRepositoryFake areaRepository;
    private EpiRepositoryFake epiRepository;
    private AreaRiscoUseCase useCase;

    @BeforeEach
    void setUp() {
        areaRepository = new AreaRiscoRepositoryFake();
        epiRepository = new EpiRepositoryFake();
        epiRepository.adicionar(epi(1, "Capacete"));
        epiRepository.adicionar(epi(2, "Luva térmica"));
        useCase = new AreaRiscoUseCase(areaRepository, epiRepository);
    }

    private static Epi epi(Integer id, String nome) {
        return new Epi(id, nome, "CA-" + (1000 + id), 10, 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), 365, null, null);
    }

    private static CadastrarAreaRiscoInputDTO input(String codigo, NivelPerigo nivel, List<Integer> epis) {
        return new CadastrarAreaRiscoInputDTO(codigo, "Linha de Produção", "Solda e montagem", nivel, epis);
    }

    @Test
    @DisplayName("Deve cadastrar área de risco com os EPIs obrigatórios resolvidos do catálogo")
    void deveCadastrarAreaRiscoComEpisResolvidos() {
        AreaRiscoOutputDTO salva = useCase.cadastrarAreaRisco(input("SET-01", NivelPerigo.ALTO, List.of(1, 2)));

        assertThat(salva.id()).isEqualTo(1);
        assertThat(salva.codigo()).isEqualTo("SET-01");
        assertThat(salva.nivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(salva.episObrigatorios()).hasSize(2);
        assertThat(salva.episObrigatorios().get(0).nome()).isEqualTo("Capacete");
        // o domínio normaliza o CA removendo o prefixo "CA-"
        assertThat(salva.episObrigatorios().get(0).numeroCa()).isEqualTo("1001");
    }

    @Test
    @DisplayName("UC03 exceção I: deve rejeitar código de setor já cadastrado")
    void deveRejeitarCodigoDuplicado() {
        useCase.cadastrarAreaRisco(input("SET-01", NivelPerigo.ALTO, List.of(1)));

        assertThatThrownBy(() -> useCase.cadastrarAreaRisco(input("SET-01", NivelPerigo.BAIXO, List.of(2))))
                .isInstanceOf(CodigoAreaRiscoDuplicadoException.class)
                .hasMessageContaining("SET-01");
    }

    @Test
    @DisplayName("UC03 exceção I: a verificação de duplicidade ignora caixa e espaços")
    void verificacaoDeDuplicidadeIgnoraCaixaEEspacos() {
        useCase.cadastrarAreaRisco(input("SET-01", NivelPerigo.ALTO, List.of(1)));

        assertThatThrownBy(() -> useCase.cadastrarAreaRisco(input(" set-01 ", NivelPerigo.BAIXO, List.of(2))))
                .isInstanceOf(CodigoAreaRiscoDuplicadoException.class);
    }

    @Test
    @DisplayName("UC03 exceção II: deve bloquear cadastro sem EPIs obrigatórios e não persistir nada")
    void deveBloquearCadastroSemEpisObrigatorios() {
        assertThatThrownBy(() -> useCase.cadastrarAreaRisco(input("SET-02", NivelPerigo.CRITICO, List.of())))
                .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class);

        assertThat(areaRepository.listarTodas()).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar vínculo com EPI inexistente no catálogo")
    void deveRejeitarEpiInexistente() {
        assertThatThrownBy(() -> useCase.cadastrarAreaRisco(input("SET-03", NivelPerigo.MEDIO, List.of(99))))
                .isInstanceOf(EpiNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Deve ignorar IDs de EPI repetidos no formulário")
    void deveIgnorarEpisRepetidos() {
        AreaRiscoOutputDTO salva = useCase.cadastrarAreaRisco(input("SET-04", NivelPerigo.BAIXO, List.of(1, 1, 2)));

        assertThat(salva.episObrigatorios()).hasSize(2);
    }

    @Test
    @DisplayName("UC03 alternativo I: deve filtrar o mapa de riscos pelo grau de perigo")
    void deveFiltrarPorNivelPerigo() {
        useCase.cadastrarAreaRisco(input("SET-01", NivelPerigo.ALTO, List.of(1)));
        useCase.cadastrarAreaRisco(input("SET-02", NivelPerigo.BAIXO, List.of(1)));
        useCase.cadastrarAreaRisco(input("SET-03", NivelPerigo.ALTO, List.of(2)));

        assertThat(useCase.listarPorNivelPerigo(NivelPerigo.ALTO)).hasSize(2);
        assertThat(useCase.listarPorNivelPerigo(NivelPerigo.BAIXO)).hasSize(1);
        assertThat(useCase.listarPorNivelPerigo(NivelPerigo.CRITICO)).isEmpty();
    }

    @Test
    @DisplayName("Filtro sem nível informado devolve o mapa completo")
    void filtroSemNivelDevolveMapaCompleto() {
        useCase.cadastrarAreaRisco(input("SET-01", NivelPerigo.ALTO, List.of(1)));
        useCase.cadastrarAreaRisco(input("SET-02", NivelPerigo.BAIXO, List.of(1)));

        assertThat(useCase.listarPorNivelPerigo(null)).hasSize(2);
        assertThat(useCase.listar()).hasSize(2);
    }

    @Test
    @DisplayName("Deve buscar área por ID e falhar quando não existe")
    void deveBuscarPorIdEFalharQuandoNaoExiste() {
        useCase.cadastrarAreaRisco(input("SET-01", NivelPerigo.ALTO, List.of(1)));

        assertThat(useCase.buscarPorId(1).codigo()).isEqualTo("SET-01");
        assertThatThrownBy(() -> useCase.buscarPorId(99))
                .isInstanceOf(AreaRiscoNaoEncontradaException.class)
                .hasMessageContaining("99");
    }

    private static class AreaRiscoRepositoryFake implements AreaRiscoRepositoryPort {
        private final Map<Integer, AreaRisco> storage = new HashMap<>();
        private int nextId = 1;

        @Override
        public AreaRisco salvar(AreaRisco areaRisco) {
            Integer id = areaRisco.getId() != null ? areaRisco.getId() : nextId++;
            AreaRisco salva = new AreaRisco(id, areaRisco.getCodigo(), areaRisco.getNome(),
                    areaRisco.getDescricao(), areaRisco.getNivelPerigo(),
                    areaRisco.verificarEpisObrigatoriosArea());
            storage.put(id, salva);
            return salva;
        }

        @Override
        public Optional<AreaRisco> buscarPorId(Integer id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<AreaRisco> buscarPorCodigo(String codigo) {
            return storage.values().stream()
                    .filter(area -> area.getCodigo() != null && area.getCodigo().equals(codigo))
                    .findFirst();
        }

        @Override
        public List<AreaRisco> listarTodas() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<AreaRisco> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
            return storage.values().stream()
                    .filter(area -> area.getNivelPerigo() == nivelPerigo)
                    .toList();
        }
    }

    private static class EpiRepositoryFake implements EpiRepositoryPort {
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
            return storage.values().stream()
                    .filter(epi -> epi.getNumeroCa().equals(numeroCa))
                    .findFirst();
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
            throw new UnsupportedOperationException("Não usado neste teste.");
        }

        @Override
        public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
            return List.of();
        }
    }
}

package br.edu.safeplace.backend.application.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TarefaUseCaseTest {

    private TarefaRepositoryFake repository;
    private TarefaUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new TarefaRepositoryFake();
        useCase = new TarefaUseCase(repository);
    }

    @Test
    @DisplayName("Deve cadastrar tarefa já classificada")
    void deveCadastrarTarefaClassificada() {
        TarefaOutputDTO salva = useCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Solda em altura", NivelPerigo.ALTO));

        assertThat(salva.id()).isEqualTo(1);
        assertThat(salva.descricao()).isEqualTo("Solda em altura");
        assertThat(salva.nivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(salva.classificada()).isTrue();
        assertThat(salva.dataClassificacao()).isNotNull();
    }

    @Test
    @DisplayName("UC11: deve cadastrar tarefa sem classificação prévia")
    void deveCadastrarTarefaSemClassificacao() {
        TarefaOutputDTO salva = useCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Inspeção visual", null));

        assertThat(salva.classificada()).isFalse();
        assertThat(salva.nivelPerigo()).isNull();
        assertThat(salva.dataClassificacao()).isNull();
    }

    @Test
    @DisplayName("UC11: deve classificar tarefa existente e persistir a alteração")
    void deveClassificarTarefaExistente() {
        Integer id = useCase.cadastrarTarefa(new CadastrarTarefaInputDTO("Inspeção visual", null)).id();

        TarefaOutputDTO classificada = useCase.classificar(id, NivelPerigo.CRITICO);

        assertThat(classificada.nivelPerigo()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(classificada.classificada()).isTrue();
        assertThat(useCase.buscarPorId(id).nivelPerigo()).isEqualTo(NivelPerigo.CRITICO);
    }

    @Test
    @DisplayName("UC11 alternativo I: reavaliação substitui o grau anterior")
    void reavaliacaoSubstituiGrauAnterior() {
        Integer id = useCase.cadastrarTarefa(
                new CadastrarTarefaInputDTO("Solda em altura", NivelPerigo.CRITICO)).id();

        useCase.classificar(id, NivelPerigo.BAIXO);

        assertThat(useCase.buscarPorId(id).nivelPerigo()).isEqualTo(NivelPerigo.BAIXO);
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

    private static class TarefaRepositoryFake implements TarefaRepositoryPort {
        private final Map<Integer, Tarefa> storage = new HashMap<>();
        private int nextId = 1;

        @Override
        public Tarefa salvar(Tarefa tarefa) {
            Integer id = tarefa.getId() != null ? tarefa.getId() : nextId++;
            Tarefa salva = new Tarefa(id, tarefa.getDescricao(), tarefa.getNivelPerigo(),
                    tarefa.getDataClassificacao());
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
}

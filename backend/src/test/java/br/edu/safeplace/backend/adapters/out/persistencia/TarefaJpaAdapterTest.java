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

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - TarefaJpaAdapter")
class TarefaJpaAdapterTest {

    @Mock
    private TarefaJpaRepository tarefaRepository;

    @Mock
    private UsuarioJpaRepositorio usuarioRepository;

    @Mock
    private AreaRiscoJpaRepository areaRiscoRepository;

    @Mock
    private SetorJpaRepository setorRepository;

    @Mock
    private EpiJpaRepository epiRepository;

    private TarefaJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TarefaJpaAdapter(
                tarefaRepository,
                usuarioRepository,
                areaRiscoRepository,
                setorRepository,
                epiRepository);
    }

    private static GestorDeSeguranca gestor(Integer id) {
        return new GestorDeSeguranca(id, "49216091040", "Mariana", LocalDate.of(1988, 7, 20),
                "mariana@empresa.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private static Epi epi(Integer id, String nome) {
        return new Epi(id, nome, "CA-" + id, 10, 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), 365, null, null);
    }

    private static EpiEntity epiEntity(Integer id, String nome) {
        LoteEpiEntity lote = new LoteEpiEntity(id, null, null, null, null, 10,
                new ModeloEpiEntity(id, id, null, LocalDate.now().plusYears(1)));
        return new EpiEntity(id, nome, 10, 2, "DISPONIVEL", 365, "Descrição", null, null, lote);
    }

    @Test
    @DisplayName("Deve salvar tarefa com EPIs vinculados chamando repositório de EPIs")
    void deveSalvarTarefaComEpis() {
        Tarefa tarefa = Tarefa.cadastrar("Trabalho em altura", NivelPerigo.ALTO, gestor(1));
        Epi cinto = epi(10, "Cinto Paraquedista");
        tarefa.associarEpis(List.of(cinto));

        GestorDeSegurancaEntidade gestorEntidade = new GestorDeSegurancaEntidade(
                1, "49216091040", "Mariana", LocalDate.of(1988, 7, 20), "mariana@empresa.com", "hash",
                true, LocalDateTime.now(), LocalDateTime.now());
        EpiEntity cintoEntity = epiEntity(10, "Cinto Paraquedista");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(gestorEntidade));
        when(epiRepository.findAllById(List.of(10))).thenReturn(List.of(cintoEntity));

        TarefaEntity entitySalva = new TarefaEntity(
                5, "Trabalho em altura", "ALTO", LocalDateTime.now(), gestorEntidade, null, List.of(), List.of(cintoEntity));
        when(tarefaRepository.save(any(TarefaEntity.class))).thenReturn(entitySalva);

        Tarefa resultado = adapter.salvar(tarefa);

        ArgumentCaptor<TarefaEntity> captor = ArgumentCaptor.forClass(TarefaEntity.class);
        verify(tarefaRepository).save(captor.capture());
        assertThat(captor.getValue().getEpis()).containsExactly(cintoEntity);

        assertThat(resultado.getId()).isEqualTo(5);
        assertThat(resultado.getEpisObrigatorios()).hasSize(1);
        assertThat(resultado.getEpisObrigatorios().get(0).getId()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve buscar tarefa por id e reconstituir lista de EPIs")
    void deveBuscarPorIdComEpis() {
        EpiEntity capaceteEntity = epiEntity(20, "Capacete de Segurança");
        TarefaEntity entity = new TarefaEntity(
                3, "Inspeção de tubulação", "MEDIO", LocalDateTime.now(), null, null, List.of(), List.of(capaceteEntity));

        when(tarefaRepository.findById(3)).thenReturn(Optional.of(entity));

        Optional<Tarefa> resultado = adapter.buscarPorId(3);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(3);
        assertThat(resultado.get().getEpisObrigatorios()).hasSize(1);
        assertThat(resultado.get().getEpisObrigatorios().get(0).getId()).isEqualTo(20);
        assertThat(resultado.get().getEpisObrigatorios().get(0).getNome()).isEqualTo("Capacete de Segurança");
    }

    @Test
    @DisplayName("Deve listar todas as tarefas mantendo EPIs obrigatórios")
    void deveListarTodasComEpis() {
        EpiEntity luvaEntity = epiEntity(30, "Luva de Raspa");
        TarefaEntity entity = new TarefaEntity(
                1, "Solda", "ALTO", LocalDateTime.now(), null, null, List.of(), List.of(luvaEntity));

        when(tarefaRepository.findAll()).thenReturn(List.of(entity));

        List<Tarefa> resultado = adapter.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEpisObrigatorios()).hasSize(1);
        assertThat(resultado.get(0).getEpisObrigatorios().get(0).getId()).isEqualTo(30);
    }
}

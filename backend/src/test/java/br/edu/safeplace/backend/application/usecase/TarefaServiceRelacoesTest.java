package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

/**
 * Área e setores são associações por objeto (#260): o caso de uso os resolve pelas portas antes de montar a
 * tarefa, com as mensagens que a cadeia de validação usava para ids inexistentes.
 */
class TarefaServiceRelacoesTest {

    private final TarefaRepository tarefas = mock(TarefaRepository.class);
    private final AreaRiscoRepository areas = mock(AreaRiscoRepository.class);
    private final SetorRepository setores = mock(SetorRepository.class);
    private final AreaRisco area = mock(AreaRisco.class);
    private final Setor setor = mock(Setor.class);
    private TarefaService service;

    @BeforeEach
    void setUp() {
        when(area.getId()).thenReturn(1);
        when(setor.getId()).thenReturn(2);
        when(areas.buscarPorId(anyInt())).thenReturn(Optional.empty());
        when(areas.buscarPorId(1)).thenReturn(Optional.of(area));
        when(setores.buscarPorId(anyInt())).thenReturn(Optional.empty());
        when(setores.buscarPorId(2)).thenReturn(Optional.of(setor));
        when(tarefas.salvar(any(Tarefa.class))).thenAnswer(chamada -> chamada.getArgument(0));
        service = new TarefaService(tarefas, new TrilhaAuditoriaEmMemoria(),
                OperadorAutenticadoFixo.semSessao().operadorAtual(mock(UsuarioRepository.class)), areas, setores,
                mock(EpiRepository.class));
    }

    private static CadastrarTarefaInputDTO tarefa(Integer areaId, List<Integer> setoresIds) {
        return new CadastrarTarefaInputDTO("Inspeção visual", null, areaId, setoresIds);
    }

    @Test
    void associaAAreaEOsSetoresResolvidos() {
        TarefaOutputDTO salva = service.cadastrarTarefa(tarefa(1, List.of(2, 2)));

        assertThat(salva.areaRiscoId()).isEqualTo(1);
        assertThat(salva.setoresIds()).containsExactly(2);
    }

    @Test
    void recusaAreaInexistenteSemGravar() {
        assertThatThrownBy(() -> service.cadastrarTarefa(tarefa(99, List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Área de risco não encontrada com id: 99");
        verify(tarefas, never()).salvar(any());
    }

    @Test
    void recusaIdDeSetorNulo() {
        assertThatThrownBy(() -> service.cadastrarTarefa(tarefa(null, Arrays.asList(2, null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Identificador de setor não pode ser nulo.");
    }

    @Test
    void recusaSetorInexistenteSemGravar() {
        assertThatThrownBy(() -> service.cadastrarTarefa(tarefa(null, List.of(2, 77))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setor não encontrado com id: 77");
        verify(tarefas, never()).salvar(any());
    }

    @Test
    void conferePrimeiroAAreaComoACadeiaFazia() {
        assertThatThrownBy(() -> service.cadastrarTarefa(tarefa(99, List.of(77))))
                .hasMessage("Área de risco não encontrada com id: 99");
    }
}

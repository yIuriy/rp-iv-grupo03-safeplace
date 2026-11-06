package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

/** Sem classe espelhada (#260): o adaptador só repassa a entidade de domínio ao Spring Data. */
class TarefaJpaAdapterTest {

    private final TarefaJpaRepository repositorio = mock(TarefaJpaRepository.class);
    private final TarefaJpaAdapter adapter = new TarefaJpaAdapter(repositorio);

    @Test
    void salvarDevolveATarefaGravadaPeloRepositorio() {
        Tarefa nova = Tarefa.cadastrar("Solda");
        Tarefa gravada = new Tarefa(5, "Solda", null, null);
        when(repositorio.save(nova)).thenReturn(gravada);

        assertThat(adapter.salvar(nova)).isSameAs(gravada);
    }

    @Test
    void consultasDelegamAoRepositorio() {
        Tarefa tarefa = new Tarefa(3, "Pintura", NivelPerigo.BAIXO, null);
        when(repositorio.findById(3)).thenReturn(Optional.of(tarefa));
        when(repositorio.findAll()).thenReturn(List.of(tarefa));
        when(repositorio.findByNivelPerigo(NivelPerigo.BAIXO)).thenReturn(List.of(tarefa));

        assertThat(adapter.buscarPorId(3)).containsSame(tarefa);
        assertThat(adapter.listarTodas()).containsExactly(tarefa);
        assertThat(adapter.listarPorNivelPerigo(NivelPerigo.BAIXO)).containsExactly(tarefa);
    }
}

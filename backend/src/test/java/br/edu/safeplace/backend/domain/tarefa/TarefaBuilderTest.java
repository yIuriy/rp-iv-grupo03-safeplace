package br.edu.safeplace.backend.domain.tarefa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

@DisplayName("Builder (#240): TarefaBuilder em código de produção")
class TarefaBuilderTest {

    @Test
    @DisplayName("Monta Tarefa com todos os atributos nomeados")
    void montaTarefaComTodosAtributos() {
        GestorDeSeguranca gestor = mock(GestorDeSeguranca.class);
        AreaRisco area = mock(AreaRisco.class);
        Setor setor = mock(Setor.class);
        Epi epi = mock(Epi.class);
        LocalDateTime data = LocalDateTime.of(2026, 11, 7, 10, 0);

        Tarefa tarefa = new TarefaBuilder()
                .id(42)
                .descricao(" Manutenção preventiva ")
                .nivelPerigo(NivelPerigo.ALTO)
                .dataClassificacao(data)
                .responsavelClassificacao(gestor)
                .areaRisco(area)
                .setores(List.of(setor, setor))
                .episObrigatorios(List.of(epi))
                .build();

        assertThat(tarefa.getId()).isEqualTo(42);
        assertThat(tarefa.getDescricao()).isEqualTo("Manutenção preventiva");
        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(tarefa.getDataClassificacao()).isEqualTo(data);
        assertThat(tarefa.getResponsavelClassificacao()).isSameAs(gestor);
        assertThat(tarefa.getAreaRisco()).isSameAs(area);
        assertThat(tarefa.getSetores()).containsExactly(setor);
        assertThat(tarefa.getEpisObrigatorios()).containsExactly(epi);
    }

    @Test
    @DisplayName("Gera data de classificação automática quando nível informado sem data")
    void geraDataClassificacaoAutomatica() {
        GestorDeSeguranca gestor = mock(GestorDeSeguranca.class);

        Tarefa tarefa = Tarefa.builder()
                .descricao("Solda")
                .nivelPerigo(NivelPerigo.MEDIO)
                .responsavelClassificacao(gestor)
                .build();

        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.MEDIO);
        assertThat(tarefa.getDataClassificacao()).isNotNull();
        assertThat(tarefa.getResponsavelClassificacao()).isSameAs(gestor);
    }

    @Test
    @DisplayName("Builder não valida: cria tarefa mesmo sem campos obrigatórios")
    void builderNaoValida() {
        Tarefa tarefa = new TarefaBuilder()
                .descricao(null)
                .nivelPerigo(null)
                .build();

        assertThat(tarefa.getDescricao()).isNull();
        assertThat(tarefa.estaClassificada()).isFalse();
    }
}

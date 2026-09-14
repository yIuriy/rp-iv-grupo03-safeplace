package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TarefaTest {

    @Test
    @DisplayName("Deve cadastrar tarefa já classificada registrando a data da classificação")
    void deveCadastrarTarefaClassificada() {
        Tarefa tarefa = Tarefa.cadastrar("Solda em altura", NivelPerigo.ALTO);

        assertThat(tarefa.getId()).isNull();
        assertThat(tarefa.getDescricao()).isEqualTo("Solda em altura");
        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(tarefa.estaClassificada()).isTrue();
        assertThat(tarefa.getDataClassificacao()).isNotNull();
    }

    @Test
    @DisplayName("UC11: deve permitir tarefa sem classificação prévia")
    void devePermitirTarefaSemClassificacao() {
        Tarefa tarefa = Tarefa.cadastrar("Inspeção visual", null);

        assertThat(tarefa.estaClassificada()).isFalse();
        assertThat(tarefa.getNivelPerigo()).isNull();
        assertThat(tarefa.getDataClassificacao()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar descrição nula ou em branco")
    void deveRejeitarDescricaoNulaOuEmBranco(String descricaoInvalida) {
        assertThatThrownBy(() -> Tarefa.cadastrar(descricaoInvalida, NivelPerigo.BAIXO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Descrição da tarefa é obrigatória.");
    }

    @Test
    @DisplayName("UC11: deve classificar tarefa ainda sem grau de risco")
    void deveClassificarTarefaSemGrauDeRisco() {
        Tarefa tarefa = Tarefa.cadastrar("Inspeção visual", null);

        tarefa.classificar(NivelPerigo.MEDIO);

        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.MEDIO);
        assertThat(tarefa.estaClassificada()).isTrue();
        assertThat(tarefa.getDataClassificacao()).isNotNull();
    }

    @Test
    @DisplayName("UC11 alternativo I: reavaliação atualiza o grau e a data da classificação")
    void reavaliacaoAtualizaGrauEData() {
        Tarefa tarefa = new Tarefa(1, "Solda em altura", NivelPerigo.CRITICO,
                LocalDateTime.of(2020, 1, 1, 8, 0));

        tarefa.classificar(NivelPerigo.BAIXO);

        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.BAIXO);
        assertThat(tarefa.getDataClassificacao()).isAfter(LocalDateTime.of(2020, 1, 1, 8, 0));
    }

    @Test
    @DisplayName("Deve rejeitar classificação com nível nulo")
    void deveRejeitarClassificacaoComNivelNulo() {
        Tarefa tarefa = Tarefa.cadastrar("Inspeção visual", null);

        assertThatThrownBy(() -> tarefa.classificar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nível de perigo da tarefa é obrigatório.");
    }

    @Test
    @DisplayName("UC11 exceção I: deve bloquear alocação em tarefa sem grau de risco cadastrado")
    void deveBloquearAlocacaoEmTarefaSemClassificacao() {
        Tarefa tarefa = Tarefa.cadastrar("Inspeção visual", null);

        assertThatThrownBy(tarefa::validarClassificacaoParaAlocacao)
                .isInstanceOf(TarefaSemClassificacaoException.class)
                .hasMessageContaining("Inspeção visual");

        tarefa.classificar(NivelPerigo.BAIXO);

        assertThatCode(tarefa::validarClassificacaoParaAlocacao).doesNotThrowAnyException();
    }
}

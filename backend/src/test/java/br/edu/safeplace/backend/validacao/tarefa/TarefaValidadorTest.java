package br.edu.safeplace.backend.validacao.tarefa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

@DisplayName("TarefaValidador - cadeia de validação da tarefa (Chain of Responsibility)")
class TarefaValidadorTest {

    private AreaRiscoRepository areas;
    private SetorRepository setores;
    private TarefaValidador validador;

    @BeforeEach
    void setUp() {
        areas = mock(AreaRiscoRepository.class);
        setores = mock(SetorRepository.class);
        when(areas.buscarPorId(anyInt())).thenReturn(Optional.empty());
        when(areas.buscarPorId(1)).thenReturn(Optional.of(mock(AreaRisco.class)));
        when(setores.buscarPorId(anyInt())).thenReturn(Optional.empty());
        when(setores.buscarPorId(2)).thenReturn(Optional.of(mock(Setor.class)));
        validador = new TarefaValidador(areas, setores);
    }

    @Test
    @DisplayName("Aceita tarefa com descrição, área e setores existentes")
    void aceitaTarefaValida() {
        Tarefa tarefa = Tarefa.cadastrarComRelacoes("Inspeção visual", 1, List.of(2));

        assertThatCode(() -> validador.validar(tarefa)).doesNotThrowAnyException();
        assertThat(validador.verificar(tarefa).valido()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Rejeita descrição nula ou em branco (regra que saiu do construtor de Tarefa)")
    void rejeitaDescricaoNulaOuEmBranco(String descricaoInvalida) {
        Tarefa tarefa = Tarefa.cadastrar(descricaoInvalida);

        assertThatThrownBy(() -> validador.validar(tarefa))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Descrição da tarefa é obrigatória.");
    }

    @Test
    @DisplayName("Rejeita área de risco inexistente")
    void rejeitaAreaInexistente() {
        Tarefa tarefa = Tarefa.cadastrarComRelacoes("Inspeção visual", 99, List.of());

        assertThatThrownBy(() -> validador.validar(tarefa))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Área de risco não encontrada com id: 99");
    }

    @Test
    @DisplayName("Rejeita identificador de setor nulo")
    void rejeitaSetorNulo() {
        Tarefa tarefa = Tarefa.cadastrarComRelacoes("Inspeção visual", null, Arrays.asList(2, null));

        assertThatThrownBy(() -> validador.validar(tarefa))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Identificador de setor não pode ser nulo.");
    }

    @Test
    @DisplayName("Rejeita setor inexistente")
    void rejeitaSetorInexistente() {
        Tarefa tarefa = Tarefa.cadastrarComRelacoes("Inspeção visual", null, List.of(2, 77));

        assertThatThrownBy(() -> validador.validar(tarefa))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setor não encontrado com id: 77");
    }

    @Test
    @DisplayName("Lista todos os problemas na ordem dos elos; validar lança o primeiro, como antes")
    void listaProblemasNaOrdemDosElos() {
        Tarefa tarefa = Tarefa.cadastrarComRelacoes(" ", 99, List.of(77));

        assertThat(validador.verificar(tarefa).mensagens()).containsExactly(
                "Área de risco não encontrada com id: 99",
                "Setor não encontrado com id: 77",
                "Descrição da tarefa é obrigatória.");
        assertThatThrownBy(() -> validador.validar(tarefa))
                .hasMessage("Área de risco não encontrada com id: 99");
    }

    @Test
    @DisplayName("Sem as portas de área e setor, valida só a descrição")
    void semPortasValidaSoADescricao() {
        TarefaValidador semPortas = new TarefaValidador(null, null);

        assertThatCode(() -> semPortas.validar(Tarefa.cadastrarComRelacoes("Inspeção visual", 99, List.of(77))))
                .doesNotThrowAnyException();
    }
}

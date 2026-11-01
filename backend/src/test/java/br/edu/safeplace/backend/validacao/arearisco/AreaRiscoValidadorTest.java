package br.edu.safeplace.backend.validacao.arearisco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

/**
 * Os casos de recusa vieram de {@code AreaRiscoTest} com as mesmas asserções (issue #263): antes, quem
 * lançava era {@code AreaRisco.cadastrar} e o construtor; agora é a cadeia, antes do repository.
 */
@DisplayName("AreaRiscoValidador - cadeia de validação da área de risco (Chain of Responsibility)")
class AreaRiscoValidadorTest {

    private final AreaRiscoValidador validador = new AreaRiscoValidador(null, null);

    private static Epi epi(Integer id, String nome) {
        return new Epi(id, nome, "CA-" + id, 10, 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), 365, null, null);
    }

    private static AreaRisco area(Integer id, String codigo, String nome) {
        return new AreaRisco(id, codigo, nome, null, NivelPerigo.ALTO, null, List.of("Ruído"),
                "Galpão 3", List.of(epi(1, "Capacete")));
    }

    @Test
    @DisplayName("Aceita área com código, nível, agentes, limites, EPIs e nome")
    void aceitaAreaValida() {
        assertThatCode(() -> validador.validar(area(null, "SET-01", "Caldeiras"))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar nome nulo ou em branco")
    void deveRejeitarNomeNuloOuEmBranco(String nomeInvalido) {
        assertThatThrownBy(() -> validador.validar(area(1, "SET-01", nomeInvalido)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da área de risco é obrigatório.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("UC03: deve rejeitar cadastro sem código identificador do setor")
    void deveRejeitarCadastroSemCodigo(String codigoInvalido) {
        assertThatThrownBy(() -> validador.validar(AreaRisco.cadastrar(codigoInvalido, "Caldeiras", null,
                NivelPerigo.ALTO, List.of(epi(1, "Capacete")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código da área de risco é obrigatório.");
    }

    @Test
    @DisplayName("UC03: deve rejeitar cadastro sem nível de perigo")
    void deveRejeitarCadastroSemNivelPerigo() {
        assertThatThrownBy(() -> validador.validar(AreaRisco.cadastrar("SET-03", "Caldeiras", null, null,
                List.of(epi(1, "Capacete")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nível de perigo da área de risco é obrigatório.");
    }

    @Test
    @DisplayName("UC03 RN1: deve bloquear cadastro sem EPIs obrigatórios de acesso")
    void deveBloquearCadastroSemEpisObrigatorios() {
        assertThatThrownBy(() -> validador.validar(AreaRisco.cadastrar("SET-04", "Caldeiras", null,
                NivelPerigo.BAIXO, List.of())))
                .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class)
                .hasMessageContaining("SET-04");

        assertThatThrownBy(() -> validador.validar(AreaRisco.cadastrar("SET-04", "Caldeiras", null,
                NivelPerigo.BAIXO, null)))
                .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class);
    }

    @Test
    @DisplayName("UC03 RN1: a exigência de EPI vale para qualquer grau de perigo")
    void deveExigirEpiEmQualquerNivelDePerigo() {
        for (NivelPerigo nivel : NivelPerigo.values()) {
            assertThatThrownBy(() -> validador.validar(AreaRisco.cadastrar("SET-05", "Setor", null, nivel, List.of())))
                    .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class);
        }
    }

    @Test
    @DisplayName("UC03, exceção I: código de outra área é duplicado; o da própria área, na atualização, não")
    void codigoDuplicadoSoQuandoEDeOutraArea() {
        AreaRiscoRepository areas = mock(AreaRiscoRepository.class);
        when(areas.buscarPorCodigo(anyString())).thenReturn(Optional.empty());
        when(areas.buscarPorCodigo("SET-07")).thenReturn(Optional.of(area(7, "SET-07", "Existente")));
        AreaRiscoValidador comRepositorio = new AreaRiscoValidador(areas, null);

        assertThatThrownBy(() -> comRepositorio.validar(area(null, "set-07", "Nova")))
                .isInstanceOf(CodigoAreaRiscoDuplicadoException.class)
                .hasMessageContaining("SET-07");
        assertThatCode(() -> comRepositorio.validar(area(7, "SET-07", "Existente renomeada")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("UC03: setor informado precisa existir")
    void setorInformadoPrecisaExistir() {
        SetorRepository setores = mock(SetorRepository.class);
        when(setores.buscarPorId(anyInt())).thenReturn(Optional.empty());
        AreaRiscoValidador comSetores = new AreaRiscoValidador(null, setores);
        AreaRisco comSetor = new AreaRisco(null, "SET-08", "Pintura", null, NivelPerigo.MEDIO, 99,
                List.of("Solventes"), "Cabine", List.of(epi(1, "Máscara")));

        assertThatThrownBy(() -> comSetores.validar(comSetor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setor não encontrado com id: 99");
    }

    @Test
    @DisplayName("Lista os problemas na ordem dos elos: campos obrigatórios e, por último, o nome")
    void listaProblemasNaOrdemDosElos() {
        AreaRisco vazia = new AreaRisco(null, null, null, null, null, null, null, null, null);

        List<String> mensagens = validador.verificar(vazia).mensagens();

        assertThat(mensagens).hasSize(6);
        assertThat(mensagens.subList(0, 4)).containsExactly(
                "Código da área de risco é obrigatório.",
                "Nível de perigo da área de risco é obrigatório.",
                "Agentes de risco são obrigatórios para a área de risco.",
                "Limites físicos ou operacionais são obrigatórios para a área de risco.");
        assertThat(mensagens.get(4)).contains("exige ao menos um EPI obrigatório");
        assertThat(mensagens.get(5)).isEqualTo("Nome da área de risco é obrigatório.");
    }
}

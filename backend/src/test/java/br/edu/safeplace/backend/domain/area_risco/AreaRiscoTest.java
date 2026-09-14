package br.edu.safeplace.backend.domain.area_risco;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AreaRiscoTest {

    private static Epi epi(Integer id, String nome) {
        return new Epi(id, nome, "CA-" + id, 10, 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), 365, null, null);
    }

    @Test
    @DisplayName("Deve instanciar AreaRisco com dados válidos")
    void deveInstanciarAreaRiscoComDadosValidos() {
        AreaRisco area = new AreaRisco(1, "SET-01", "Linha de Produção", "Setor de solda e montagem",
                NivelPerigo.ALTO, List.of(epi(1, "Capacete")));

        assertThat(area.getId()).isEqualTo(1);
        assertThat(area.getCodigo()).isEqualTo("SET-01");
        assertThat(area.getNome()).isEqualTo("Linha de Produção");
        assertThat(area.getDescricao()).isEqualTo("Setor de solda e montagem");
        assertThat(area.getNivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(area.verificarEpisObrigatoriosArea()).hasSize(1);
    }

    @Test
    @DisplayName("Deve cadastrar nova AreaRisco sem ID através de método de fábrica")
    void deveCadastrarNovaAreaRiscoComMetodoFabrica() {
        AreaRisco area = AreaRisco.cadastrar("SET-02", "Caldeiras", "Setor de alta pressão",
                NivelPerigo.CRITICO, List.of(epi(2, "Luva térmica")));

        assertThat(area.getId()).isNull();
        assertThat(area.getNome()).isEqualTo("Caldeiras");
        assertThat(area.getDescricao()).isEqualTo("Setor de alta pressão");
        assertThat(area.getNivelPerigo()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(area.isCadastrada()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar nome nulo ou em branco")
    void deveRejeitarNomeNuloOuEmBranco(String nomeInvalido) {
        assertThatThrownBy(() -> new AreaRisco(1, "SET-01", nomeInvalido, "Descricao",
                NivelPerigo.MEDIO, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da área de risco é obrigatório.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("UC03: deve rejeitar cadastro sem código identificador do setor")
    void deveRejeitarCadastroSemCodigo(String codigoInvalido) {
        assertThatThrownBy(() -> AreaRisco.cadastrar(codigoInvalido, "Caldeiras", null,
                NivelPerigo.ALTO, List.of(epi(1, "Capacete"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código da área de risco é obrigatório.");
    }

    @Test
    @DisplayName("UC03: deve rejeitar cadastro sem nível de perigo")
    void deveRejeitarCadastroSemNivelPerigo() {
        assertThatThrownBy(() -> AreaRisco.cadastrar("SET-03", "Caldeiras", null, null,
                List.of(epi(1, "Capacete"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nível de perigo da área de risco é obrigatório.");
    }

    @Test
    @DisplayName("UC03 RN1: deve bloquear cadastro sem EPIs obrigatórios de acesso")
    void deveBloquearCadastroSemEpisObrigatorios() {
        assertThatThrownBy(() -> AreaRisco.cadastrar("SET-04", "Caldeiras", null,
                NivelPerigo.BAIXO, List.of()))
                .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class)
                .hasMessageContaining("SET-04");

        assertThatThrownBy(() -> AreaRisco.cadastrar("SET-04", "Caldeiras", null,
                NivelPerigo.BAIXO, null))
                .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class);
    }

    @Test
    @DisplayName("UC03 RN1: a exigência de EPI vale para qualquer grau de perigo")
    void deveExigirEpiEmQualquerNivelDePerigo() {
        for (NivelPerigo nivel : NivelPerigo.values()) {
            assertThatThrownBy(() -> AreaRisco.cadastrar("SET-05", "Setor", null, nivel, List.of()))
                    .isInstanceOf(AreaRiscoSemEpiObrigatorioException.class);
        }
    }

    @Test
    @DisplayName("Deve normalizar o código para maiúsculas e informar os EPIs exigidos")
    void deveNormalizarCodigoEInformarEpisExigidos() {
        AreaRisco area = AreaRisco.cadastrar(" set-06 ", "Pintura", null, NivelPerigo.MEDIO,
                List.of(epi(7, "Máscara"), epi(8, "Luva")));

        assertThat(area.getCodigo()).isEqualTo("SET-06");
        assertThat(area.exigeEpi(7)).isTrue();
        assertThat(area.exigeEpi(99)).isFalse();
        assertThat(area.exigeEpi(null)).isFalse();
    }

    @Test
    @DisplayName("Referência nominal de local não é considerada uma área cadastrada")
    void referenciaNominalNaoEhAreaCadastrada() {
        AreaRisco referencia = AreaRisco.novo("Pátio externo", null, null);

        assertThat(referencia.isCadastrada()).isFalse();
        assertThat(referencia.getCodigo()).isNull();
        assertThat(referencia.verificarEpisObrigatoriosArea()).isEmpty();
    }

    @Test
    @DisplayName("A lista de EPIs obrigatórios é imutável")
    void listaDeEpisObrigatoriosEhImutavel() {
        AreaRisco area = AreaRisco.cadastrar("SET-07", "Solda", null, NivelPerigo.ALTO,
                List.of(epi(1, "Capacete")));

        assertThatThrownBy(() -> area.verificarEpisObrigatoriosArea().add(epi(2, "Bota")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Áreas cadastradas com o mesmo código são iguais")
    void areasComMesmoCodigoSaoIguais() {
        AreaRisco primeira = AreaRisco.cadastrar("SET-08", "Solda", null, NivelPerigo.ALTO,
                List.of(epi(1, "Capacete")));
        AreaRisco segunda = AreaRisco.cadastrar("set-08", "Solda renomeada", null, NivelPerigo.BAIXO,
                List.of(epi(2, "Bota")));

        assertThat(primeira).isEqualTo(segunda);
        assertThat(primeira).hasSameHashCodeAs(segunda);
        assertThat(primeira).isNotEqualTo(AreaRisco.novo("Solda", null, null));
    }
}

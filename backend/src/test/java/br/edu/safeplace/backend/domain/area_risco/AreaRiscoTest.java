package br.edu.safeplace.backend.domain.area_risco;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Deve atualizar dados, agentes de risco e limites de uma área existente")
    void deveAtualizarDadosAgentesELimites() {
        AreaRisco area = AreaRisco.cadastrar("SET-06B", "Pintura", "Cabine 1", NivelPerigo.MEDIO,
                1, List.of("Vapores"), "Bloco A", List.of(epi(7, "Máscara")));

        AreaRisco atualizada = area.atualizar("Pintura Automotiva", "Cabine Nova", NivelPerigo.ALTO,
                2, List.of("Vapores", "Tíner"), "Bloco B", List.of(epi(7, "Máscara"), epi(8, "Luva")));

        assertThat(atualizada.getNome()).isEqualTo("Pintura Automotiva");
        assertThat(atualizada.getDescricao()).isEqualTo("Cabine Nova");
        assertThat(atualizada.getNivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(atualizada.getSetorId()).isEqualTo(2);
        assertThat(atualizada.getAgentesRisco()).containsExactly("Vapores", "Tíner");
        assertThat(atualizada.getLimites()).isEqualTo("Bloco B");
        assertThat(atualizada.exigeEpi(8)).isTrue();
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
        AreaRisco diferente = AreaRisco.cadastrar("SET-99", "Outro", null, NivelPerigo.BAIXO,
                List.of(epi(2, "Bota")));

        assertThat(primeira).isEqualTo(segunda);
        assertThat(primeira).hasSameHashCodeAs(segunda);
        assertThat(primeira).isNotEqualTo(diferente);
    }
}

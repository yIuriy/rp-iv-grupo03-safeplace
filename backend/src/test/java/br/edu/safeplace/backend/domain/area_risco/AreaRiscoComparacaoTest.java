/*
 * Testes da comparação entre versões de uma área de risco (AreaRisco.compararCom), base do histórico
 * de alterações do UC03, cenário alternativo II, e da regra 4 da US05 (issue #114).
 */
package br.edu.safeplace.backend.domain.area_risco;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

import static org.assertj.core.api.Assertions.assertThat;

class AreaRiscoComparacaoTest {

    private static final Map<Integer, String> SETORES = Map.of(1, "Usinagem", 2, "Caldeiraria");
    private static final Function<Integer, String> NOME_DO_SETOR = SETORES::get;

    private static Epi epi(Integer id, String nome) {
        return new Epi(id, nome, "CA-" + id, 10, 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), 365, null, null);
    }

    private static AreaRisco original() {
        return new AreaRisco(7, "AR-01", "Solda", "Solda MIG", NivelPerigo.MEDIO, 1,
                List.of("Ruído"), "Entre as linhas A e B", List.of(epi(1, "Capacete")));
    }

    @Test
    @DisplayName("Sem mudança de dados, nenhuma alteração é registrada")
    void semMudancaNaoHaAlteracoes() {
        AreaRisco area = original();
        AreaRisco igual = area.atualizar("Solda", "Solda MIG", NivelPerigo.MEDIO, 1,
                List.of("Ruído"), "Entre as linhas A e B", List.of(epi(1, "Capacete")));

        assertThat(area.compararCom(igual, NOME_DO_SETOR)).isEmpty();
    }

    @Test
    @DisplayName("Agentes, limites e nível alterados guardam o valor anterior e o novo")
    void agentesLimitesENivelGuardamAnteriorENovo() {
        AreaRisco area = original();
        AreaRisco atualizada = area.atualizar("Solda", "Solda MIG", NivelPerigo.ALTO, null,
                List.of("Ruído", "Calor"), "Entre as linhas A e C", List.of(epi(1, "Capacete")));

        assertThat(area.compararCom(atualizada, NOME_DO_SETOR)).containsExactly(
                new AlteracaoAreaRisco("nível de perigo", "MEDIO", "ALTO"),
                new AlteracaoAreaRisco("agentes de risco", "Ruído", "Ruído, Calor"),
                new AlteracaoAreaRisco("limites", "Entre as linhas A e B", "Entre as linhas A e C"));
    }

    @Test
    @DisplayName("Troca de setor aparece pelo nome do setor, e não pelo id")
    void trocaDeSetorUsaNome() {
        AreaRisco area = original();
        AreaRisco atualizada = area.atualizar("Solda", "Solda MIG", NivelPerigo.MEDIO, 2,
                List.of("Ruído"), "Entre as linhas A e B", List.of(epi(1, "Capacete")));

        assertThat(area.compararCom(atualizada, NOME_DO_SETOR))
                .containsExactly(new AlteracaoAreaRisco("setor", "Usinagem", "Caldeiraria"));
    }

    @Test
    @DisplayName("Mudança nos EPIs obrigatórios lista os nomes antes e depois")
    void mudancaNosEpisListaNomes() {
        AreaRisco area = original();
        AreaRisco atualizada = area.atualizar("Solda", "Solda MIG", NivelPerigo.MEDIO, 1,
                List.of("Ruído"), "Entre as linhas A e B", List.of(epi(1, "Capacete"), epi(2, "Luva térmica")));

        assertThat(area.compararCom(atualizada, NOME_DO_SETOR))
                .containsExactly(new AlteracaoAreaRisco("EPIs obrigatórios", "Capacete", "Capacete, Luva térmica"));
    }

    @Test
    @DisplayName("Mesmos EPIs em outra ordem não contam como alteração")
    void mesmosEpisEmOutraOrdemNaoMudam() {
        AreaRisco area = new AreaRisco(7, "AR-01", "Solda", null, NivelPerigo.MEDIO, null,
                List.of("Ruído"), "Entre as linhas A e B", List.of(epi(1, "Capacete"), epi(2, "Luva térmica")));
        AreaRisco atualizada = area.atualizar("Solda", null, NivelPerigo.MEDIO, null,
                List.of("Ruído"), "Entre as linhas A e B", List.of(epi(2, "Luva térmica"), epi(1, "Capacete")));

        assertThat(area.compararCom(atualizada, NOME_DO_SETOR)).isEmpty();
    }

    @Test
    @DisplayName("Campo que passa a ter valor registra o anterior como ausente")
    void campoQuePassaATerValorRegistraAnteriorNulo() {
        AreaRisco area = new AreaRisco(7, "AR-01", "Solda", null, NivelPerigo.MEDIO, null,
                List.of("Ruído"), null, List.of(epi(1, "Capacete")));
        AreaRisco atualizada = area.atualizar("Solda", "Solda MIG", NivelPerigo.MEDIO, 1,
                List.of("Ruído"), "Linha amarela", List.of(epi(1, "Capacete")));

        assertThat(area.compararCom(atualizada, NOME_DO_SETOR)).containsExactly(
                new AlteracaoAreaRisco("descrição", null, "Solda MIG"),
                new AlteracaoAreaRisco("setor", null, "Usinagem"),
                new AlteracaoAreaRisco("limites", null, "Linha amarela"));
    }
}

package br.edu.safeplace.backend.domain.area_risco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

import static org.assertj.core.api.Assertions.assertThat;

class SetorTest {

    @Test
    @DisplayName("Deve criar setor com dados válidos")
    void deveCriarSetorComDadosValidos() {
        Setor setor = Setor.novo("Galpão A", "Caldeiraria pesada", "Bloco 1, Piso Térreo", NivelPerigo.ALTO);

        assertThat(setor.getNome()).isEqualTo("Galpão A");
        assertThat(setor.getDescricao()).isEqualTo("Caldeiraria pesada");
        assertThat(setor.getLocalizacao()).isEqualTo("Bloco 1, Piso Térreo");
        assertThat(setor.getStatus()).isEqualTo(NivelPerigo.ALTO);
        assertThat(setor.obterDadosPlanta()).isEqualTo(setor);
        assertThat(setor.validarLimitesSetor(1)).isTrue();
    }

    @Test
    @DisplayName("Setores com mesmo nome são iguais")
    void setoresComMesmoNomeSaoIguais() {
        Setor setor1 = Setor.novo("Galpão C", "Desc 1", "Loc 1", NivelPerigo.BAIXO);
        Setor setor2 = Setor.novo("Galpão C", "Desc 2", "Loc 2", NivelPerigo.ALTO);
        Setor setor3 = Setor.novo("Galpão D", "Desc 1", "Loc 1", NivelPerigo.BAIXO);

        assertThat(setor1).isEqualTo(setor2);
        assertThat(setor1).hasSameHashCodeAs(setor2);
        assertThat(setor1).isNotEqualTo(setor3);
    }
}

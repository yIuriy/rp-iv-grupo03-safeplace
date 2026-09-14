package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncidenteTest {

    @Test
    @DisplayName("Deve criar Incidente completo com situação de risco e potencial de dano")
    void deveCriarIncidenteCompleto() {
        LocalDateTime dataFato = LocalDateTime.now().minusHours(3);
        AreaRisco area = AreaRisco.novo("Almoxarifado", "Estocagem vertical", NivelPerigo.MEDIO);
        Colaborador colaborador = Colaborador.novo("Juliana", "87455877074", LocalDate.of(1995, 12, 1), "juliana@empresa.com");
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Fernando", "49216091040", LocalDate.of(1982, 8, 14), "fernando@empresa.com", "senha123");

        Incidente incidente = new Incidente(
                2,
                "Palete instável quase desabou sobre corredor",
                dataFato,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                List.of("Beatriz"),
                colaborador,
                List.of("palete_torto.jpg"),
                area,
                gestor,
                "Corredor 4",
                null,
                "Paletes empilhados além da altura máxima permitida",
                "Esmagamento e fraturas múltiplas"
        );

        assertThat(incidente.getId()).isEqualTo(2);
        assertThat(incidente.getDescricao()).isEqualTo("Palete instável quase desabou sobre corredor");
        assertThat(incidente.getSituacaoRisco()).isEqualTo("Paletes empilhados além da altura máxima permitida");
        assertThat(incidente.getPotencialDano()).isEqualTo("Esmagamento e fraturas múltiplas");
        assertThat(incidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Deve rejeitar incidente com situação de risco vazia ou nula")
    void deveRejeitarSituacaoRiscoInvalida(String situacaoInvalida) {
        assertThatThrownBy(() -> new Incidente(
                null,
                "Descricao",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null,
                situacaoInvalida,
                "Potencial de corte"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Situação de risco é obrigatória para incidentes.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Deve rejeitar incidente com potencial de dano vazio ou nulo")
    void deveRejeitarPotencialDanoInvalido(String potencialInvalido) {
        assertThatThrownBy(() -> new Incidente(
                null,
                "Descricao",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null,
                "Piso escorregadio",
                potencialInvalido
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Potencial de dano é obrigatório para incidentes.");
    }

    @Test
    @DisplayName("Deve aceitar criação via construtor e método de fábrica compatíveis")
    void deveAceitarCriacaoViaFabricaCompativel() {
        Incidente incidente = Incidente.novo(
                LocalDateTime.of(2026, 9, 8, 11, 0),
                "Estacionamento",
                "Quase atropelamento por empilhadeira",
                null,
                "Falta de sinalização no cruzamento",
                "Atropelamento com trauma severo"
        );

        assertThat(incidente.getId()).isNull();
        assertThat(incidente.getLocal()).isEqualTo("Estacionamento");
        assertThat(incidente.getSituacaoRisco()).isEqualTo("Falta de sinalização no cruzamento");
        assertThat(incidente.getPotencialDano()).isEqualTo("Atropelamento com trauma severo");
    }
}

package br.edu.safeplace.backend.domain.ocorrencia;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;

class IncidenteTest {

    @Test
    @DisplayName("Deve criar Incidente completo com situação de risco e potencial de dano")
    void deveCriarIncidenteCompleto() {
        LocalDateTime dataFato = LocalDateTime.now().minusHours(3);
        Epi epi = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comCa("CA-123")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.now().plusYears(1))
                .comVidaUtilDias(180)
                .comDescricao("Capacete")
                .build();
        AreaRisco area = AreaRisco.cadastrar("ALM-01", "Almoxarifado", "Estocagem vertical", NivelPerigo.MEDIO, List.of(epi));
        Colaborador colaborador = Colaborador.novo("Juliana", "87455877074", LocalDate.of(1995, 12, 1), "juliana@empresa.com");
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Fernando", "49216091040", LocalDate.of(1982, 8, 14), "fernando@empresa.com", "senha123");

        Incidente incidente = new Incidente(
                2,
                "Palete instável quase desabou sobre corredor",
                dataFato,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                gestor,
                null,
                null,
                "Paletes empilhados além da altura máxima permitida",
                "Esmagamento e fraturas múltiplas"
        );

        assertThat(incidente.getId()).isEqualTo(2);
        assertThat(incidente.getDescricao()).isEqualTo("Palete instável quase desabou sobre corredor");
        assertThat(incidente.getSituacaoRisco()).isEqualTo("Paletes empilhados além da altura máxima permitida");
        assertThat(incidente.getPotencialDano()).isEqualTo("Esmagamento e fraturas múltiplas");
        assertThat(incidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(incidente.getRelator()).isEqualTo(gestor);
    }

    @Test
    @DisplayName("Fábrica cria incidente aberto sem texto livre de local")
    void deveCriarIncidentePelaFabrica() {
        AreaRisco area = AreaRisco.cadastrar("EST-01", "Estacionamento", "Circulação de veículos", NivelPerigo.MEDIO,
                List.of(EpiDeTeste.umEpi()
                        .comId(3)
                        .comNome("Colete")
                        .comCa("CA-789")
                        .comQuantidade(5)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.now().plusYears(1))
                        .comVidaUtilDias(180)
                        .comDescricao("Colete")
                        .build()));

        Incidente incidente = Incidente.novo(
                "Quase atropelamento por empilhadeira",
                LocalDateTime.of(2026, 9, 8, 11, 0),
                area,
                null,
                null,
                "Falta de sinalização no cruzamento",
                "Atropelamento com trauma severo"
        );

        assertThat(incidente.getId()).isNull();
        assertThat(incidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(incidente.getLocalInformado()).isNull();
        assertThat(incidente.getLocal()).isEqualTo("Estacionamento");
        assertThat(incidente.getSituacaoRisco()).isEqualTo("Falta de sinalização no cruzamento");
        assertThat(incidente.getPotencialDano()).isEqualTo("Atropelamento com trauma severo");
    }
}

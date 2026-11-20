package br.edu.safeplace.backend.application.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoIncidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcaoBuilder;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;

class OcorrenciaOutputAssemblerTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 11, 2, 9, 30);

    private final OcorrenciaOutputAssembler assembler = new OcorrenciaOutputAssembler();

    @Test
    void dtoNaoExpoeTiposDoDominio() {
        assertThat(Arrays.stream(OcorrenciaOutputDTO.class.getRecordComponents()).map(RecordComponent::getType))
                .noneMatch(tipo -> tipo.getPackageName().startsWith("br.edu.safeplace.backend.domain"));
    }

    @Test
    void montaAcidenteComAParteComumEAEspecifica() {
        PlanoDeAcao plano = new PlanoDeAcaoBuilder()
                .id(7)
                .medidasCorretivas("Isolar a área")
                .prazo(LocalDate.of(2026, 12, 1))
                .status("PENDENTE")
                .medidasPreventivas("Treinar")
                .build();
        Acidente acidente = Acidente.builder()
                .idOcorrencia(10)
                .descricao("Queda")
                .dataOcorrencia(DATA)
                .dataRegistro(DATA)
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .localInformado("Galpão")
                .planoDeAcao(plano)
                .causaRaiz(CausaRaizAcidente.values()[0])
                .tipo("Queda")
                .dano("Fratura")
                .numeroProtocoloCAT(NumeroProtocoloCAT.de("CAT-2026-11-0001"))
                .destino("Hospital")
                .gravidade(NivelPerigo.ALTO)
                .build();

        OcorrenciaOutputDTO dto = assembler.montar(acidente);

        assertThat(dto.idOcorrencia()).isEqualTo(10);
        assertThat(dto.tipoOcorrencia()).isEqualTo("ACIDENTE");
        assertThat(dto.statusOcorrencia()).isEqualTo("ABERTA");
        assertThat(dto.local()).isEqualTo("Galpão");
        assertThat(dto.causaRaiz()).isEqualTo(CausaRaizAcidente.values()[0].name());
        assertThat(dto.gravidade()).isEqualTo("ALTO");
        assertThat(dto.numeroProtocolo()).isEqualTo(dto.numeroProtocoloCAT()).isEqualTo("CAT-2026-11-0001");
        assertThat(dto.destino()).isEqualTo("Hospital");
        assertThat(dto.situacaoRisco()).isNull();
        assertThat(dto.potencialDano()).isNull();
        assertThat(dto.planoDeAcao().id()).isEqualTo(7);
        assertThat(dto.planoDeAcao().medidasCorretivas()).isEqualTo("Isolar a área");
    }

    @Test
    void montaIncidenteSemCamposDeAcidente() {
        Incidente incidente = Incidente.builder()
                .idOcorrencia(11)
                .descricao("Quase queda")
                .dataOcorrencia(DATA)
                .dataRegistro(DATA)
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .localInformado("Pátio")
                .situacaoRisco("Piso molhado")
                .potencialDano("Queda")
                .build();

        OcorrenciaOutputDTO dto = assembler.montar(incidente);

        assertThat(dto.tipoOcorrencia()).isEqualTo("INCIDENTE");
        assertThat(dto.situacaoRisco()).isEqualTo("Piso molhado");
        assertThat(dto.potencialDano()).isEqualTo("Queda");
        assertThat(dto.causaRaiz()).isNull();
        assertThat(dto.numeroProtocoloCAT()).isNull();
        assertThat(dto.gravidade()).isNull();
        assertThat(dto.planoDeAcao()).isNull();
    }

    @Test
    void associacoesAusentesViramNulas() {
        Incidente incidente = Incidente.builder()
                .idOcorrencia(12)
                .descricao("Relato")
                .dataOcorrencia(DATA)
                .dataRegistro(DATA)
                .statusOcorrencia(null)
                .situacaoRisco("Risco")
                .potencialDano("Dano")
                .build();

        OcorrenciaOutputDTO dto = assembler.montar(incidente);

        assertThat(dto.areaId()).isNull();
        assertThat(dto.colaboradorNome()).isNull();
        assertThat(dto.relatorPerfil()).isNull();
    }

    /**
     * Substitui a checagem de {@code sealed} (#239): a hierarquia foi reaberta no commit 01a0d18
     * porque o Hibernate não gera proxy de classe {@code sealed}. A garantia que importa continua
     * verificada aqui: todo {@link TipoOcorrencia} tem uma fábrica e uma subclasse correspondentes.
     */
    @Test
    @DisplayName("Todo TipoOcorrencia tem fábrica e subclasse correspondentes (#239)")
    void todoTipoDeOcorrenciaTemFabricaESubclasse() {
        assertThat(Modifier.isAbstract(Ocorrencia.class.getModifiers())).isTrue();
        assertThat(List.of(new FabricaRelatoAcidente(), new FabricaRelatoIncidente()))
                .extracting(FabricaRelatoOcorrencia::getTipo)
                .containsExactlyInAnyOrder(TipoOcorrencia.values());
        assertThat(List.of(Acidente.builder().idOcorrencia(1).build(), Incidente.builder().idOcorrencia(2).build()))
                .extracting(Ocorrencia::getTipoOcorrencia)
                .containsExactlyInAnyOrder(TipoOcorrencia.values());
    }
}

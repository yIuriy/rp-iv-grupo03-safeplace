package br.edu.safeplace.backend.application.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;

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
        PlanoDeAcao plano = new PlanoDeAcao(7, "Isolar a área", LocalDate.of(2026, 12, 1), "PENDENTE", "Treinar");
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

    @Test
    @DisplayName("Hierarquia de Ocorrencia é sealed permitindo apenas Acidente e Incidente (#239)")
    void hierarquiaDeOcorrenciaEhSealed() {
        assertThat(Ocorrencia.class.isSealed()).isTrue();
        assertThat(Ocorrencia.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(Acidente.class, Incidente.class);
    }
}

/*
 * Testes unitários dos critérios de consulta de ocorrências (issue #128): período inclusivo pela data
 * do fato, setor alcançado pela área de risco, colaborador envolvido, combinação e período invertido;
 * tipo e gravidade (issue #195).
 */
package br.edu.safeplace.backend.application.dto.input;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

class FiltroOcorrenciasDTOTest {

    private static final LocalDate SETEMBRO_1 = LocalDate.of(2026, 9, 1);
    private static final LocalDate SETEMBRO_30 = LocalDate.of(2026, 9, 30);

    private static final Setor SETOR_3 = new Setor(3, "Prensaria", null, "Bloco 3", NivelPerigo.ALTO);
    private static final AreaRisco AREA_DO_SETOR_3 = new AreaRisco(10, "AR-10", "Linha de Prensas", null,
            NivelPerigo.ALTO, SETOR_3, List.of(), null, List.of());
    private static final Colaborador JOAO = new Colaborador(7, "52998224725", "João", LocalDate.of(1990, 1, 1),
            null, true, null, null);

    private static Ocorrencia incidente(LocalDateTime dataFato, AreaRisco area, Colaborador colaborador) {
        return new Incidente(1, "Piso escorregadio", dataFato, LocalDateTime.of(2026, 10, 1, 8, 0),
                StatusOcorrencia.ABERTA, colaborador, area, null, null, null,
                "Óleo no corredor", "Queda");
    }

    @Test
    @DisplayName("Período inclui o primeiro e o último dia inteiros e exclui o dia seguinte")
    void periodoInclusivoPelaDataDoFato() {
        FiltroOcorrenciasDTO setembro = new FiltroOcorrenciasDTO(SETEMBRO_1, SETEMBRO_30, null, null);

        assertThat(setembro.aceita(incidente(SETEMBRO_1.atStartOfDay(), null, null))).isTrue();
        assertThat(setembro.aceita(incidente(LocalDateTime.of(2026, 9, 30, 23, 59, 59), null, null))).isTrue();
        assertThat(setembro.aceita(incidente(LocalDateTime.of(2026, 10, 1, 0, 0), null, null))).isFalse();
        assertThat(setembro.aceita(incidente(LocalDateTime.of(2026, 8, 31, 23, 59), null, null))).isFalse();
    }

    @Test
    @DisplayName("Período com só um dos limites deixa o outro lado aberto")
    void periodoComUmLimite() {
        LocalDateTime agosto = LocalDateTime.of(2026, 8, 15, 10, 0);
        assertThat(new FiltroOcorrenciasDTO(null, SETEMBRO_30, null, null).aceita(incidente(agosto, null, null))).isTrue();
        assertThat(new FiltroOcorrenciasDTO(SETEMBRO_1, null, null, null).aceita(incidente(agosto, null, null))).isFalse();
    }

    @Test
    @DisplayName("Setor é o da área de risco; ocorrência sem área não pertence a setor algum")
    void setorPelaAreaDeRisco() {
        LocalDateTime fato = LocalDateTime.of(2026, 9, 10, 9, 0);
        assertThat(new FiltroOcorrenciasDTO(null, null, 3, null).aceita(incidente(fato, AREA_DO_SETOR_3, null))).isTrue();
        assertThat(new FiltroOcorrenciasDTO(null, null, 4, null).aceita(incidente(fato, AREA_DO_SETOR_3, null))).isFalse();
        assertThat(new FiltroOcorrenciasDTO(null, null, 3, null).aceita(incidente(fato, null, null))).isFalse();
    }

    @Test
    @DisplayName("Colaborador envolvido é comparado pelo identificador")
    void colaboradorEnvolvido() {
        LocalDateTime fato = LocalDateTime.of(2026, 9, 10, 9, 0);
        assertThat(new FiltroOcorrenciasDTO(null, null, null, 7).aceita(incidente(fato, null, JOAO))).isTrue();
        assertThat(new FiltroOcorrenciasDTO(null, null, null, 8).aceita(incidente(fato, null, JOAO))).isFalse();
        assertThat(new FiltroOcorrenciasDTO(null, null, null, 7).aceita(incidente(fato, null, null))).isFalse();
    }

    @Test
    @DisplayName("Critérios combinados exigem que todos sejam atendidos")
    void criteriosCombinados() {
        FiltroOcorrenciasDTO todos = new FiltroOcorrenciasDTO(SETEMBRO_1, SETEMBRO_30, 3, 7);
        assertThat(todos.aceita(incidente(LocalDateTime.of(2026, 9, 10, 9, 0), AREA_DO_SETOR_3, JOAO))).isTrue();
        assertThat(todos.aceita(incidente(LocalDateTime.of(2026, 8, 10, 9, 0), AREA_DO_SETOR_3, JOAO))).isFalse();
        assertThat(FiltroOcorrenciasDTO.SEM_FILTRO.aceita(incidente(LocalDateTime.of(2020, 1, 1, 0, 0), null, null))).isTrue();
    }

    @Test
    @DisplayName("Data inicial posterior à final é recusada; o mesmo dia nos dois limites é aceito")
    void periodoInvertido() {
        assertThatThrownBy(() -> new FiltroOcorrenciasDTO(SETEMBRO_30, SETEMBRO_1, null, null))
                .isInstanceOf(DataOcorrenciaInvalidaException.class)
                .hasMessage("A data inicial do período não pode ser posterior à data final.");
        assertThat(new FiltroOcorrenciasDTO(SETEMBRO_1, SETEMBRO_1, null, null).fimExclusivo())
                .isEqualTo(LocalDateTime.of(2026, 9, 2, 0, 0));
    }

    private static Ocorrencia acidente(NivelPerigo gravidade) {
        LocalDateTime fato = LocalDateTime.of(2026, 9, 10, 9, 0);
        return new Acidente(2, "Corte na mão", fato, fato, StatusOcorrencia.ABERTA, JOAO, AREA_DO_SETOR_3, null,
                null, null, CausaRaizAcidente.FATOR_HUMANO, null, null, null, null, gravidade);
    }

    @Test
    @DisplayName("Tipo separa acidentes de incidentes (issue #195)")
    void tipoDaOcorrencia() {
        Ocorrencia incidente = incidente(LocalDateTime.of(2026, 9, 10, 9, 0), AREA_DO_SETOR_3, JOAO);
        FiltroOcorrenciasDTO soAcidentes = new FiltroOcorrenciasDTO(null, null, null, null, TipoOcorrencia.ACIDENTE, null);
        FiltroOcorrenciasDTO soIncidentes = new FiltroOcorrenciasDTO(null, null, null, null, TipoOcorrencia.INCIDENTE, null);

        assertThat(soAcidentes.aceita(acidente(NivelPerigo.BAIXO))).isTrue();
        assertThat(soAcidentes.aceita(incidente)).isFalse();
        assertThat(soIncidentes.aceita(incidente)).isTrue();
        assertThat(soIncidentes.aceita(acidente(NivelPerigo.BAIXO))).isFalse();
    }

    @Test
    @DisplayName("Gravidade compara a do acidente e deixa os incidentes de fora (issue #195)")
    void gravidadeDoAcidente() {
        FiltroOcorrenciasDTO graves = new FiltroOcorrenciasDTO(null, null, null, null, null, NivelPerigo.ALTO);

        assertThat(graves.aceita(acidente(NivelPerigo.ALTO))).isTrue();
        assertThat(graves.aceita(acidente(NivelPerigo.CRITICO))).isFalse();
        assertThat(graves.aceita(incidente(LocalDateTime.of(2026, 9, 10, 9, 0), AREA_DO_SETOR_3, JOAO))).isFalse();
    }

    @Test
    @DisplayName("Parâmetros textuais da API viram critérios; vazios são ignorados e inválidos recusados")
    void parametrosDaApi() {
        FiltroOcorrenciasDTO filtro = FiltroOcorrenciasDTO.deParametros(null, null, null, null, "acidente", "alto");
        assertThat(filtro.tipoOcorrencia()).isEqualTo(TipoOcorrencia.ACIDENTE);
        assertThat(filtro.gravidade()).isEqualTo(NivelPerigo.ALTO);

        FiltroOcorrenciasDTO vazio = FiltroOcorrenciasDTO.deParametros(null, null, null, null, " ", "");
        assertThat(vazio.tipoOcorrencia()).isNull();
        assertThat(vazio.gravidade()).isNull();

        assertThatThrownBy(() -> FiltroOcorrenciasDTO.deParametros(null, null, null, null, "OUTRO", null))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class);
        assertThatThrownBy(() -> FiltroOcorrenciasDTO.deParametros(null, null, null, null, null, "GRAVE"))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class);
    }
}

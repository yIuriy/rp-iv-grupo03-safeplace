/*
 * Critérios opcionais da consulta de ocorrências (UC05/UC09, issue #128), correspondentes a
 * Ocorrencia.consultarOcorrencias(criterios) do diagrama de classes: período da data do fato, setor
 * da área de risco, colaborador envolvido, tipo e gravidade (issue #195). Campo ausente significa
 * "sem esse critério".
 */
package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * O período usa a data do fato ({@code dataOcorrencia}), nunca a data do cadastro, e inclui os dois
 * dias informados. O setor é alcançado pela área de risco, porque a ocorrência não tem vínculo direto
 * com {@code Setor} (decisão DS09, seção 2.3). A ocorrência tem um único colaborador envolvido (2.2).
 * Só acidentes têm gravidade (seção 2.7): o critério de gravidade deixa os incidentes de fora.
 */
public record FiltroOcorrenciasDTO(
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer setorId,
        Integer colaboradorId,
        TipoOcorrencia tipoOcorrencia,
        NivelPerigo gravidade) {

    public static final FiltroOcorrenciasDTO SEM_FILTRO = new FiltroOcorrenciasDTO(null, null, null, null);

    public FiltroOcorrenciasDTO(LocalDate dataInicio, LocalDate dataFim, Integer setorId, Integer colaboradorId) {
        this(dataInicio, dataFim, setorId, colaboradorId, null, null);
    }

    /** Monta o filtro a partir dos parâmetros textuais da API; tipo ou gravidade desconhecidos são recusados. */
    public static FiltroOcorrenciasDTO deParametros(LocalDate dataInicio, LocalDate dataFim, Integer setorId,
            Integer colaboradorId, String tipoOcorrencia, String gravidade) {
        return new FiltroOcorrenciasDTO(dataInicio, dataFim, setorId, colaboradorId,
                tipoOcorrencia == null || tipoOcorrencia.isBlank() ? null : TipoOcorrencia.de(tipoOcorrencia),
                gravidade == null || gravidade.isBlank() ? null : Acidente.lerGravidade(gravidade));
    }

    public FiltroOcorrenciasDTO {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new DataOcorrenciaInvalidaException(
                    "A data inicial do período não pode ser posterior à data final.");
        }
    }

    /** Primeiro instante incluído no período, ou nulo quando não há limite inicial. */
    public LocalDateTime inicioInclusivo() {
        return dataInicio != null ? dataInicio.atStartOfDay() : null;
    }

    /** Primeiro instante fora do período (dia seguinte ao final), ou nulo quando não há limite final. */
    public LocalDateTime fimExclusivo() {
        return dataFim != null ? dataFim.plusDays(1).atStartOfDay() : null;
    }

    /** Mesma regra da consulta ao banco, usada por implementações em memória da porta. */
    public boolean aceita(Ocorrencia ocorrencia) {
        LocalDateTime dataFato = ocorrencia.getDataOcorrencia();
        if (inicioInclusivo() != null && (dataFato == null || dataFato.isBefore(inicioInclusivo()))) {
            return false;
        }
        if (fimExclusivo() != null && (dataFato == null || !dataFato.isBefore(fimExclusivo()))) {
            return false;
        }
        AreaRisco area = ocorrencia.getArea();
        if (setorId != null && (area == null || !Objects.equals(area.getSetorId(), setorId))) {
            return false;
        }
        Colaborador colaborador = ocorrencia.getColaborador();
        if (colaboradorId != null && (colaborador == null || !Objects.equals(colaborador.getId(), colaboradorId))) {
            return false;
        }
        if (tipoOcorrencia == TipoOcorrencia.ACIDENTE && !(ocorrencia instanceof Acidente)) {
            return false;
        }
        if (tipoOcorrencia == TipoOcorrencia.INCIDENTE && !(ocorrencia instanceof Incidente)) {
            return false;
        }
        return gravidade == null || ocorrencia instanceof Acidente acidente && acidente.getGravidade() == gravidade;
    }
}

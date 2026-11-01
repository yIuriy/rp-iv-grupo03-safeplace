package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

public class Incidente extends Ocorrencia {
    private final String situacaoRisco;
    private final String potencialDano;

    public Incidente(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            Colaborador colaborador,
            AreaRisco area,
            Colaborador relator,
            String local,
            PlanoDeAcao planoDeAcao,
            String situacaoRisco,
            String potencialDano) {
        super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                colaborador, area, relator, local, planoDeAcao);

        // Situação de risco e potencial de dano: OcorrenciaValidador (issue #266).
        this.situacaoRisco = situacaoRisco != null ? situacaoRisco.trim() : null;
        this.potencialDano = potencialDano != null ? potencialDano.trim() : null;
    }

    public static Incidente novo(
            String descricao,
            LocalDateTime dataOcorrencia,
            AreaRisco area,
            Colaborador colaborador,
            Colaborador relator,
            String situacaoRisco,
            String potencialDano) {
        return new Incidente(
                null,
                descricao,
                dataOcorrencia,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                relator,
                null,
                null,
                situacaoRisco,
                potencialDano
        );
    }

    public String getSituacaoRisco() {
        return situacaoRisco;
    }

    public String getPotencialDano() {
        return potencialDano;
    }
}

package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

public class Incidente extends Ocorrencia {
    private final String situacaoRisco;
    private final String potencialDano;

    public Incidente(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            List<String> testemunhas,
            Colaborador colaborador,
            List<String> midias,
            AreaRisco area,
            GestorDeSeguranca gestor,
            String local,
            PlanoDeAcao planoDeAcao,
            String situacaoRisco,
            String potencialDano) {
        super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                testemunhas, colaborador, midias, area, gestor, local, planoDeAcao);

        if (situacaoRisco == null || situacaoRisco.isBlank()) {
            throw new IllegalArgumentException("Situação de risco é obrigatória para incidentes.");
        }
        if (potencialDano == null || potencialDano.isBlank()) {
            throw new IllegalArgumentException("Potencial de dano é obrigatório para incidentes.");
        }

        this.situacaoRisco = situacaoRisco.trim();
        this.potencialDano = potencialDano.trim();
    }

    /**
     * Construtor de compatibilidade.
     */
    public Incidente(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            String local,
            String descricao,
            PlanoDeAcao planoDeAcao,
            String situacaoRisco,
            String potencialDano) {
        this(
                idOcorrencia,
                descricao,
                dataOcorrencia,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null,
                null,
                null,
                local != null ? AreaRisco.novo(local, null, null) : null,
                null,
                local,
                planoDeAcao,
                situacaoRisco,
                potencialDano
        );
    }

    public static Incidente novo(
            String descricao,
            LocalDateTime dataOcorrencia,
            AreaRisco area,
            Colaborador colaborador,
            GestorDeSeguranca gestor,
            List<String> testemunhas,
            List<String> midias,
            String situacaoRisco,
            String potencialDano) {
        return new Incidente(
                null,
                descricao,
                dataOcorrencia,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                testemunhas,
                colaborador,
                midias,
                area,
                gestor,
                area != null ? area.getNome() : null,
                null,
                situacaoRisco,
                potencialDano
        );
    }

    public static Incidente novo(
            LocalDateTime dataOcorrencia,
            String local,
            String descricao,
            PlanoDeAcao planoDeAcao,
            String situacaoRisco,
            String potencialDano) {
        return new Incidente(
                null,
                dataOcorrencia,
                local,
                descricao,
                planoDeAcao,
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
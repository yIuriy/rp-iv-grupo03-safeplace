package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

public class Acidente extends Ocorrencia {
    private final CausaRaiz causaRaiz;
    private final String tipo;
    private final String dano;
    private final String numeroProtocoloCAT;
    private final String destino;

    public Acidente(
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
            CausaRaiz causaRaiz,
            String tipo,
            String dano,
            String numeroProtocoloCAT,
            String destino) {
        super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                testemunhas, colaborador, midias, area, gestor, local, planoDeAcao);

        if (causaRaiz == null) {
            throw new IllegalArgumentException("Causa raiz é obrigatória para acidentes.");
        }

        if (numeroProtocoloCAT != null && !numeroProtocoloCAT.isBlank()) {
            this.numeroProtocoloCAT = ProtocoloCAT.de(numeroProtocoloCAT).getValor();
        } else {
            this.numeroProtocoloCAT = null;
        }

        this.causaRaiz = causaRaiz;
        this.tipo = tipo != null ? tipo.trim() : null;
        this.dano = dano != null ? dano.trim() : null;
        this.destino = destino != null ? destino.trim() : null;
    }

    /**
     * Construtor de compatibilidade.
     */
    public Acidente(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            String local,
            String descricao,
            PlanoDeAcao planoDeAcao,
            CausaRaiz causaRaiz,
            String tipo,
            String dano,
            String numeroProtocolo,
            String destino) {
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
                causaRaiz,
                tipo,
                dano,
                numeroProtocolo,
                destino
        );
    }

    /**
     * Construtor de compatibilidade aceitando causaRaiz como String.
     */
    public Acidente(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            String local,
            String descricao,
            PlanoDeAcao planoDeAcao,
            String causaRaiz,
            String tipo,
            String dano,
            String numeroProtocolo,
            String destino) {
        this(
                idOcorrencia,
                dataOcorrencia,
                local,
                descricao,
                planoDeAcao,
                converterCausaRaiz(causaRaiz),
                tipo,
                dano,
                numeroProtocolo,
                destino
        );
    }

    public static Acidente novo(
            String descricao,
            LocalDateTime dataOcorrencia,
            AreaRisco area,
            Colaborador colaborador,
            GestorDeSeguranca gestor,
            List<String> testemunhas,
            List<String> midias,
            CausaRaiz causaRaiz,
            String tipo,
            String dano,
            String destino) {
        return new Acidente(
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
                causaRaiz,
                tipo,
                dano,
                null,
                destino
        );
    }

    public static Acidente novo(
            LocalDateTime dataOcorrencia,
            String local,
            String descricao,
            PlanoDeAcao planoDeAcao,
            String causaRaiz,
            String tipo,
            String dano,
            String numeroProtocolo,
            String destino) {
        return new Acidente(
                null,
                dataOcorrencia,
                local,
                descricao,
                planoDeAcao,
                causaRaiz,
                tipo,
                dano,
                numeroProtocolo,
                destino
        );
    }

    public Acidente consolidarCAT(long sequencial) {
        if (this.numeroProtocoloCAT != null) {
            throw new IllegalStateException("Protocolo CAT já emitido e não pode ser alterado.");
        }
        ProtocoloCAT protocolo = ProtocoloCAT.gerar(this.getDataOcorrencia(), sequencial);
        return new Acidente(
                this.getIdOcorrencia(),
                this.getDescricao(),
                this.getDataOcorrencia(),
                this.getDataRegistro(),
                this.getStatusOcorrencia(),
                this.getTestemunhas(),
                this.getColaborador(),
                this.getMidias(),
                this.getArea(),
                this.getGestor(),
                this.getLocal(),
                this.getPlanoDeAcao(),
                this.causaRaiz,
                this.tipo,
                this.dano,
                protocolo.getValor(),
                this.destino
        );
    }

    public CausaRaiz getCausaRaiz() {
        return causaRaiz;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDano() {
        return dano;
    }

    public String getNumeroProtocoloCAT() {
        return numeroProtocoloCAT;
    }

    public String getNumeroProtocolo() {
        return numeroProtocoloCAT;
    }

    public String getDestino() {
        return destino;
    }

    private static CausaRaiz converterCausaRaiz(String causa) {
        if (causa == null || causa.isBlank()) {
            return CausaRaiz.OUTRO;
        }
        try {
            return CausaRaiz.valueOf(causa.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CausaRaiz.OUTRO;
        }
    }
}
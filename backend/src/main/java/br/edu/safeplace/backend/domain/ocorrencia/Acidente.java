package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

public class Acidente extends Ocorrencia {
    private final CausaRaizAcidente causaRaiz;
    private final String tipo;
    private final String dano;
    private final String numeroProtocoloCAT;
    private final String destino;
    private final NivelPerigo gravidade;

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
            CausaRaizAcidente causaRaiz,
            String tipo,
            String dano,
            String numeroProtocoloCAT,
            String destino,
            NivelPerigo gravidade) {
        super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                testemunhas, colaborador, midias, area, gestor, local, planoDeAcao);

        if (causaRaiz == null) {
            throw new IllegalArgumentException("Causa raiz é obrigatória para acidentes.");
        }

        if (numeroProtocoloCAT != null && !numeroProtocoloCAT.isBlank()) {
            this.numeroProtocoloCAT = NumeroProtocoloCAT.de(numeroProtocoloCAT).getValor();
        } else {
            this.numeroProtocoloCAT = null;
        }

        this.causaRaiz = causaRaiz;
        this.tipo = tipo != null ? tipo.trim() : null;
        this.dano = dano != null ? dano.trim() : null;
        this.destino = destino != null ? destino.trim() : null;
        this.gravidade = gravidade != null ? gravidade : NivelPerigo.BAIXO;
    }

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
            CausaRaizAcidente causaRaiz,
            String tipo,
            String dano,
            String numeroProtocoloCAT,
            String destino) {
        this(
                idOcorrencia,
                descricao,
                dataOcorrencia,
                dataRegistro,
                statusOcorrencia,
                testemunhas,
                colaborador,
                midias,
                area,
                gestor,
                local,
                planoDeAcao,
                causaRaiz,
                tipo,
                dano,
                numeroProtocoloCAT,
                destino,
                NivelPerigo.BAIXO
        );
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
            CausaRaizAcidente causaRaiz,
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
                null,
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
            CausaRaizAcidente causaRaiz,
            String tipo,
            String dano,
            String destino) {
        return novo(
                descricao,
                dataOcorrencia,
                area,
                colaborador,
                gestor,
                testemunhas,
                midias,
                causaRaiz,
                tipo,
                dano,
                destino,
                NivelPerigo.BAIXO
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
            CausaRaizAcidente causaRaiz,
            String tipo,
            String dano,
            String destino,
            NivelPerigo gravidade) {
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
                destino,
                gravidade
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
        return novo(
                dataOcorrencia,
                local,
                descricao,
                planoDeAcao,
                causaRaiz,
                tipo,
                dano,
                numeroProtocolo,
                destino,
                NivelPerigo.BAIXO
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
            String destino,
            NivelPerigo gravidade) {
        return new Acidente(
                null,
                descricao,
                dataOcorrencia,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null,
                null,
                null,
                null,
                null,
                local,
                planoDeAcao,
                converterCausaRaiz(causaRaiz),
                tipo,
                dano,
                numeroProtocolo,
                destino,
                gravidade
        );
    }

    public Acidente consolidarCAT(long sequencial) {
        if (this.numeroProtocoloCAT != null) {
            throw new IllegalStateException("Protocolo CAT já emitido e não pode ser alterado.");
        }
        NumeroProtocoloCAT protocolo = NumeroProtocoloCAT.gerar(this.getDataOcorrencia(), sequencial);
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
                this.getLocalInformado(),
                this.getPlanoDeAcao(),
                this.causaRaiz,
                this.tipo,
                this.dano,
                protocolo.getValor(),
                this.destino,
                this.gravidade
        );
    }

    public NivelPerigo getGravidade() {
        return gravidade;
    }

    @Override
    public boolean isGrave() {
        return gravidade != null && gravidade.isPeloMenos(NivelPerigo.ALTO);
    }

    public CausaRaizAcidente getCausaRaiz() {
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

    private static CausaRaizAcidente converterCausaRaiz(String causa) {
        if (causa == null || causa.isBlank()) {
            return CausaRaizAcidente.OUTRO;
        }
        try {
            return CausaRaizAcidente.valueOf(causa.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CausaRaizAcidente.OUTRO;
        }
    }
}
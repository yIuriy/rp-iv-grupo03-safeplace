package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object representando um alerta preventivo gerado para um EPI (RF21, UC10).
 */
public class AlertaSubstituicaoEpi {

    private final Integer epiId;
    private final String nomeEpi;
    private final TipoAlertaSubstituicao tipoAlerta;
    private final String mensagem;
    private final LocalDate dataLimiteEstimada;
    private final Long diasRestantes;
    private final String prioridade;

    public AlertaSubstituicaoEpi(Integer epiId, String nomeEpi, TipoAlertaSubstituicao tipoAlerta,
                                 String mensagem, LocalDate dataLimiteEstimada, Long diasRestantes,
                                 String prioridade) {
        if (epiId == null) {
            throw new IllegalArgumentException("Identificador do EPI é obrigatório no alerta.");
        }
        if (nomeEpi == null || nomeEpi.isBlank()) {
            throw new IllegalArgumentException("Nome do EPI é obrigatório no alerta.");
        }
        if (tipoAlerta == null) {
            throw new IllegalArgumentException("Tipo de alerta é obrigatório.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem do alerta é obrigatória.");
        }

        this.epiId = epiId;
        this.nomeEpi = nomeEpi.trim();
        this.tipoAlerta = tipoAlerta;
        this.mensagem = mensagem.trim();
        this.dataLimiteEstimada = dataLimiteEstimada;
        this.diasRestantes = diasRestantes;
        this.prioridade = prioridade != null ? prioridade.trim() : "MEDIA";
    }

    public static AlertaSubstituicaoEpi descarteProximo(Integer epiId, String nomeEpi,
                                                        LocalDate dataDescarte, long diasRestantes) {
        String prioridade = diasRestantes <= 7 ? "ALTA" : (diasRestantes <= 15 ? "MEDIA" : "BAIXA");
        String msg = String.format("EPI '%s' (ID %d) atingirá a data estimada de descarte em %s (%d dias restantes).",
                nomeEpi, epiId, dataDescarte, diasRestantes);
        return new AlertaSubstituicaoEpi(epiId, nomeEpi, TipoAlertaSubstituicao.DESCARTE_PROXIMO,
                msg, dataDescarte, diasRestantes, prioridade);
    }

    public static AlertaSubstituicaoEpi caProximoVencimento(Integer epiId, String nomeEpi,
                                                            LocalDate validadeCa, long diasRestantes) {
        String prioridade = diasRestantes <= 10 ? "ALTA" : "MEDIA";
        String msg = diasRestantes < 0
                ? String.format("O CA do EPI '%s' (ID %d) venceu em %s (há %d dias).",
                        nomeEpi, epiId, validadeCa, -diasRestantes)
                : String.format("O CA do EPI '%s' (ID %d) expirará em %s (%d dias restantes).",
                        nomeEpi, epiId, validadeCa, diasRestantes);
        return new AlertaSubstituicaoEpi(epiId, nomeEpi, TipoAlertaSubstituicao.CA_PROXIMO_VENCIMENTO,
                msg, validadeCa, diasRestantes, prioridade);
    }

    public static AlertaSubstituicaoEpi estoqueCritico(Integer epiId, String nomeEpi,
                                                       int saldoAtual, int estoqueMinimo) {
        String prioridade = saldoAtual == 0 ? "CRITICA" : "ALTA";
        String msg = String.format("EPI '%s' (ID %d) está em estoque crítico: saldo atual %d <= estoque mínimo %d.",
                nomeEpi, epiId, saldoAtual, estoqueMinimo);
        return new AlertaSubstituicaoEpi(epiId, nomeEpi, TipoAlertaSubstituicao.ESTOQUE_CRITICO,
                msg, null, null, prioridade);
    }

    public Integer getEpiId() {
        return epiId;
    }

    public String getNomeEpi() {
        return nomeEpi;
    }

    public TipoAlertaSubstituicao getTipoAlerta() {
        return tipoAlerta;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDate getDataLimiteEstimada() {
        return dataLimiteEstimada;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    public String getPrioridade() {
        return prioridade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertaSubstituicaoEpi that = (AlertaSubstituicaoEpi) o;
        return Objects.equals(epiId, that.epiId) &&
                tipoAlerta == that.tipoAlerta &&
                Objects.equals(dataLimiteEstimada, that.dataLimiteEstimada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epiId, tipoAlerta, dataLimiteEstimada);
    }
}

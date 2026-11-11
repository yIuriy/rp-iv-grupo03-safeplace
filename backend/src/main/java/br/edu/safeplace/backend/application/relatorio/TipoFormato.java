/*
 * Formatos de exportação dos relatórios (RF18, issue #299). Cada formato tem exatamente um
 * FormatoRelatorio registrado (conferido na inicialização do RelatorioService e em teste).
 */
package br.edu.safeplace.backend.application.relatorio;

import java.util.Arrays;

public enum TipoFormato {
    CSV("text/csv;charset=UTF-8", "csv"),
    PDF("application/pdf", "pdf");

    private final String tipoConteudo;
    private final String extensao;

    TipoFormato(String tipoConteudo, String extensao) {
        this.tipoConteudo = tipoConteudo;
        this.extensao = extensao;
    }

    public String tipoConteudo() {
        return tipoConteudo;
    }

    public String extensao() {
        return extensao;
    }

    /** Formato ausente vira PDF; formato desconhecido é recusado. */
    public static TipoFormato de(String valor) {
        if (valor == null || valor.isBlank()) {
            return PDF;
        }
        return Arrays.stream(values())
                .filter(formato -> formato.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Formato de relatório inválido: " + valor.trim() + ". Use CSV ou PDF."));
    }
}

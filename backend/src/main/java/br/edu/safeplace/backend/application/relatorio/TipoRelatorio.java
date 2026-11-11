/*
 * Relatórios estatísticos disponíveis (RF18, issue #299), na ordem do requisito: acidentes,
 * treinamentos realizados e áreas de maior risco. Cada tipo tem exatamente um Relatorio registrado
 * (conferido na inicialização do RelatorioService e em teste).
 */
package br.edu.safeplace.backend.application.relatorio;

import java.util.Arrays;

public enum TipoRelatorio {
    ACIDENTES("Relatório de acidentes e incidentes", "acidentes"),
    TREINAMENTOS("Relatório de treinamentos e certificações", "treinamentos"),
    AREAS_RISCO("Relatório de áreas de maior risco", "areas-risco");

    private final String titulo;
    private final String caminho;

    TipoRelatorio(String titulo, String caminho) {
        this.titulo = titulo;
        this.caminho = caminho;
    }

    public String titulo() {
        return titulo;
    }

    /** Trecho usado na URL e no nome do arquivo (ex.: {@code areas-risco}). */
    public String caminho() {
        return caminho;
    }

    /** Aceita o nome da constante ou o trecho da URL, sem diferenciar maiúsculas. */
    public static TipoRelatorio de(String valor) {
        String informado = valor == null ? "" : valor.trim();
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equalsIgnoreCase(informado) || tipo.caminho.equalsIgnoreCase(informado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de relatório inválido: " + informado
                        + ". Use acidentes, treinamentos ou areas-risco."));
    }
}

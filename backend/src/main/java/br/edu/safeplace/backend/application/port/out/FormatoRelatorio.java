/*
 * Porta de saída da escrita de relatórios (RF18, issue #299). No padrão Bridge é o Implementor: os
 * relatórios (Abstraction) descrevem o conteúdo e cada formato (CSV, PDF) decide como escrevê-lo.
 * As implementações ficam em adapters/out/relatorio, porque dependem de bibliotecas de arquivo.
 */
package br.edu.safeplace.backend.application.port.out;

import br.edu.safeplace.backend.application.relatorio.TipoFormato;

public interface FormatoRelatorio {

    /** Formato atendido; o RelatorioService monta o mapa de formatos por este valor. */
    TipoFormato tipo();

    /**
     * Começa um arquivo novo. A escrita devolvida guarda o estado desse arquivo, então o formato
     * em si não guarda estado e pode ser um bean compartilhado.
     */
    EscritaRelatorio iniciar(String titulo, String subtitulo);
}

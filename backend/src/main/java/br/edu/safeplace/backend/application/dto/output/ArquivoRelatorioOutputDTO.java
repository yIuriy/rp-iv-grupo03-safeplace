/*
 * Arquivo de um relatório exportado (RF18, issue #299): nome sugerido para o download, tipo de
 * conteúdo HTTP e os bytes já escritos pelo formato escolhido.
 */
package br.edu.safeplace.backend.application.dto.output;

public record ArquivoRelatorioOutputDTO(String nome, String tipoConteudo, byte[] conteudo) {
}

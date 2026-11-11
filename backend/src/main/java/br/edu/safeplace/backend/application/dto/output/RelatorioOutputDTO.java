/*
 * Conteúdo de um relatório estatístico (RF18, issue #299), independente de formato: é o que a tela
 * pré-visualiza e o que os formatos CSV e PDF escrevem. Cada seção tem indicadores (rótulo e valor)
 * e, opcionalmente, uma tabela. {@code semRegistros} avisa que o período não tem nenhum registro do
 * assunto, para a tela e o arquivo não parecerem um relatório quebrado.
 */
package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RelatorioOutputDTO(
        String tipo,
        String titulo,
        LocalDate inicio,
        LocalDate fim,
        LocalDateTime geradoEm,
        boolean semRegistros,
        List<Secao> secoes) {

    public record Secao(String titulo, List<Indicador> indicadores, Tabela tabela) {

        /** Seção só com indicadores, sem tabela. */
        public Secao(String titulo, List<Indicador> indicadores) {
            this(titulo, indicadores, null);
        }

        /** Seção só com tabela, sem indicadores. */
        public Secao(String titulo, Tabela tabela) {
            this(titulo, List.of(), tabela);
        }
    }

    public record Indicador(String rotulo, String valor) {
    }

    /**
     * Tabela da seção. {@code semDados} é o texto exibido no lugar das linhas quando não há nenhuma
     * (por exemplo, "Nenhum treinamento realizado no período.").
     */
    public record Tabela(List<String> colunas, List<List<String>> linhas, String semDados) {

        public boolean vazia() {
            return linhas.isEmpty();
        }
    }
}

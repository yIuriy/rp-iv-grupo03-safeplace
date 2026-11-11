/*
 * Formato CSV dos relatórios (RF18, issue #299): ConcreteImplementor do Bridge. Usa ponto e vírgula
 * como separador e começa com o BOM do UTF-8, para o Excel em português abrir com colunas e acentos
 * corretos. Cada seção vira um bloco: título, indicadores (rótulo;valor) e a tabela com cabeçalho.
 */
package br.edu.safeplace.backend.adapters.out.relatorio;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.EscritaRelatorio;
import br.edu.safeplace.backend.application.port.out.FormatoRelatorio;
import br.edu.safeplace.backend.application.relatorio.TipoFormato;

@Component
public class FormatoCsv implements FormatoRelatorio {

    static final char SEPARADOR = ';';
    private static final String BOM = "﻿";
    private static final String QUEBRA = "\r\n";

    @Override
    public TipoFormato tipo() {
        return TipoFormato.CSV;
    }

    @Override
    public EscritaRelatorio iniciar(String titulo, String subtitulo) {
        return new EscritaCsv(titulo, subtitulo);
    }

    private static final class EscritaCsv implements EscritaRelatorio {

        private final StringBuilder texto = new StringBuilder(BOM);

        EscritaCsv(String titulo, String subtitulo) {
            linha(List.of(titulo));
            linha(List.of(subtitulo));
        }

        @Override
        public void secao(String titulo) {
            texto.append(QUEBRA);
            linha(List.of(titulo));
        }

        @Override
        public void indicador(String rotulo, String valor) {
            linha(List.of(rotulo, valor));
        }

        @Override
        public void tabela(List<String> colunas, List<List<String>> linhas) {
            linha(colunas);
            linhas.forEach(this::linha);
        }

        @Override
        public void aviso(String texto) {
            linha(List.of(texto));
        }

        @Override
        public byte[] concluir() {
            return texto.toString().getBytes(StandardCharsets.UTF_8);
        }

        private void linha(List<String> celulas) {
            texto.append(celulas.stream().map(FormatoCsv::celula).collect(Collectors.joining(String.valueOf(SEPARADOR))))
                    .append(QUEBRA);
        }
    }

    /** Aspas só quando a célula tem separador, aspas ou quebra de linha (RFC 4180). */
    static String celula(String valor) {
        String texto = valor == null ? "" : valor;
        if (texto.indexOf(SEPARADOR) >= 0 || texto.indexOf('"') >= 0 || texto.indexOf('\n') >= 0 || texto.indexOf('\r') >= 0) {
            return '"' + texto.replace("\"", "\"\"") + '"';
        }
        return texto;
    }
}

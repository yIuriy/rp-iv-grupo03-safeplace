package br.edu.safeplace.backend.adapters.out.relatorio;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.EscritaRelatorio;
import br.edu.safeplace.backend.application.port.out.FormatoRelatorio;
import br.edu.safeplace.backend.application.relatorio.TipoFormato;

@DisplayName("Formatos de relatório - ConcreteImplementors do Bridge (RF18, #299)")
class FormatosRelatorioTest {

    /** Mesma sequência de operações primitivas para os dois formatos, como o relatório faz. */
    private static byte[] escrever(FormatoRelatorio formato, int linhas) {
        EscritaRelatorio escrita = formato.iniciar("Relatório de teste", "Período: 01/11/2026 a 30/11/2026");
        escrita.secao("Resumo");
        escrita.indicador("Acidentes", "2");
        escrita.secao("Ocorrências por área");
        List<List<String>> corpo = new ArrayList<>();
        IntStream.rangeClosed(1, linhas).forEach(i -> corpo.add(List.of("Área " + i + "; galpão \"A\"", Integer.toString(i))));
        escrita.tabela(List.of("Área", "Total"), corpo);
        escrita.aviso("Fim do relatório.");
        return escrita.concluir();
    }

    @Test
    @DisplayName("Cada formato informa o seu tipo")
    void tipos() {
        assertThat(List.of(new FormatoCsv(), new FormatoPdf()))
                .extracting(FormatoRelatorio::tipo)
                .containsExactlyInAnyOrder(TipoFormato.values());
    }

    @Test
    @DisplayName("CSV: BOM, ponto e vírgula e aspas apenas quando a célula precisa")
    void csv() {
        String texto = new String(escrever(new FormatoCsv(), 1), StandardCharsets.UTF_8);

        assertThat(texto).startsWith("﻿Relatório de teste\r\n");
        assertThat(texto).contains("Acidentes;2\r\n");
        assertThat(texto).contains("Área;Total\r\n");
        assertThat(texto).contains("\"Área 1; galpão \"\"A\"\"\";1\r\n");
        assertThat(texto).endsWith("Fim do relatório.\r\n");
    }

    @Test
    @DisplayName("PDF: documento válido, com título e acentos, e várias páginas numeradas quando a tabela é longa")
    void pdf() throws IOException {
        byte[] curto = escrever(new FormatoPdf(), 3);
        byte[] longo = escrever(new FormatoPdf(), 120);

        try (PDDocument documento = Loader.loadPDF(curto)) {
            String texto = new PDFTextStripper().getText(documento);
            assertThat(documento.getNumberOfPages()).isEqualTo(1);
            assertThat(texto).contains("Relatório de teste", "Ocorrências por área", "Página 1 de 1");
        }
        try (PDDocument documento = Loader.loadPDF(longo)) {
            int paginas = documento.getNumberOfPages();
            assertThat(paginas).isGreaterThan(1);
            assertThat(new PDFTextStripper().getText(documento)).contains("Página " + paginas + " de " + paginas);
        }
    }

    @Test
    @DisplayName("PDF: caractere fora das fontes padrão vira '?' em vez de falhar")
    void pdfComCaractereSemGlifo() throws IOException {
        EscritaRelatorio escrita = new FormatoPdf().iniciar("Relatório ✓", "Subtítulo");
        try (PDDocument documento = Loader.loadPDF(escrita.concluir())) {
            assertThat(new PDFTextStripper().getText(documento)).contains("Relatório ?");
        }
    }
}

/*
 * Formato PDF dos relatórios (RF18, issue #299): ConcreteImplementor do Bridge, escrito com o Apache
 * PDFBox. Página A4 com cabeçalho, indicadores em lista, tabelas com cabeçalho destacado e linhas
 * alternadas, quebra de página repetindo o cabeçalho da tabela e rodapé com "Página n de N".
 */
package br.edu.safeplace.backend.adapters.out.relatorio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.EscritaRelatorio;
import br.edu.safeplace.backend.application.port.out.FormatoRelatorio;
import br.edu.safeplace.backend.application.relatorio.TipoFormato;

@Component
public class FormatoPdf implements FormatoRelatorio {

    @Override
    public TipoFormato tipo() {
        return TipoFormato.PDF;
    }

    @Override
    public EscritaRelatorio iniciar(String titulo, String subtitulo) {
        return new EscritaPdf(titulo, subtitulo);
    }

    /** Estado de um arquivo: documento, página atual e posição vertical do cursor. */
    private static final class EscritaPdf implements EscritaRelatorio {

        private static final PDRectangle PAGINA = PDRectangle.A4;
        private static final float MARGEM = 48;
        private static final float LARGURA = PAGINA.getWidth() - 2 * MARGEM;
        private static final float LIMITE_INFERIOR = MARGEM + 20;
        private static final float LINHA_TABELA = 16;

        private static final float[] TEXTO = {0.13f, 0.15f, 0.18f};
        private static final float[] SECUNDARIO = {0.40f, 0.43f, 0.48f};
        private static final float[] DESTAQUE = {0.08f, 0.29f, 0.47f};
        private static final float[] FUNDO_CABECALHO = {0.89f, 0.92f, 0.95f};
        private static final float[] FUNDO_ALTERNADO = {0.97f, 0.97f, 0.98f};
        private static final float[] DIVISORIA = {0.82f, 0.84f, 0.87f};

        private final PDDocument documento = new PDDocument();
        private final PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont negrito = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private final PDFont italico = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        private final String titulo;

        private PDPageContentStream conteudo;
        private float y;

        EscritaPdf(String titulo, String subtitulo) {
            this.titulo = titulo;
            executar(() -> {
                novaPagina();
                escrever("SAFEPLACE", MARGEM, y, negrito, 8, DESTAQUE);
                y -= 22;
                escrever(titulo, MARGEM, y, negrito, 17, TEXTO);
                y -= 16;
                escrever(subtitulo, MARGEM, y, regular, 9, SECUNDARIO);
                y -= 10;
                divisoria(y, 1f);
                y -= 6;
            });
        }

        @Override
        public void secao(String tituloSecao) {
            executar(() -> {
                garantir(48);
                y -= 20;
                escrever(tituloSecao, MARGEM, y, negrito, 12, DESTAQUE);
                y -= 8;
            });
        }

        @Override
        public void indicador(String rotulo, String valor) {
            executar(() -> {
                garantir(18);
                y -= 14;
                escrever(rotulo, MARGEM, y, regular, 10, TEXTO);
                escreverDireita(valor, MARGEM + LARGURA, y, negrito, 10, TEXTO);
                y -= 4;
                divisoria(y, 0.4f);
            });
        }

        @Override
        public void tabela(List<String> colunas, List<List<String>> linhas) {
            executar(() -> {
                float[] larguras = larguras(colunas, linhas);
                boolean[] numericas = numericas(colunas.size(), linhas);
                garantir(LINHA_TABELA * 3);
                y -= 6;
                cabecalhoTabela(colunas, larguras, numericas);
                for (int i = 0; i < linhas.size(); i++) {
                    if (y - LINHA_TABELA < LIMITE_INFERIOR) {
                        novaPagina();
                        cabecalhoTabela(colunas, larguras, numericas);
                    }
                    if (i % 2 == 1) {
                        retangulo(MARGEM, y - LINHA_TABELA, LARGURA, LINHA_TABELA, FUNDO_ALTERNADO);
                    }
                    celulas(linhas.get(i), larguras, numericas, regular);
                }
                divisoria(y, 0.4f);
            });
        }

        @Override
        public void aviso(String texto) {
            executar(() -> {
                garantir(18);
                y -= 14;
                escrever(texto, MARGEM, y, italico, 9, SECUNDARIO);
            });
        }

        @Override
        public byte[] concluir() {
            try (documento) {
                conteudo.close();
                rodapes();
                ByteArrayOutputStream saida = new ByteArrayOutputStream();
                documento.save(saida);
                return saida.toByteArray();
            } catch (IOException e) {
                throw new UncheckedIOException("Não foi possível gerar o PDF do relatório.", e);
            }
        }

        // Tabela

        private void cabecalhoTabela(List<String> colunas, float[] larguras, boolean[] numericas) throws IOException {
            retangulo(MARGEM, y - LINHA_TABELA, LARGURA, LINHA_TABELA, FUNDO_CABECALHO);
            celulas(colunas, larguras, numericas, negrito);
        }

        private void celulas(List<String> valores, float[] larguras, boolean[] numericas, PDFont fonte) throws IOException {
            float x = MARGEM;
            float base = y - LINHA_TABELA + 5;
            for (int c = 0; c < larguras.length; c++) {
                String valor = c < valores.size() ? valores.get(c) : "";
                String cabe = caber(valor, fonte, 8.5f, larguras[c] - 8);
                if (numericas[c]) {
                    escreverDireita(cabe, x + larguras[c] - 4, base, fonte, 8.5f, TEXTO);
                } else {
                    escrever(cabe, x + 4, base, fonte, 8.5f, TEXTO);
                }
                x += larguras[c];
            }
            y -= LINHA_TABELA;
        }

        /** Largura proporcional ao maior texto da coluna, com limites para nenhuma sumir ou dominar. */
        private static float[] larguras(List<String> colunas, List<List<String>> linhas) {
            float[] pesos = new float[colunas.size()];
            float total = 0;
            for (int c = 0; c < colunas.size(); c++) {
                int maior = colunas.get(c).length();
                for (List<String> linha : linhas) {
                    if (c < linha.size() && linha.get(c) != null) {
                        maior = Math.max(maior, linha.get(c).length());
                    }
                }
                pesos[c] = Math.clamp(maior, 6, 40);
                total += pesos[c];
            }
            for (int c = 0; c < pesos.length; c++) {
                pesos[c] = LARGURA * pesos[c] / total;
            }
            return pesos;
        }

        /** Coluna numérica (todas as células são inteiros) fica alinhada à direita. */
        private static boolean[] numericas(int quantidade, List<List<String>> linhas) {
            boolean[] numericas = new boolean[quantidade];
            for (int c = 0; c < quantidade; c++) {
                int coluna = c;
                numericas[c] = !linhas.isEmpty() && linhas.stream()
                        .allMatch(l -> coluna < l.size() && l.get(coluna) != null && l.get(coluna).matches("-?\\d+"));
            }
            return numericas;
        }

        // Página

        private void garantir(float altura) throws IOException {
            if (y - altura < LIMITE_INFERIOR) {
                novaPagina();
            }
        }

        private void novaPagina() throws IOException {
            if (conteudo != null) {
                conteudo.close();
            }
            PDPage pagina = new PDPage(PAGINA);
            documento.addPage(pagina);
            conteudo = new PDPageContentStream(documento, pagina);
            y = PAGINA.getHeight() - MARGEM;
        }

        private void rodapes() throws IOException {
            int total = documento.getNumberOfPages();
            for (int i = 0; i < total; i++) {
                try (PDPageContentStream rodape = new PDPageContentStream(documento, documento.getPage(i),
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    PDPageContentStream atual = conteudo;
                    conteudo = rodape;
                    escrever("SafePlace · " + titulo, MARGEM, MARGEM - 20, regular, 8, SECUNDARIO);
                    escreverDireita("Página " + (i + 1) + " de " + total, MARGEM + LARGURA, MARGEM - 20, regular, 8, SECUNDARIO);
                    conteudo = atual;
                }
            }
        }

        // Desenho

        private void escrever(String texto, float x, float yBase, PDFont fonte, float tamanho, float[] cor) throws IOException {
            conteudo.beginText();
            conteudo.setFont(fonte, tamanho);
            conteudo.setNonStrokingColor(cor[0], cor[1], cor[2]);
            conteudo.newLineAtOffset(x, yBase);
            conteudo.showText(seguro(texto, fonte));
            conteudo.endText();
        }

        private void escreverDireita(String texto, float xFinal, float yBase, PDFont fonte, float tamanho, float[] cor)
                throws IOException {
            String seguro = seguro(texto, fonte);
            escrever(seguro, xFinal - largura(seguro, fonte, tamanho), yBase, fonte, tamanho, cor);
        }

        private void retangulo(float x, float yBase, float largura, float altura, float[] cor) throws IOException {
            conteudo.setNonStrokingColor(cor[0], cor[1], cor[2]);
            conteudo.addRect(x, yBase, largura, altura);
            conteudo.fill();
        }

        private void divisoria(float yLinha, float espessura) throws IOException {
            conteudo.setStrokingColor(DIVISORIA[0], DIVISORIA[1], DIVISORIA[2]);
            conteudo.setLineWidth(espessura);
            conteudo.moveTo(MARGEM, yLinha);
            conteudo.lineTo(MARGEM + LARGURA, yLinha);
            conteudo.stroke();
        }

        /** Corta o texto com reticências para caber na largura da célula. */
        private String caber(String texto, PDFont fonte, float tamanho, float maximo) throws IOException {
            String seguro = seguro(texto, fonte);
            if (largura(seguro, fonte, tamanho) <= maximo) {
                return seguro;
            }
            String cortado = seguro;
            while (!cortado.isEmpty() && largura(cortado + "…", fonte, tamanho) > maximo) {
                cortado = cortado.substring(0, cortado.length() - 1);
            }
            return cortado + "…";
        }

        private static float largura(String texto, PDFont fonte, float tamanho) throws IOException {
            return fonte.getStringWidth(texto) / 1000 * tamanho;
        }

        /**
         * As fontes padrão do PDF só têm os caracteres do WinAnsi (acentos do português incluídos).
         * Qualquer outro caractere vira "?" em vez de interromper a geração.
         */
        private static String seguro(String texto, PDFont fonte) {
            String valor = texto == null ? "" : texto.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
            try {
                fonte.encode(valor);
                return valor;
            } catch (IOException | IllegalArgumentException e) {
                StringBuilder seguro = new StringBuilder();
                valor.codePoints().forEach(codigo -> {
                    String caractere = new String(Character.toChars(codigo));
                    try {
                        fonte.encode(caractere);
                        seguro.append(caractere);
                    } catch (IOException | IllegalArgumentException semGlifo) {
                        seguro.append('?');
                    }
                });
                return seguro.toString();
            }
        }

        private void executar(Passo passo) {
            try {
                passo.executar();
            } catch (IOException e) {
                throw new UncheckedIOException("Não foi possível gerar o PDF do relatório.", e);
            }
        }

        @FunctionalInterface
        private interface Passo {
            void executar() throws IOException;
        }
    }
}

/*
 * Operações primitivas da escrita de um relatório (RF18, issue #299), oferecidas pelo Implementor do
 * Bridge. O relatório chama, na ordem, uma seção, seus indicadores e sua tabela; concluir devolve os
 * bytes do arquivo.
 */
package br.edu.safeplace.backend.application.port.out;

import java.util.List;

public interface EscritaRelatorio {

    void secao(String titulo);

    void indicador(String rotulo, String valor);

    void tabela(List<String> colunas, List<List<String>> linhas);

    /** Texto avulso, como o aviso de tabela sem linhas. */
    void aviso(String texto);

    byte[] concluir();
}

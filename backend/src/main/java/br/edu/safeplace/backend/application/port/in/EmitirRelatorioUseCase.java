/*
 * Porta de entrada dos relatórios estatísticos (RF18, issue #299). Sem datas, o período é o mês
 * corrente. O tipo aceita o nome (ACIDENTES) ou o trecho da URL (acidentes); o formato, CSV ou PDF.
 */
package br.edu.safeplace.backend.application.port.in;

import java.time.LocalDate;

import br.edu.safeplace.backend.application.dto.output.ArquivoRelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;

public interface EmitirRelatorioUseCase {

    /** Conteúdo do relatório, para a pré-visualização na tela. */
    RelatorioOutputDTO gerar(String tipo, LocalDate inicio, LocalDate fim);

    /** Arquivo do relatório no formato pedido, com a emissão registrada na auditoria. */
    ArquivoRelatorioOutputDTO exportar(String tipo, LocalDate inicio, LocalDate fim, String formato);
}

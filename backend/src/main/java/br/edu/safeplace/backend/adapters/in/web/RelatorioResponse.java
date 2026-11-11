/*
 * Resposta da pré-visualização de relatório (RF18, issue #299): o mesmo conteúdo que os formatos CSV e
 * PDF escrevem, em JSON.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;

public record RelatorioResponse(
        String tipo,
        String titulo,
        LocalDate inicio,
        LocalDate fim,
        LocalDateTime geradoEm,
        boolean semRegistros,
        List<SecaoResponse> secoes) {

    public record SecaoResponse(String titulo, List<IndicadorResponse> indicadores, TabelaResponse tabela) {
    }

    public record IndicadorResponse(String rotulo, String valor) {
    }

    public record TabelaResponse(List<String> colunas, List<List<String>> linhas, String semDados) {
    }

    public static RelatorioResponse fromOutputDTO(RelatorioOutputDTO dto) {
        return new RelatorioResponse(dto.tipo(), dto.titulo(), dto.inicio(), dto.fim(), dto.geradoEm(), dto.semRegistros(),
                dto.secoes().stream().map(RelatorioResponse::secao).toList());
    }

    private static SecaoResponse secao(RelatorioOutputDTO.Secao secao) {
        RelatorioOutputDTO.Tabela tabela = secao.tabela();
        return new SecaoResponse(secao.titulo(),
                secao.indicadores().stream().map(i -> new IndicadorResponse(i.rotulo(), i.valor())).toList(),
                tabela == null ? null : new TabelaResponse(tabela.colunas(), tabela.linhas(), tabela.semDados()));
    }
}

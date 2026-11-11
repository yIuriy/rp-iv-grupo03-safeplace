/*
 * Endpoints dos relatórios estatísticos (RF18, issue #299): pré-visualização em JSON e download do
 * arquivo em CSV ou PDF. Leitura para Gestor de Segurança e Supervisor no SecurityConfig.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.dto.output.ArquivoRelatorioOutputDTO;
import br.edu.safeplace.backend.application.port.in.EmitirRelatorioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Relatórios estatísticos em CSV e PDF (RF18)")
public class RelatorioController {

    private final EmitirRelatorioUseCase useCase;

    public RelatorioController(EmitirRelatorioUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{tipo}")
    @Operation(summary = "Pré-visualiza um relatório",
            description = "Tipos: acidentes, treinamentos e areas-risco. Sem datas, usa o mês corrente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteúdo do relatório, com resumo e seções"),
            @ApiResponse(responseCode = "400", description = "Tipo desconhecido, só uma das datas, período invertido ou maior que um ano"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public RelatorioResponse gerar(@PathVariable String tipo,
            @Parameter(description = "Primeiro dia do período (AAAA-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Último dia do período (AAAA-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return RelatorioResponse.fromOutputDTO(useCase.gerar(tipo, inicio, fim));
    }

    @GetMapping("/{tipo}/arquivo")
    @Operation(summary = "Baixa um relatório em CSV ou PDF",
            description = "A emissão fica registrada na trilha de auditoria. Sem formato, gera PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo do relatório como anexo"),
            @ApiResponse(responseCode = "400", description = "Tipo ou formato desconhecido, ou período inválido"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public ResponseEntity<byte[]> exportar(@PathVariable String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "CSV ou PDF") @RequestParam(required = false) String formato) {
        ArquivoRelatorioOutputDTO arquivo = useCase.exportar(tipo, inicio, fim, formato);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.tipoConteudo()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo.nome()).build().toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(arquivo.conteudo());
    }
}

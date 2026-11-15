package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.dto.input.GerarAnaliseProjecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlertaSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProjecaoSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ResultadoAnaliseSubstituicaoDTO;
import br.edu.safeplace.backend.application.port.in.PlanejarSubstituicaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Adaptador Web REST para o Caso de Uso UC10 – Planejar Substituição Inteligente de EPI
 * (RF21, US21, DS10).
 */
@RestController
@RequestMapping("/api/projecoes")
@Tag(name = "Substituição Inteligente de EPI", description = "Projeções de desgaste, alertas preventivos e reposição de EPIs (UC10, RF21)")
public class SubstituicaoEpiController {

    private final PlanejarSubstituicaoUseCase planejarSubstituicaoUseCase;

    public SubstituicaoEpiController(PlanejarSubstituicaoUseCase planejarSubstituicaoUseCase) {
        this.planejarSubstituicaoUseCase = planejarSubstituicaoUseCase;
    }

    /**
     * Operação gerarAnalise() do DS10: executa a análise de projeção de desgaste para os EPIs,
     * persistindo as projeções calculadas e retornando os dados consolidados.
     */
    @PostMapping("/analise")
    @Operation(summary = "Gera análise de projeção de desgaste para EPIs, persistindo resultados e reportando pendências",
            description = "Executa o cálculo de projeção considerando histórico de uso, periculosidade e CA legal. Erros ou pendências são reportados para permitir nova tentativa.")
    @ApiResponse(responseCode = "200", description = "Análise processada com sucesso")
    public ResponseEntity<ResultadoAnaliseSubstituicaoDTO> gerarAnalise(
            @RequestBody(required = false) GerarAnaliseProjecaoInputDTO input) {
        if (input == null) {
            input = new GerarAnaliseProjecaoInputDTO(LocalDate.now(), null, null);
        }
        ResultadoAnaliseSubstituicaoDTO resultado = planejarSubstituicaoUseCase.gerarAnalise(input);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Lista todas as projeções de substituição registradas no histórico.
     */
    @GetMapping
    @Operation(summary = "Lista todas as projeções de substituição registradas no histórico")
    public ResponseEntity<List<ProjecaoSubstituicaoOutputDTO>> listarProjecoes() {
        return ResponseEntity.ok(planejarSubstituicaoUseCase.listar());
    }

    /**
     * Lista o histórico de projeções de um EPI específico.
     */
    @GetMapping("/epi/{epiId}")
    @Operation(summary = "Lista o histórico de projeções de um EPI específico")
    public ResponseEntity<List<ProjecaoSubstituicaoOutputDTO>> listarPorEpi(@PathVariable Integer epiId) {
        return ResponseEntity.ok(planejarSubstituicaoUseCase.listarPorEpi(epiId));
    }

    /**
     * Operação emitirListaReposicao() do DS10: retorna a lista de alertas preventivos
     * (descarte ou CA vencido ou a vencer em até 30 dias, DS10 2.4, e estoque crítico).
     */
    @GetMapping("/alertas")
    @Operation(summary = "Emite alertas preventivos de substituição de EPIs",
            description = "Lista EPIs com descarte em até 30 dias, CA vencido ou a vencer em até 30 dias ou estoque em nível crítico.")
    public ResponseEntity<List<AlertaSubstituicaoOutputDTO>> emitirAlertas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        return ResponseEntity.ok(planejarSubstituicaoUseCase.emitirAlertas(dataReferencia));
    }
}

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

/**
 * Adaptador Web REST para o Caso de Uso UC10 – Planejar Substituição Inteligente de EPI
 * (RF21, US21, DS10).
 */
@RestController
@RequestMapping("/api/projecoes")
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
    public ResponseEntity<ResultadoAnaliseSubstituicaoDTO> gerarAnalise(
            @RequestBody(required = false) GerarAnaliseProjecaoInputDTO input) {
        if (input == null) {
            input = new GerarAnaliseProjecaoInputDTO(LocalDate.now(), null, 90);
        }
        ResultadoAnaliseSubstituicaoDTO resultado = planejarSubstituicaoUseCase.gerarAnalise(input);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Lista todas as projeções de substituição registradas no histórico.
     */
    @GetMapping
    public ResponseEntity<List<ProjecaoSubstituicaoOutputDTO>> listarProjecoes() {
        return ResponseEntity.ok(planejarSubstituicaoUseCase.listarProjecoes());
    }

    /**
     * Lista o histórico de projeções de um EPI específico.
     */
    @GetMapping("/epi/{epiId}")
    public ResponseEntity<List<ProjecaoSubstituicaoOutputDTO>> listarPorEpi(@PathVariable Integer epiId) {
        return ResponseEntity.ok(planejarSubstituicaoUseCase.listarProjecoesPorEpi(epiId));
    }

    /**
     * Operação emitirListaReposicao() do DS10: retorna a lista de alertas preventivos
     * (descarte ou CA próximo do vencimento em até diasAntecedencia e estoque crítico).
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaSubstituicaoOutputDTO>> emitirAlertas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia,
            @RequestParam(required = false, defaultValue = "30") Long diasAntecedencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        return ResponseEntity.ok(planejarSubstituicaoUseCase.emitirAlertasPreventivos(dataReferencia, diasAntecedencia));
    }
}

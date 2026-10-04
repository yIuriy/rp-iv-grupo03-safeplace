/*
 * Endpoints REST somente leitura de certificações e treinamentos (RF10, UC02, US10, issue #119):
 * listagem com filtro por situação, detalhes por colaborador, alertas de vencimento e consulta de
 * aptidão para uma tarefa. O cadastro não é exposto até a definição do fluxo e dos papéis de
 * escrita (#116, #126).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.dto.capacitacao.AlertasCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CapacitacoesColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ColaboradorCapacitacaoResumoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.capacitacao.AcompanharCapacitacoesUseCase;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/capacitacoes")
@Tag(name = "Capacitações", description = "Consulta de certificações, treinamentos, alertas de vencimento e aptidão (RF10, UC02, US10)")
public class CapacitacaoController {

    private final AcompanharCapacitacoesUseCase acompanharUseCase;
    private final AlocarColaboradorTarefaUseCase alocarUseCase;

    public CapacitacaoController(AcompanharCapacitacoesUseCase acompanharUseCase,
                                 AlocarColaboradorTarefaUseCase alocarUseCase) {
        this.acompanharUseCase = acompanharUseCase;
        this.alocarUseCase = alocarUseCase;
    }

    @GetMapping("/colaboradores")
    @Operation(summary = "Lista os colaboradores com a situação de suas certificações",
            description = "UC02, passo 2 e cenário alternativo IV. Sem filtro, inclui colaboradores sem certificação "
                    + "(semCertificacoes = true); com filtro, devolve apenas quem tem a situação consolidada informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem, vazia quando nada corresponde ao filtro"),
            @ApiResponse(responseCode = "400", description = "Situação fora de EM_DIA, PROXIMA_DO_VENCIMENTO ou VENCIDA"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public List<ColaboradorCapacitacaoResumoOutputDTO> listarColaboradores(
            @RequestParam(required = false) StatusCertificacao situacao) {
        return acompanharUseCase.listarColaboradores(situacao);
    }

    @GetMapping("/colaboradores/{colaboradorId}")
    @Operation(summary = "Detalha certificações, treinamentos e pendências de um colaborador",
            description = "UC02, passos 4 e 5 e cenários alternativos I e III.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capacitações do colaborador"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão"),
            @ApiResponse(responseCode = "404", description = "Colaborador não existe")
    })
    public CapacitacoesColaboradorOutputDTO consultarColaborador(@PathVariable Integer colaboradorId) {
        return acompanharUseCase.consultarColaborador(colaboradorId);
    }

    @GetMapping("/alertas")
    @Operation(summary = "Lista certificações próximas do vencimento, vencidas e pendências de integração",
            description = "UC02, regra 1 (antecedência de 30 dias) e cenários alternativos I e III.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alertas calculados na data da consulta"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public AlertasCapacitacaoOutputDTO consultarAlertas() {
        return acompanharUseCase.consultarAlertas();
    }

    @GetMapping("/colaboradores/{colaboradorId}/aptidao")
    @Operation(summary = "Consulta prévia de aptidão do colaborador para uma tarefa",
            description = "Aplica a mesma regra da confirmação de alocação (#117). O resultado é informativo e não "
                    + "autoriza a alocação: POST /api/tarefas/{id}/alocacoes valida novamente no momento da confirmação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diagnóstico de aptidão e impedimentos"),
            @ApiResponse(responseCode = "400", description = "Tarefa não informada"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão"),
            @ApiResponse(responseCode = "404", description = "Tarefa ou colaborador não encontrado")
    })
    public ConsultaAptidaoOutputDTO consultarAptidao(@PathVariable Integer colaboradorId,
                                                     @RequestParam Integer tarefaId) {
        return alocarUseCase.consultarAptidao(tarefaId, colaboradorId);
    }
}

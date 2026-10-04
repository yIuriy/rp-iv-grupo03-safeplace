/*
 * Porta de entrada somente leitura do acompanhamento de capacitações (RF10, UC02, US10, issue
 * #119). O cadastro de certificações e treinamentos não é exposto aqui: o fluxo e os papéis de
 * escrita aguardam decisão (#116, #126). A aptidão para uma tarefa é consultada pela regra de
 * #117 em AlocarColaboradorTarefaUseCase.
 */
package br.edu.safeplace.backend.application.port.in.capacitacao;

import java.util.List;

import br.edu.safeplace.backend.application.dto.capacitacao.AlertasCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CapacitacoesColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ColaboradorCapacitacaoResumoOutputDTO;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;

public interface AcompanharCapacitacoesUseCase {

    /**
     * Lista os colaboradores com a situação de suas certificações. Com filtro, devolve apenas os
     * que têm essa situação consolidada; sem filtro, inclui também quem não tem certificação.
     */
    List<ColaboradorCapacitacaoResumoOutputDTO> listarColaboradores(StatusCertificacao situacao);

    CapacitacoesColaboradorOutputDTO consultarColaborador(Integer colaboradorId);

    AlertasCapacitacaoOutputDTO consultarAlertas();
}

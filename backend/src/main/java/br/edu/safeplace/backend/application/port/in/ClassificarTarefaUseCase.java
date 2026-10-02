/*
 * Porta de entrada da classificação de periculosidade das tarefas (RF06 / UC11): cadastro, consultas,
 * classificação e histórico das classificações (issue #114).
 */
package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/**
 * Porta de entrada da classificação de periculosidade das tarefas (RF06 / UC11).
 */
public interface ClassificarTarefaUseCase {

    TarefaOutputDTO cadastrarTarefa(CadastrarTarefaInputDTO inputDTO);

    List<TarefaOutputDTO> listar();

    List<TarefaOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo);

    TarefaOutputDTO buscarPorId(Integer id);

    /**
     * UC11, cenário principal (passos 3 a 6) e alternativo I (reavaliação periódica).
     */
    TarefaOutputDTO classificar(Integer id, NivelPerigo nivelPerigo);

    /**
     * UC11, regra 1: cadastro e classificações registradas da tarefa, da mais recente para a mais
     * antiga, com nível anterior, novo, data, hora e responsável técnico.
     */
    List<RegistroHistoricoOutputDTO> listarHistorico(Integer id);
}

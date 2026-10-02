package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.AlocarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO;

public interface AlocarColaboradorTarefaUseCase {

    /**
     * Mutação final: valida rigorosamente as regras normativas e classifica a alocação,
     * persistindo no banco de dados e gravando log de auditoria.
     */
    AlocacaoTarefaOutputDTO alocar(Integer tarefaId, AlocarColaboradorInputDTO entrada, String operadorEmail);

    /**
     * Consulta prévia de aptidão: verifica preventivamente a situação do colaborador frente à tarefa
     * sem realizar mutação ou bloquear via exceção HTTP.
     */
    ConsultaAptidaoOutputDTO consultarAptidao(Integer tarefaId, Integer colaboradorId);

    /**
     * Lista o histórico de alocações registradas para uma tarefa.
     */
    List<AlocacaoTarefaOutputDTO> listarPorTarefa(Integer tarefaId);
}

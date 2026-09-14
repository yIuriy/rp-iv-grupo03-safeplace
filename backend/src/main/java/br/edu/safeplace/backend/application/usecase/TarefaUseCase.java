package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;

/**
 * Classificação do nível de periculosidade das tarefas (RF06 / UC11).
 */
@Service
public class TarefaUseCase implements ClassificarTarefaUseCase {

    private final TarefaRepositoryPort repositoryPort;

    public TarefaUseCase(TarefaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public TarefaOutputDTO cadastrarTarefa(CadastrarTarefaInputDTO inputDTO) {
        Tarefa novaTarefa = Tarefa.cadastrar(inputDTO.descricao(), inputDTO.nivelPerigo());
        return TarefaOutputDTO.deDominio(repositoryPort.salvar(novaTarefa));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarefaOutputDTO> listar() {
        return repositoryPort.listarTodas().stream()
                .map(TarefaOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarefaOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        if (nivelPerigo == null) {
            return listar();
        }
        return repositoryPort.listarPorNivelPerigo(nivelPerigo).stream()
                .map(TarefaOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TarefaOutputDTO buscarPorId(Integer id) {
        return TarefaOutputDTO.deDominio(buscarTarefa(id));
    }

    @Override
    @Transactional
    public TarefaOutputDTO classificar(Integer id, NivelPerigo nivelPerigo) {
        Tarefa tarefa = buscarTarefa(id);
        tarefa.classificar(nivelPerigo);
        return TarefaOutputDTO.deDominio(repositoryPort.salvar(tarefa));
    }

    private Tarefa buscarTarefa(Integer id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }
}

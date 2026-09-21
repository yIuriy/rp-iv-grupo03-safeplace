package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Classificação do nível de periculosidade das tarefas (RF06 / UC11).
 */
@Service
public class TarefaUseCase implements ClassificarTarefaUseCase {

    private final TarefaRepositoryPort repositoryPort;
    private final LogAuditoriaRepositoryPort logAuditoriaRepositoryPort;

    public TarefaUseCase(TarefaRepositoryPort repositoryPort) {
        this(repositoryPort, null);
    }

    @Autowired
    public TarefaUseCase(TarefaRepositoryPort repositoryPort, LogAuditoriaRepositoryPort logAuditoriaRepositoryPort) {
        this.repositoryPort = repositoryPort;
        this.logAuditoriaRepositoryPort = logAuditoriaRepositoryPort;
    }

    @Override
    @Transactional
    public TarefaOutputDTO cadastrarTarefa(CadastrarTarefaInputDTO inputDTO) {
        Tarefa novaTarefa = Tarefa.cadastrar(inputDTO.descricao(), inputDTO.nivelPerigo());
        Tarefa salva = repositoryPort.salvar(novaTarefa);

        if (logAuditoriaRepositoryPort != null) {
            LogAuditoria log = LogAuditoria.registrarCadastro(
                    resolverOperador(),
                    "Tarefa",
                    salva.getId(),
                    "Cadastro de Tarefa: " + salva.getDescricao()
            );
            logAuditoriaRepositoryPort.salvar(log);
        }

        return TarefaOutputDTO.deDominio(salva);
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
        Tarefa salva = repositoryPort.salvar(tarefa);

        if (logAuditoriaRepositoryPort != null) {
            LogAuditoria log = LogAuditoria.registrarAlteracao(
                    resolverOperador(),
                    "Tarefa",
                    salva.getId(),
                    "Classificação de risco da Tarefa para " + nivelPerigo
            );
            logAuditoriaRepositoryPort.salvar(log);
        }

        return TarefaOutputDTO.deDominio(salva);
    }

    private String resolverOperador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName().trim();
        }
        return "SISTEMA";
    }

    private Tarefa buscarTarefa(Integer id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }
}

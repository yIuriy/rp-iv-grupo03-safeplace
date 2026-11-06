package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.AlocacaoTarefaRepository;
import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;

/** Delega ao repositório JPA da própria AlocacaoTarefa, sem classe espelhada (#260). */
@Component
public class AlocacaoTarefaJpaAdapter implements AlocacaoTarefaRepository {
    private final AlocacaoTarefaJpaRepository repository;

    public AlocacaoTarefaJpaAdapter(AlocacaoTarefaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AlocacaoTarefa salvar(AlocacaoTarefa alocacao) {
        return repository.save(alocacao);
    }

    @Override
    public List<AlocacaoTarefa> listarPorTarefa(Integer tarefaId) {
        return repository.findByTarefa_IdOrderByDataAlocacaoDesc(tarefaId);
    }

    @Override
    public List<AlocacaoTarefa> listarPorColaborador(Integer colaboradorId) {
        return repository.findByColaborador_IdOrderByDataAlocacaoDesc(colaboradorId);
    }
}

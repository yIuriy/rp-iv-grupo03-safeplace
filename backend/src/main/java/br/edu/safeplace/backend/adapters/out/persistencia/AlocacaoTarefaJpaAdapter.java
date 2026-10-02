package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.AlocacaoTarefaRepositoryPort;
import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;

@Component
public class AlocacaoTarefaJpaAdapter implements AlocacaoTarefaRepositoryPort {

    private final AlocacaoTarefaJpaRepository repository;

    public AlocacaoTarefaJpaAdapter(AlocacaoTarefaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AlocacaoTarefa salvar(AlocacaoTarefa alocacao) {
        AlocacaoTarefaEntity entity = new AlocacaoTarefaEntity(
                alocacao.getId(),
                alocacao.getTarefaId(),
                alocacao.getColaboradorId(),
                alocacao.getDataAlocacao(),
                alocacao.getResponsavelAlocacaoId(),
                alocacao.getObservacao()
        );
        AlocacaoTarefaEntity salva = repository.save(entity);
        return new AlocacaoTarefa(
                salva.getId(),
                salva.getTarefaId(),
                salva.getColaboradorId(),
                salva.getDataAlocacao(),
                salva.getResponsavelAlocacaoId(),
                salva.getObservacao()
        );
    }

    @Override
    public List<AlocacaoTarefa> listarPorTarefa(Integer tarefaId) {
        return repository.findByTarefaIdOrderByDataAlocacaoDesc(tarefaId).stream()
                .map(this::deEntidade)
                .toList();
    }

    @Override
    public List<AlocacaoTarefa> listarPorColaborador(Integer colaboradorId) {
        return repository.findByColaboradorIdOrderByDataAlocacaoDesc(colaboradorId).stream()
                .map(this::deEntidade)
                .toList();
    }

    private AlocacaoTarefa deEntidade(AlocacaoTarefaEntity entity) {
        return new AlocacaoTarefa(
                entity.getId(),
                entity.getTarefaId(),
                entity.getColaboradorId(),
                entity.getDataAlocacao(),
                entity.getResponsavelAlocacaoId(),
                entity.getObservacao()
        );
    }
}

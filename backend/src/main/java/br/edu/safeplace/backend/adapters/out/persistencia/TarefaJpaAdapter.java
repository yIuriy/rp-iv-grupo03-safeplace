package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

@Repository
public class TarefaJpaAdapter implements TarefaRepositoryPort {

    private final TarefaJpaRepository tarefaRepository;
    private final UsuarioJpaRepositorio usuarioRepository;

    public TarefaJpaAdapter(TarefaJpaRepository tarefaRepository, UsuarioJpaRepositorio usuarioRepository) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Tarefa salvar(Tarefa tarefa) {
        return toDomain(tarefaRepository.save(toEntity(tarefa)));
    }

    @Override
    public Optional<Tarefa> buscarPorId(Integer id) {
        return tarefaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Tarefa> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        return tarefaRepository.findByNivelPerigo(nivelPerigo.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private TarefaEntity toEntity(Tarefa domain) {
        return new TarefaEntity(
                domain.getId(),
                domain.getDescricao(),
                domain.getNivelPerigo() == null ? null : domain.getNivelPerigo().name(),
                domain.getDataClassificacao(),
                carregarGestor(domain.getResponsavelClassificacao()));
    }

    private GestorDeSegurancaEntidade carregarGestor(GestorDeSeguranca gestor) {
        if (gestor == null || gestor.getId() == null) {
            return null;
        }
        return usuarioRepository.findById(gestor.getId())
                .filter(GestorDeSegurancaEntidade.class::isInstance)
                .map(GestorDeSegurancaEntidade.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Responsável técnico não encontrado entre os gestores de segurança: " + gestor.getId()));
    }

    private Tarefa toDomain(TarefaEntity entity) {
        return new Tarefa(
                entity.getId(),
                entity.getDescricao(),
                entity.getNivelPerigo() == null ? null : NivelPerigo.valueOf(entity.getNivelPerigo()),
                entity.getDataClassificacao(),
                entity.getResponsavelClassificacao() == null ? null : entity.getResponsavelClassificacao().paraDominio());
    }
}

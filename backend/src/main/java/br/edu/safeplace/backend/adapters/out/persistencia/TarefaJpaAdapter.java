package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Objects;
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
    private final AreaRiscoJpaRepository areaRiscoRepository;
    private final SetorJpaRepository setorRepository;

    public TarefaJpaAdapter(TarefaJpaRepository tarefaRepository, UsuarioJpaRepositorio usuarioRepository,
            AreaRiscoJpaRepository areaRiscoRepository, SetorJpaRepository setorRepository) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.areaRiscoRepository = areaRiscoRepository;
        this.setorRepository = setorRepository;
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
                carregarGestor(domain.getResponsavelClassificacao()),
                carregarAreaRisco(domain.getAreaRiscoId()),
                carregarSetores(domain.getSetoresIds()));
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

    private AreaRiscoEntity carregarAreaRisco(Integer areaRiscoId) {
        return areaRiscoId == null ? null : areaRiscoRepository.findById(areaRiscoId)
                .orElseThrow(() -> new IllegalArgumentException("Área de risco não encontrada com id: " + areaRiscoId));
    }

    private List<SetorEntity> carregarSetores(List<Integer> setoresIds) {
        if (setoresIds == null || setoresIds.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = setoresIds.stream().filter(Objects::nonNull).distinct().toList();
        List<SetorEntity> setores = setorRepository.findAllById(ids);
        if (setores.size() != ids.size()) {
            throw new IllegalArgumentException("Um ou mais setores informados não existem.");
        }
        return setores;
    }

    private Tarefa toDomain(TarefaEntity entity) {
        return new Tarefa(
                entity.getId(),
                entity.getDescricao(),
                entity.getNivelPerigo() == null ? null : NivelPerigo.valueOf(entity.getNivelPerigo()),
                entity.getDataClassificacao(),
                entity.getResponsavelClassificacao() == null ? null : entity.getResponsavelClassificacao().paraDominio(),
                entity.getAreaRisco() == null ? null : entity.getAreaRisco().getId(),
                entity.getSetores().stream().map(SetorEntity::getId).toList());
    }
}

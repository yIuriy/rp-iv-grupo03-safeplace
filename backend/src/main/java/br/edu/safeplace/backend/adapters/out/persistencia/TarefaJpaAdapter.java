package br.edu.safeplace.backend.adapters.out.persistencia;

import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

@Repository
public class TarefaJpaAdapter implements TarefaRepository {

    private final TarefaJpaRepository tarefaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final AreaRiscoJpaRepository areaRiscoRepository;
    private final SetorJpaRepository setorRepository;
    private final EpiJpaRepository epiRepository;

    public TarefaJpaAdapter(TarefaJpaRepository tarefaRepository, UsuarioJpaRepository usuarioRepository,
            AreaRiscoJpaRepository areaRiscoRepository, SetorJpaRepository setorRepository,
            EpiJpaRepository epiRepository) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.areaRiscoRepository = areaRiscoRepository;
        this.setorRepository = setorRepository;
        this.epiRepository = epiRepository;
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
                carregarSetores(domain.getSetoresIds()),
                carregarEpisGerenciados(domain));
    }

    private List<Epi> carregarEpisGerenciados(Tarefa domain) {
        List<Integer> ids = domain.verificarEpisObrigatoriosTarefa().stream()
                .map(Epi::getId)
                .filter(Objects::nonNull)
                .toList();
        return ids.isEmpty() ? List.of() : epiRepository.findAllById(ids);
    }

    private GestorDeSeguranca carregarGestor(GestorDeSeguranca gestor) {
        if (gestor == null || gestor.getId() == null) {
            return null;
        }
        return usuarioRepository.findById(gestor.getId())
                .filter(GestorDeSeguranca.class::isInstance)
                .map(GestorDeSeguranca.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Responsável técnico não encontrado entre os gestores de segurança: " + gestor.getId()));
    }

    private AreaRisco carregarAreaRisco(Integer areaRiscoId) {
        return areaRiscoId == null ? null : areaRiscoRepository.findById(areaRiscoId)
                .orElseThrow(() -> new IllegalArgumentException("Área de risco não encontrada com id: " + areaRiscoId));
    }

    private List<Setor> carregarSetores(List<Integer> setoresIds) {
        if (setoresIds == null || setoresIds.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = setoresIds.stream().filter(Objects::nonNull).distinct().toList();
        List<Setor> setores = setorRepository.findAllById(ids);
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
                entity.getResponsavelClassificacao() == null ? null
                        : (GestorDeSeguranca) Hibernate.unproxy(entity.getResponsavelClassificacao()),
                entity.getAreaRisco() == null ? null : entity.getAreaRisco().getId(),
                entity.getSetores().stream().map(Setor::getId).toList(),
                List.copyOf(entity.getEpis()));
    }
}

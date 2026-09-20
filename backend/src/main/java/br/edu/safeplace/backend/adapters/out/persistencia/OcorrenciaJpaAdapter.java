package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

@Repository
public class OcorrenciaJpaAdapter implements OcorrenciaRepositoryPort {
    private final OcorrenciaJpaRepository repository;
    private final AreaRiscoJpaRepository areaRiscoRepository;
    private final UsuarioJpaRepositorio usuarioRepository;

    @Autowired
    public OcorrenciaJpaAdapter(
            OcorrenciaJpaRepository repository,
            AreaRiscoJpaRepository areaRiscoRepository,
            UsuarioJpaRepositorio usuarioRepository) {
        this.repository = repository;
        this.areaRiscoRepository = areaRiscoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public OcorrenciaJpaAdapter(OcorrenciaJpaRepository repository) {
        this(repository, null, null);
    }

    @Override
    public Acidente salvarAcidente(Acidente acidente) {
        AcidenteEntity salvo = repository.save(toEntity(acidente));
        return toDomain(salvo);
    }

    @Override
    public Incidente salvarIncidente(Incidente incidente) {
        IncidenteEntity salvo = repository.save(toEntity(incidente));
        return toDomain(salvo);
    }

    @Override
    public List<Ocorrencia> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Ocorrencia> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public long proximoSequencialCAT(int ano, int mes) {
        return repository.count() + 1;
    }

    private AcidenteEntity toEntity(Acidente acidente) {
        AreaRiscoEntity areaEntity = carregarArea(acidente.getArea());
        ColaboradorEntidade colaboradorEntity = carregarColaborador(acidente.getColaborador());
        GestorDeSegurancaEntidade gestorEntity = carregarGestor(acidente.getGestor());

        return new AcidenteEntity(
                acidente.getIdOcorrencia(),
                acidente.getDataOcorrencia(),
                acidente.getDataRegistro(),
                acidente.getStatusOcorrencia() != null ? acidente.getStatusOcorrencia().name() : "ABERTA",
                areaEntity,
                colaboradorEntity,
                gestorEntity,
                acidente.getLocal(),
                acidente.getDescricao(),
                toEntity(acidente.getPlanoDeAcao()),
                acidente.getCausaRaiz() != null ? acidente.getCausaRaiz().name() : null,
                acidente.getTipo(),
                acidente.getDano(),
                acidente.getNumeroProtocoloCAT(),
                acidente.getDestino(),
                acidente.getGravidade() != null ? acidente.getGravidade().name() : "BAIXO");
    }

    private IncidenteEntity toEntity(Incidente incidente) {
        AreaRiscoEntity areaEntity = carregarArea(incidente.getArea());
        ColaboradorEntidade colaboradorEntity = carregarColaborador(incidente.getColaborador());
        GestorDeSegurancaEntidade gestorEntity = carregarGestor(incidente.getGestor());

        return new IncidenteEntity(
                incidente.getIdOcorrencia(),
                incidente.getDataOcorrencia(),
                incidente.getDataRegistro(),
                incidente.getStatusOcorrencia() != null ? incidente.getStatusOcorrencia().name() : "ABERTA",
                areaEntity,
                colaboradorEntity,
                gestorEntity,
                incidente.getLocal(),
                incidente.getDescricao(),
                toEntity(incidente.getPlanoDeAcao()),
                incidente.getSituacaoRisco(),
                incidente.getPotencialDano());
    }

    private AreaRiscoEntity carregarArea(AreaRisco area) {
        if (area == null || area.getId() == null || areaRiscoRepository == null) {
            return null;
        }
        return areaRiscoRepository.findById(area.getId()).orElse(null);
    }

    private ColaboradorEntidade carregarColaborador(Colaborador colaborador) {
        if (colaborador == null || colaborador.getId() == null || usuarioRepository == null) {
            return null;
        }
        return usuarioRepository.findById(colaborador.getId()).orElse(null);
    }

    private GestorDeSegurancaEntidade carregarGestor(GestorDeSeguranca gestor) {
        if (gestor == null || gestor.getId() == null || usuarioRepository == null) {
            return null;
        }
        return usuarioRepository.findById(gestor.getId())
                .filter(GestorDeSegurancaEntidade.class::isInstance)
                .map(GestorDeSegurancaEntidade.class::cast)
                .orElse(null);
    }

    private PlanoDeAcaoEntity toEntity(PlanoDeAcao plano) {
        if (plano == null)
            return null;

        return new PlanoDeAcaoEntity(
                plano.getId(),
                plano.getMedidasCorretivas(),
                plano.getPrazo(),
                plano.getStatus(),
                plano.getMedidasPreventivas());
    }

    private Ocorrencia toDomain(OcorrenciaEntity entity) {
        if (entity instanceof AcidenteEntity acidente)
            return toDomain(acidente);
        if (entity instanceof IncidenteEntity incidente)
            return toDomain(incidente);

        throw new IllegalStateException("Tipo de ocorrência não suportado.");
    }

    private Acidente toDomain(AcidenteEntity entity) {
        AreaRisco area = toDomain(entity.getArea());
        Colaborador colaborador = entity.getColaborador() != null ? entity.getColaborador().paraDominio() : null;
        GestorDeSeguranca gestor = entity.getGestor() != null ? entity.getGestor().paraDominio() : null;
        StatusOcorrencia status = entity.getStatusOcorrencia() != null
                ? StatusOcorrencia.valueOf(entity.getStatusOcorrencia())
                : StatusOcorrencia.ABERTA;
        NivelPerigo gravidade = entity.getGravidade() != null
                ? NivelPerigo.valueOf(entity.getGravidade())
                : NivelPerigo.BAIXO;
        CausaRaizAcidente causaRaiz = entity.getCausaRaiz() != null
                ? CausaRaizAcidente.valueOf(entity.getCausaRaiz())
                : null;

        return new Acidente(
                entity.getIdOcorrencia(),
                entity.getDescricao(),
                entity.getDataOcorrencia(),
                entity.getDataRegistro(),
                status,
                null,
                colaborador,
                null,
                area,
                gestor,
                entity.getLocal(),
                toDomain(entity.getPlanoDeAcao()),
                causaRaiz,
                entity.getTipo(),
                entity.getDano(),
                entity.getNumeroProtocolo(),
                entity.getDestino(),
                gravidade);
    }

    private Incidente toDomain(IncidenteEntity entity) {
        AreaRisco area = toDomain(entity.getArea());
        Colaborador colaborador = entity.getColaborador() != null ? entity.getColaborador().paraDominio() : null;
        GestorDeSeguranca gestor = entity.getGestor() != null ? entity.getGestor().paraDominio() : null;
        StatusOcorrencia status = entity.getStatusOcorrencia() != null
                ? StatusOcorrencia.valueOf(entity.getStatusOcorrencia())
                : StatusOcorrencia.ABERTA;

        return new Incidente(
                entity.getIdOcorrencia(),
                entity.getDescricao(),
                entity.getDataOcorrencia(),
                entity.getDataRegistro(),
                status,
                null,
                colaborador,
                null,
                area,
                gestor,
                entity.getLocal(),
                toDomain(entity.getPlanoDeAcao()),
                entity.getSituacaoRisco(),
                entity.getPotencialDano());
    }

    private AreaRisco toDomain(AreaRiscoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AreaRisco(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getNivelPerigo() != null ? NivelPerigo.valueOf(entity.getNivelPerigo()) : null,
                entity.getSetorId(),
                entity.getAgentesRiscoList(),
                entity.getLimites(),
                List.of());
    }

    private PlanoDeAcao toDomain(PlanoDeAcaoEntity entity) {
        if (entity == null)
            return null;

        return new PlanoDeAcao(
                entity.getId(),
                entity.getMedidasCorretivas(),
                entity.getPrazo(),
                entity.getStatus(),
                entity.getMedidasPreventivas());
    }
}

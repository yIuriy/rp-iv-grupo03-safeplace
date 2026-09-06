package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

@Repository
public class OcorrenciaJpaAdapter implements OcorrenciaRepositoryPort {
    private final OcorrenciaJpaRepository repository;

    public OcorrenciaJpaAdapter(OcorrenciaJpaRepository repository) {
        this.repository = repository;
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

    private AcidenteEntity toEntity(Acidente acidente) {
        return new AcidenteEntity(
                acidente.getIdOcorrencia(),
                acidente.getDataOcorrencia(),
                acidente.getLocal(),
                acidente.getDescricao(),
                toEntity(acidente.getPlanoDeAcao()),
                acidente.getCausaRaiz(),
                acidente.getTipo(),
                acidente.getDano(),
                acidente.getNumeroProtocolo(),
                acidente.getDestino());
    }

    private IncidenteEntity toEntity(Incidente incidente) {
        return new IncidenteEntity(
                incidente.getIdOcorrencia(),
                incidente.getDataOcorrencia(),
                incidente.getLocal(),
                incidente.getDescricao(),
                toEntity(incidente.getPlanoDeAcao()),
                incidente.getSituacaoRisco(),
                incidente.getPotencialDano());
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
        return new Acidente(
                entity.getIdOcorrencia(),
                entity.getDataOcorrencia(),
                entity.getLocal(),
                entity.getDescricao(),
                toDomain(entity.getPlanoDeAcao()),
                entity.getCausaRaiz(),
                entity.getTipo(),
                entity.getDano(),
                entity.getNumeroProtocolo(),
                entity.getDestino());
    }

    private Incidente toDomain(IncidenteEntity entity) {
        return new Incidente(
                entity.getIdOcorrencia(),
                entity.getDataOcorrencia(),
                entity.getLocal(),
                entity.getDescricao(),
                toDomain(entity.getPlanoDeAcao()),
                entity.getSituacaoRisco(),
                entity.getPotencialDano());
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

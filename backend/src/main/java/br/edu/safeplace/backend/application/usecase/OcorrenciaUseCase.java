package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OcorrenciaUseCase implements RegistrarOcorrenciaUseCase {
    private final OcorrenciaRepositoryPort repository;

    public OcorrenciaUseCase(OcorrenciaRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarAcidente(RegistrarAcidenteInputDTO inputDTO) {
        Acidente acidente = Acidente.novo(
                inputDTO.dataOcorrencia(),
                inputDTO.local(),
                inputDTO.descricao(),
                inputDTO.planoDeAcao(),
                inputDTO.causaRaiz(),
                inputDTO.tipo(),
                inputDTO.dano(),
                inputDTO.numeroProtocolo(),
                inputDTO.destino()
        );
        Acidente salvo = repository.salvarAcidente(acidente);
        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarIncidente(RegistrarIncidenteInputDTO inputDTO) {
        Incidente incidente = Incidente.novo(
                inputDTO.dataOcorrencia(),
                inputDTO.local(),
                inputDTO.descricao(),
                inputDTO.planoDeAcao(),
                inputDTO.situacaoRisco(),
                inputDTO.potencialDano()
        );
        Incidente salvo = repository.salvarIncidente(incidente);
        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcorrenciaOutputDTO> listar() {
        return repository.listar().stream()
                .map(OcorrenciaOutputDTO::deDominio)
                .toList();
    }
}

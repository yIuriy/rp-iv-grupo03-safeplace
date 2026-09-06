package br.edu.safeplace.backend.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

@Service
public class OcorrenciaService implements RegistrarOcorrenciaUseCase {
    private final OcorrenciaRepositoryPort repository;

    public OcorrenciaService(OcorrenciaRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Acidente registrarAcidente(Acidente acidente) {
        return repository.salvarAcidente(acidente);
    }

    @Override
    public Incidente registrarIncidente(Incidente incidente) {
        return repository.salvarIncidente(incidente);
    }

    @Override
    public List<Ocorrencia> listar() {
        return repository.listar();
    }
}

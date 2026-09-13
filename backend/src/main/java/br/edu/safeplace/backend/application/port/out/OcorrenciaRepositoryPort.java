package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

public interface OcorrenciaRepositoryPort {
    Acidente salvarAcidente(Acidente acidente);

    Incidente salvarIncidente(Incidente incidente);

    List<Ocorrencia> listar();

    Optional<Ocorrencia> buscarPorId(Integer id);

    long proximoSequencialCAT(int ano, int mes);

    default Optional<Acidente> buscarAcidentePorId(Integer id) {
        return buscarPorId(id)
                .filter(Acidente.class::isInstance)
                .map(Acidente.class::cast);
    }

    default Optional<Incidente> buscarIncidentePorId(Integer id) {
        return buscarPorId(id)
                .filter(Incidente.class::isInstance)
                .map(Incidente.class::cast);
    }
}

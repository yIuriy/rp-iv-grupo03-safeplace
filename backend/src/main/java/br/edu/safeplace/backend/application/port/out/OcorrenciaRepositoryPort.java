package br.edu.safeplace.backend.application.port.out;

import java.util.List;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

public interface OcorrenciaRepositoryPort {
    Acidente salvarAcidente(Acidente acidente);

    Incidente salvarIncidente(Incidente incidente);

    List<Ocorrencia> listar();
}

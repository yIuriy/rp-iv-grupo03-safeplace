package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

public interface RegistrarOcorrenciaUseCase {
    Acidente registrarAcidente(Acidente acidente);

    Incidente registrarIncidente(Incidente incidente);

    List<Ocorrencia> listar();
}

/*
 * Porta de saída de ocorrências: gravação de acidentes e incidentes, consultas por id, protocolo CAT
 * e critérios (issue #128), e o sequencial do protocolo CAT.
 */
package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

public interface OcorrenciaRepositoryPort {
    Acidente salvarAcidente(Acidente acidente);

    Incidente salvarIncidente(Incidente incidente);

    List<Ocorrencia> listar();

    /**
     * Ocorrências que atendem aos critérios, na ordem de registro. A versão padrão filtra em memória;
     * o adaptador JPA a substitui por uma consulta ao banco.
     */
    default List<Ocorrencia> listar(FiltroOcorrenciasDTO filtro) {
        return listar().stream().filter(filtro::aceita).toList();
    }

    Optional<Ocorrencia> buscarPorId(Integer id);

    Optional<Ocorrencia> buscarPorProtocolo(String protocolo);

    long proximoSequencialCAT(int ano, int mes);

    default Optional<Acidente> buscarAcidentePorId(Integer id) {
        return buscarPorId(id)
                .filter(Acidente.class::isInstance)
                .map(Acidente.class::cast);
    }
}

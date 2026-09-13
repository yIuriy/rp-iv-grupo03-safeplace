package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;

public interface GerenciarOcorrenciaUseCase {
    OcorrenciaOutputDTO buscarPorId(Integer idOcorrencia);
    OcorrenciaOutputDTO enviarParaTriagem(Integer idOcorrencia);
    OcorrenciaOutputDTO arquivar(Integer idOcorrencia);
    OcorrenciaOutputDTO consolidarCAT(Integer idAcidente);
}

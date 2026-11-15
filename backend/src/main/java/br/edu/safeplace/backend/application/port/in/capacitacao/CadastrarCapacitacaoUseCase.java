package br.edu.safeplace.backend.application.port.in.capacitacao;

import br.edu.safeplace.backend.application.dto.capacitacao.CertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ExigenciaCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.TreinamentoOutputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;

public interface CadastrarCapacitacaoUseCase {
    CertificacaoOutputDTO cadastrar(CadastrarCertificacaoInputDTO entrada);
    TreinamentoOutputDTO cadastrar(CadastrarTreinamentoInputDTO entrada);
    ExigenciaCapacitacaoOutputDTO cadastrar(DefinirExigenciaInputDTO entrada);
}

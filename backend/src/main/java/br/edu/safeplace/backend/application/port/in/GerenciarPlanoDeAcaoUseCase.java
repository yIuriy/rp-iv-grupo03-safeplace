package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.AtualizarResponsavelInputDTO;
import br.edu.safeplace.backend.application.dto.input.AtualizarStatusPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ConfigurarAlertaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ProrrogarPrazoInputDTO;
import br.edu.safeplace.backend.application.dto.output.PlanoDeAcaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProrrogacaoPrazoOutputDTO;

import java.util.List;

public interface GerenciarPlanoDeAcaoUseCase {

    PlanoDeAcaoOutputDTO cadastrar(Integer idOcorrencia, CriarAcaoPlanoInputDTO inputDTO);

    List<PlanoDeAcaoOutputDTO> listarPorOcorrencia(Integer idOcorrencia);

    PlanoDeAcaoOutputDTO buscarPorId(Integer idAcao);

    PlanoDeAcaoOutputDTO atualizarStatus(Integer idAcao, AtualizarStatusPlanoInputDTO inputDTO);

    ProrrogacaoPrazoOutputDTO prorrogarPrazo(Integer idAcao, ProrrogarPrazoInputDTO inputDTO);

    List<ProrrogacaoPrazoOutputDTO> listarProrrogacoes(Integer idAcao);

    PlanoDeAcaoOutputDTO atualizarResponsavel(Integer idAcao, AtualizarResponsavelInputDTO inputDTO);

    PlanoDeAcaoOutputDTO configurarAlerta(Integer idAcao, ConfigurarAlertaInputDTO inputDTO);
}

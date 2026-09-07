package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;

import java.util.List;

public interface GerenciarEpiUseCase {
    EpiOutputDTO cadastrarEpi(CadastrarEpiInputDTO inputDTO);
    List<EpiOutputDTO> listar();
    EpiOutputDTO buscarPorId(Integer id);
    MovimentacaoEstoqueOutputDTO registrarMovimentacao(Integer epiId, TipoMovimentacao tipo, int quantidade, String motivo);
}

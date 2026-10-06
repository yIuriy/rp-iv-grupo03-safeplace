package br.edu.safeplace.backend.application.port.in;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.application.dto.input.GerarAnaliseProjecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlertaSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProjecaoSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ResultadoAnaliseSubstituicaoDTO;

/**
 * Porta de entrada do Caso de Uso UC10 – Planejar Substituição Inteligente de EPI (RF21, US21).
 * Corresponde ao papel de ControleSubstituicaoEPI no DS10.
 */
public interface PlanejarSubstituicaoUseCase {

    /**
     * Operação gerarAnalise() do DS10: executa DS10.1 para cada EPI, persiste as projeções
     * calculadas e retorna o resultado consolidado (incluindo falhas para permitir nova tentativa).
     */
    ResultadoAnaliseSubstituicaoDTO gerarAnalise(GerarAnaliseProjecaoInputDTO input);

    /**
     * Operação consultarHistorico() / buscarEPIs() / exibirProjecoes() do DS10.
     */
    List<ProjecaoSubstituicaoOutputDTO> listarProjecoes();

    /**
     * Consulta as projeções registradas para um EPI específico.
     */
    List<ProjecaoSubstituicaoOutputDTO> listarProjecoesPorEpi(Integer epiId);

    /**
     * Operação emitirListaReposicao() do DS10: emite alertas preventivos de descarte iminente,
     * validade de CA e estoque crítico, com a antecedência fixa de 30 dias do DS10, 2.4.
     */
    List<AlertaSubstituicaoOutputDTO> emitirAlertasPreventivos(LocalDate dataReferencia);
}

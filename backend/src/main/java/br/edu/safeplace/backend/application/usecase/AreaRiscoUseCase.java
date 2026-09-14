package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;

/**
 * Mapeamento das áreas de risco do ambiente de trabalho (RF05 / UC03).
 */
@Service
public class AreaRiscoUseCase implements GerenciarAreaRiscoUseCase {

    private final AreaRiscoRepositoryPort areaRiscoRepositoryPort;
    private final EpiRepositoryPort epiRepositoryPort;

    public AreaRiscoUseCase(
            AreaRiscoRepositoryPort areaRiscoRepositoryPort,
            EpiRepositoryPort epiRepositoryPort) {
        this.areaRiscoRepositoryPort = areaRiscoRepositoryPort;
        this.epiRepositoryPort = epiRepositoryPort;
    }

    @Override
    @Transactional
    public AreaRiscoOutputDTO cadastrarAreaRisco(CadastrarAreaRiscoInputDTO inputDTO) {
        String codigo = inputDTO.codigo();

        if (codigo != null && !codigo.isBlank()) {
            String codigoNormalizado = codigo.trim().toUpperCase();
            areaRiscoRepositoryPort.buscarPorCodigo(codigoNormalizado).ifPresent(existente -> {
                throw new CodigoAreaRiscoDuplicadoException(codigoNormalizado);
            });
        }

        List<Epi> episObrigatorios = resolverEpis(inputDTO.episObrigatoriosIds());

        AreaRisco novaArea = AreaRisco.cadastrar(
                codigo,
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.nivelPerigo(),
                episObrigatorios);

        return AreaRiscoOutputDTO.deDominio(areaRiscoRepositoryPort.salvar(novaArea));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaRiscoOutputDTO> listar() {
        return areaRiscoRepositoryPort.listarTodas().stream()
                .map(AreaRiscoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaRiscoOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        if (nivelPerigo == null) {
            return listar();
        }
        return areaRiscoRepositoryPort.listarPorNivelPerigo(nivelPerigo).stream()
                .map(AreaRiscoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AreaRiscoOutputDTO buscarPorId(Integer id) {
        AreaRisco areaRisco = areaRiscoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new AreaRiscoNaoEncontradaException(id));
        return AreaRiscoOutputDTO.deDominio(areaRisco);
    }

    /**
     * Resolve os EPIs selecionados no formulário do UC03 contra o catálogo cadastrado. A lista
     * vazia é devolvida como está para que a validação de RN1 permaneça no domínio.
     */
    private List<Epi> resolverEpis(List<Integer> episObrigatoriosIds) {
        if (episObrigatoriosIds == null || episObrigatoriosIds.isEmpty()) {
            return List.of();
        }
        return episObrigatoriosIds.stream()
                .distinct()
                .map(epiId -> epiRepositoryPort.buscarPorId(epiId)
                        .orElseThrow(() -> new EpiNaoEncontradoException(epiId)))
                .toList();
    }
}

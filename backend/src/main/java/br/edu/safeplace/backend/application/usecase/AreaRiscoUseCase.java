package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.AtualizarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;

/**
 * Mapeamento das áreas de risco do ambiente de trabalho (RF05 / UC03 / US05).
 */
@Service
public class AreaRiscoUseCase implements GerenciarAreaRiscoUseCase {

    private final AreaRiscoRepositoryPort areaRiscoRepositoryPort;
    private final EpiRepositoryPort epiRepositoryPort;
    private final SetorRepositoryPort setorRepositoryPort;
    private final LogAuditoriaRepositoryPort logAuditoriaRepositoryPort;

    @Autowired
    public AreaRiscoUseCase(
            AreaRiscoRepositoryPort areaRiscoRepositoryPort,
            EpiRepositoryPort epiRepositoryPort,
            SetorRepositoryPort setorRepositoryPort,
            LogAuditoriaRepositoryPort logAuditoriaRepositoryPort) {
        this.areaRiscoRepositoryPort = areaRiscoRepositoryPort;
        this.epiRepositoryPort = epiRepositoryPort;
        this.setorRepositoryPort = setorRepositoryPort;
        this.logAuditoriaRepositoryPort = logAuditoriaRepositoryPort;
    }

    public AreaRiscoUseCase(
            AreaRiscoRepositoryPort areaRiscoRepositoryPort,
            EpiRepositoryPort epiRepositoryPort) {
        this(areaRiscoRepositoryPort, epiRepositoryPort, null, null);
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

        if (inputDTO.setorId() != null && setorRepositoryPort != null) {
            setorRepositoryPort.buscarPorId(inputDTO.setorId())
                    .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + inputDTO.setorId()));
        }

        List<Epi> episObrigatorios = resolverEpis(inputDTO.episObrigatoriosIds());

        AreaRisco novaArea = AreaRisco.cadastrar(
                codigo,
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.nivelPerigo(),
                inputDTO.setorId(),
                inputDTO.agentesRisco(),
                inputDTO.limites(),
                episObrigatorios);

        return AreaRiscoOutputDTO.deDominio(areaRiscoRepositoryPort.salvar(novaArea));
    }

    @Override
    @Transactional
    public AreaRiscoOutputDTO atualizarAreaRisco(Integer id, AtualizarAreaRiscoInputDTO inputDTO, String operador) {
        AreaRisco existente = areaRiscoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new AreaRiscoNaoEncontradaException(id));

        if (inputDTO.setorId() != null && setorRepositoryPort != null) {
            setorRepositoryPort.buscarPorId(inputDTO.setorId())
                    .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + inputDTO.setorId()));
        }

        List<Epi> episObrigatorios = resolverEpis(inputDTO.episObrigatoriosIds());

        AreaRisco atualizada = existente.atualizar(
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.nivelPerigo(),
                inputDTO.setorId(),
                inputDTO.agentesRisco(),
                inputDTO.limites(),
                episObrigatorios);

        AreaRisco salva = areaRiscoRepositoryPort.atualizar(atualizada);

        if (logAuditoriaRepositoryPort != null) {
            LogAuditoria log = LogAuditoria.registrarAlteracao(
                    operador != null ? operador : "sistema",
                    "AreaRisco",
                    id,
                    inputDTO.justificativa()
            );
            logAuditoriaRepositoryPort.salvar(log);
        }

        return AreaRiscoOutputDTO.deDominio(salva);
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

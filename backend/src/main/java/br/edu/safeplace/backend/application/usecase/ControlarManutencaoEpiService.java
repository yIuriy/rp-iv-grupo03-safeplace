package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarManutencaoEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ControlarManutencaoEpiService implements ControlarManutencaoEpiUseCase {
    private final EpiRepositoryPort epiRepositoryPort;
    private final ManutencaoEpiRepositoryPort manutencaoEpiRepositoryPort;
    private final UsuarioRepositorioPorta usuarioRepositorioPorta;

    public ControlarManutencaoEpiService(EpiRepositoryPort epiRepositoryPort,
                                         ManutencaoEpiRepositoryPort manutencaoEpiRepositoryPort) {
        this(epiRepositoryPort, manutencaoEpiRepositoryPort, null);
    }

    @Autowired
    public ControlarManutencaoEpiService(EpiRepositoryPort epiRepositoryPort,
                                         ManutencaoEpiRepositoryPort manutencaoEpiRepositoryPort,
                                         UsuarioRepositorioPorta usuarioRepositorioPorta) {
        this.epiRepositoryPort = epiRepositoryPort;
        this.manutencaoEpiRepositoryPort = manutencaoEpiRepositoryPort;
        this.usuarioRepositorioPorta = usuarioRepositorioPorta;
    }

    @Override
    @Transactional
    public EpiOutputDTO enviarParaManutencao(Integer epiId) {
        Epi epi = epiRepositoryPort.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));

        epi.enviarParaManutencao();
        Epi salvo = epiRepositoryPort.salvar(epi);
        return EpiOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public ManutencaoEpiOutputDTO concluirManutencao(ConcluirManutencaoInputDTO inputDTO) {
        return concluirManutencao(inputDTO, null);
    }

    @Override
    @Transactional
    public ManutencaoEpiOutputDTO concluirManutencao(ConcluirManutencaoInputDTO inputDTO,
                                                      String responsavelEmail) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("Dados para conclusão de manutenção são obrigatórios.");
        }

        Epi epi = epiRepositoryPort.buscarPorId(inputDTO.epiId())
                .orElseThrow(() -> new EpiNaoEncontradoException(inputDTO.epiId()));

        LocalDate dataReferencia = inputDTO.dataManutencao() != null
                ? inputDTO.dataManutencao().toLocalDate()
                : LocalDate.now();
        epi.validarCaValido(dataReferencia);

        ManutencaoEpi manutencao = ManutencaoEpi.novo(
                epi.getId(),
                inputDTO.dataManutencao(),
                inputDTO.tipoManutencao(),
                inputDTO.descricao(),
                inputDTO.resultado(),
                responsavelEmail != null ? responsavelEmail : inputDTO.responsavelManutencao(),
                buscarResponsavelId(responsavelEmail)
        );

        epi.concluirManutencao(manutencao);
        epiRepositoryPort.salvar(epi);

        ManutencaoEpi salva = manutencaoEpiRepositoryPort.salvar(manutencao);
        return ManutencaoEpiOutputDTO.deDominio(salva, epi.getStatus());
    }

    private Integer buscarResponsavelId(String responsavelEmail) {
        if (responsavelEmail == null || responsavelEmail.isBlank()) {
            if (usuarioRepositorioPorta != null) {
                throw new IllegalArgumentException("Responsável autenticado é obrigatório.");
            }
            return null;
        }
        if (usuarioRepositorioPorta == null) {
            throw new IllegalStateException("Repositório de usuários indisponível para rastrear manutenção.");
        }
        return usuarioRepositorioPorta.buscarPorEmail(responsavelEmail.toLowerCase())
                .map(usuario -> usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Responsável autenticado não encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManutencaoEpiOutputDTO> listarHistoricoPorEpi(Integer epiId) {
        Epi epi = epiRepositoryPort.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));

        return manutencaoEpiRepositoryPort.listarPorEpiId(epiId).stream()
                .map(m -> ManutencaoEpiOutputDTO.deDominio(m, epi.getStatus()))
                .toList();
    }
}

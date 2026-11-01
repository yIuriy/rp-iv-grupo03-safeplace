package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarManutencaoEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ControlarManutencaoEpiService implements ControlarManutencaoEpiUseCase {
    private final EpiRepository epiRepository;
    private final ManutencaoEpiRepository manutencaoEpiRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    public ControlarManutencaoEpiService(EpiRepository epiRepository,
                                         ManutencaoEpiRepository manutencaoEpiRepository,
                                         UsuarioRepository usuarioRepository,
                                         LogAuditoriaRepository logAuditoriaRepository) {
        this.epiRepository = epiRepository;
        this.manutencaoEpiRepository = manutencaoEpiRepository;
        this.usuarioRepository = usuarioRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
    }

    @Override
    @Transactional
    public EpiOutputDTO enviarParaManutencao(Integer epiId) {
        Epi epi = epiRepository.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));

        epi.enviarParaManutencao();
        Epi salvo = epiRepository.salvar(epi);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                resolverOperador(null),
                "Epi",
                salvo.getId(),
                "EPI enviado para manutenção"
        );
        logAuditoriaRepository.salvar(log);

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

        Epi epi = epiRepository.buscarPorId(inputDTO.epiId())
                .orElseThrow(() -> new EpiNaoEncontradoException(inputDTO.epiId()));

        LocalDate dataReferencia = inputDTO.dataManutencao() != null
                ? inputDTO.dataManutencao().toLocalDate()
                : LocalDate.now();
        epi.validarCaValido(dataReferencia);

        if (epi.getStatus() == StatusEpi.DISPONIVEL) {
            epi.enviarParaManutencao(dataReferencia);
        }

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
        epiRepository.salvar(epi);

        ManutencaoEpi salva = manutencaoEpiRepository.salvar(manutencao);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                resolverOperador(responsavelEmail),
                "ManutencaoEpi",
                salva.getId(),
                "Manutenção de EPI concluída com resultado: " + inputDTO.resultado()
        );
        logAuditoriaRepository.salvar(log);

        return ManutencaoEpiOutputDTO.deDominio(salva, epi.getStatus());
    }

    private String resolverOperador(String explicit) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit.trim();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName().trim();
        }
        return "SISTEMA";
    }

    private Integer buscarResponsavelId(String responsavelEmail) {
        if (responsavelEmail == null || responsavelEmail.isBlank()) {
            if (usuarioRepository != null) {
                throw new IllegalArgumentException("Responsável autenticado é obrigatório.");
            }
            return null;
        }
        if (usuarioRepository == null) {
            throw new IllegalStateException("Repositório de usuários indisponível para rastrear manutenção.");
        }
        return usuarioRepository.buscarPorEmail(responsavelEmail.toLowerCase())
                .map(usuario -> usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Responsável autenticado não encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManutencaoEpiOutputDTO> listarHistoricoPorEpi(Integer epiId) {
        Epi epi = epiRepository.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));

        return manutencaoEpiRepository.listarPorEpiId(epiId).stream()
                .map(m -> ManutencaoEpiOutputDTO.deDominio(m, epi.getStatus()))
                .toList();
    }
}

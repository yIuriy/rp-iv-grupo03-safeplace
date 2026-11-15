package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarManutencaoEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.epi.ManutencaoEpiValidador;

@Service
public class ControlarManutencaoEpiService implements ControlarManutencaoEpiUseCase {
    private final EpiRepository epiRepository;
    private final ManutencaoEpiRepository manutencaoEpiRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistradorAuditoria auditoria;
    private final Validador<ManutencaoEpi> manutencaoValidador;
    private final OperadorAtual operadorAtual;
    private final Clock clock;

    /** Usa o validador padrão; usado pelos testes sem contexto Spring. */
    public ControlarManutencaoEpiService(EpiRepository epiRepository,
                                         ManutencaoEpiRepository manutencaoEpiRepository,
                                         UsuarioRepository usuarioRepository,
                                         LogAuditoriaRepository logAuditoriaRepository,
                                         OperadorAtual operadorAtual) {
        this(epiRepository, manutencaoEpiRepository, usuarioRepository,
                new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                new ManutencaoEpiValidador(), operadorAtual, Clock.systemDefaultZone());
    }

    public ControlarManutencaoEpiService(EpiRepository epiRepository,
                                         ManutencaoEpiRepository manutencaoEpiRepository,
                                         UsuarioRepository usuarioRepository,
                                         LogAuditoriaRepository logAuditoriaRepository,
                                         Validador<ManutencaoEpi> manutencaoValidador,
                                         OperadorAtual operadorAtual) {
        this(epiRepository, manutencaoEpiRepository, usuarioRepository,
                new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                manutencaoValidador, operadorAtual, Clock.systemDefaultZone());
    }

    public ControlarManutencaoEpiService(EpiRepository epiRepository,
                                         ManutencaoEpiRepository manutencaoEpiRepository,
                                         UsuarioRepository usuarioRepository,
                                         LogAuditoriaRepository logAuditoriaRepository,
                                         Validador<ManutencaoEpi> manutencaoValidador,
                                         OperadorAtual operadorAtual,
                                         Clock clock) {
        this(epiRepository, manutencaoEpiRepository, usuarioRepository,
                new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                manutencaoValidador, operadorAtual, clock);
    }

    @Autowired
    public ControlarManutencaoEpiService(EpiRepository epiRepository,
                                         ManutencaoEpiRepository manutencaoEpiRepository,
                                         UsuarioRepository usuarioRepository,
                                         RegistradorAuditoria auditoria,
                                         Validador<ManutencaoEpi> manutencaoValidador,
                                         OperadorAtual operadorAtual,
                                         Clock clock) {
        this.manutencaoValidador = manutencaoValidador;
        this.epiRepository = epiRepository;
        this.manutencaoEpiRepository = manutencaoEpiRepository;
        this.usuarioRepository = usuarioRepository;
        // Facade (#223, #237): auditoria simplificada sem manipular LogAuditoria diretamente.
        this.auditoria = java.util.Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        // Adapter (#236): o operador vem da porta OperadorAutenticado.
        this.operadorAtual = java.util.Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
        this.clock = java.util.Objects.requireNonNull(clock, "Clock é obrigatório.");
    }

    @Override
    @Transactional
    public EpiOutputDTO enviarParaManutencao(Integer epiId) {
        Epi epi = epiRepository.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));

        epi.enviarParaManutencao(LocalDate.now(clock));
        Epi salvo = epiRepository.salvar(epi);

        auditoria.alteracao("Epi", salvo.getId(), "EPI enviado para manutenção");

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
                : LocalDate.now(clock);
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
        // Chain of Responsibility (#264): valida antes de concluir a manutenção e de gravar.
        manutencaoValidador.validar(manutencao);

        // State (#224): o próprio Epi decide se passa por EM_MANUTENCAO antes de concluir.
        epi.registrarManutencao(manutencao, dataReferencia);
        epiRepository.salvar(epi);

        ManutencaoEpi salva = manutencaoEpiRepository.salvar(manutencao);

        auditoria.por(responsavelEmail).alteracao(
                "ManutencaoEpi", salva.getId(), "Manutenção de EPI concluída com resultado: " + inputDTO.resultado());

        return ManutencaoEpiOutputDTO.deDominio(salva, epi.getStatus());
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

        return manutencaoEpiRepository.listarPorEpi(epiId).stream()
                .map(m -> ManutencaoEpiOutputDTO.deDominio(m, epi.getStatus()))
                .toList();
    }
}

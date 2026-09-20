package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OcorrenciaUseCase implements RegistrarOcorrenciaUseCase, GerenciarOcorrenciaUseCase {
    private final OcorrenciaRepositoryPort repository;
    private final AreaRiscoRepositoryPort areaRiscoRepository;
    private final UsuarioRepositorioPorta usuarioRepository;

    public OcorrenciaUseCase(
            OcorrenciaRepositoryPort repository,
            AreaRiscoRepositoryPort areaRiscoRepository,
            UsuarioRepositorioPorta usuarioRepository) {
        this.repository = repository;
        this.areaRiscoRepository = areaRiscoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public OcorrenciaUseCase(OcorrenciaRepositoryPort repository) {
        this(repository, null, null);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarAcidente(RegistrarAcidenteInputDTO inputDTO) {
        AreaRisco area = resolverArea(inputDTO.areaId(), inputDTO.local());
        Colaborador colaborador = resolverColaborador(inputDTO.colaboradorId());
        GestorDeSeguranca gestor = resolverGestor(inputDTO.gestorId());
        CausaRaizAcidente causaRaiz = converterCausaRaiz(inputDTO.causaRaiz());

        Acidente acidente = new Acidente(
                null,
                inputDTO.descricao(),
                inputDTO.dataOcorrencia(),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                List.of(),
                colaborador,
                List.of(),
                area,
                gestor,
                area != null ? area.getNome() : inputDTO.local(),
                inputDTO.planoDeAcao(),
                causaRaiz,
                inputDTO.tipo(),
                inputDTO.dano(),
                inputDTO.numeroProtocolo(),
                inputDTO.destino(),
                inputDTO.gravidade()
        );
        Acidente salvo = repository.salvarAcidente(acidente);
        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarIncidente(RegistrarIncidenteInputDTO inputDTO) {
        AreaRisco area = resolverArea(inputDTO.areaId(), inputDTO.local());
        Colaborador colaborador = resolverColaborador(inputDTO.colaboradorId());
        GestorDeSeguranca gestor = resolverGestor(inputDTO.gestorId());

        Incidente incidente = new Incidente(
                null,
                inputDTO.descricao(),
                inputDTO.dataOcorrencia(),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                List.of(),
                colaborador,
                List.of(),
                area,
                gestor,
                area != null ? area.getNome() : inputDTO.local(),
                inputDTO.planoDeAcao(),
                inputDTO.situacaoRisco(),
                inputDTO.potencialDano()
        );
        Incidente salvo = repository.salvarIncidente(incidente);
        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    private AreaRisco resolverArea(Integer areaId, String local) {
        if (areaId != null && areaRiscoRepository != null) {
            return areaRiscoRepository.buscarPorId(areaId)
                    .orElseThrow(() -> new IllegalArgumentException("Área de risco não encontrada para o ID: " + areaId));
        }
        if (local != null && !local.isBlank()) {
            return new AreaRisco(null, null, local.trim(), null, null, null, null, null, null);
        }
        return null;
    }

    private Colaborador resolverColaborador(Integer colaboradorId) {
        if (colaboradorId != null && usuarioRepository != null) {
            return usuarioRepository.buscarPorId(colaboradorId)
                    .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado para o ID: " + colaboradorId));
        }
        return null;
    }

    private GestorDeSeguranca resolverGestor(Integer gestorId) {
        if (gestorId != null && usuarioRepository != null) {
            return usuarioRepository.buscarPorId(gestorId)
                    .filter(GestorDeSeguranca.class::isInstance)
                    .map(GestorDeSeguranca.class::cast)
                    .orElseThrow(() -> new IllegalArgumentException("Gestor de segurança não encontrado para o ID: " + gestorId));
        }
        return null;
    }

    private CausaRaizAcidente converterCausaRaiz(String causaRaiz) {
        if (causaRaiz == null || causaRaiz.isBlank()) {
            return CausaRaizAcidente.OUTRO;
        }
        try {
            return CausaRaizAcidente.valueOf(causaRaiz.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CausaRaizAcidente.OUTRO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcorrenciaOutputDTO> listar() {
        return repository.listar().stream()
                .map(OcorrenciaOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OcorrenciaOutputDTO buscarPorId(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));
        return OcorrenciaOutputDTO.deDominio(ocorrencia);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO enviarParaTriagem(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));

        ocorrencia.enviarParaTriagem();

        Ocorrencia salva = salvarOcorrencia(ocorrencia);
        return OcorrenciaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO arquivar(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));

        ocorrencia.arquivar();

        Ocorrencia salva = salvarOcorrencia(ocorrencia);
        return OcorrenciaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO consolidarCAT(Integer idAcidente) {
        Acidente acidente = repository.buscarAcidentePorId(idAcidente)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idAcidente));

        LocalDateTime dataRef = acidente.getDataOcorrencia() != null
                ? acidente.getDataOcorrencia()
                : LocalDateTime.now();

        long sequencial = repository.proximoSequencialCAT(dataRef.getYear(), dataRef.getMonthValue());
        Acidente consolidado = acidente.consolidarCAT(sequencial);
        Acidente salvo = repository.salvarAcidente(consolidado);

        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    private Ocorrencia salvarOcorrencia(Ocorrencia ocorrencia) {
        if (ocorrencia instanceof Acidente acidente) {
            return repository.salvarAcidente(acidente);
        } else if (ocorrencia instanceof Incidente incidente) {
            return repository.salvarIncidente(incidente);
        }
        throw new IllegalStateException("Tipo de ocorrência não suportado para persistência.");
    }
}

package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OcorrenciaUseCase implements RegistrarOcorrenciaUseCase, GerenciarOcorrenciaUseCase {
    private final OcorrenciaRepositoryPort repository;

    public OcorrenciaUseCase(OcorrenciaRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarAcidente(RegistrarAcidenteInputDTO inputDTO) {
        Acidente acidente = Acidente.novo(
                inputDTO.dataOcorrencia(),
                inputDTO.local(),
                inputDTO.descricao(),
                inputDTO.planoDeAcao(),
                inputDTO.causaRaiz(),
                inputDTO.tipo(),
                inputDTO.dano(),
                inputDTO.numeroProtocolo(),
                inputDTO.destino()
        );
        Acidente salvo = repository.salvarAcidente(acidente);
        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarIncidente(RegistrarIncidenteInputDTO inputDTO) {
        Incidente incidente = Incidente.novo(
                inputDTO.dataOcorrencia(),
                inputDTO.local(),
                inputDTO.descricao(),
                inputDTO.planoDeAcao(),
                inputDTO.situacaoRisco(),
                inputDTO.potencialDano()
        );
        Incidente salvo = repository.salvarIncidente(incidente);
        return OcorrenciaOutputDTO.deDominio(salvo);
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

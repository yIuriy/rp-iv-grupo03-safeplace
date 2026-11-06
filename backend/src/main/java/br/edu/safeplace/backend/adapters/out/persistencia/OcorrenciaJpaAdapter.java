/*
 * Adaptador JPA da porta de ocorrências (#261): delega diretamente ao repositório JPA do domínio,
 * grava, consulta por id, protocolo CAT e critérios via Specification, e gera o sequencial da CAT.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

@Repository
public class OcorrenciaJpaAdapter implements OcorrenciaRepository {

    private final OcorrenciaJpaRepository repository;
    private final SequenciaProtocoloCatJpaRepository sequenciaProtocoloCatRepository;

    @Autowired
    public OcorrenciaJpaAdapter(
            OcorrenciaJpaRepository repository,
            SequenciaProtocoloCatJpaRepository sequenciaProtocoloCatRepository) {
        this.repository = repository;
        this.sequenciaProtocoloCatRepository = sequenciaProtocoloCatRepository;
    }

    /** Construtor legado para compatibilidade com testes unitários existentes. */
    public OcorrenciaJpaAdapter(
            OcorrenciaJpaRepository repository,
            AreaRiscoJpaRepository areaRiscoRepository,
            UsuarioJpaRepository usuarioRepository,
            SequenciaProtocoloCatJpaRepository sequenciaProtocoloCatRepository) {
        this(repository, sequenciaProtocoloCatRepository);
    }

    @Override
    public Acidente salvarAcidente(Acidente acidente) {
        return repository.save(acidente);
    }

    @Override
    public Incidente salvarIncidente(Incidente incidente) {
        return repository.save(incidente);
    }

    @Override
    public List<Ocorrencia> listar() {
        return repository.findAll();
    }

    @Override
    public List<Ocorrencia> listar(FiltroOcorrenciasDTO filtro) {
        return repository.findAll(OcorrenciaEspecificacoes.atendem(filtro), OcorrenciaEspecificacoes.ORDEM_DE_REGISTRO);
    }

    @Override
    public Optional<Ocorrencia> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    @Override
    public Optional<Ocorrencia> buscarPorProtocolo(String protocolo) {
        if (protocolo == null || protocolo.isBlank()) {
            return Optional.empty();
        }
        return repository.findByNumeroProtocolo(protocolo.trim()).map(a -> a);
    }

    @Override
    public long proximoSequencialCAT(int ano, int mes) {
        if (sequenciaProtocoloCatRepository == null) {
            throw new IllegalStateException(
                    "Sequência de protocolo CAT indisponível: o adaptador foi criado sem acesso ao banco.");
        }
        return sequenciaProtocoloCatRepository.reservarProximoSequencial(ano, mes);
    }
}

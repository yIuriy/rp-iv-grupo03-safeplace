package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;

/**
 * Adaptador JPA de persistência de planos de ação (RF07, UC07, US07, Issue #98, #262): delega
 * diretamente aos repositórios JPA das próprias entidades de domínio, sem classes espelhadas nem N+1.
 */
@Component
public class PlanoDeAcaoJpaAdapter implements PlanoDeAcaoRepository {

    private final PlanoDeAcaoJpaRepository planoRepository;
    private final ProrrogacaoPrazoJpaRepository prorrogacaoRepository;

    public PlanoDeAcaoJpaAdapter(
            PlanoDeAcaoJpaRepository planoRepository,
            ProrrogacaoPrazoJpaRepository prorrogacaoRepository) {
        this.planoRepository = Objects.requireNonNull(planoRepository, "Repositório de planos é obrigatório.");
        this.prorrogacaoRepository = Objects.requireNonNull(prorrogacaoRepository, "Repositório de prorrogações é obrigatório.");
    }

    @Override
    @Transactional
    public PlanoDeAcao salvar(PlanoDeAcao plano) {
        if (plano == null) {
            throw new IllegalArgumentException("Plano de ação não pode ser nulo.");
        }
        if (plano.getPrazo() == null) {
            throw new PrazoAcaoInvalidoException("O prazo da ação é obrigatório.");
        }
        return planoRepository.save(plano);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanoDeAcao> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return planoRepository.buscarPorIdComAssociacoes(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanoDeAcao> listarPorOcorrencia(Integer idOcorrencia) {
        if (idOcorrencia == null) {
            return List.of();
        }
        return planoRepository.listarPorOcorrencia(idOcorrencia);
    }

    @Override
    @Transactional
    public ProrrogacaoPrazo salvar(ProrrogacaoPrazo prorrogacao) {
        if (prorrogacao == null) {
            throw new IllegalArgumentException("Dados de prorrogação não podem ser nulos.");
        }
        return prorrogacaoRepository.save(prorrogacao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProrrogacaoPrazo> listarProrrogacoes(Integer idAcao) {
        if (idAcao == null) {
            return List.of();
        }
        return prorrogacaoRepository.listarPorPlanoDeAcao(idAcao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanoDeAcao> listarAbertasComAlerta() {
        return planoRepository.listarAbertasComAlerta(StatusPlanoDeAcao.ABERTA.name());
    }
}

package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Adaptador JPA de persistência de planos de ação (RF07, UC07, US07, Issue #98).
 * A auditoria é de responsabilidade da camada de aplicação (Facade #238), mantendo o adaptador
 * exclusivamente focado na persistência.
 */
@Component
public class PlanoDeAcaoJpaAdapter implements PlanoDeAcaoRepository {

    private final PlanoDeAcaoJpaRepository planoRepository;
    private final ProrrogacaoPrazoJpaRepository prorrogacaoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final SetorJpaRepository setorRepository;

    public PlanoDeAcaoJpaAdapter(
            PlanoDeAcaoJpaRepository planoRepository,
            ProrrogacaoPrazoJpaRepository prorrogacaoRepository,
            UsuarioJpaRepository usuarioRepository,
            SetorJpaRepository setorRepository) {
        this.planoRepository = Objects.requireNonNull(planoRepository, "Repositório de planos é obrigatório.");
        this.prorrogacaoRepository = Objects.requireNonNull(prorrogacaoRepository, "Repositório de prorrogações é obrigatório.");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "Repositório de usuários é obrigatório.");
        this.setorRepository = Objects.requireNonNull(setorRepository, "Repositório de setores é obrigatório.");
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

        PlanoDeAcaoEntity paraSalvar = toEntity(plano);
        PlanoDeAcaoEntity salvo = planoRepository.save(paraSalvar);
        return toDomain(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanoDeAcao> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return planoRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanoDeAcao> listarPorOcorrencia(Integer idOcorrencia) {
        if (idOcorrencia == null) {
            return List.of();
        }
        return planoRepository.findByOcorrenciaIdOrderByIdAsc(idOcorrencia).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public ProrrogacaoPrazo salvar(ProrrogacaoPrazo prorrogacao) {
        if (prorrogacao == null) {
            throw new IllegalArgumentException("Dados de prorrogação não podem ser nulos.");
        }
        ProrrogacaoPrazoEntity entity = new ProrrogacaoPrazoEntity(
                null,
                prorrogacao.idAcao(),
                prorrogacao.prazoAnterior(),
                prorrogacao.novoPrazo(),
                prorrogacao.justificativa(),
                LocalDateTime.now()
        );
        prorrogacaoRepository.save(entity);
        return prorrogacao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProrrogacaoPrazo> listarProrrogacoes(Integer idAcao) {
        if (idAcao == null) {
            return List.of();
        }
        return prorrogacaoRepository.findByPlanoDeAcaoIdOrderByCriadoEmAscIdAsc(idAcao)
                .stream()
                .map(e -> new ProrrogacaoPrazo(
                        e.getPlanoDeAcaoId(),
                        e.getPrazoAnterior(),
                        e.getNovoPrazo(),
                        e.getJustificativa()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanoDeAcao> listarAbertasComAlerta() {
        return planoRepository.findByAlertaAtivoTrueAndStatusOrderByIdAsc(StatusPlanoDeAcao.ABERTA.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private PlanoDeAcaoEntity toEntity(PlanoDeAcao plano) {
        Integer colaboradorId = plano.getResponsavelColaborador() != null
                ? plano.getResponsavelColaborador().getId()
                : null;
        Integer setorId = plano.getResponsavelSetor() != null
                ? plano.getResponsavelSetor().getId()
                : null;
        Integer gestorId = plano.getGestor() != null
                ? plano.getGestor().getId()
                : null;

        return new PlanoDeAcaoEntity(
                plano.getId(),
                plano.getIdOcorrencia(),
                plano.getMedidasCorretivas(),
                plano.getPrazo(),
                plano.getStatus(),
                plano.getMedidasPreventivas(),
                colaboradorId,
                setorId,
                gestorId,
                plano.isAlertaAtivo(),
                plano.getAntecedenciaAlertaDias()
        );
    }

    private PlanoDeAcao toDomain(PlanoDeAcaoEntity entity) {
        if (entity == null) {
            return null;
        }

        Colaborador colaborador = entity.getColaboradorId() != null
                ? carregarColaborador(entity.getColaboradorId())
                : null;

        Setor setor = entity.getSetorId() != null
                ? carregarSetor(entity.getSetorId())
                : null;

        GestorDeSeguranca gestor = entity.getGestorId() != null
                ? carregarGestor(entity.getGestorId())
                : null;

        return new PlanoDeAcao(
                entity.getId(),
                entity.getOcorrenciaId(),
                entity.getMedidasCorretivas(),
                entity.getMedidasPreventivas(),
                entity.getPrazo(),
                entity.getStatus(),
                colaborador,
                setor,
                entity.isAlertaAtivo(),
                entity.getAntecedenciaAlertaDias(),
                gestor
        );
    }

    private Colaborador carregarColaborador(Integer id) {
        if (usuarioRepository == null || id == null) {
            return null;
        }
        return usuarioRepository.findById(id).orElse(null);
    }

    private Setor carregarSetor(Integer id) {
        if (setorRepository == null || id == null) {
            return null;
        }
        return setorRepository.findById(id).orElse(null);
    }

    private GestorDeSeguranca carregarGestor(Integer id) {
        if (usuarioRepository == null || id == null) {
            return null;
        }
        return usuarioRepository.findById(id)
                .filter(GestorDeSeguranca.class::isInstance)
                .map(GestorDeSeguranca.class::cast)
                .orElse(null);
    }
}

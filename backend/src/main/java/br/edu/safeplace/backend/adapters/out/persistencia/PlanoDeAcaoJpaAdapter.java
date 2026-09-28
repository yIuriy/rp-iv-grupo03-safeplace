package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Adaptador JPA de persistência de planos de ação (RF07, RNF05, UC07, US07, Issue #98).
 */
@Component
public class PlanoDeAcaoJpaAdapter implements PlanoDeAcaoRepositoryPort {

    private final PlanoDeAcaoJpaRepository planoRepository;
    private final ProrrogacaoPrazoJpaRepository prorrogacaoRepository;
    private final UsuarioJpaRepositorio usuarioRepository;
    private final SetorJpaRepository setorRepository;
    private final LogAuditoriaRepositoryPort auditoriaRepository;

    public PlanoDeAcaoJpaAdapter(
            PlanoDeAcaoJpaRepository planoRepository,
            ProrrogacaoPrazoJpaRepository prorrogacaoRepository,
            UsuarioJpaRepositorio usuarioRepository,
            SetorJpaRepository setorRepository,
            LogAuditoriaRepositoryPort auditoriaRepository) {
        this.planoRepository = planoRepository;
        this.prorrogacaoRepository = prorrogacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.setorRepository = setorRepository;
        this.auditoriaRepository = auditoriaRepository;
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

        PlanoDeAcaoEntity existente = plano.getId() != null
                ? planoRepository.findById(plano.getId()).orElse(null)
                : null;

        PlanoDeAcaoEntity paraSalvar = toEntity(plano);
        PlanoDeAcaoEntity salvo = planoRepository.save(paraSalvar);

        if (auditoriaRepository != null) {
            if (existente == null) {
                LogAuditoria log = LogAuditoria.registrarCadastro(
                        resolverOperador(plano),
                        "PlanoDeAcao",
                        salvo.getId(),
                        "Criação de ação vinculada à ocorrência " + salvo.getOcorrenciaId()
                );
                auditoriaRepository.salvar(log);
            } else {
                boolean colaboradorMudou = !Objects.equals(existente.getColaboradorId(), salvo.getColaboradorId());
                boolean setorMudou = !Objects.equals(existente.getSetorId(), salvo.getSetorId());
                if (colaboradorMudou || setorMudou) {
                    LogAuditoria log = LogAuditoria.registrarAlteracao(
                            resolverOperador(plano),
                            "PlanoDeAcao",
                            salvo.getId(),
                            "Reatribuição de responsável da ação"
                    );
                    auditoriaRepository.salvar(log);
                }

                if (!Objects.equals(existente.getStatus(), salvo.getStatus())) {
                    LogAuditoria log = LogAuditoria.registrarAlteracao(
                            resolverOperador(plano),
                            "PlanoDeAcao",
                            salvo.getId(),
                            "Alteração de situação da ação para " + salvo.getStatus()
                    );
                    auditoriaRepository.salvar(log);
                }

                boolean alertaMudou = existente.isAlertaAtivo() != salvo.isAlertaAtivo()
                        || !Objects.equals(existente.getAntecedenciaAlertaDias(), salvo.getAntecedenciaAlertaDias());
                if (alertaMudou) {
                    LogAuditoria log = LogAuditoria.registrarAlteracao(
                            resolverOperador(plano),
                            "PlanoDeAcao",
                            salvo.getId(),
                            "Configuração de alerta da ação: ativo=" + salvo.isAlertaAtivo()
                                    + ", antecedencia=" + salvo.getAntecedenciaAlertaDias() + " dias"
                    );
                    auditoriaRepository.salvar(log);
                }
            }
        }

        return toDomain(salvo);
    }

    @Override
    @Transactional
    public List<PlanoDeAcao> salvarTodos(List<PlanoDeAcao> planos) {
        if (planos == null || planos.isEmpty()) {
            return List.of();
        }
        return planos.stream().map(this::salvar).toList();
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
    public ProrrogacaoPrazo salvarProrrogacao(ProrrogacaoPrazo prorrogacao) {
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

        if (auditoriaRepository != null) {
            LogAuditoria log = new LogAuditoria(
                    null,
                    "PRORROGAR_PRAZO",
                    LocalDateTime.now(),
                    resolverOperador(null),
                    prorrogacao.justificativa(),
                    "PlanoDeAcao",
                    prorrogacao.idAcao()
            );
            auditoriaRepository.salvar(log);
        }

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
        return usuarioRepository.findById(id)
                .map(ColaboradorEntidade::paraDominio)
                .orElse(null);
    }

    private Setor carregarSetor(Integer id) {
        if (setorRepository == null || id == null) {
            return null;
        }
        return setorRepository.findById(id)
                .map(SetorEntity::paraDominio)
                .orElse(null);
    }

    private GestorDeSeguranca carregarGestor(Integer id) {
        if (usuarioRepository == null || id == null) {
            return null;
        }
        return usuarioRepository.findById(id)
                .filter(GestorDeSegurancaEntidade.class::isInstance)
                .map(GestorDeSegurancaEntidade.class::cast)
                .map(GestorDeSegurancaEntidade::paraDominio)
                .orElse(null);
    }

    private String resolverOperador(PlanoDeAcao plano) {
        String authUser = emailAutenticado();
        if (authUser != null) {
            return authUser;
        }
        if (plano != null && plano.getGestor() != null && plano.getGestor().getEmail() != null) {
            return plano.getGestor().getEmail();
        }
        return "SISTEMA";
    }

    private String emailAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            String email = auth.getName().trim();
            return "anonymousUser".equals(email) ? null : email;
        }
        return null;
    }
}

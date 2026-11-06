package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.application.usecase.OperadorAtual;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Adaptador JPA de persistência de planos de ação (RF07, RNF05, UC07, US07, Issue #98).
 */
@Component
public class PlanoDeAcaoJpaAdapter implements PlanoDeAcaoRepository {

    private final PlanoDeAcaoJpaRepository planoRepository;
    private final ProrrogacaoPrazoJpaRepository prorrogacaoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final SetorJpaRepository setorRepository;
    private final LogAuditoriaRepository auditoriaRepository;
    private final OperadorAtual operadorAtual;

    public PlanoDeAcaoJpaAdapter(
            PlanoDeAcaoJpaRepository planoRepository,
            ProrrogacaoPrazoJpaRepository prorrogacaoRepository,
            UsuarioJpaRepository usuarioRepository,
            SetorJpaRepository setorRepository,
            LogAuditoriaRepository auditoriaRepository,
            OperadorAtual operadorAtual) {
        this.planoRepository = planoRepository;
        this.prorrogacaoRepository = prorrogacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.setorRepository = setorRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.auditoriaRepository = Objects.requireNonNull(
                auditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
        // Adapter (#236): o operador da auditoria vem da porta OperadorAutenticado.
        this.operadorAtual = Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
    }

    /** Cópia dos campos auditados antes da gravação. */
    private record EstadoAnterior(Integer colaboradorId, Integer setorId, String status,
                                  boolean alertaAtivo, Integer antecedenciaAlertaDias) {
        static EstadoAnterior de(PlanoDeAcaoEntity entidade) {
            return new EstadoAnterior(entidade.getColaboradorId(), entidade.getSetorId(),
                    entidade.getStatus(), entidade.isAlertaAtivo(), entidade.getAntecedenciaAlertaDias());
        }
    }

    private static String descreverResponsavel(Integer colaboradorId, Integer setorId) {
        if (colaboradorId != null) {
            return "colaborador id " + colaboradorId;
        }
        return setorId != null ? "setor id " + setorId : "nenhum responsável";
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

        // Valores anteriores copiados antes do save: o merge atualiza a própria instância gerenciada
        // devolvida por findById, e a comparação feita depois não veria a mudança.
        EstadoAnterior anterior = plano.getId() != null
                ? planoRepository.findById(plano.getId()).map(EstadoAnterior::de).orElse(null)
                : null;

        PlanoDeAcaoEntity paraSalvar = toEntity(plano);
        PlanoDeAcaoEntity salvo = planoRepository.save(paraSalvar);

        if (anterior == null) {
            LogAuditoria log = LogAuditoria.registrarCadastro(
                    resolverOperador(plano),
                    "PlanoDeAcao",
                    salvo.getId(),
                    "Criação de ação vinculada à ocorrência " + salvo.getOcorrenciaId()
            );
            auditoriaRepository.salvar(log);
        } else {
            boolean colaboradorMudou = !Objects.equals(anterior.colaboradorId(), salvo.getColaboradorId());
            boolean setorMudou = !Objects.equals(anterior.setorId(), salvo.getSetorId());
            if (colaboradorMudou || setorMudou) {
                // RNF05 / contrato UC07 §2.6: a reatribuição guarda o responsável anterior e o novo.
                LogAuditoria log = LogAuditoria.registrarAlteracao(
                        resolverOperador(plano),
                        "PlanoDeAcao",
                        salvo.getId(),
                        "Reatribuição de responsável da ação: de "
                                + descreverResponsavel(anterior.colaboradorId(), anterior.setorId())
                                + " para " + descreverResponsavel(salvo.getColaboradorId(), salvo.getSetorId())
                );
                auditoriaRepository.salvar(log);
            }

            if (!Objects.equals(anterior.status(), salvo.getStatus())) {
                LogAuditoria log = LogAuditoria.registrarAlteracao(
                        resolverOperador(plano),
                        "PlanoDeAcao",
                        salvo.getId(),
                        "Alteração de situação da ação: de " + anterior.status() + " para " + salvo.getStatus()
                );
                auditoriaRepository.salvar(log);
            }

            boolean alertaMudou = anterior.alertaAtivo() != salvo.isAlertaAtivo()
                    || !Objects.equals(anterior.antecedenciaAlertaDias(), salvo.getAntecedenciaAlertaDias());
            if (alertaMudou) {
                LogAuditoria log = LogAuditoria.registrarAlteracao(
                        resolverOperador(plano),
                        "PlanoDeAcao",
                        salvo.getId(),
                        "Configuração de alerta da ação: de ativo=" + anterior.alertaAtivo()
                                + ", antecedencia=" + anterior.antecedenciaAlertaDias() + " dias"
                                + " para ativo=" + salvo.isAlertaAtivo()
                                + ", antecedencia=" + salvo.getAntecedenciaAlertaDias() + " dias"
                );
                auditoriaRepository.salvar(log);
            }
        }

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
    public List<PlanoDeAcao> listarAbertasComAlertaAtivo() {
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

    /** Operador autenticado; sem sessão, o Gestor do plano; sem nenhum dos dois, SISTEMA. */
    private String resolverOperador(PlanoDeAcao plano) {
        Optional<String> autenticado = operadorAtual.obterIdentificadorOperador();
        if (autenticado.isPresent()) {
            return autenticado.get();
        }
        if (plano != null && plano.getGestor() != null && plano.getGestor().getEmail() != null) {
            return plano.getGestor().getEmail();
        }
        return OperadorAtual.SISTEMA;
    }
}

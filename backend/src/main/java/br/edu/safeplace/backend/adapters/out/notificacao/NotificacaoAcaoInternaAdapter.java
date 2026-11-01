package br.edu.safeplace.backend.adapters.out.notificacao;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoAcaoEntity;
import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoAcaoJpaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import org.springframework.beans.factory.annotation.Autowired;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.ocorrencia.AvisoAcaoValidador;

/**
 * Canal interno dos avisos das ações do plano (UC07; decisão 2.6 da correspondência do plano de ação;
 * issue #196): o aviso é gravado em {@code notificacoes_acao} e consultado pelo Gestor de Segurança em
 * {@code GET /api/notificacoes/acoes}. O responsável, Colaborador ou Setor, não tem acesso ao sistema e
 * é comunicado pelo Gestor; e-mail ou SMS não são presumidos.
 */
@Component
public class NotificacaoAcaoInternaAdapter implements NotificadorAcao {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoAcaoInternaAdapter.class);

    private final NotificacaoAcaoJpaRepository repository;
    private final Validador<AvisoAcao> validador;

    @Autowired
    public NotificacaoAcaoInternaAdapter(NotificacaoAcaoJpaRepository repository, Validador<AvisoAcao> validador) {
        this.repository = repository;
        this.validador = validador;
    }

    /** Usa o validador padrão; usado pelos testes sem contexto Spring. */
    public NotificacaoAcaoInternaAdapter(NotificacaoAcaoJpaRepository repository) {
        this(repository, new AvisoAcaoValidador());
    }

    @Override
    @Transactional
    public boolean notificar(AvisoAcao aviso) {
        // Chain of Responsibility (#266): valida antes de montar e gravar a notificação.
        validador.validar(aviso);
        NotificacaoAcao notificacao = NotificacaoAcao.deAviso(aviso);
        int gravados = repository.inserir(
                notificacao.idAcao(),
                notificacao.idOcorrencia(),
                notificacao.motivo().name(),
                notificacao.responsavelTipo(),
                notificacao.responsavelId(),
                notificacao.responsavelNome(),
                notificacao.prazo(),
                notificacao.dataAviso(),
                notificacao.mensagem(),
                LocalDateTime.now());
        if (gravados > 0) {
            log.info("[AVISO DE AÇÃO] {}", notificacao.mensagem());
        }
        return gravados > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacaoAcao> listar() {
        return repository.findAllByOrderByCriadaEmDescIdDesc().stream()
                .map(NotificacaoAcaoEntity::paraDominio)
                .toList();
    }
}

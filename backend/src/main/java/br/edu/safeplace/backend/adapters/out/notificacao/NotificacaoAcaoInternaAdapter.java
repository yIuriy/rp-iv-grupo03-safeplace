package br.edu.safeplace.backend.adapters.out.notificacao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoAcaoGravador;
import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoAcaoJpaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.ocorrencia.AvisoAcaoValidador;

/**
 * Canal interno dos avisos das ações do plano (UC07; decisão 2.6 da correspondência do plano de ação;
 * issue #196, #262): o aviso é gravado em {@code notificacoes_acao} via JPA e consultado pelo Gestor de
 * Segurança em {@code GET /api/notificacoes/acoes}.
 */
@Component
public class NotificacaoAcaoInternaAdapter implements NotificadorAcao {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoAcaoInternaAdapter.class);

    private final NotificacaoAcaoJpaRepository repository;
    private final NotificacaoAcaoGravador gravador;
    private final Validador<AvisoAcao> validador;

    @Autowired
    public NotificacaoAcaoInternaAdapter(
            NotificacaoAcaoJpaRepository repository,
            NotificacaoAcaoGravador gravador,
            Validador<AvisoAcao> validador) {
        this.repository = repository;
        this.gravador = gravador;
        this.validador = validador;
    }

    /** Usa o gravador e o validador padrão; usado pelos testes sem contexto Spring. */
    public NotificacaoAcaoInternaAdapter(NotificacaoAcaoJpaRepository repository) {
        this(repository, new NotificacaoAcaoGravador(repository), new AvisoAcaoValidador());
    }

    @Override
    @Transactional
    public boolean notificar(AvisoAcao aviso) {
        // Chain of Responsibility (#266): valida antes de montar e gravar a notificação.
        validador.validar(aviso);
        NotificacaoAcao notificacao = new NotificacaoAcao(aviso);
        boolean gravado = notificacao.getMotivo() == MotivoAvisoAcao.PRAZO
                ? gravador.gravarAlertaPrazoUnico(notificacao)
                : gravador.gravar(notificacao);
        if (gravado) {
            log.info("[AVISO DE AÇÃO] {}", notificacao.getMensagem());
        }
        return gravado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacaoAcao> listar() {
        return repository.findAllByOrderByCriadaEmDescIdDesc();
    }
}

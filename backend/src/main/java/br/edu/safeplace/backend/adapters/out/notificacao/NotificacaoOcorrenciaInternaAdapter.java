package br.edu.safeplace.backend.adapters.out.notificacao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoOcorrenciaJpaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.ocorrencia.NotificacaoOcorrenciaValidador;

/**
 * Adaptador de notificação interna para alertar o Gestor de Segurança sobre novos relatos
 * de acidentes e incidentes (UC09, DS09, Issue #99, #261).
 * A notificação é gravada diretamente como entidade JPA de domínio (#254, #261).
 */
@Component
public class NotificacaoOcorrenciaInternaAdapter implements NotificadorOcorrencia {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoOcorrenciaInternaAdapter.class);

    private final NotificacaoOcorrenciaJpaRepository repository;
    private final Validador<Ocorrencia> validador;

    @Autowired
    public NotificacaoOcorrenciaInternaAdapter(NotificacaoOcorrenciaJpaRepository repository,
            @Qualifier("notificacaoOcorrenciaValidador") Validador<Ocorrencia> validador) {
        this.repository = repository;
        this.validador = validador;
    }

    /** Usa o validador padrão; usado pelos testes sem contexto Spring. */
    public NotificacaoOcorrenciaInternaAdapter(NotificacaoOcorrenciaJpaRepository repository) {
        this(repository, new NotificacaoOcorrenciaValidador());
    }

    @Override
    public void notificarGestor(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            return;
        }

        // Chain of Responsibility (#266): valida antes de montar e gravar a notificação.
        validador.validar(ocorrencia);
        NotificacaoOcorrencia notificacao = NotificacaoOcorrencia.deNovoRelato(ocorrencia);
        repository.save(notificacao);

        log.info("[NOTIFICAÇÃO GESTOR] {} ID={}", notificacao.mensagem(), notificacao.ocorrenciaId());
    }

    @Override
    public List<NotificacaoOcorrencia> listar() {
        return repository.findAllByOrderByCriadaEmDescIdDesc();
    }
}

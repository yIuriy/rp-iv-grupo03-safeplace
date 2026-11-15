package br.edu.safeplace.backend.adapters.out.notificacao;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoOcorrenciaJpaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoIncidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.ocorrencia.NotificacaoOcorrenciaValidador;

/**
 * Adaptador de notificação interna para alertar o Gestor de Segurança sobre novos relatos
 * de acidentes e incidentes (UC09, DS09, Issue #99, #261).
 * Delega a criação da notificação para a família de {@link FabricaRelatoOcorrencia} (#239).
 */
@Component
public class NotificacaoOcorrenciaInternaAdapter implements NotificadorOcorrencia {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoOcorrenciaInternaAdapter.class);

    private final NotificacaoOcorrenciaJpaRepository repository;
    private final Validador<Ocorrencia> validador;
    private final Map<TipoOcorrencia, FabricaRelatoOcorrencia<?, ?>> fabricas;

    @Autowired
    public NotificacaoOcorrenciaInternaAdapter(
            NotificacaoOcorrenciaJpaRepository repository,
            @Qualifier("notificacaoOcorrenciaValidador") Validador<Ocorrencia> validador,
            FabricaRelatoAcidente fabricaAcidente,
            FabricaRelatoIncidente fabricaIncidente) {
        this.repository = repository;
        this.validador = validador;
        this.fabricas = Map.of(
                TipoOcorrencia.ACIDENTE, Objects.requireNonNull(fabricaAcidente, "Fábrica de acidente é obrigatória."),
                TipoOcorrencia.INCIDENTE, Objects.requireNonNull(fabricaIncidente, "Fábrica de incidente é obrigatória.")
        );
    }

    /** Usa o validador e as fábricas padrão; usado pelos testes sem contexto Spring. */
    public NotificacaoOcorrenciaInternaAdapter(NotificacaoOcorrenciaJpaRepository repository) {
        this(repository, new NotificacaoOcorrenciaValidador(), new FabricaRelatoAcidente(), new FabricaRelatoIncidente());
    }

    @Override
    public void notificar(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            return;
        }

        // Chain of Responsibility (#266): valida antes de montar e gravar a notificação.
        validador.validar(ocorrencia);
        FabricaRelatoOcorrencia<?, ?> fabrica = fabricas.get(ocorrencia.getTipoOcorrencia());
        if (fabrica == null) {
            throw new IllegalStateException("Fábrica de relato não configurada para tipo: " + ocorrencia.getTipoOcorrencia());
        }
        NotificacaoOcorrencia notificacao = fabrica.criarNotificacao(ocorrencia);
        repository.save(notificacao);

        log.info("[NOTIFICAÇÃO GESTOR] {} ID={}", notificacao.mensagem(), notificacao.ocorrenciaId());
    }

    @Override
    public List<NotificacaoOcorrencia> listar() {
        return repository.findAllByOrderByCriadaEmDescIdDesc();
    }
}

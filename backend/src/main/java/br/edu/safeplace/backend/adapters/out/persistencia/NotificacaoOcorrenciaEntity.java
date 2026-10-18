package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Notificação de novo relato gravada para o Gestor de Segurança (UC09, passo 12; issue #195). */
@Entity
@Table(name = "notificacoes_ocorrencia")
@Immutable
public class NotificacaoOcorrenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ocorrencia_id", nullable = false)
    private Integer ocorrenciaId;

    @Column(name = "tipo_ocorrencia", nullable = false, length = 20)
    private String tipoOcorrencia;

    @Column(name = "numero_protocolo_cat", length = 80)
    private String numeroProtocoloCAT;

    @Column(name = "area_nome", length = 160)
    private String areaNome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected NotificacaoOcorrenciaEntity() {
    }

    public static NotificacaoOcorrenciaEntity deDominio(NotificacaoOcorrencia notificacao) {
        NotificacaoOcorrenciaEntity entidade = new NotificacaoOcorrenciaEntity();
        entidade.id = notificacao.id();
        entidade.ocorrenciaId = notificacao.ocorrenciaId();
        entidade.tipoOcorrencia = notificacao.tipoOcorrencia().name();
        entidade.numeroProtocoloCAT = notificacao.numeroProtocoloCAT();
        entidade.areaNome = notificacao.areaNome();
        entidade.mensagem = notificacao.mensagem();
        entidade.criadaEm = notificacao.criadaEm();
        return entidade;
    }

    public NotificacaoOcorrencia paraDominio() {
        return new NotificacaoOcorrencia(id, ocorrenciaId, TipoOcorrencia.valueOf(tipoOcorrencia),
                numeroProtocoloCAT, areaNome, mensagem, criadaEm);
    }
}

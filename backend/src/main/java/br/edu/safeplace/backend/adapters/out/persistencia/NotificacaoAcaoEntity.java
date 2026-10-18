package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Aviso de prazo ou de reatribuição de uma ação do plano (UC07; issue #196). A inclusão é feita por
 * {@link NotificacaoAcaoJpaRepository#inserir}, que garante a emissão única do alerta de prazo.
 */
@Entity
@Table(name = "notificacoes_acao")
@Immutable
public class NotificacaoAcaoEntity {

    @Id
    private Integer id;

    @Column(name = "acao_id", nullable = false)
    private Integer acaoId;

    @Column(name = "ocorrencia_id")
    private Integer ocorrenciaId;

    @Column(nullable = false, length = 20)
    private String motivo;

    @Column(name = "responsavel_tipo", nullable = false, length = 20)
    private String responsavelTipo;

    @Column(name = "responsavel_id", nullable = false)
    private Integer responsavelId;

    @Column(name = "responsavel_nome", length = 160)
    private String responsavelNome;

    @Column(nullable = false)
    private LocalDate prazo;

    @Column(name = "data_aviso", nullable = false)
    private LocalDate dataAviso;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected NotificacaoAcaoEntity() {
    }

    public NotificacaoAcao paraDominio() {
        return new NotificacaoAcao(id, acaoId, ocorrenciaId, MotivoAvisoAcao.valueOf(motivo), responsavelTipo,
                responsavelId, responsavelNome, prazo, dataAviso, mensagem, criadaEm);
    }
}

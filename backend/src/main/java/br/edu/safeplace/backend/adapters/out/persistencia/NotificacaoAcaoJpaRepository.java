package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Avisos das ações do plano: só inclusão e consulta (issue #196). */
public interface NotificacaoAcaoJpaRepository extends Repository<NotificacaoAcaoEntity, Integer> {

    /**
     * Grava o aviso. O alerta de prazo repetido para a mesma ação e o mesmo prazo é ignorado pelo índice
     * único parcial {@code ux_notificacoes_acao_prazo}; devolve 0 nesse caso e 1 quando grava.
     */
    @Modifying
    @Query(value = """
            INSERT INTO notificacoes_acao (acao_id, ocorrencia_id, motivo, responsavel_tipo, responsavel_id,
                                           responsavel_nome, prazo, data_aviso, mensagem, criada_em)
            VALUES (:acaoId, :ocorrenciaId, :motivo, :responsavelTipo, :responsavelId,
                    :responsavelNome, :prazo, :dataAviso, :mensagem, :criadaEm)
            ON CONFLICT (acao_id, prazo) WHERE motivo = 'PRAZO' DO NOTHING
            """, nativeQuery = true)
    int inserir(@Param("acaoId") Integer acaoId,
                @Param("ocorrenciaId") Integer ocorrenciaId,
                @Param("motivo") String motivo,
                @Param("responsavelTipo") String responsavelTipo,
                @Param("responsavelId") Integer responsavelId,
                @Param("responsavelNome") String responsavelNome,
                @Param("prazo") LocalDate prazo,
                @Param("dataAviso") LocalDate dataAviso,
                @Param("mensagem") String mensagem,
                @Param("criadaEm") LocalDateTime criadaEm);

    List<NotificacaoAcaoEntity> findAllByOrderByCriadaEmDescIdDesc();
}

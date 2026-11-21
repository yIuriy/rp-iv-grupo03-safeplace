package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

/**
 * Repositório Spring Data da tabela planos_de_acao, sobre a própria entidade de domínio (#262).
 * Usa LEFT JOIN FETCH nas consultas para carregar ocorrência, responsáveis e gestor sem N+1.
 */
public interface PlanoDeAcaoJpaRepository extends JpaRepository<PlanoDeAcao, Integer> {

    @Query("""
            SELECT p FROM PlanoDeAcao p
            LEFT JOIN FETCH p.ocorrencia
            LEFT JOIN FETCH p.responsavelColaborador
            LEFT JOIN FETCH p.responsavelSetor
            LEFT JOIN FETCH p.gestor
            WHERE p.id = :id
            """)
    Optional<PlanoDeAcao> buscarPorIdComAssociacoes(@Param("id") Integer id);

    @Query("""
            SELECT p FROM PlanoDeAcao p
            LEFT JOIN FETCH p.ocorrencia
            LEFT JOIN FETCH p.responsavelColaborador
            LEFT JOIN FETCH p.responsavelSetor
            LEFT JOIN FETCH p.gestor
            WHERE p.ocorrencia.idOcorrencia = :ocorrenciaId
            ORDER BY p.id ASC
            """)
    List<PlanoDeAcao> listarPorOcorrencia(@Param("ocorrenciaId") Integer ocorrenciaId);

    /** Ações com alerta ativo na situação informada, para a verificação periódica de prazo (#196, #262). */
    @Query("""
            SELECT p FROM PlanoDeAcao p
            LEFT JOIN FETCH p.ocorrencia
            LEFT JOIN FETCH p.responsavelColaborador
            LEFT JOIN FETCH p.responsavelSetor
            LEFT JOIN FETCH p.gestor
            WHERE p.alertaAtivo = true AND p.status = :status
            ORDER BY p.id ASC
            """)
    List<PlanoDeAcao> listarAbertasComAlerta(@Param("status") String status);
}

package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;

/** Repositório Spring Data da tabela prorrogacoes_prazo, sobre a própria entidade de domínio (#262). */
public interface ProrrogacaoPrazoJpaRepository extends JpaRepository<ProrrogacaoPrazo, Integer> {

    @Query("""
            SELECT p FROM ProrrogacaoPrazo p
            JOIN FETCH p.planoDeAcao
            WHERE p.planoDeAcao.id = :planoDeAcaoId
            ORDER BY p.criadoEm ASC, p.id ASC
            """)
    List<ProrrogacaoPrazo> listarPorPlanoDeAcao(@Param("planoDeAcaoId") Integer planoDeAcaoId);
}

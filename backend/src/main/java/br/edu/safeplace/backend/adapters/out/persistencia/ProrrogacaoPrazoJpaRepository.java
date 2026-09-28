package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProrrogacaoPrazoJpaRepository extends JpaRepository<ProrrogacaoPrazoEntity, Integer> {

    List<ProrrogacaoPrazoEntity> findByPlanoDeAcaoIdOrderByCriadoEmAscIdAsc(Integer planoDeAcaoId);
}

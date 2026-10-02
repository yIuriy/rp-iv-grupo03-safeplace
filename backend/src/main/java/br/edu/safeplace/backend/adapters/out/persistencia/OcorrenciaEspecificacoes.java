/*
 * Consulta de ocorrências por critérios no banco (issue #128): monta, com a Criteria API, só as
 * condições informadas no FiltroOcorrenciasDTO. Período pela data do fato, setor pela área de risco
 * e colaborador envolvido pela chave estrangeira da ocorrência.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

final class OcorrenciaEspecificacoes {

    /** Ordem de registro, a mesma que a listagem já entregava, agora garantida pela consulta. */
    static final Sort ORDEM_DE_REGISTRO = Sort.by("idOcorrencia");

    private OcorrenciaEspecificacoes() {
    }

    static Specification<OcorrenciaEntity> atendem(FiltroOcorrenciasDTO filtro) {
        return (root, query, builder) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.inicioInclusivo() != null) {
                condicoes.add(builder.greaterThanOrEqualTo(root.get("dataOcorrencia"), filtro.inicioInclusivo()));
            }
            if (filtro.fimExclusivo() != null) {
                condicoes.add(builder.lessThan(root.get("dataOcorrencia"), filtro.fimExclusivo()));
            }
            if (filtro.setorId() != null) {
                // Ocorrência sem área não pertence a setor algum: a junção interna a exclui.
                condicoes.add(builder.equal(root.join("area", JoinType.INNER).get("setorId"), filtro.setorId()));
            }
            if (filtro.colaboradorId() != null) {
                condicoes.add(builder.equal(root.get("colaborador").get("id"), filtro.colaboradorId()));
            }
            return builder.and(condicoes.toArray(Predicate[]::new));
        };
    }
}

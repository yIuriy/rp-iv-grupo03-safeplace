/*
 * Consulta de ocorrências por critérios no banco (issue #128, #261): monta, com a Criteria API, só as
 * condições informadas no FiltroOcorrenciasDTO. Período pela data do fato, setor pela área de risco,
 * colaborador envolvido pela chave estrangeira da ocorrência, tipo pela subclasse e gravidade pela
 * tabela de acidentes (issue #195).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

final class OcorrenciaEspecificacoes {

    /** Ordem de registro, a mesma que a listagem já entregava, agora garantida pela consulta. */
    static final Sort ORDEM_DE_REGISTRO = Sort.by("idOcorrencia");

    private OcorrenciaEspecificacoes() {
    }

    static Specification<Ocorrencia> atendem(FiltroOcorrenciasDTO filtro) {
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
                condicoes.add(builder.equal(root.join("area", JoinType.INNER).get("setor").get("id"), filtro.setorId()));
            }
            if (filtro.colaboradorId() != null) {
                condicoes.add(builder.equal(root.get("colaborador").get("id"), filtro.colaboradorId()));
            }
            if (filtro.tipoOcorrencia() != null) {
                condicoes.add(builder.equal(root.type(), filtro.tipoOcorrencia() == TipoOcorrencia.ACIDENTE
                        ? Acidente.class
                        : Incidente.class));
            }
            if (filtro.gravidade() != null) {
                // Só acidentes têm gravidade: a subconsulta deixa os incidentes de fora.
                Subquery<Integer> acidentesComGravidade = query.subquery(Integer.class);
                Root<Acidente> acidente = acidentesComGravidade.from(Acidente.class);
                acidentesComGravidade.select(acidente.get("idOcorrencia"))
                        .where(builder.equal(acidente.get("gravidade"), filtro.gravidade()));
                condicoes.add(root.get("idOcorrencia").in(acidentesComGravidade));
            }
            return builder.and(condicoes.toArray(Predicate[]::new));
        };
    }
}

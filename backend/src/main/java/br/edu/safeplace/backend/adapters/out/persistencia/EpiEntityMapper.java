package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

/**
 * Conversão entre {@link EpiEntity} e {@link Epi}, compartilhada pelos adaptadores que precisam
 * materializar EPIs (o próprio {@link EpiJpaAdapter} e o {@link AreaRiscoJpaAdapter}, que carrega
 * os EPIs obrigatórios de acesso à área).
 */
final class EpiEntityMapper {

    private EpiEntityMapper() {
    }

    static EpiEntity toEntity(Epi domain) {
        return new EpiEntity(
                domain.getId(),
                domain.getNome(),
                domain.getNumeroCa(),
                domain.getQuantidade(),
                domain.getEstoqueMinimo(),
                domain.getStatus().name(),
                domain.getDataValidadeCa(),
                domain.getVidaUtilDias(),
                domain.getEspecificacao().getDescricao(),
                domain.getEspecificacao().getClassificacao() == null
                        ? null
                        : domain.getEspecificacao().getClassificacao().name(),
                domain.getLocalizacao());
    }

    static Epi toDomain(EpiEntity entity) {
        return new Epi(
                entity.getId(),
                entity.getNome(),
                entity.getNumeroCa(),
                entity.getQuantidade(),
                entity.getEstoqueMinimo(),
                StatusEpi.valueOf(entity.getStatus()),
                entity.getDataValidadeCa(),
                entity.getVidaUtilDias(),
                entity.getDescricao(),
                entity.getClassificacao() == null
                        ? null
                        : ClassificacaoEPI.valueOf(entity.getClassificacao()),
                entity.getLocalizacao());
    }
}

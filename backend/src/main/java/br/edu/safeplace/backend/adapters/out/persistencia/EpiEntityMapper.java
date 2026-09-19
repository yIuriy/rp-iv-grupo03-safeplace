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
                domain.getLocalizacao(),
                toEntity(domain.getLote()));
    }

    static Epi toDomain(EpiEntity entity) {
        return Epi.reconstituir(
                entity.getId(),
                entity.getNome(),
                toDomain(entity.getLote(), entity),
                entity.getQuantidade(),
                entity.getEstoqueMinimo(),
                StatusEpi.valueOf(entity.getStatus()),
                entity.getVidaUtilDias(),
                entity.getDescricao(),
                entity.getClassificacao() == null
                        ? null
                        : ClassificacaoEPI.valueOf(entity.getClassificacao()),
                entity.getLocalizacao());
    }

    private static LoteEpiEntity toEntity(br.edu.safeplace.backend.domain.epi.LoteEPI lote) {
        return new LoteEpiEntity(lote.getId(), lote.getNumeroLote(), lote.getNotaFiscal(),
                lote.getDataFabricacao(), lote.getValidade(), lote.getQuantidadeRecebida(),
                new ModeloEpiEntity(lote.getModelo().getId(), lote.getModelo().getCa(), lote.getModelo().getMarca(),
                        lote.getModelo().getValidadeCA()));
    }

    private static br.edu.safeplace.backend.domain.epi.LoteEPI toDomain(LoteEpiEntity lote, EpiEntity legado) {
        if (lote == null) {
            return new br.edu.safeplace.backend.domain.epi.LoteEPI(null, null, null, null,
                    legado.getQuantidade(),
                    new br.edu.safeplace.backend.domain.epi.ModeloEPI(Integer.parseInt(legado.getNumeroCa()),
                            null, legado.getDataValidadeCa()));
        }
        return new br.edu.safeplace.backend.domain.epi.LoteEPI(lote.getId(), lote.getNumeroLote(),
                lote.getNotaFiscal(), lote.getDataFabricacao(), lote.getValidade(),
                lote.getQuantidadeRecebida(), new br.edu.safeplace.backend.domain.epi.ModeloEPI(
                lote.getModelo().getId(), lote.getModelo().getCa(), lote.getModelo().getMarca(), lote.getModelo().getValidadeCA()));
    }
}

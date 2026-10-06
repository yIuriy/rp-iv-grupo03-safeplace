package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.LoteEPI;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

/**
 * Conversão entre {@link EpiEntity} e {@link Epi}, compartilhada pelos adaptadores que precisam
 * materializar EPIs (o próprio {@link EpiJpaAdapter} e o {@link AreaRiscoJpaAdapter}, que carrega
 * os EPIs obrigatórios de acesso à área). CA e validade vêm sempre do modelo do lote (decisão #124).
 */
final class EpiEntityMapper {

    private EpiEntityMapper() {
    }

    static EpiEntity toEntity(Epi domain) {
        return new EpiEntity(
                domain.getId(),
                domain.getNome(),
                domain.getQuantidade(),
                domain.getEstoqueMinimo(),
                domain.getStatus().name(),
                domain.getVidaUtilDias(),
                domain.getEspecificacao().getDescricao(),
                domain.getEspecificacao().getClassificacao() == null
                        ? null
                        : domain.getEspecificacao().getClassificacao().name(),
                domain.getLocalizacao(),
                toEntity(domain.getLote()));
    }

    static Epi toDomain(EpiEntity entity) {
        if (entity.getLote() == null) {
            throw new IllegalStateException("EPI " + entity.getId() + " sem lote: CA indisponível.");
        }
        return Epi.reconstituir(
                entity.getId(),
                entity.getNome(),
                toDomain(entity.getLote()),
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

    private static LoteEpiEntity toEntity(LoteEPI lote) {
        ModeloEPI modelo = lote.getModelo();
        return new LoteEpiEntity(lote.getId(), lote.getNumeroLote(), lote.getNotaFiscal(),
                lote.getDataFabricacao(), lote.getValidade(), lote.getQuantidadeRecebida(),
                new ModeloEpiEntity(modelo.getId(), modelo.getCa(), modelo.getMarca(), modelo.getValidadeCA()));
    }

    private static LoteEPI toDomain(LoteEpiEntity lote) {
        ModeloEpiEntity modelo = lote.getModelo();
        return new LoteEPI(lote.getId(), lote.getNumeroLote(), lote.getNotaFiscal(),
                lote.getDataFabricacao(), lote.getValidade(), lote.getQuantidadeRecebida(),
                new ModeloEPI(modelo.getId(), modelo.getCa(), modelo.getMarca(), modelo.getValidadeCA()));
    }
}

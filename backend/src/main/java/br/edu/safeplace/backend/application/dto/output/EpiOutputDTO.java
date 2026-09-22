package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;

import java.time.LocalDate;

public record EpiOutputDTO(
        Integer id,
        String nome,
        String numeroCa,
        int quantidade,
        int estoqueMinimo,
        StatusEpi status,
        LocalDate dataValidadeCa,
        Integer vidaUtilDias,
        boolean estoqueCritico,
        String descricao,
        ClassificacaoEPI classificacao,
        String localizacao,
        Integer loteId,
        String numeroLote,
        String notaFiscal,
        LocalDate dataFabricacao,
        LocalDate validadeLote,
        Integer modeloId,
        String marca) {

    public EpiOutputDTO(Integer id, String nome, String numeroCa, int quantidade, int estoqueMinimo,
                        StatusEpi status, LocalDate dataValidadeCa, Integer vidaUtilDias,
                        boolean estoqueCritico, String descricao, ClassificacaoEPI classificacao) {
        this(id, nome, numeroCa, quantidade, estoqueMinimo, status, dataValidadeCa, vidaUtilDias,
                estoqueCritico, descricao, classificacao, null, null, null, null, null, null, null, null);
    }

    public EpiOutputDTO(Integer id, String nome, String numeroCa, int quantidade, int estoqueMinimo,
                        StatusEpi status, LocalDate dataValidadeCa, Integer vidaUtilDias,
                        boolean estoqueCritico, String descricao, ClassificacaoEPI classificacao,
                        String localizacao) {
        this(id, nome, numeroCa, quantidade, estoqueMinimo, status, dataValidadeCa, vidaUtilDias,
                estoqueCritico, descricao, classificacao, localizacao, null, null, null, null, null, null, null);
    }
    public static EpiOutputDTO deDominio(Epi epi) {
        return new EpiOutputDTO(
                epi.getId(),
                epi.getNome(),
                epi.getNumeroCa(),
                epi.getQuantidade(),
                epi.getEstoqueMinimo(),
                epi.getStatus(),
                epi.getDataValidadeCa(),
                epi.getVidaUtilDias(),
                epi.isEstoqueCritico(),
                epi.getEspecificacao().getDescricao(),
                epi.getEspecificacao().getClassificacao(),
                epi.getLocalizacao(),
                epi.getLote().getId(),
                epi.getLote().getNumeroLote(),
                epi.getLote().getNotaFiscal(),
                epi.getLote().getDataFabricacao(),
                epi.getLote().getValidade(),
                epi.getModelo().getId(),
                epi.getModelo().getMarca());
    }
}

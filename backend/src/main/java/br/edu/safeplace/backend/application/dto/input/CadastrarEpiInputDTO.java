package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;

public record CadastrarEpiInputDTO(
                String nome,
                String numeroCa,
                int quantidade,
                int estoqueMinimo,
                LocalDate dataValidadeCa,
                Integer vidaUtilDias,
                String descricao,
                ClassificacaoEPI classificacao,
                String localizacao,
                String numeroLote,
                String notaFiscal,
                LocalDate dataFabricacao,
                LocalDate validadeLote,
                String marca) {

    public CadastrarEpiInputDTO(String nome, String numeroCa, int quantidade, int estoqueMinimo,
                                LocalDate dataValidadeCa, Integer vidaUtilDias, String descricao,
                                ClassificacaoEPI classificacao) {
        this(nome, numeroCa, quantidade, estoqueMinimo, dataValidadeCa, vidaUtilDias,
                descricao, classificacao, null, null, null, null, null, null);
    }
}

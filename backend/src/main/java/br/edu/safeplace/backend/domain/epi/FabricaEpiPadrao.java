package br.edu.safeplace.backend.domain.epi;

/**
 * ConcreteCreator da {@link FabricaEpi} (#225), registrado no Spring por {@code ServicosDominioConfig}. Um EPI
 * novo ainda não tem id, e a situação inicial acompanha o saldo recebido (UC06).
 */
public class FabricaEpiPadrao implements FabricaEpi {

    @Override
    public Epi criar(String nome, EspecificacaoEPI especificacao, LoteEPI lote, int quantidade,
            Integer vidaUtilDias, String localizacao) {
        StatusEpi situacaoInicial = quantidade > 0 ? StatusEpi.DISPONIVEL : StatusEpi.ESGOTADO;
        return new Epi(nome, especificacao, lote, quantidade, situacaoInicial, vidaUtilDias, localizacao);
    }
}

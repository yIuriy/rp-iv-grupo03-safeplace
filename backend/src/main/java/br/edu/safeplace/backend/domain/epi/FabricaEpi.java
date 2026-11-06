package br.edu.safeplace.backend.domain.epi;

/**
 * Creator do Factory Method (#225): única forma de criar um EPI novo. Recebe a {@link EspecificacaoEPI} inteira
 * em vez dos campos soltos e não valida; as regras de cadastro rodam na camada validacao, antes do repository.
 * Ler um EPI gravado é papel do JPA, e não desta fábrica (decisão #254).
 */
public interface FabricaEpi {

    Epi criar(String nome, EspecificacaoEPI especificacao, LoteEPI lote, int quantidade, Integer vidaUtilDias,
            String localizacao);
}

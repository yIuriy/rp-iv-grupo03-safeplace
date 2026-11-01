package br.edu.safeplace.backend.validacao.usuario;

import java.util.List;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;
import br.edu.safeplace.backend.validacao.Validador;

/**
 * Delega o formato do CPF ao {@link CpfValidador}. CPF inválido interrompe a cadeia: não faz
 * sentido consultar se ele já está cadastrado.
 */
class CpfValidoElo extends EloValidacao<Colaborador> {

    private final Validador<String> cpfValidador;

    CpfValidoElo(Validador<String> cpfValidador) {
        this.cpfValidador = cpfValidador;
    }

    @Override
    protected void verificar(Colaborador pessoa, ResultadoValidacao resultado) {
        ResultadoValidacao doCpf = cpfValidador.verificar(pessoa.getCpf());
        if (!doCpf.valido()) {
            List<Problema> problemas = doCpf.problemas();
            problemas.subList(0, problemas.size() - 1).forEach(resultado::adicionar);
            resultado.interromper(problemas.getLast());
        }
    }
}

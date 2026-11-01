package br.edu.safeplace.backend.validacao.usuario;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/**
 * RF23 e UC13: Supervisor e Gestor de Segurança têm conta de acesso, então exigem e-mail e senha.
 * O Colaborador não tem conta e passa direto. Antes da issue #263, ficava nos construtores das
 * subclasses.
 */
class CredenciaisDoPerfilElo extends EloValidacao<Colaborador> {

    @Override
    protected void verificar(Colaborador pessoa, ResultadoValidacao resultado) {
        if (pessoa instanceof Supervisor supervisor) {
            exigirCredenciais(resultado, supervisor.getEmail(), supervisor.getSenha(), "Supervisor");
        } else if (pessoa instanceof GestorDeSeguranca gestor) {
            exigirCredenciais(resultado, gestor.getEmail(), gestor.getSenha(), "Gestor de Segurança");
        }
    }

    private static void exigirCredenciais(ResultadoValidacao resultado, String email, String senha, String perfil) {
        if (email == null || email.isBlank()) {
            adicionar(resultado, "Email é obrigatório para o perfil " + perfil + ".");
        }
        if (senha == null || senha.isBlank()) {
            adicionar(resultado, "Senha é obrigatória para o perfil " + perfil + ".");
        }
    }

    private static void adicionar(ResultadoValidacao resultado, String mensagem) {
        resultado.adicionar(Problema.comExcecao(mensagem, () -> new IllegalArgumentException(mensagem)));
    }
}

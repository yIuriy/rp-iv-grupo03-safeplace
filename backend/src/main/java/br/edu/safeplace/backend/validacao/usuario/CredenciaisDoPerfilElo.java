package br.edu.safeplace.backend.validacao.usuario;

import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;

/**
 * RF23 e UC13: Supervisor e Gestor de Segurança têm conta de acesso, então exigem e-mail e senha.
 * O Colaborador não tem conta e passa direto. Antes da issue #263, ficava nos construtores das
 * subclasses. Desde a issue #308, o formato do e-mail usa o {@code @Email} do Bean Validation, a
 * mesma regra dos Requests de cadastro.
 */
class CredenciaisDoPerfilElo extends EloValidacao<Colaborador> {

    private static final Validator BEAN_VALIDATION = Validation.buildDefaultValidatorFactory().getValidator();

    /** Só existe para levar a anotação {@code @Email} até o validador. */
    private record EmailInformado(@Email String valor) {
    }

    @Override
    protected void verificar(Colaborador pessoa, ResultadoValidacao resultado) {
        if (pessoa instanceof Supervisor supervisor) {
            exigirCredenciais(resultado, supervisor.getEmail(), supervisor.getSenha(), "Supervisor");
        } else if (pessoa instanceof GestorDeSeguranca gestor) {
            exigirCredenciais(resultado, gestor.getEmail(), gestor.getSenha(), "Gestor de Segurança");
        }
    }

    private static void exigirCredenciais(ResultadoValidacao resultado, String email, String senha, String perfil) {
        if (isBlankOrNull(email)) {
            adicionar(resultado, "Email é obrigatório para o perfil " + perfil + ".");
        } else if (!BEAN_VALIDATION.validateValue(EmailInformado.class, "valor", email).isEmpty()) {
            adicionar(resultado, "Email inválido para o perfil " + perfil + ".");
        }
        if (isBlankOrNull(senha)) {
            adicionar(resultado, "Senha é obrigatória para o perfil " + perfil + ".");
        }
    }

    private static void adicionar(ResultadoValidacao resultado, String mensagem) {
        resultado.adicionar(Problema.comExcecao(mensagem, () -> new IllegalArgumentException(mensagem)));
    }
}

package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;

/**
 * Requisição de {@code POST /api/usuarios/supervisores} (RF23).
 *
 * <p>A credencial inicial do Supervisor é gerada pelo sistema (issue #91) e devolvida uma única
 * vez na resposta do cadastro. O campo {@code senha} é declarado apenas para ser recusado com
 * 400, deixando explícito que a senha não pode ser escolhida por quem cadastra.</p>
 */
@Schema(description = "Requisição para cadastro de Supervisor pelo Gestor de Segurança")
public record CadastrarSupervisorRequisicao(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome completo", example = "Joana Ribeiro")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        @Schema(description = "CPF com ou sem máscara", example = "52998224725")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        @Schema(description = "Data de nascimento (AAAA-MM-DD)", example = "1988-04-20")
        LocalDate dataNascimento,

        @NotBlank(message = "Email é obrigatório para o perfil Supervisor")
        @Email(message = "Email inválido")
        @Schema(description = "Email corporativo usado no login", example = "joana.ribeiro@safeplace.com")
        String email,

        @Null(message = "A senha inicial do Supervisor é gerada pelo sistema: remova o campo da requisição")
        @Schema(hidden = true)
        String senha) {

    public CadastrarSupervisorInputDTO paraDTOEntrada() {
        return new CadastrarSupervisorInputDTO(nome, cpf, dataNascimento, email);
    }
}

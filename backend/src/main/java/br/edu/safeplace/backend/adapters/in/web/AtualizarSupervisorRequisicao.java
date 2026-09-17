package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;

/**
 * Requisição de {@code PUT /api/usuarios/supervisores/{id}} (RF23, issue #122).
 *
 * <p>Só dados cadastrais. CPF, senha, perfil e situação são declarados apenas para serem
 * recusados com 400: a alteração de CPF e a desativação aguardam decisão do grupo (MVP, seção
 * 11.1), a senha não é trocada por este caminho e o perfil é dado pela rota.</p>
 */
@Schema(description = "Requisição para atualizar os dados cadastrais de um Supervisor")
public record AtualizarSupervisorRequisicao(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome completo", example = "Joana Ribeiro")
        String nome,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        @Schema(description = "Data de nascimento (AAAA-MM-DD)", example = "1988-04-20")
        LocalDate dataNascimento,

        @NotBlank(message = "Email é obrigatório para o perfil Supervisor")
        @Email(message = "Email inválido")
        @Schema(description = "Email corporativo usado no login", example = "joana.ribeiro@safeplace.com")
        String email,

        @Null(message = "O CPF não é alterado por esta operação: remova o campo da requisição")
        @Schema(hidden = true)
        String cpf,

        @Null(message = "A senha não é alterada por esta operação: remova o campo da requisição")
        @Schema(hidden = true)
        String senha,

        @Null(message = "O perfil é definido pela rota e não muda na atualização: remova o campo da requisição")
        @Schema(hidden = true)
        String perfil,

        @Null(message = "Desativação e reativação aguardam decisão do grupo: remova o campo da requisição")
        @Schema(hidden = true)
        Boolean ativo) {

    public AtualizarPessoaInputDTO paraDTOEntrada() {
        return new AtualizarPessoaInputDTO(nome, dataNascimento, email);
    }
}

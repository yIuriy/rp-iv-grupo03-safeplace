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
 * Requisição de {@code PUT /api/usuarios/colaboradores/{id}} (RF23, issue #122).
 *
 * <p>O Colaborador continua sem conta de acesso: senha e perfil são recusados com 400, como no
 * cadastro. CPF e situação também são recusados, porque a alteração de CPF e a desativação
 * aguardam decisão do grupo (MVP, seção 11.1).</p>
 */
@Schema(description = "Requisição para atualizar os dados cadastrais de um Colaborador")
public record AtualizarColaboradorRequisicao(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome completo", example = "João da Silva")
        String nome,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        @Schema(description = "Data de nascimento (AAAA-MM-DD)", example = "1992-06-18")
        LocalDate dataNascimento,

        @Email(message = "Email inválido")
        @Schema(description = "Email de contato. Opcional: o Colaborador não usa o sistema.",
                example = "joao.silva@safeplace.com")
        String email,

        @Null(message = "O CPF não é alterado por esta operação: remova o campo da requisição")
        @Schema(hidden = true)
        String cpf,

        @Null(message = "Colaborador não possui senha: remova o campo da requisição")
        @Schema(hidden = true)
        String senha,

        @Null(message = "O perfil deste cadastro é sempre COLABORADOR: remova o campo da requisição")
        @Schema(hidden = true)
        String perfil,

        @Null(message = "Desativação e reativação aguardam decisão do grupo: remova o campo da requisição")
        @Schema(hidden = true)
        Boolean ativo) {

    public AtualizarPessoaInputDTO paraDTOEntrada() {
        return new AtualizarPessoaInputDTO(nome, dataNascimento, email);
    }
}

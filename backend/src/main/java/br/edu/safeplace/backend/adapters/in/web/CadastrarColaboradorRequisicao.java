package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;

/**
 * Requisição de {@code POST /api/usuarios/colaboradores} (RF23).
 *
 * <p>O Colaborador não acessa o sistema: existe apenas para vinculação a ocorrências,
 * capacitações e empréstimos de EPIs.</p>
 *
 * <p>Os campos {@code senha} e {@code perfil} são declarados apenas para serem recusados: se a
 * requisição os enviar, a resposta é 400. O critério de RF23 é que este caminho não exija nem
 * aceite credenciais, e recusar é mais honesto do que descartar o campo em silêncio.</p>
 */
@Schema(description = "Requisição para cadastro de Colaborador, que não possui conta de acesso")
public record CadastrarColaboradorRequisicao(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome completo", example = "João da Silva")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        @Schema(description = "CPF com ou sem máscara", example = "52998224725")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        @Schema(description = "Data de nascimento (AAAA-MM-DD)", example = "1992-06-18")
        LocalDate dataNascimento,

        @Email(message = "Email inválido")
        @Schema(description = "Email de contato. Opcional: o Colaborador não usa o sistema.",
                example = "joao.silva@safeplace.com")
        String email,

        @Null(message = "Colaborador não possui senha: remova o campo da requisição")
        @Schema(hidden = true)
        String senha,

        @Null(message = "O perfil deste cadastro é sempre COLABORADOR: remova o campo da requisição")
        @Schema(hidden = true)
        String perfil) {

    public CadastrarColaboradorInputDTO paraDTOEntrada() {
        return new CadastrarColaboradorInputDTO(nome, cpf, dataNascimento, email);
    }
}

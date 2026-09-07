package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Requisição para cadastro de usuário ou colaborador")
public record CriarUsuarioRequisicao(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome completo", example = "João da Silva")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @Schema(description = "CPF com ou sem máscara", example = "52998224725")
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Schema(description = "Data de nascimento (AAAA-MM-DD)", example = "1988-04-20")
        LocalDate dataNascimento,

        @Schema(description = "Email corporativo (obrigatório para perfis com acesso ao sistema)", example = "joao.silva@safeplace.com")
        String email,

        @Schema(description = "Senha de acesso (obrigatória para Supervisor e Gestor de Segurança)", example = "Segredo@123")
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        @Schema(description = "Perfil do usuário", example = "SUPERVISOR")
        Perfil perfil
) {
    public CadastrarUsuarioEntradaDTO paraDTOEntrada() {
        return new CadastrarUsuarioEntradaDTO(
                nome,
                cpf,
                dataNascimento,
                email,
                senha,
                perfil
        );
    }
}

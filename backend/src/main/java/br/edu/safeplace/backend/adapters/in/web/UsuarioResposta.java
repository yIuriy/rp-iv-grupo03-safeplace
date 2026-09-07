package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Dados públicos do usuário (sem exposição de credenciais)")
public record UsuarioResposta(
        @Schema(description = "Identificador único", example = "1")
        Integer id,

        @Schema(description = "Nome completo", example = "João da Silva")
        String nome,

        @Schema(description = "CPF sanitizado", example = "52998224725")
        String cpf,

        @Schema(description = "Data de nascimento", example = "1988-04-20")
        LocalDate dataNascimento,

        @Schema(description = "Email cadastrado", example = "joao.silva@safeplace.com")
        String email,

        @Schema(description = "Perfil de acesso ou funcionalidade", example = "SUPERVISOR")
        Perfil perfil,

        @Schema(description = "Status de ativação do usuário", example = "true")
        boolean ativo,

        @Schema(description = "Data e hora de criação no sistema", example = "2026-09-07T10:00:00")
        LocalDateTime criadoEm
) {
    public static UsuarioResposta aPartirDe(UsuarioSaidaDTO dto) {
        return new UsuarioResposta(
                dto.id(),
                dto.nome(),
                dto.cpf(),
                dto.dataNascimento(),
                dto.email(),
                dto.perfil(),
                dto.ativo(),
                dto.criadoEm()
        );
    }
}

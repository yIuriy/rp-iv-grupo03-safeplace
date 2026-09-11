package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.LoginEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.TokenSaidaDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioCasoDeUso;
import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.TokenPorta;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.CredenciaisInvalidasException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AutenticacaoCasoDeUso implements AutenticarUsuarioCasoDeUso {

    private final UsuarioRepositorioPorta usuarioRepositorioPorta;
    private final CodificadorSenhaPorta codificadorSenhaPorta;
    private final TokenPorta tokenPorta;

    public AutenticacaoCasoDeUso(UsuarioRepositorioPorta usuarioRepositorioPorta,
                                 CodificadorSenhaPorta codificadorSenhaPorta,
                                 TokenPorta tokenPorta) {
        this.usuarioRepositorioPorta = usuarioRepositorioPorta;
        this.codificadorSenhaPorta = codificadorSenhaPorta;
        this.tokenPorta = tokenPorta;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenSaidaDTO autenticar(LoginEntradaDTO entrada) {
        if (entrada == null || entrada.email() == null || entrada.email().isBlank()
                || entrada.senha() == null || entrada.senha().isBlank()) {
            throw new CredenciaisInvalidasException("E-mail e senha são obrigatórios.");
        }

        String emailSanitizado = entrada.email().trim().toLowerCase();
        Optional<Colaborador> usuarioOpt = usuarioRepositorioPorta.buscarPorEmail(emailSanitizado);

        if (usuarioOpt.isEmpty()) {
            throw new CredenciaisInvalidasException("Credenciais inválidas.");
        }

        Colaborador usuario = usuarioOpt.get();

        if (usuario.getPerfil() == Perfil.COLABORADOR) {
            throw new CredenciaisInvalidasException("Colaborador não possui permissão de login no sistema.");
        }

        if (!usuario.isAtivo()) {
            throw new CredenciaisInvalidasException("Usuário inativo.");
        }

        String senhaCodificada;
        if (usuario instanceof GestorDeSeguranca gestor) {
            senhaCodificada = gestor.getSenha();
        } else if (usuario instanceof Supervisor supervisor) {
            senhaCodificada = supervisor.getSenha();
        } else {
            throw new CredenciaisInvalidasException("Perfil de usuário não suporta autenticação.");
        }

        if (!codificadorSenhaPorta.validar(entrada.senha(), senhaCodificada)) {
            throw new CredenciaisInvalidasException("Credenciais inválidas.");
        }

        String token = tokenPorta.gerarToken(usuario.getEmail(), usuario.getPerfil().name());
        return new TokenSaidaDTO(token, "Bearer", usuario.getEmail(), usuario.getNome(), usuario.getPerfil().name());
    }
}

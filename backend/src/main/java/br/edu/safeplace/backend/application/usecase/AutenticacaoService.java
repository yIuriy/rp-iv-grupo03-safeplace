package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.LoginInputDTO;
import br.edu.safeplace.backend.application.dto.output.TokenOutputDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioUseCase;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.GerenciadorToken;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.CredenciaisInvalidasException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AutenticacaoService implements AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorSenha codificadorSenha;
    private final GerenciadorToken gerenciadorToken;

    public AutenticacaoService(UsuarioRepository usuarioRepository,
                                 CodificadorSenha codificadorSenha,
                                 GerenciadorToken gerenciadorToken) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorSenha = codificadorSenha;
        this.gerenciadorToken = gerenciadorToken;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenOutputDTO autenticar(LoginInputDTO entrada) {
        if (entrada == null || entrada.email() == null || entrada.email().isBlank()
                || entrada.senha() == null || entrada.senha().isBlank()) {
            throw new CredenciaisInvalidasException("E-mail e senha são obrigatórios.");
        }

        String emailSanitizado = entrada.email().trim().toLowerCase();
        Optional<Colaborador> usuarioOpt = usuarioRepository.buscarPorEmail(emailSanitizado);

        if (usuarioOpt.isEmpty()) {
            throw new CredenciaisInvalidasException("Credenciais inválidas.");
        }

        Colaborador usuario = usuarioOpt.get();

        if (usuario.getPerfil() == null) {
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

        if (!codificadorSenha.validar(entrada.senha(), senhaCodificada)) {
            throw new CredenciaisInvalidasException("Credenciais inválidas.");
        }

        String token = gerenciadorToken.gerar(usuario.getEmail(), usuario.getPerfil().name());
        return new TokenOutputDTO(token, "Bearer", usuario.getEmail(), usuario.getNome(), usuario.getPerfil().name());
    }
}

package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.GeradorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.CpfValidador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioCasoDeUso implements GerenciarUsuarioCasoDeUso {
    private final UsuarioRepositorioPorta repositorioPorta;
    private final CodificadorSenhaPorta codificadorSenhaPorta;
    private final GeradorSenhaPorta geradorSenhaPorta;

    public UsuarioCasoDeUso(UsuarioRepositorioPorta repositorioPorta,
                            CodificadorSenhaPorta codificadorSenhaPorta,
                            GeradorSenhaPorta geradorSenhaPorta) {
        this.repositorioPorta = repositorioPorta;
        this.codificadorSenhaPorta = codificadorSenhaPorta;
        this.geradorSenhaPorta = geradorSenhaPorta;
    }

    @Override
    @Transactional
    public UsuarioSaidaDTO cadastrarUsuario(CadastrarUsuarioEntradaDTO entrada) {
        if (entrada.perfil() == null) {
            throw new IllegalArgumentException("Perfil de usuário é obrigatório.");
        }

        CpfValidador.validar(entrada.cpf());
        String cpfSanitizado = CpfValidador.sanitizar(entrada.cpf());

        if (repositorioPorta.buscarPorCpf(cpfSanitizado).isPresent()) {
            throw new CpfJaCadastradoException("CPF já cadastrado no sistema: " + cpfSanitizado);
        }

        if (entrada.email() != null && !entrada.email().isBlank()) {
            String emailSanitizado = entrada.email().trim().toLowerCase();
            if (repositorioPorta.buscarPorEmail(emailSanitizado).isPresent()) {
                throw new EmailJaCadastradoException("Email já cadastrado no sistema: " + emailSanitizado);
            }
        }

        Colaborador novoUsuario;
        String senhaInicial = null;
        if (entrada.perfil() == Perfil.SUPERVISOR) {
            // RF23: a senha inicial do Supervisor é gerada pelo sistema; a informada na requisição é ignorada.
            senhaInicial = geradorSenhaPorta.gerar();
            String hash = codificadorSenhaPorta.codificar(senhaInicial);
            novoUsuario = Supervisor.novo(entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email(), hash);
        } else if (entrada.perfil() == Perfil.GESTOR_SEGURANCA) {
            String hash = codificadorSenhaPorta.codificar(entrada.senha());
            novoUsuario = GestorDeSeguranca.novo(entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email(), hash);
        } else {
            novoUsuario = Colaborador.novo(entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email());
        }

        Colaborador salvo = repositorioPorta.salvar(novoUsuario);
        return UsuarioSaidaDTO.deDominio(salvo).comSenhaInicial(senhaInicial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioSaidaDTO> listarUsuarios() {
        return repositorioPorta.listarTodos()
                .stream()
                .map(UsuarioSaidaDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioSaidaDTO buscarPorId(Integer id) {
        return repositorioPorta.buscarPorId(id)
                .map(UsuarioSaidaDTO::deDominio)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + id));
    }
}

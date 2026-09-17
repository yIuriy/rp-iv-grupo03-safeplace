package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroColaboradorDTO;
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
                            CodificadorSenhaPorta codificadorSenhaPorta) {
        this(repositorioPorta, codificadorSenhaPorta, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
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

        String cpfSanitizado = validarIdentificacaoDisponivel(entrada.cpf(), entrada.email());

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
    @Transactional
    public UsuarioSaidaDTO cadastrarSupervisor(CadastrarSupervisorInputDTO entrada) {
        String cpfSanitizado = validarIdentificacaoDisponivel(entrada.cpf(), entrada.email());

        // RF23 / issue #91: a credencial inicial é gerada pelo sistema e só o hash é persistido.
        String senhaInicial = geradorSenhaPorta.gerar();
        String hash = codificadorSenhaPorta.codificar(senhaInicial);

        Supervisor novoSupervisor = Supervisor.novo(
                entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email(), hash);

        Colaborador salvo = repositorioPorta.salvar(novoSupervisor);
        return UsuarioSaidaDTO.deDominio(salvo).comSenhaInicial(senhaInicial);
    }

    @Override
    @Transactional
    public UsuarioSaidaDTO cadastrarColaborador(CadastrarColaboradorInputDTO entrada) {
        String cpfSanitizado = validarIdentificacaoDisponivel(entrada.cpf(), entrada.email());

        Colaborador novoColaborador = Colaborador.novo(
                entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email());

        // Sem senha inicial: o Colaborador não possui conta de acesso.
        return UsuarioSaidaDTO.deDominio(repositorioPorta.salvar(novoColaborador));
    }

    @Override
    @Transactional
    public UsuarioSaidaDTO atualizarSupervisor(Integer id, AtualizarPessoaInputDTO entrada) {
        return atualizarCadastro(id, Perfil.SUPERVISOR, entrada);
    }

    @Override
    @Transactional
    public UsuarioSaidaDTO atualizarColaborador(Integer id, AtualizarPessoaInputDTO entrada) {
        return atualizarCadastro(id, Perfil.COLABORADOR, entrada);
    }

    /**
     * Issue #122: atualiza no lugar, preservando id e CPF para que os vínculos existentes continuem
     * apontando para a mesma pessoa. O perfil vem da rota e é conferido antes de alterar, então um
     * Colaborador nunca vira Supervisor por esta operação. A autoria da alteração depende do módulo
     * de auditoria (#121); aqui só a data de atualização é registrada.
     */
    private UsuarioSaidaDTO atualizarCadastro(Integer id, Perfil perfilEsperado, AtualizarPessoaInputDTO entrada) {
        Colaborador atual = repositorioPorta.buscarPorId(id)
                .filter(pessoa -> pessoa.getPerfil() == perfilEsperado)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(
                        nomeDoPerfil(perfilEsperado) + " não encontrado com ID: " + id));

        validarEmailDisponivelPara(atual, entrada.email());

        Colaborador atualizado = atual.comDadosAtualizados(entrada.nome(), entrada.dataNascimento(), entrada.email());
        return UsuarioSaidaDTO.deDominio(repositorioPorta.salvar(atualizado));
    }

    private static String nomeDoPerfil(Perfil perfil) {
        return perfil == Perfil.SUPERVISOR ? "Supervisor" : "Colaborador";
    }

    /** O e-mail continua único, mas a própria pessoa pode manter o que já tem. */
    private void validarEmailDisponivelPara(Colaborador atual, String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        String emailSanitizado = email.trim().toLowerCase();
        repositorioPorta.buscarPorEmail(emailSanitizado)
                .filter(outro -> !outro.getId().equals(atual.getId()))
                .ifPresent(outro -> {
                    throw new EmailJaCadastradoException("Email já cadastrado no sistema: " + emailSanitizado);
                });
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
    public List<UsuarioSaidaDTO> listarColaboradores(FiltroColaboradorDTO filtro) {
        FiltroColaboradorDTO filtroAplicado = filtro != null ? filtro : FiltroColaboradorDTO.SEM_FILTRO;
        return repositorioPorta.listarColaboradores(filtroAplicado.nome(), filtroAplicado.cpf())
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

    @Override
    @Transactional(readOnly = true)
    public UsuarioSaidaDTO buscarPorCpf(String cpf) {
        CpfValidador.validar(cpf);
        String cpfSanitizado = CpfValidador.sanitizar(cpf);
        return repositorioPorta.buscarPorCpf(cpfSanitizado)
                .map(UsuarioSaidaDTO::deDominio)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com CPF: " + cpfSanitizado));
    }

    /**
     * Valida o CPF e garante que nem ele nem o e-mail já estejam em uso.
     *
     * @return o CPF sanitizado, pronto para persistência
     */
    private String validarIdentificacaoDisponivel(String cpf, String email) {
        CpfValidador.validar(cpf);
        String cpfSanitizado = CpfValidador.sanitizar(cpf);

        if (repositorioPorta.buscarPorCpf(cpfSanitizado).isPresent()) {
            throw new CpfJaCadastradoException("CPF já cadastrado no sistema: " + cpfSanitizado);
        }

        if (email != null && !email.isBlank()) {
            String emailSanitizado = email.trim().toLowerCase();
            if (repositorioPorta.buscarPorEmail(emailSanitizado).isPresent()) {
                throw new EmailJaCadastradoException("Email já cadastrado no sistema: " + emailSanitizado);
            }
        }

        return cpfSanitizado;
    }
}

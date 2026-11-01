/*
 * Caso de uso do módulo de usuários: implementa GerenciarUsuarioUseCase. Garante CPF e e-mail
 * únicos, gera e codifica a senha inicial do Supervisor (issue #91) e faz a atualização cadastral
 * por papel (issue #122), usando as portas de saída de repositório, codificador e gerador de
 * senha.
 */

package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioInputDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroColaboradorDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioUseCase;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.GeradorSenha;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Cpf;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.usuario.ColaboradorValidador;
import br.edu.safeplace.backend.validacao.usuario.CpfValidador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService implements GerenciarUsuarioUseCase {
    private final UsuarioRepository repositorioPorta;
    private final CodificadorSenha codificadorSenha;
    private final GeradorSenha geradorSenha;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final OperadorAutenticado operadorAutenticado;
    private final Validador<Colaborador> validador;
    private final Validador<String> cpfValidador;

    /** Monta os validadores padrão; usado pelos testes sem contexto Spring. */
    public UsuarioService(UsuarioRepository repositorioPorta,
                            CodificadorSenha codificadorSenha,
                            GeradorSenha geradorSenha,
                            LogAuditoriaRepository logAuditoriaRepository,
                            OperadorAutenticado operadorAutenticado) {
        this(repositorioPorta, codificadorSenha, geradorSenha, logAuditoriaRepository, operadorAutenticado,
                new ColaboradorValidador(repositorioPorta, new CpfValidador()), new CpfValidador());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public UsuarioService(UsuarioRepository repositorioPorta,
                            CodificadorSenha codificadorSenha,
                            GeradorSenha geradorSenha,
                            LogAuditoriaRepository logAuditoriaRepository,
                            OperadorAutenticado operadorAutenticado,
                            Validador<Colaborador> validador,
                            @org.springframework.beans.factory.annotation.Qualifier("cpfValidador") Validador<String> cpfValidador) {
        this.repositorioPorta = repositorioPorta;
        this.validador = java.util.Objects.requireNonNull(validador, "Validador de pessoas é obrigatório.");
        this.cpfValidador = java.util.Objects.requireNonNull(cpfValidador, "Validador de CPF é obrigatório.");
        this.codificadorSenha = codificadorSenha;
        this.geradorSenha = geradorSenha;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
        // Null Object (#241): sem sessão, quem monta o serviço passa OperadorAutenticadoNulo, não null.
        this.operadorAutenticado = java.util.Objects.requireNonNull(
                operadorAutenticado, "Porta do operador autenticado é obrigatória.");
    }

    @Override
    @Transactional
    public UsuarioOutputDTO cadastrarUsuario(CadastrarUsuarioInputDTO entrada) {
        return cadastrarUsuario(entrada, null);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO cadastrarUsuario(CadastrarUsuarioInputDTO entrada, String operador) {
        String cpfSanitizado = Cpf.sanitizar(entrada.cpf());

        Colaborador novoUsuario;
        String senhaInicial = null;
        if (entrada.perfil() == Perfil.SUPERVISOR) {
            // RF23: a senha inicial do Supervisor é gerada pelo sistema; a informada na requisição é ignorada.
            senhaInicial = geradorSenha.gerar();
            String hash = codificadorSenha.codificar(senhaInicial);
            novoUsuario = Supervisor.novo(entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email(), hash);
        } else if (entrada.perfil() == Perfil.GESTOR_SEGURANCA) {
            String hash = codificadorSenha.codificar(entrada.senha());
            novoUsuario = GestorDeSeguranca.novo(entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email(), hash);
        } else {
            novoUsuario = Colaborador.novo(entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email());
        }

        // Chain of Responsibility (#263): CPF, e-mail, dados pessoais e credenciais, antes do repository.
        validador.validar(novoUsuario);
        Colaborador salvo = repositorioPorta.salvar(novoUsuario);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                resolverOperador(operador),
                salvo.getPerfil() != null ? salvo.getPerfil().name() : "Usuario",
                salvo.getId(),
                "Cadastro de " + nomeDoPerfil(salvo.getPerfil())
        );
        logAuditoriaRepository.salvar(log);

        return UsuarioOutputDTO.deDominio(salvo).comSenhaInicial(senhaInicial);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO cadastrarSupervisor(CadastrarSupervisorInputDTO entrada) {
        return cadastrarSupervisor(entrada, null);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO cadastrarSupervisor(CadastrarSupervisorInputDTO entrada, String operador) {
        String cpfSanitizado = Cpf.sanitizar(entrada.cpf());

        // RF23 / issue #91: a credencial inicial é gerada pelo sistema e só o hash é persistido.
        String senhaInicial = geradorSenha.gerar();
        String hash = codificadorSenha.codificar(senhaInicial);

        Supervisor novoSupervisor = Supervisor.novo(
                entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email(), hash);

        validador.validar(novoSupervisor);
        Colaborador salvo = repositorioPorta.salvar(novoSupervisor);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                resolverOperador(operador),
                "Supervisor",
                salvo.getId(),
                "Cadastro de Supervisor"
        );
        logAuditoriaRepository.salvar(log);

        return UsuarioOutputDTO.deDominio(salvo).comSenhaInicial(senhaInicial);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO cadastrarColaborador(CadastrarColaboradorInputDTO entrada) {
        return cadastrarColaborador(entrada, null);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO cadastrarColaborador(CadastrarColaboradorInputDTO entrada, String operador) {
        String cpfSanitizado = Cpf.sanitizar(entrada.cpf());

        Colaborador novoColaborador = Colaborador.novo(
                entrada.nome(), cpfSanitizado, entrada.dataNascimento(), entrada.email());

        validador.validar(novoColaborador);
        Colaborador salvo = repositorioPorta.salvar(novoColaborador);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                resolverOperador(operador),
                "Colaborador",
                salvo.getId(),
                "Cadastro de Colaborador"
        );
        logAuditoriaRepository.salvar(log);

        // Sem senha inicial: o Colaborador não possui conta de acesso.
        return UsuarioOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO atualizarSupervisor(Integer id, AtualizarPessoaInputDTO entrada) {
        return atualizarSupervisor(id, entrada, null);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO atualizarSupervisor(Integer id, AtualizarPessoaInputDTO entrada, String operador) {
        return atualizarCadastro(id, Perfil.SUPERVISOR, entrada, operador);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO atualizarColaborador(Integer id, AtualizarPessoaInputDTO entrada) {
        return atualizarColaborador(id, entrada, null);
    }

    @Override
    @Transactional
    public UsuarioOutputDTO atualizarColaborador(Integer id, AtualizarPessoaInputDTO entrada, String operador) {
        return atualizarCadastro(id, null, entrada, operador);
    }

    /**
     * Issue #122: atualiza no lugar, preservando id e CPF para que os vínculos existentes continuem
     * apontando para a mesma pessoa. O perfil vem da rota e é conferido antes de alterar, então um
     * Colaborador nunca vira Supervisor por esta operação.
     */
    private UsuarioOutputDTO atualizarCadastro(Integer id, Perfil perfilEsperado, AtualizarPessoaInputDTO entrada, String operador) {
        Colaborador atual = repositorioPorta.buscarPorId(id)
                .filter(pessoa -> pessoa.getPerfil() == perfilEsperado)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(
                        nomeDoPerfil(perfilEsperado) + " não encontrado com ID: " + id));

        Colaborador atualizado = atual.comDadosAtualizados(entrada.nome(), entrada.dataNascimento(), entrada.email());
        validador.validar(atualizado);
        Colaborador salvo = repositorioPorta.salvar(atualizado);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                resolverOperador(operador),
                nomeDoPerfil(perfilEsperado),
                salvo.getId(),
                "Atualização cadastral de " + nomeDoPerfil(perfilEsperado) + ": " + descreverAlteracoes(atual, salvo)
        );
        logAuditoriaRepository.salvar(log);

        return UsuarioOutputDTO.deDominio(salvo);
    }

    /**
     * RNF05 exige o dado alterado: cada campo mudado com valor anterior e novo, no formato
     * "de ... para ..." já usado no histórico de áreas e tarefas. Senha nunca entra (RNF05, item 3).
     */
    private static String descreverAlteracoes(Colaborador anterior, Colaborador atual) {
        List<String> mudancas = new java.util.ArrayList<>();
        adicionarMudanca(mudancas, "nome", anterior.getNome(), atual.getNome());
        adicionarMudanca(mudancas, "data de nascimento", anterior.getDataNascimento(), atual.getDataNascimento());
        adicionarMudanca(mudancas, "e-mail", anterior.getEmail(), atual.getEmail());
        return mudancas.isEmpty() ? "sem mudança de dados." : String.join("; ", mudancas) + ".";
    }

    private static void adicionarMudanca(List<String> mudancas, String campo, Object anterior, Object atual) {
        if (!java.util.Objects.equals(anterior, atual)) {
            mudancas.add(campo + " de " + (anterior != null ? anterior : "não informado")
                    + " para " + (atual != null ? atual : "não informado"));
        }
    }

    private String resolverOperador(String operador) {
        if (operador != null && !operador.isBlank()) {
            return operador.trim();
        }
        return operadorAutenticado.obterIdentificadorOperador()
                .orElse("SISTEMA");
    }

    private static String nomeDoPerfil(Perfil perfil) {
        if (perfil == Perfil.GESTOR_SEGURANCA) {
            return "Gestor de Segurança";
        }
        if (perfil == Perfil.SUPERVISOR) {
            return "Supervisor";
        }
        return "Colaborador";
    }
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioOutputDTO> listarUsuarios() {
        return repositorioPorta.listarTodos()
                .stream()
                .map(UsuarioOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioOutputDTO> listarColaboradores(FiltroColaboradorDTO filtro) {
        FiltroColaboradorDTO filtroAplicado = filtro != null ? filtro : FiltroColaboradorDTO.SEM_FILTRO;
        return repositorioPorta.listarColaboradores(filtroAplicado.nome(), filtroAplicado.cpf())
                .stream()
                .map(UsuarioOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioOutputDTO buscarPorId(Integer id) {
        Colaborador usuario = repositorioPorta.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com ID: " + id));

        // RF23: Supervisor só pode consultar colaboradores; tentar consultar supervisor ou gestor lança AcessoNegadoException (403)
        if (!operadorAutenticado.possuiPapel("ROLE_GESTOR_SEGURANCA")
                && operadorAutenticado.possuiPapel("ROLE_SUPERVISOR")
                && usuario.getPerfil() != null) {
            throw new AcessoNegadoException("Supervisor não possui permissão para consultar dados de " + nomeDoPerfil(usuario.getPerfil()) + ".");
        }

        return UsuarioOutputDTO.deDominio(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioOutputDTO buscarPorCpf(String cpf) {
        cpfValidador.validar(cpf);
        String cpfSanitizado = Cpf.sanitizar(cpf);
        return repositorioPorta.buscarPorCpf(cpfSanitizado)
                .map(UsuarioOutputDTO::deDominio)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com CPF: " + cpfSanitizado));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioOutputDTO buscarPorEmail(String email) {
        String emailSanitizado = email == null ? "" : email.trim().toLowerCase();
        return repositorioPorta.buscarPorEmail(emailSanitizado)
                .map(UsuarioOutputDTO::deDominio)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com e-mail: " + emailSanitizado));
    }
}

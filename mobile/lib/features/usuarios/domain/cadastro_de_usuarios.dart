import '../data/usuario.dart';

/// Os dois cadastros do módulo (RF23). O Gestor cadastra os dois; o Supervisor, só colaboradores.
enum TipoCadastro { supervisor, colaborador }

/// Dados do formulário de pessoa, já validados. [cpf] só com os 11 dígitos; [email] nulo quando não
/// foi informado (o do Colaborador é opcional).
class DadosPessoa {
  const DadosPessoa({
    required this.nome,
    required this.cpf,
    required this.dataNascimento,
    this.email,
  });

  final String nome;
  final String cpf;
  final DateTime dataNascimento;
  final String? email;
}

/// Escrita de supervisores e colaboradores (`POST/PUT /api/usuarios/...`). As leituras ficam nos
/// providers com cache offline (`data/usuarios_api.dart`); escrita precisa do servidor.
///
/// Lança a falha da API para a tela escolher a mensagem e o campo do erro.
abstract interface class CadastroDeUsuarios {
  /// Só o Gestor. A resposta traz a senha inicial, que a tela mostra uma única vez.
  Future<UsuarioCriado> cadastrarSupervisor(DadosPessoa dados);

  /// Gestor e Supervisor. Sem senha nem perfil: o Colaborador não tem conta.
  Future<Usuario> cadastrarColaborador(DadosPessoa dados);

  /// Só o Gestor. Envia apenas nome, data de nascimento e e-mail: CPF, perfil e senha não mudam.
  Future<Usuario> atualizarSupervisor(int id, DadosPessoa dados);

  /// Gestor e Supervisor. Envia apenas nome, data de nascimento e e-mail; o CPF não muda.
  Future<Usuario> atualizarColaborador(int id, DadosPessoa dados);
}

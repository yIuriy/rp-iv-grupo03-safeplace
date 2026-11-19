import '../../../core/api/leitura.dart';
import '../../../core/sessao/sessao.dart';

/// Pessoa cadastrada (`UsuarioResponse` de `/api/usuarios`): Gestor de Segurança, Supervisor ou
/// Colaborador. O Colaborador vem com `perfil` nulo, porque não tem conta de acesso (RF23).
///
/// Leitura defensiva, como o `lerUsuario` do web, mas sem derrubar a tela: campo ausente ou de
/// outro tipo vira `null` (ou a reserva do nome), e a tela mostra "Não informado". Sem [id] o
/// cadastro aparece, mas não pode ser editado nem escolhido em um seletor.
class Usuario {
  const Usuario({
    this.id,
    required this.nome,
    this.cpf,
    this.dataNascimento,
    this.email,
    this.perfil,
    this.ativo = true,
    this.criadoEm,
  });

  final int? id;
  final String nome;

  /// Só os 11 dígitos, como o backend devolve.
  final String? cpf;
  final DateTime? dataNascimento;
  final String? email;
  final Perfil? perfil;
  final bool ativo;

  /// Data e hora do cadastro; o perfil próprio mostra.
  final DateTime? criadoEm;

  bool get ehSupervisor => perfil == Perfil.supervisor;

  factory Usuario.ler(Object? dado) {
    final b = objeto(dado, 'Registro de usuário inválido');
    return Usuario(
      id: numeroOuNulo(b['id'])?.toInt(),
      nome: texto(b['nome'], 'Nome não informado'),
      cpf: textoOuNulo(b['cpf']),
      dataNascimento: dataOuNula(b['dataNascimento']),
      email: textoOuNulo(b['email']),
      perfil: Perfil.ler(b['perfil']),
      // Como no web: só `false` explícito é inativo.
      ativo: b['ativo'] != false,
      criadoEm: dataOuNula(b['criadoEm']),
    );
  }

  /// O mesmo cadastro: pelo [id] quando há, senão por todos os campos. O `SeletorComBusca` usa
  /// isto para marcar a opção escolhida mesmo depois de a lista ser recarregada.
  @override
  bool operator ==(Object other) =>
      other is Usuario &&
      (id != null
          ? other.id == id
          : other.id == null &&
                other.nome == nome &&
                other.cpf == cpf &&
                other.email == email);

  @override
  int get hashCode => id != null ? id.hashCode : Object.hash(nome, cpf, email);

  @override
  String toString() => 'Usuario($id, $nome)';
}

/// Resposta do cadastro. [senhaInicial] só vem no cadastro de Supervisor (issue #91): a tela a
/// mostra uma única vez e ela nunca vai para listas nem para o cache offline.
class UsuarioCriado {
  const UsuarioCriado(this.usuario, {this.senhaInicial});

  final Usuario usuario;
  final String? senhaInicial;

  factory UsuarioCriado.ler(Object? dado) {
    final b = objeto(dado, 'Registro de usuário inválido');
    return UsuarioCriado(
      Usuario.ler(b),
      senhaInicial: textoOuNulo(b['senhaInicial']),
    );
  }
}

/// Lista de `GET /api/usuarios` ou `/api/usuarios/colaboradores`.
List<Usuario> lerUsuarios(Object? dado) =>
    lista(dado, Usuario.ler, 'Lista de usuários inválida');

/// `GET /api/usuarios` devolve gestores, supervisores e colaboradores; a aba do Gestor mostra só os
/// supervisores, como o web.
List<Usuario> lerSupervisores(Object? dado) => [
  for (final usuario in lerUsuarios(dado))
    if (usuario.ehSupervisor) usuario,
];

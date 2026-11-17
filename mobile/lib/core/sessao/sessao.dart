import '../api/leitura.dart';

/// Perfis que fazem login. O Colaborador não tem conta no sistema.
enum Perfil {
  gestorSeguranca('GESTOR_SEGURANCA', 'Gestor de Segurança'),
  supervisor('SUPERVISOR', 'Supervisor');

  const Perfil(this.codigo, this.rotulo);

  /// Valor usado pela API.
  final String codigo;
  final String rotulo;

  static Perfil? ler(Object? valor) {
    for (final perfil in values) {
      if (perfil.codigo == valor) return perfil;
    }
    return null;
  }
}

class Sessao {
  const Sessao({
    required this.token,
    required this.email,
    required this.nome,
    required this.perfil,
  });

  final String token;
  final String email;
  final String nome;
  final Perfil perfil;

  bool get ehGestor => perfil == Perfil.gestorSeguranca;

  /// Aceita a resposta de `POST /api/auth/login` ou o JSON guardado no aparelho.
  factory Sessao.ler(Object? dado) {
    final bruto = objeto(dado, 'Sessão inválida');
    final token = textoOuNulo(bruto['token']);
    final email = bruto['email'];
    final nome = bruto['nome'];
    final perfil = Perfil.ler(bruto['perfil']);
    if (token == null ||
        email is! String ||
        nome is! String ||
        perfil == null) {
      throw const FormatException('Sessão inválida');
    }
    return Sessao(token: token, email: email, nome: nome, perfil: perfil);
  }

  Map<String, Object?> toJson() => {
    'token': token,
    'email': email,
    'nome': nome,
    'perfil': perfil.codigo,
  };
}

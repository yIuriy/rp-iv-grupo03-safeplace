import '../../../core/sessao/sessao.dart';

/// Troca e-mail e senha por uma sessão. Lança a falha para a tela escolher a mensagem.
abstract interface class Autenticador {
  Future<Sessao> autenticar(String email, String senha);
}

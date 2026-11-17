import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/sessao/sessao.dart';

void main() {
  group('Sessao.ler', () {
    test('lê a resposta do login, ignorando campos extras', () {
      final sessao = Sessao.ler({
        'token': 'abc',
        'tipo': 'Bearer',
        'email': 'sup@safeplace.com',
        'nome': 'Sup',
        'perfil': 'SUPERVISOR',
      });
      expect(sessao.token, 'abc');
      expect(sessao.perfil, Perfil.supervisor);
      expect(sessao.ehGestor, isFalse);
    });

    test('ida e volta pelo JSON guardado', () {
      final original = Sessao.ler({
        'token': 't',
        'email': 'g@s.com',
        'nome': 'G',
        'perfil': 'GESTOR_SEGURANCA',
      });
      final lida = Sessao.ler(original.toJson());
      expect(lida.toJson(), original.toJson());
    });

    for (final (caso, dado) in [
      ('não é objeto', 'texto'),
      (
        'token em branco',
        {'token': ' ', 'email': 'e', 'nome': 'n', 'perfil': 'SUPERVISOR'},
      ),
      ('sem e-mail', {'token': 't', 'nome': 'n', 'perfil': 'SUPERVISOR'}),
      (
        'perfil sem login',
        {'token': 't', 'email': 'e', 'nome': 'n', 'perfil': 'COLABORADOR'},
      ),
    ]) {
      test(
        'recusa $caso',
        () => expect(() => Sessao.ler(dado), throwsFormatException),
      );
    }
  });
}

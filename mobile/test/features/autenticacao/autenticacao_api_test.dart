import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/autenticacao/data/autenticacao_api.dart';

class _ClienteFalso extends Mock implements ApiClient {}

void main() {
  test('envia e-mail e senha ao login e lê a sessão', () async {
    final cliente = _ClienteFalso();
    when(
      () => cliente.post(
        caminhoDoLogin,
        corpo: {'email': 'g@s.com', 'senha': '123'},
      ),
    ).thenAnswer(
      (_) async => {
        'token': 't',
        'tipo': 'Bearer',
        'email': 'g@s.com',
        'nome': 'G',
        'perfil': 'GESTOR_SEGURANCA',
      },
    );

    final sessao = await AutenticacaoApi(cliente).autenticar('g@s.com', '123');

    expect(sessao.token, 't');
    expect(sessao.ehGestor, isTrue);
  });
}

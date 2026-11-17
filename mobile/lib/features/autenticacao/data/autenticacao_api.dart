import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/api/api_client_provider.dart';
import '../../../core/sessao/sessao.dart';
import '../domain/autenticador.dart';

class AutenticacaoApi implements Autenticador {
  const AutenticacaoApi(this._client);

  final ApiClient _client;

  /// `POST /api/auth/login`: aceita só e-mail e senha e devolve token, nome e perfil.
  @override
  Future<Sessao> autenticar(String email, String senha) async {
    final dados = await _client.post(
      caminhoDoLogin,
      corpo: {'email': email, 'senha': senha},
    );
    return Sessao.ler(dados);
  }
}

final autenticadorProvider = Provider<Autenticador>(
  (ref) => AutenticacaoApi(ref.read(apiClientProvider)),
);

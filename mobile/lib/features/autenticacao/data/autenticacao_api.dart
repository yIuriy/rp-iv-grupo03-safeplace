import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/sessao/sessao.dart';

class AutenticacaoApi {
  const AutenticacaoApi(this._client);

  final Dio _client;

  /// `POST /api/auth/login`: aceita só e-mail e senha e devolve token, nome e perfil.
  Future<Sessao> autenticar(String email, String senha) async {
    final resposta = await _client.post<Object?>(
      caminhoDoLogin,
      data: {'email': email, 'senha': senha},
    );
    return Sessao.ler(resposta.data);
  }
}

final autenticacaoApiProvider = Provider<AutenticacaoApi>(
  (ref) => AutenticacaoApi(ref.read(apiClientProvider)),
);

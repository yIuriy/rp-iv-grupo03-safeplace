// Cliente HTTP compartilhado por todas as features. As features recebem o `Dio` pelo
// `apiClientProvider` e chamam caminhos relativos a `/api` (ex.: `client.get('/epis')`).

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../config.dart';
import '../sessao/controlador_sessao.dart';

const caminhoDoLogin = '/auth/login';

/// Monta o cliente com o token da sessão em toda chamada e o aviso de 401.
///
/// [tokenAtual] é lido a cada requisição, assim o token vale logo após o login.
/// [aoExpirar] roda quando um 401 fora do login indica token expirado ou revogado: o backend não
/// tem refresh token (JWT de 7 dias), então a única saída é entrar de novo.
Dio criarApiClient({
  required String? Function() tokenAtual,
  required void Function() aoExpirar,
  String baseUrl = urlDaApi,
  HttpClientAdapter? adaptador,
}) {
  final dio = Dio(
    BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      sendTimeout: const Duration(seconds: 10),
      contentType: Headers.jsonContentType,
    ),
  );
  if (adaptador != null) dio.httpClientAdapter = adaptador;
  dio.interceptors.add(
    InterceptorDeSessao(tokenAtual: tokenAtual, aoExpirar: aoExpirar),
  );
  return dio;
}

class InterceptorDeSessao extends Interceptor {
  InterceptorDeSessao({required this.tokenAtual, required this.aoExpirar});

  final String? Function() tokenAtual;
  final void Function() aoExpirar;

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final token = tokenAtual();
    if (token != null && !options.headers.containsKey('Authorization')) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    final ehLogin = err.requestOptions.path.endsWith(caminhoDoLogin);
    if (err.response?.statusCode == 401 && !ehLogin && tokenAtual() != null) {
      aoExpirar();
    }
    handler.next(err);
  }
}

final apiClientProvider = Provider<Dio>((ref) {
  return criarApiClient(
    tokenAtual: () => ref.read(controladorSessaoProvider).value?.sessao?.token,
    aoExpirar: () => ref.read(controladorSessaoProvider.notifier).expirar(),
  );
});

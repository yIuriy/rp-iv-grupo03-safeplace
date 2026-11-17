// Implementação do ApiClient com dio. É o único arquivo do app que conhece o dio.

import 'package:dio/dio.dart';

import '../config.dart';
import 'api_client.dart';

const tempoLimite = Duration(seconds: 10);

class DioApiClient implements ApiClient {
  /// [tokenAtual] é lido a cada requisição, assim o token vale logo após o login.
  /// [aoExpirar] roda quando um 401 fora do login indica token expirado ou revogado: o backend não
  /// tem refresh token (JWT de 7 dias), então a única saída é entrar de novo.
  DioApiClient({
    required String? Function() tokenAtual,
    required void Function() aoExpirar,
    String baseUrl = urlDaApi,
    HttpClientAdapter? adaptador,
  }) : _dio = Dio(
         BaseOptions(
           baseUrl: baseUrl,
           connectTimeout: tempoLimite,
           receiveTimeout: tempoLimite,
           sendTimeout: tempoLimite,
           contentType: Headers.jsonContentType,
         ),
       ) {
    if (adaptador != null) _dio.httpClientAdapter = adaptador;
    _dio.interceptors.add(
      InterceptorDeSessao(tokenAtual: tokenAtual, aoExpirar: aoExpirar),
    );
  }

  final Dio _dio;

  @override
  Future<Object?> get(String caminho, {Map<String, Object?>? consulta}) =>
      _executar(() => _dio.get<Object?>(caminho, queryParameters: consulta));

  @override
  Future<Object?> post(String caminho, {Object? corpo}) =>
      _executar(() => _dio.post<Object?>(caminho, data: corpo));

  @override
  Future<Object?> put(String caminho, {Object? corpo}) =>
      _executar(() => _dio.put<Object?>(caminho, data: corpo));

  @override
  Future<Object?> patch(String caminho, {Object? corpo}) =>
      _executar(() => _dio.patch<Object?>(caminho, data: corpo));

  @override
  Future<Object?> delete(String caminho) =>
      _executar(() => _dio.delete<Object?>(caminho));

  Future<Object?> _executar(
    Future<Response<Object?>> Function() chamada,
  ) async {
    try {
      return (await chamada()).data;
    } on DioException catch (erro) {
      throw FalhaApi(
        status: erro.response?.statusCode,
        corpo: erro.response?.data,
        causa: erro,
      );
    }
  }
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

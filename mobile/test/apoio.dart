import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:safeplace/core/sessao/sessao.dart';

const sessaoGestor = Sessao(
  token: 'token-gestor',
  email: 'gestor.demo@safeplace.com',
  nome: 'Gestor Demo',
  perfil: Perfil.gestorSeguranca,
);

/// Responde as chamadas do Dio sem rede, guardando cada requisição recebida.
class AdaptadorFalso implements HttpClientAdapter {
  AdaptadorFalso(this.responder);

  final (int, Object?) Function(RequestOptions opcoes) responder;
  final List<RequestOptions> recebidas = [];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    recebidas.add(options);
    final (status, corpo) = responder(options);
    return ResponseBody.fromString(
      jsonEncode(corpo),
      status,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

DioException falhaHttp(int status, [Object? corpo]) {
  final opcoes = RequestOptions(path: '/qualquer');
  return DioException.badResponse(
    statusCode: status,
    requestOptions: opcoes,
    response: Response(requestOptions: opcoes, statusCode: status, data: corpo),
  );
}

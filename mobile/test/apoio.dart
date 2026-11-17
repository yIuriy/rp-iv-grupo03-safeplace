import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/core/sessao/sessao.dart';

const sessaoGestor = Sessao(
  token: 'token-gestor',
  email: 'gestor.demo@safeplace.com',
  nome: 'Gestor Demo',
  perfil: Perfil.gestorSeguranca,
);

/// Falha HTTP como o [ApiClient] a entrega às features.
FalhaApi falhaHttp(int status, [Object? corpo]) =>
    FalhaApi(status: status, corpo: corpo);

class ArmazenamentoSessaoEmMemoria implements ArmazenamentoSessao {
  ArmazenamentoSessaoEmMemoria([this.sessao]);

  Sessao? sessao;

  @override
  Future<Sessao?> ler() async => sessao;

  @override
  Future<void> guardar(Sessao sessao) async => this.sessao = sessao;

  @override
  Future<void> apagar() async => sessao = null;
}

/// Responde as chamadas do dio sem rede, guardando cada requisição recebida. Só para testar o
/// `DioApiClient`; as features usam um fake de [ApiClient].
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

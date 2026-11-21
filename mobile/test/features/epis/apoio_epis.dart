// Apoio dos testes de EPIs: API falsa que grava cada requisição e devolve o que [responder] mandar.

import 'package:safeplace/core/api/api_client.dart';

/// Uma chamada recebida pela [ApiEpisFalsa].
class Requisicao {
  const Requisicao(this.metodo, this.caminho, {this.consulta, this.corpo});

  final String metodo;
  final String caminho;
  final Map<String, Object?>? consulta;
  final Object? corpo;

  @override
  String toString() => '$metodo $caminho $consulta $corpo';
}

/// [responder] devolve o corpo de cada chamada (ou lança uma [FalhaApi]) e todas as requisições
/// ficam em [recebidas], para conferir caminho e corpo enviado.
class ApiEpisFalsa implements ApiClient {
  ApiEpisFalsa(this.responder);

  Object? Function(Requisicao requisicao) responder;
  final List<Requisicao> recebidas = [];

  Future<Object?> _responder(Requisicao requisicao) async {
    recebidas.add(requisicao);
    return responder(requisicao);
  }

  @override
  Future<Object?> get(String caminho, {Map<String, Object?>? consulta}) =>
      _responder(Requisicao('GET', caminho, consulta: consulta));

  @override
  Future<Object?> post(String caminho, {Object? corpo}) =>
      _responder(Requisicao('POST', caminho, corpo: corpo));

  @override
  Future<Object?> put(String caminho, {Object? corpo}) =>
      _responder(Requisicao('PUT', caminho, corpo: corpo));

  @override
  Future<Object?> patch(String caminho, {Object? corpo}) =>
      _responder(Requisicao('PATCH', caminho, corpo: corpo));

  @override
  Future<Object?> delete(String caminho) =>
      _responder(Requisicao('DELETE', caminho));
}

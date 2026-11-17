import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

/// Cache em memória, para os testes não dependerem do armazenamento seguro do aparelho.
class ArmazenamentoCacheEmMemoria implements ArmazenamentoCache {
  final Map<String, String> valores = {};

  @override
  Future<String?> ler(String chave) async => valores[chave];

  @override
  Future<void> gravar(String chave, String valor) async =>
      valores[chave] = valor;

  @override
  Future<void> apagarComPrefixo(String prefixo) async =>
      valores.removeWhere((chave, _) => chave.startsWith(prefixo));
}

/// API falsa: [responder] devolve o corpo de cada GET ou lança uma [FalhaApi]. Guarda os caminhos.
class ApiFalsa implements ApiClient {
  ApiFalsa(this.responder);

  Object? Function(String caminho) responder;
  final List<String> chamadas = [];

  @override
  Future<Object?> get(String caminho, {Map<String, Object?>? consulta}) async {
    chamadas.add(caminho);
    return responder(caminho);
  }

  @override
  Future<Object?> post(String caminho, {Object? corpo}) =>
      throw UnimplementedError();

  @override
  Future<Object?> put(String caminho, {Object? corpo}) =>
      throw UnimplementedError();

  @override
  Future<Object?> patch(String caminho, {Object? corpo}) =>
      throw UnimplementedError();

  @override
  Future<Object?> delete(String caminho) => throw UnimplementedError();
}

/// Servidor fora do ar: o cliente lança [FalhaApi] sem status.
Never semConexao(String _) => throw const FalhaApi();

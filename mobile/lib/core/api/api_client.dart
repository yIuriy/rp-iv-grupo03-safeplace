/// Rota pública de login. O interceptador não trata o 401 dela como sessão expirada.
const caminhoDoLogin = '/auth/login';

/// Porta HTTP das features. Caminhos são relativos a `/api` (ex.: `client.get('/epis')`).
///
/// Cada método devolve o corpo JSON ainda sem tipo, para o `X.ler` da feature, e lança
/// [FalhaApi] em qualquer falha. As features não conhecem a biblioteca HTTP usada por baixo.
abstract interface class ApiClient {
  Future<Object?> get(String caminho, {Map<String, Object?>? consulta});
  Future<Object?> post(String caminho, {Object? corpo});
  Future<Object?> put(String caminho, {Object? corpo});
  Future<Object?> patch(String caminho, {Object? corpo});
  Future<Object?> delete(String caminho);
}

/// Falha de uma chamada à API. [status] nulo indica que o servidor não respondeu (rede, timeout).
class FalhaApi implements Exception {
  const FalhaApi({this.status, this.corpo, this.causa});

  final int? status;

  /// Corpo da resposta de erro, normalmente `{timestamp, status, error, message}`. Alguns 409 e
  /// 422 trazem campos extras (duplicidade de empréstimo, motivos de bloqueio).
  final Object? corpo;

  /// Erro original, para log.
  final Object? causa;

  bool get semResposta => status == null;

  @override
  String toString() =>
      'FalhaApi(status: $status, corpo: $corpo, causa: $causa)';
}

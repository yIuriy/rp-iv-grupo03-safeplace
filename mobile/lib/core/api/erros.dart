// Leitura das falhas da API (corpo `{timestamp, status, error, message}` do `ApiExceptionHandler`)
// e escolha da mensagem para o usuário pelo padrão Strategy, como em `frontend/src/shared/api/erros.ts`.

import 'package:dio/dio.dart';

import 'leitura.dart';

/// Corpo de erro padrão do backend (`CorpoErro`). `message` pode vir nulo.
class CorpoErro {
  const CorpoErro({this.timestamp, this.status, this.error, this.message});

  final String? timestamp;
  final int? status;
  final String? error;
  final String? message;

  /// Lê o corpo sem falhar: corpo fora do contrato devolve `null`.
  static CorpoErro? ler(Object? dado) {
    if (dado is! Map<String, Object?>) return null;
    return CorpoErro(
      timestamp: textoOuNulo(dado['timestamp']),
      status: numeroOuNulo(dado['status'])?.toInt(),
      error: textoOuNulo(dado['error']),
      message: textoOuNulo(dado['message']),
    );
  }
}

/// Status HTTP da falha, ou `null` quando o servidor não respondeu.
int? statusDoErro(Object erro) =>
    erro is DioException ? erro.response?.statusCode : null;

/// Corpo de erro devolvido pela API, quando houver.
CorpoErro? corpoDoErro(Object erro) =>
    erro is DioException ? CorpoErro.ler(erro.response?.data) : null;

/// Mensagem devolvida pela API no campo `message`.
String? mensagemDaApi(Object erro) => corpoDoErro(erro)?.message;

/// Strategy: regra que transforma uma falha na mensagem exibida ao usuário.
typedef EstrategiaDeMensagem = String Function(Object erro);

/// Texto fixo, sem consultar o erro.
EstrategiaDeMensagem fixa(String mensagem) =>
    (_) => mensagem;

/// Usa a mensagem enviada pela API e, sem ela, a [alternativa].
EstrategiaDeMensagem usarMensagemDaApi(String alternativa) =>
    (erro) => mensagemDaApi(erro) ?? alternativa;

const mensagemSemConexao =
    'Não foi possível falar com o servidor. Verifique sua conexão e tente novamente.';

/// Mensagens padrão por status. Cada tela sobrescreve só o que precisa em [criarDescritorDeFalha].
/// 409 e 422 trazem a regra de negócio violada no `message`, por isso a API tem a preferência.
final Map<int, EstrategiaDeMensagem> estrategiasPadrao = {
  400: usarMensagemDaApi('Confira os dados informados.'),
  401: fixa('Sua sessão expirou. Entre novamente.'),
  403: fixa('Seu perfil não tem permissão para esta ação.'),
  404: usarMensagemDaApi('Registro não encontrado.'),
  409: usarMensagemDaApi('A operação conflita com um registro existente.'),
  422: usarMensagemDaApi('A operação foi bloqueada por uma regra do sistema.'),
};

/// Context do Strategy: escolhe a estratégia pelo status da falha. Falha sem resposta do servidor
/// (rede, timeout) usa [semConexao]; status sem estratégia própria usa [padrao].
String Function(Object erro) criarDescritorDeFalha({
  Map<int, EstrategiaDeMensagem> porStatus = const {},
  String padrao = 'Não foi possível concluir a operação. Tente novamente.',
  String semConexao = mensagemSemConexao,
}) {
  final estrategias = {...estrategiasPadrao, ...porStatus};
  return (erro) {
    final status = statusDoErro(erro);
    if (status == null) return erro is DioException ? semConexao : padrao;
    return (estrategias[status] ?? fixa(padrao))(erro);
  };
}

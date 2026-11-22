// Apoio dos testes de empréstimos: cliente HTTP falso e dados de exemplo.

import 'package:safeplace/core/api/api_client.dart';

/// Uma chamada HTTP recebida pela [ApiEmprestimosFalsa].
class Requisicao {
  const Requisicao(this.metodo, this.caminho, {this.consulta, this.corpo});

  final String metodo;
  final String caminho;
  final Map<String, Object?>? consulta;
  final Object? corpo;

  @override
  String toString() => '$metodo $caminho $consulta $corpo';
}

/// Cliente de API falso que armazena requisições e executa o handler [responder].
class ApiEmprestimosFalsa implements ApiClient {
  ApiEmprestimosFalsa(this.responder);

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

final emprestimoExemploJson = <String, Object?>{
  'id': 1,
  'epiId': 2,
  'epiNome': 'Luva de Vaqueta',
  'colaboradorId': 3,
  'colaboradorNome': 'João da Silva',
  'responsavelEntregaId': 1,
  'dataRetirada': '2026-11-20T10:00:00',
  'dataDevolucaoPrevista': '2026-11-25',
  'dataDevolucaoReal': null,
  'responsavelDevolucaoId': null,
  'estadoDevolucao': null,
  'status': 'INDISPONIVEL',
  'atrasado': false,
  'termoCautela': 'Termo de responsabilidade assinado.',
  'justificativaSubstituicao': null,
};

final devolucaoExemploJson = <String, Object?>{
  'emprestimoId': 1,
  'epiId': 2,
  'epiNome': 'Luva de Vaqueta',
  'colaboradorId': 3,
  'colaboradorNome': 'João da Silva',
  'estadoDevolucao': 'APTO',
  'destinoAprovado': 'ESTOQUE',
  'saldoPersistido': 20,
  'dataDevolucaoReal': '2026-11-21T16:00:00',
  'responsavelDevolucaoId': 1,
};

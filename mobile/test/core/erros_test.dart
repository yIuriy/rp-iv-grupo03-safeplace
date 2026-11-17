import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/erros.dart';

import '../apoio.dart';

void main() {
  final descrever = criarDescritorDeFalha(padrao: 'Padrão');

  test('lê o corpo padrão do backend', () {
    final corpo = corpoDoErro(
      falhaHttp(409, {
        'timestamp': '2026-11-17T10:00:00',
        'status': 409,
        'error': 'Conflict',
        'message': 'CPF já cadastrado',
      }),
    );
    expect(corpo?.status, 409);
    expect(corpo?.message, 'CPF já cadastrado');
  });

  test('409, 422, 400 e 404 preferem a mensagem da API', () {
    for (final status in [400, 404, 409, 422]) {
      expect(
        descrever(falhaHttp(status, {'message': 'Regra $status'})),
        'Regra $status',
      );
    }
  });

  test('sem mensagem da API, usa o texto do status', () {
    expect(
      descrever(falhaHttp(422, {'message': null})),
      'A operação foi bloqueada por uma regra do sistema.',
    );
    expect(
      descrever(falhaHttp(403)),
      'Seu perfil não tem permissão para esta ação.',
    );
  });

  test('401 ignora a mensagem da API', () {
    expect(
      descrever(falhaHttp(401, {'message': 'Unauthorized'})),
      'Sua sessão expirou. Entre novamente.',
    );
  });

  test('status sem estratégia usa o padrão', () {
    expect(
      descrever(falhaHttp(500, {'message': 'NullPointerException'})),
      'Padrão',
    );
  });

  test('a tela sobrescreve um status', () {
    final daTela = criarDescritorDeFalha(
      porStatus: {401: fixa('Senha errada')},
    );
    expect(daTela(falhaHttp(401)), 'Senha errada');
  });

  test('falha sem resposta do servidor vira aviso de conexão', () {
    final semRede = DioException.connectionError(
      requestOptions: RequestOptions(path: '/x'),
      reason: 'refused',
    );
    expect(descrever(semRede), mensagemSemConexao);
  });

  test('erro que não é HTTP usa o padrão', () {
    expect(descrever(const FormatException('Sessão inválida')), 'Padrão');
  });
}

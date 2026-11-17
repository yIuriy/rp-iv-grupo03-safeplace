import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/core/api/dio_api_client.dart';

import '../apoio.dart';

void main() {
  late String? token;
  late int expiracoes;

  DioApiClient cliente(AdaptadorFalso adaptador) => DioApiClient(
    tokenAtual: () => token,
    aoExpirar: () => expiracoes++,
    baseUrl: 'http://api.teste/api',
    adaptador: adaptador,
  );

  Matcher falhaCom(int? status) =>
      throwsA(isA<FalhaApi>().having((f) => f.status, 'status', status));

  setUp(() {
    token = 'abc';
    expiracoes = 0;
  });

  test('devolve o corpo JSON e envia a consulta', () async {
    final adaptador = AdaptadorFalso((_) => (200, [1, 2]));
    final dados = await cliente(adaptador)
        .get('/epis', consulta: {'nivelPerigo': 'ALTO'});
    expect(dados, [1, 2]);
    expect(adaptador.recebidas.single.queryParameters, {'nivelPerigo': 'ALTO'});
  });

  test('anexa o Bearer da sessão atual', () async {
    final adaptador = AdaptadorFalso((_) => (200, []));
    await cliente(adaptador).get('/epis');
    expect(adaptador.recebidas.single.headers['Authorization'], 'Bearer abc');
  });

  test('sem sessão, não envia Authorization', () async {
    token = null;
    final adaptador = AdaptadorFalso((_) => (200, []));
    await cliente(adaptador).get('/epis');
    expect(
      adaptador.recebidas.single.headers.containsKey('Authorization'),
      isFalse,
    );
  });

  test('falha HTTP vira FalhaApi com status e corpo', () async {
    final api = cliente(
      AdaptadorFalso((_) => (409, {'message': 'CPF já cadastrado'})),
    );
    await expectLater(
      api.post('/usuarios/colaboradores'),
      throwsA(
        isA<FalhaApi>().having((f) => f.status, 'status', 409).having(
          (f) => f.corpo,
          'corpo',
          {'message': 'CPF já cadastrado'},
        ),
      ),
    );
  });

  test('falha de rede vira FalhaApi sem resposta', () async {
    final api = cliente(
      AdaptadorFalso((_) => throw const SocketException('recusada')),
    );
    await expectLater(api.get('/epis'), falhaCom(null));
  });

  test('401 fora do login expira a sessão e repassa a falha', () async {
    final api = cliente(AdaptadorFalso((_) => (401, {'status': 401})));
    await expectLater(api.get('/epis'), falhaCom(401));
    expect(expiracoes, 1);
  });

  test('401 do login é senha errada, não sessão expirada', () async {
    final api = cliente(AdaptadorFalso((_) => (401, {'status': 401})));
    await expectLater(api.post(caminhoDoLogin), falhaCom(401));
    expect(expiracoes, 0);
  });

  test('403 não expira a sessão', () async {
    final api = cliente(AdaptadorFalso((_) => (403, {'status': 403})));
    await expectLater(api.get('/usuarios'), falhaCom(403));
    expect(expiracoes, 0);
  });
}

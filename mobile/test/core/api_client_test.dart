import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';

import '../apoio.dart';

void main() {
  late String? token;
  late int expiracoes;

  Dio cliente(AdaptadorFalso adaptador) => criarApiClient(
    tokenAtual: () => token,
    aoExpirar: () => expiracoes++,
    baseUrl: 'http://api.teste/api',
    adaptador: adaptador,
  );

  setUp(() {
    token = 'abc';
    expiracoes = 0;
  });

  test('anexa o Bearer da sessão atual', () async {
    final adaptador = AdaptadorFalso((_) => (200, []));
    await cliente(adaptador).get<Object?>('/epis');
    expect(adaptador.recebidas.single.headers['Authorization'], 'Bearer abc');
  });

  test('sem sessão, não envia Authorization', () async {
    token = null;
    final adaptador = AdaptadorFalso((_) => (200, []));
    await cliente(adaptador).get<Object?>('/epis');
    expect(
      adaptador.recebidas.single.headers.containsKey('Authorization'),
      isFalse,
    );
  });

  test('401 fora do login expira a sessão e repassa a falha', () async {
    final dio = cliente(AdaptadorFalso((_) => (401, {'status': 401})));
    await expectLater(dio.get<Object?>('/epis'), throwsA(isA<DioException>()));
    expect(expiracoes, 1);
  });

  test('401 do login é senha errada, não sessão expirada', () async {
    final dio = cliente(AdaptadorFalso((_) => (401, {'status': 401})));
    await expectLater(
      dio.post<Object?>(caminhoDoLogin),
      throwsA(isA<DioException>()),
    );
    expect(expiracoes, 0);
  });

  test('403 não expira a sessão', () async {
    final dio = cliente(AdaptadorFalso((_) => (403, {'status': 403})));
    await expectLater(
      dio.get<Object?>('/usuarios'),
      throwsA(isA<DioException>()),
    );
    expect(expiracoes, 0);
  });
}

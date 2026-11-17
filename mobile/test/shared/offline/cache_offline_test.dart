import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/app/dependencias.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/core/api/api_client_provider.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/core/sessao/controlador_sessao.dart';
import 'package:safeplace/core/sessao/sessao.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../apoio_offline.dart';

void main() {
  late ArmazenamentoCacheEmMemoria armazenamento;
  late ApiFalsa api;

  setUp(() {
    armazenamento = ArmazenamentoCacheEmMemoria();
    api = ApiFalsa((_) => {'nome': 'Gestor Demo'});
  });

  /// Contêiner com a sessão de [sessao] já carregada e a limpeza do cache ligada, como no app.
  Future<ProviderContainer> conteiner([Sessao? sessao = sessaoGestor]) async {
    final c = ProviderContainer(
      overrides: [
        ...sobreposicoesDoApp,
        armazenamentoCacheProvider.overrideWithValue(armazenamento),
        apiClientProvider.overrideWithValue(api),
        armazenamentoSessaoProvider.overrideWithValue(
          ArmazenamentoSessaoEmMemoria(sessao),
        ),
      ],
    );
    addTearDown(c.dispose);
    await c.read(controladorSessaoProvider.future);
    return c;
  }

  final consulta = FutureProvider<Consultado<String>>(
    (ref) => consultarComCache(
      ref,
      chave: 'usuarios/me',
      buscar: (api) => api.get('/usuarios/me'),
      ler: (json) => (json as Map)['nome'] as String,
    ),
    retry: (_, _) => null,
  );

  test(
    'com o servidor no ar, devolve os dados e guarda a cópia da conta',
    () async {
      final c = await conteiner();

      final resultado = await c.read(consulta.future);

      expect(resultado.dados, 'Gestor Demo');
      expect(resultado.somenteLeitura, isFalse);
      expect(armazenamento.valores.keys, [
        'safeplace.cache.${sessaoGestor.email}.usuarios/me',
      ]);
    },
  );

  test('sem conexão, devolve a cópia como somente leitura com a hora da sincronização', () async {
    await CacheOffline(
      armazenamento,
      agora: () => DateTime(2026, 11, 17, 9, 30),
    ).guardar(sessaoGestor.email, 'usuarios/me', {'nome': 'Gestor da cópia'});
    api.responder = semConexao;
    final c = await conteiner();

    final resultado = await c.read(consulta.future);

    expect(resultado.dados, 'Gestor da cópia');
    expect(resultado.somenteLeitura, isTrue);
    expect(resultado.sincronizadoEm, DateTime(2026, 11, 17, 9, 30));
  });

  test('sem conexão e sem cópia, a falha continua sendo falha', () async {
    api.responder = semConexao;
    final c = await conteiner();

    await expectLater(c.read(consulta.future), throwsA(isA<FalhaApi>()));
  });

  test(
    'erro do servidor (500) não usa a cópia: o servidor respondeu',
    () async {
      await CacheOffline(armazenamento)
          .guardar(sessaoGestor.email, 'usuarios/me', {'nome': 'Cópia'});
      api.responder = (_) => throw falhaHttp(500);
      final c = await conteiner();

      await expectLater(
        c.read(consulta.future),
        throwsA(isA<FalhaApi>().having((f) => f.status, 'status', 500)),
      );
    },
  );

  test(
    'a cópia é por conta: outra pessoa no mesmo aparelho não vê os dados',
    () async {
      final cache = CacheOffline(armazenamento);
      await cache.guardar('a@safeplace.com', 'usuarios/me', {'nome': 'A'});

      expect(await cache.ler('b@safeplace.com', 'usuarios/me'), isNull);
      expect((await cache.ler('a@safeplace.com', 'usuarios/me'))?.dados, {
        'nome': 'A',
      });
    },
  );

  test('limpar a conta apaga só as cópias dela', () async {
    final cache = CacheOffline(armazenamento);
    await cache.guardar('a@safeplace.com', 'epis', []);
    await cache.guardar('a@safeplace.com', 'usuarios/me', {});
    await cache.guardar('b@safeplace.com', 'epis', []);

    await cache.limparConta('a@safeplace.com');

    expect(armazenamento.valores.keys, [
      'safeplace.cache.b@safeplace.com.epis',
    ]);
  });

  test(
    'sair da sessão apaga a cópia da conta (limpeza ligada pelo app)',
    () async {
      final c = await conteiner();
      await c.read(consulta.future);
      expect(armazenamento.valores, isNotEmpty);

      await c.read(controladorSessaoProvider.notifier).sair();

      expect(armazenamento.valores, isEmpty);
    },
  );

  test('cópia corrompida é ignorada sem derrubar a tela', () async {
    armazenamento.valores['safeplace.cache.${sessaoGestor.email}.usuarios/me'] =
        '{não é json';

    expect(
      await CacheOffline(armazenamento).ler(sessaoGestor.email, 'usuarios/me'),
      isNull,
    );
  });
}

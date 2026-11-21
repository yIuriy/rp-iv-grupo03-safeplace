// Testes de widget da lista de EPIs (`/epis`): destaque do estoque crítico, busca local, estados
// vazio e de falha, e cópia local com aviso de modo offline.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import '../usuarios/apoio_usuarios.dart' show abrirApp, sessaoSupervisor, tocar;
import 'apoio_epis.dart';
import 'epi_test.dart' show capaceteJson;

final luvaCriticaJson = <String, Object?>{
  'id': 2,
  'nome': 'Luva de Vaqueta',
  'numeroCa': '54321',
  'quantidade': 3,
  'estoqueMinimo': 10,
  'estoqueCritico': true,
  'status': 'DISPONIVEL',
  'localizacao': 'Almoxarifado B',
};

void main() {
  late ApiEpisFalsa api;

  setUp(
    () => api = ApiEpisFalsa(
      (r) => r.caminho == '/epis'
          ? [capaceteJson, luvaCriticaJson]
          : throw UnimplementedError('Chamada inesperada: $r'),
    ),
  );

  testWidgets('lista destaca o EPI com estoque crítico', (tester) async {
    final semantica = tester.ensureSemantics();
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis');

    expect(find.text('Capacete de Segurança com Aba Frontal'), findsOneWidget);
    expect(find.text('Luva de Vaqueta'), findsOneWidget);
    expect(find.text('Saldo 3 · mínimo 10'), findsOneWidget);
    expect(find.text('Crítico'), findsOneWidget);
    expect(
      find.text('1 EPI com estoque crítico (saldo igual ou abaixo do mínimo).'),
      findsOneWidget,
    );
    // O Card junta a semântica dos filhos num nó só, então o rótulo vem dentro do texto do cartão.
    expect(
      find.bySemanticsLabel(
        RegExp('Estoque crítico: saldo 3 igual ou abaixo do mínimo 10'),
      ),
      findsOneWidget,
    );
    semantica.dispose();
  });

  testWidgets('Supervisor consulta a mesma lista', (tester) async {
    await abrirApp(tester, sessao: sessaoSupervisor, api: api, rota: '/epis');

    expect(find.text('Luva de Vaqueta'), findsOneWidget);
    expect(
      find.textContaining('registradas pelo Gestor de Segurança'),
      findsOneWidget,
    );
  });

  testWidgets('busca filtra por localização e oferece limpar', (tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis');

    await tester.enterText(find.byType(TextFormField), 'almoxarifado b');
    await tester.pump();
    expect(find.text('Luva de Vaqueta'), findsOneWidget);
    expect(find.text('Capacete de Segurança com Aba Frontal'), findsNothing);

    await tester.enterText(find.byType(TextFormField), 'máscara');
    await tester.pump();
    expect(find.text('Nenhum EPI encontrado'), findsOneWidget);
    await tocar(tester, find.text('Limpar busca'));
    expect(find.text('2 EPIs'), findsOneWidget);
    expect(api.recebidas.where((r) => r.caminho == '/epis'), hasLength(1));
  });

  testWidgets('catálogo vazio explica a ausência', (tester) async {
    api.responder = (_) => <Object?>[];
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis');

    expect(find.text('Nenhum EPI cadastrado'), findsOneWidget);
  });

  testWidgets('sem conexão e sem cópia: falha com tentar novamente', (
    tester,
  ) async {
    api.responder = (_) => throw const FalhaApi();
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis');

    expect(
      find.text('Não foi possível carregar o estoque de EPIs.'),
      findsOneWidget,
    );
    api.responder = (_) => [luvaCriticaJson];
    await tocar(tester, find.text('Tentar novamente'));
    expect(find.text('Luva de Vaqueta'), findsOneWidget);
  });

  testWidgets('modo offline mostra a cópia local com aviso', (tester) async {
    final cache = ArmazenamentoCacheEmMemoria();
    await CacheOffline(cache)
        .guardar(sessaoGestor.email, 'epis', [luvaCriticaJson]);
    api.responder = (_) => throw const FalhaApi();

    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/epis',
      cache: cache,
    );

    expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
    expect(find.text('Luva de Vaqueta'), findsOneWidget);
    expect(find.text('Crítico'), findsOneWidget);
  });
}

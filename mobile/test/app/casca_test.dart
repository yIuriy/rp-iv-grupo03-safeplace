import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/app/router.dart';
import 'package:safeplace/core/api/api_client_provider.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/core/sessao/sessao.dart';
import 'package:safeplace/main.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../apoio.dart';
import '../shared/apoio_offline.dart';
import 'navegacao_test.dart' show sessaoSupervisor;

void main() {
  late ApiFalsa api;
  late ArmazenamentoCacheEmMemoria cache;

  setUp(() {
    api = ApiFalsa(
      (_) => {
        'nome': 'Fulano',
        'email': 'fulano@safeplace.com',
        'cpf': '52998224725',
      },
    );
    cache = ArmazenamentoCacheEmMemoria();
  });

  /// Abre o app logado com [sessao]; devolve o router para navegar direto por endereço.
  Future<GoRouter> abrir(WidgetTester tester, Sessao sessao) async {
    // Tela de celular (1080x2400, como o emulador), não a 800x600 padrão dos testes.
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 2.625;
    addTearDown(tester.view.reset);
    final conteiner = ProviderContainer(
      overrides: [
        armazenamentoSessaoProvider.overrideWithValue(
          ArmazenamentoSessaoEmMemoria(sessao),
        ),
        apiClientProvider.overrideWithValue(api),
        armazenamentoCacheProvider.overrideWithValue(cache),
      ],
    );
    addTearDown(conteiner.dispose);
    await tester.pumpWidget(
      UncontrolledProviderScope(
        container: conteiner,
        child: const SafePlaceApp(),
      ),
    );
    await tester.pumpAndSettle();
    return conteiner.read(routerProvider);
  }

  Future<void> abrirMenu(WidgetTester tester) async {
    await tester.tap(find.byTooltip('Open navigation menu'));
    await tester.pumpAndSettle();
  }

  testWidgets(
    'Gestor vê o menu com Usuários e Alertas, e o próprio nome e perfil',
    (tester) async {
      await abrir(tester, sessaoGestor);
      await abrirMenu(tester);

      final menu = find.byType(Drawer);
      expect(
        find.descendant(of: menu, matching: find.text('Usuários')),
        findsOneWidget,
      );
      expect(
        find.descendant(of: menu, matching: find.text('Alertas')),
        findsOneWidget,
      );
      expect(
        find.descendant(of: menu, matching: find.text(sessaoGestor.nome)),
        findsOneWidget,
      );
      expect(
        find.descendant(of: menu, matching: find.text('Gestor de Segurança')),
        findsOneWidget,
      );
    },
  );

  testWidgets('Supervisor vê Colaboradores e não vê Alertas', (tester) async {
    await abrir(tester, sessaoSupervisor);
    await abrirMenu(tester);

    final menu = find.byType(Drawer);
    expect(
      find.descendant(of: menu, matching: find.text('Colaboradores')),
      findsOneWidget,
    );
    expect(
      find.descendant(of: menu, matching: find.text('Alertas')),
      findsNothing,
    );
  });

  testWidgets(
    'item do menu troca a tela e o título; módulo pendente diz qual issue o entrega',
    (tester) async {
      await abrir(tester, sessaoGestor);
      await abrirMenu(tester);

      await tester.tap(
        find.descendant(
          of: find.byType(Drawer),
          matching: find.text('Empréstimos de EPIs'),
        ),
      );
      await tester.pumpAndSettle();

      expect(
        find.descendant(
          of: find.byType(AppBar),
          matching: find.text('Empréstimos de EPIs'),
        ),
        findsOneWidget,
      );
      expect(find.text('Funcionalidade em desenvolvimento'), findsOneWidget);
      expect(find.textContaining('issue #320'), findsOneWidget);
    },
  );

  testWidgets('Supervisor que abre /alertas pelo endereço volta ao início', (
    tester,
  ) async {
    final router = await abrir(tester, sessaoSupervisor);

    router.go('/alertas');
    await tester.pumpAndSettle();

    expect(router.routerDelegate.currentConfiguration.uri.path, Rotas.inicio);
    expect(find.text('Olá, ${sessaoSupervisor.nome}'), findsOneWidget);
  });

  testWidgets(
    'endereço desconhecido mostra a página não encontrada com volta ao início',
    (tester) async {
      final router = await abrir(tester, sessaoGestor);

      router.go('/nao-existe');
      await tester.pumpAndSettle();
      expect(find.text('Página não encontrada'), findsOneWidget);

      await tester.tap(find.text('Voltar ao início'));
      await tester.pumpAndSettle();
      expect(find.text('Olá, ${sessaoGestor.nome}'), findsOneWidget);
    },
  );
}

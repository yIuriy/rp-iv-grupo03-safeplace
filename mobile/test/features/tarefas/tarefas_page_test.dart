// Testes de widget da lista (`/tarefas`) e do detalhe (`/tarefas/{id}`): filtro por grau no
// servidor, busca local, estados vazios, nomes da área e dos setores e modo offline.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/features/tarefas/data/tarefa.dart';
import 'package:safeplace/features/tarefas/presentation/tarefas_page.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import 'apoio_tarefas.dart';

void main() {
  late ApiUsuariosFalsa api;

  setUp(() => api = ApiUsuariosFalsa(responderTarefas));

  test('busca local ignora maiúsculas', () {
    final tarefas = lerTarefas([soldaJson, limpezaJson]);
    expect(filtrarTarefas(tarefas, 'SOLDA').single.id, 1);
    expect(filtrarTarefas(tarefas, '  '), hasLength(2));
  });

  group('lista', () {
    testWidgets('mostra grau, avaliação e pendência de classificação', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/tarefas',
      );

      expect(find.text('2 tarefas'), findsOneWidget);
      expect(
        find.text('Solda em altura na estrutura metálica'),
        findsOneWidget,
      );
      expect(find.text('20/11/2026 10:15 por Gestor Demo'), findsOneWidget);
      expect(find.text('1 EPI obrigatório'), findsOneWidget);
      expect(find.text('Não classificada'), findsOneWidget);
      expect(find.text('Pendente de avaliação'), findsOneWidget);
    });

    testWidgets('filtro de grau consulta o servidor', (tester) async {
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas');

      await tocar(tester, find.widgetWithText(ChoiceChip, 'Alto'));

      expect(find.text('1 tarefa'), findsOneWidget);
      expect(find.text('Limpeza do almoxarifado'), findsNothing);
      expect(
        api.recebidas.where((r) => r.caminho == '/tarefas').last.consulta,
        {'nivelPerigo': 'ALTO'},
      );
    });

    testWidgets('filtro sem resultado oferece limpar', (tester) async {
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas');

      await tocar(tester, find.widgetWithText(ChoiceChip, 'Crítico'));
      expect(find.text('Nenhuma tarefa encontrada'), findsOneWidget);

      await tocar(tester, find.text('Limpar filtros'));
      expect(find.text('2 tarefas'), findsOneWidget);
    });

    testWidgets('lista vazia sem filtro explica a ausência', (tester) async {
      api.responder = (_) => <Object?>[];
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas');

      expect(find.text('Nenhuma tarefa cadastrada'), findsOneWidget);
    });

    testWidgets('sem conexão mostra a cópia local', (tester) async {
      final cache = ArmazenamentoCacheEmMemoria();
      await CacheOffline(cache)
          .guardar(sessaoGestor.email, 'tarefas', [soldaJson]);
      api.responder = servidorFora;

      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/tarefas',
        cache: cache,
      );

      expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
      expect(find.text('1 tarefa'), findsOneWidget);
    });
  });

  group('detalhe', () {
    testWidgets('toque no cartão abre os dados com área e setores', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/tarefas',
      );
      await tocar(tester, find.text('Solda em altura na estrutura metálica'));

      expect(find.text('AR-03 · Caldeiraria'), findsOneWidget);
      expect(find.text('Produção (Galpão A)'), findsOneWidget);
      expect(find.text('Alto'), findsOneWidget);
    });

    testWidgets('endereço sem número mostra tarefa não encontrada', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/tarefas/abc',
      );

      expect(find.text('Tarefa não encontrada'), findsOneWidget);
    });

    testWidgets('404 explica que a tarefa sumiu', (tester) async {
      api.responder = (r) => r.caminho == '/tarefas/9'
          ? throw falhaHttp(404)
          : responderTarefas(r);
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/tarefas/9',
      );

      expect(find.text('Não foi possível carregar a tarefa.'), findsOneWidget);
      expect(
        find.textContaining('Tarefa não encontrada. Ela pode ter sido'),
        findsOneWidget,
      );
    });
  });
}

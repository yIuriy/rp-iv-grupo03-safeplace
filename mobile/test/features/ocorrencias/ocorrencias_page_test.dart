// Testes de widget de `/ocorrencias` e `/ocorrencias/{id}`: lista, estados vazios, filtros
// combinados enviados ao servidor, visão do Supervisor, detalhe de acidente e de incidente, busca
// por protocolo CAT e modo offline com registro desabilitado.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/areas_risco/data/area_risco.dart';
import 'package:safeplace/features/ocorrencias/data/ocorrencia.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import '../usuarios/apoio_usuarios.dart';
import 'apoio_ocorrencias.dart';

void main() {
  late ApiUsuariosFalsa api;

  setUp(() => api = ApiUsuariosFalsa(responderOcorrencias));

  Iterable<Requisicao> listas() => api.chamadas('GET', '/ocorrencias');

  Future<void> escolher<T>(
    WidgetTester tester,
    String rotulo,
    String opcao,
  ) async {
    await tocar(
      tester,
      find.widgetWithText(DropdownButtonFormField<T>, rotulo),
    );
    await tocar(tester, find.text(opcao).last);
  }

  Future<void> digitar(WidgetTester tester, String rotulo, String texto) async {
    await tester.ensureVisible(campo(rotulo));
    await tester.enterText(campo(rotulo), texto);
    await tester.pump();
  }

  testWidgets('Gestor vê todos os relatos, com tipo, área e protocolo', (
    tester,
  ) async {
    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias',
    );

    expect(
      find.text(
        'Registre acidentes e incidentes e consulte todos os relatos da empresa.',
      ),
      findsOneWidget,
    );
    expect(find.text('2 ocorrências'), findsOneWidget);
    expect(find.text('Acidente em 10/11/2026 às 08:30'), findsOneWidget);
    expect(find.text('Incidente em 12/11/2026 às 14:00'), findsOneWidget);
    expect(find.text('Protocolo CAT CAT-2026-0007'), findsOneWidget);
    expect(find.text('Envolvido: Ana Souza'), findsOneWidget);
    expect(listas().single.consulta, isNull);
  });

  testWidgets('Supervisor vê só o que o backend devolve para ele', (
    tester,
  ) async {
    api.responder = (r) =>
        r.caminho == '/ocorrencias' ? [acidenteJson] : responderOcorrencias(r);
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/ocorrencias',
    );

    expect(
      find.text(
        'Registre acidentes e incidentes e consulte os relatos que você registrou.',
      ),
      findsOneWidget,
    );
    expect(find.text('1 ocorrência'), findsOneWidget);
    expect(find.text('Incidente em 12/11/2026 às 14:00'), findsNothing);
    expect(listas().single.consulta, isNull);
  });

  testWidgets('lista vazia mostra o estado vazio', (tester) async {
    api.responder = (r) =>
        r.caminho == '/ocorrencias' ? [] : responderOcorrencias(r);
    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias',
    );

    expect(find.text('Nenhuma ocorrência registrada'), findsOneWidget);
  });

  testWidgets('filtros combinados vão na consulta e podem ser limpos', (
    tester,
  ) async {
    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias',
    );
    api.responder = (r) => r.caminho == '/ocorrencias' && r.consulta != null
        ? []
        : responderOcorrencias(r);

    await tocar(tester, find.text('Filtros'));
    await digitar(tester, 'Data inicial', '01112026');
    await digitar(tester, 'Data final', '30112026');
    await escolher<int>(tester, 'Setor', 'Galpão A (Bloco 2)');
    await tester.ensureVisible(find.bySemanticsLabel('Colaborador envolvido'));
    await tester.tap(find.bySemanticsLabel('Colaborador envolvido'));
    await tester.pumpAndSettle();
    await tester.tap(
      find.descendant(
        of: find.byType(ListTile),
        matching: find.text('João da Silva'),
      ),
    );
    await tester.pumpAndSettle();
    await escolher<TipoOcorrencia>(tester, 'Tipo', 'Acidente');
    await escolher<NivelPerigo>(tester, 'Gravidade', 'Crítico');
    await tocar(tester, find.text('Aplicar filtros'));

    expect(listas().last.consulta, {
      'dataInicio': '2026-11-01',
      'dataFim': '2026-11-30',
      'setorId': '1',
      'colaboradorId': '10',
      'tipoOcorrencia': 'ACIDENTE',
      'gravidade': 'CRITICO',
    });
    expect(find.text('6 critérios aplicados'), findsOneWidget);
    expect(find.text('Nenhuma ocorrência encontrada'), findsOneWidget);

    await tocar(
      tester,
      find.widgetWithText(TextButton, 'Limpar filtros').first,
    );

    expect(find.text('Nenhum critério aplicado'), findsOneWidget);
    expect(find.text('2 ocorrências'), findsOneWidget);
    expect(find.text('01/11/2026'), findsNothing);
  });

  testWidgets('período invertido não é enviado', (tester) async {
    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias',
    );

    await tocar(tester, find.text('Filtros'));
    await digitar(tester, 'Data inicial', '30112026');
    await digitar(tester, 'Data final', '01112026');
    await tocar(tester, find.text('Aplicar filtros'));

    expect(
      find.text('A data final deve ser igual ou posterior à data inicial.'),
      findsOneWidget,
    );
    expect(listas(), hasLength(1));
  });

  testWidgets('tocar no acidente abre o detalhe com os dados da CAT', (
    tester,
  ) async {
    final router = await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias',
    );

    await tocar(tester, find.text('Acidente em 10/11/2026 às 08:30'));

    expect(
      router.routerDelegate.currentConfiguration.uri.path,
      '/ocorrencias/7',
    );
    expect(find.text('CAT-2026-0007'), findsOneWidget);
    expect(find.text('Falha de EPI'), findsOneWidget);
    expect(find.text('Alto'), findsOneWidget);
    expect(find.text('Joana Ribeiro (Supervisor)'), findsOneWidget);
    expect(find.text('Situação de risco'), findsNothing);
  });

  testWidgets('detalhe do incidente mostra situação e potencial de dano', (
    tester,
  ) async {
    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias/8',
    );

    expect(find.text('Em triagem'), findsOneWidget);
    expect(find.text('Piso escorregadio'), findsOneWidget);
    expect(find.text('Queda'), findsOneWidget);
    expect(find.text('Protocolo CAT'), findsNothing);
  });

  testWidgets('endereço sem número mostra ocorrência não encontrada', (
    tester,
  ) async {
    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias/abc',
    );

    expect(find.text('Ocorrência não encontrada'), findsOneWidget);
  });

  group('busca por protocolo', () {
    setUp(
      () => api.responder = (r) => switch (r.caminho) {
        '/ocorrencias/protocolo/CAT-2026-0007' => acidenteJson,
        final caminho when caminho.startsWith('/ocorrencias/protocolo/') =>
          throw falhaComMensagem(404, 'Ocorrência não encontrada.'),
        _ => responderOcorrencias(r),
      },
    );

    testWidgets('protocolo inexistente mostra o motivo', (tester) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias',
      );

      await tocar(tester, find.text('Buscar por protocolo'));
      expect(find.text('Informe o protocolo CAT do acidente.'), findsOneWidget);

      await tester.enterText(campo('Protocolo CAT'), 'CAT-0000');
      await tocar(tester, find.text('Buscar por protocolo'));

      expect(find.text('Acidente não encontrado'), findsOneWidget);
      expect(
        find.text(
          'Nenhum acidente tem esse protocolo. Confira o número e tente novamente.',
        ),
        findsOneWidget,
      );
    });

    testWidgets('protocolo encontrado abre o detalhe', (tester) async {
      final router = await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias',
      );

      await tester.enterText(campo('Protocolo CAT'), ' CAT-2026-0007 ');
      await tocar(tester, find.text('Buscar por protocolo'));

      expect(
        router.routerDelegate.currentConfiguration.uri.path,
        '/ocorrencias/7',
      );
      expect(find.text('Falha de EPI'), findsOneWidget);
    });
  });

  testWidgets('offline mostra a cópia local e desabilita registro e busca', (
    tester,
  ) async {
    final cache = ArmazenamentoCacheEmMemoria();
    await CacheOffline(cache)
        .guardar(sessaoGestor.email, 'ocorrencias', [acidenteJson]);
    api.responder = (_) => throw const FalhaApi();

    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/ocorrencias',
      cache: cache,
    );

    expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
    expect(find.text('Acidente em 10/11/2026 às 08:30'), findsOneWidget);
    expect(
      botao<FilledButton>(tester, 'Registrar ocorrência').onPressed,
      isNull,
    );
    expect(
      botao<OutlinedButton>(tester, 'Buscar por protocolo').onPressed,
      isNull,
    );
    expect(
      find.text(
        'Registro e busca por protocolo voltam quando a conexão voltar.',
      ),
      findsOneWidget,
    );
  });
}

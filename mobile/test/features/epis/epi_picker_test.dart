// Testes de widget do EpiPicker: descartados e EPIs sem id fora da lista, busca por nome, CA e
// localização, escolha inicial por id, campo obrigatório no Form, filtro próprio e falha de carga.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/core/api/api_client_provider.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/features/epis/data/epi.dart';
import 'package:safeplace/features/epis/presentation/epi_picker.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';
import 'package:safeplace/shared/tema/app_theme.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import 'apoio_epis.dart';
import 'epi_test.dart' show capaceteJson;
import 'epis_page_test.dart' show luvaCriticaJson;

const _oculosDescartadoJson = <String, Object?>{
  'id': 3,
  'nome': 'Óculos de Proteção',
  'numeroCa': '11111',
  'quantidade': 0,
  'status': 'DESCARTADO',
};

const _semIdJson = <String, Object?>{'nome': 'Máscara sem cadastro'};

const _esgotadoJson = <String, Object?>{
  'id': 4,
  'nome': 'Bota de PVC',
  'numeroCa': '22222',
  'quantidade': 0,
  'status': 'ESGOTADO',
};

void main() {
  late ApiEpisFalsa api;
  Epi? escolhido;
  final formulario = GlobalKey<FormState>();

  setUp(() {
    escolhido = null;
    api = ApiEpisFalsa(
      (_) => [
        capaceteJson,
        luvaCriticaJson,
        _oculosDescartadoJson,
        _semIdJson,
        _esgotadoJson,
      ],
    );
  });

  Finder opcao(String nome) =>
      find.descendant(of: find.byType(ListTile), matching: find.text(nome));

  Future<void> montar(
    WidgetTester tester, {
    int? epiId,
    bool obrigatorio = false,
    bool Function(Epi) filtro = EpiPicker.naoDescartado,
  }) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 2.625;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          armazenamentoSessaoProvider.overrideWithValue(
            ArmazenamentoSessaoEmMemoria(sessaoGestor),
          ),
          apiClientProvider.overrideWithValue(api),
          armazenamentoCacheProvider.overrideWithValue(
            ArmazenamentoCacheEmMemoria(),
          ),
        ],
        child: MaterialApp(
          theme: AppTheme.claro(),
          home: Scaffold(
            body: Form(
              key: formulario,
              child: EpiPicker(
                epiId: epiId,
                obrigatorio: obrigatorio,
                filtro: filtro,
                aoMudar: (epi) => escolhido = epi,
              ),
            ),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  Future<void> abrirLista(WidgetTester tester) async {
    await tester.tap(find.bySemanticsLabel('EPI'));
    await tester.pumpAndSettle();
  }

  testWidgets('esconde descartados e EPIs sem id, com CA e saldo', (
    tester,
  ) async {
    await montar(tester);
    await abrirLista(tester);

    expect(opcao('Capacete de Segurança com Aba Frontal'), findsOneWidget);
    expect(opcao('Bota de PVC'), findsOneWidget);
    expect(opcao('Óculos de Proteção'), findsNothing);
    expect(opcao('Máscara sem cadastro'), findsNothing);
    expect(find.text('CA 12345 · saldo 25 · Disponível'), findsOneWidget);
    expect(find.text('3 resultados'), findsOneWidget);
  });

  testWidgets('busca por nome sem acento, CA e localização', (tester) async {
    await montar(tester);
    await abrirLista(tester);
    final busca = find.widgetWithText(
      TextField,
      'Buscar por nome, CA ou localização',
    );

    await tester.enterText(busca, 'capacete de seguranca');
    await tester.pump();
    expect(find.text('1 resultado'), findsOneWidget);

    await tester.enterText(busca, '54321');
    await tester.pump();
    expect(opcao('Luva de Vaqueta'), findsOneWidget);
    expect(find.text('1 resultado'), findsOneWidget);

    await tester.enterText(busca, 'almoxarifado b');
    await tester.pump();
    expect(opcao('Luva de Vaqueta'), findsOneWidget);

    await tester.tap(opcao('Luva de Vaqueta'));
    await tester.pumpAndSettle();
    expect(escolhido?.id, 2);
    expect(find.text('Luva de Vaqueta'), findsOneWidget);
  });

  testWidgets('mostra a escolha inicial pelo id, mesmo descartada', (
    tester,
  ) async {
    await montar(tester, epiId: 3);

    expect(find.text('Óculos de Proteção'), findsOneWidget);
  });

  testWidgets('obrigatório recusa o envio sem escolha', (tester) async {
    await montar(tester, obrigatorio: true);

    expect(formulario.currentState!.validate(), isFalse);
    await tester.pump();
    expect(find.text(EpiPicker.mensagemObrigatorio), findsOneWidget);
  });

  testWidgets('filtro próprio oferece só EPIs com saldo', (tester) async {
    await montar(tester, filtro: (epi) => (epi.quantidade ?? 0) > 0);
    await abrirLista(tester);

    expect(find.text('2 resultados'), findsOneWidget);
    expect(opcao('Bota de PVC'), findsNothing);
  });

  testWidgets('falha de carga mostra o motivo e tenta de novo', (tester) async {
    api.responder = (_) => throw const FalhaApi();
    await montar(tester);

    expect(find.text('Não foi possível carregar os EPIs.'), findsOneWidget);
    api.responder = (_) => [capaceteJson];
    await tester.tap(find.text('Tentar novamente'));
    await tester.pumpAndSettle();

    expect(find.text('Não foi possível carregar os EPIs.'), findsNothing);
    expect(api.recebidas.where((r) => r.caminho == '/epis'), hasLength(2));
  });
}

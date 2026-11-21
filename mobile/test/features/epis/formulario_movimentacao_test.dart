// Testes de widget da movimentação de estoque: botão só do Gestor, saída acima do saldo barrada
// localmente, recusa do backend exibida com os dados mantidos, registro com aviso e modo offline.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import '../usuarios/apoio_usuarios.dart'
    show abrirApp, campo, falhaComMensagem, sessaoSupervisor, tocar;
import 'apoio_epis.dart';
import 'epi_detalhe_page_test.dart' show responderDetalhe;
import 'epi_test.dart' show capaceteJson;

void main() {
  late ApiEpisFalsa api;

  setUp(() => api = ApiEpisFalsa(responderDetalhe));

  Iterable<Requisicao> envios() =>
      api.recebidas.where((r) => r.metodo == 'POST');

  Future<void> abrirFormulario(WidgetTester tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis/1');
    await tocar(tester, find.text('Movimentar estoque'));
  }

  Future<void> preencherSaida(WidgetTester tester, String quantidade) async {
    await tester.tap(find.text('Saída (baixa)'));
    await tester.enterText(campo('Quantidade'), quantidade);
    await tester.enterText(campo('Motivo'), 'Entrega à equipe');
    await tester.pump();
  }

  testWidgets('Supervisor não vê o botão de movimentar', (tester) async {
    await abrirApp(tester, sessao: sessaoSupervisor, api: api, rota: '/epis/1');

    expect(find.text('Entrada de 10 unidades'), findsOneWidget);
    expect(find.text('Movimentar estoque'), findsNothing);
  });

  testWidgets('saída acima do saldo conhecido é barrada antes de enviar', (
    tester,
  ) async {
    await abrirFormulario(tester);
    await preencherSaida(tester, '26');
    await tocar(tester, find.text('Registrar'));

    expect(
      find.text('A saída não pode superar o saldo disponível (25).'),
      findsOneWidget,
    );
    expect(envios(), isEmpty);
  });

  testWidgets('recusa do backend aparece e os dados ficam preenchidos', (
    tester,
  ) async {
    await abrirFormulario(tester);
    api.responder = (r) => r.metodo == 'POST'
        ? throw falhaComMensagem(
            400,
            'Saldo insuficiente em estoque. Saldo atual: 4',
            erro: 'Saldo Insuficiente',
          )
        : responderDetalhe(r);
    await preencherSaida(tester, '20');
    await tocar(tester, find.text('Registrar'));

    expect(find.text('Movimentação não registrada'), findsOneWidget);
    expect(
      find.text('Saldo insuficiente em estoque. Saldo atual: 4'),
      findsOneWidget,
    );
    expect(find.text('20'), findsOneWidget);
    expect(find.text('Entrega à equipe'), findsOneWidget);
  });

  testWidgets('entrada registrada volta ao detalhe com aviso e recarrega', (
    tester,
  ) async {
    await abrirFormulario(tester);
    api.responder = (r) => r.metodo == 'POST'
        ? {
            'id': 8,
            'epiId': 1,
            'tipo': 'ENTRADA',
            'quantidade': 5,
            'dataHora': '2026-11-21T10:00:00',
            'motivo': 'Recebimento de lote',
            'saldoAtual': 30,
          }
        : responderDetalhe(r);
    await tester.enterText(campo('Quantidade'), '5');
    await tester.enterText(campo('Motivo'), ' Recebimento de lote ');
    await tocar(tester, find.text('Registrar'));

    final envio = envios().single;
    expect(envio.caminho, '/epis/1/movimentacoes');
    expect(envio.corpo, {
      'tipo': 'ENTRADA',
      'quantidade': 5,
      'motivo': 'Recebimento de lote',
    });
    expect(
      find.text(
        'Entrada de 5 unidades registrada para Capacete de Segurança com Aba Frontal. Saldo atual: 30.',
      ),
      findsOneWidget,
    );
    expect(
      api.recebidas.where((r) => r.caminho == '/epis/1/movimentacoes'),
      hasLength(3), // leitura inicial, envio e nova leitura
    );
  });

  testWidgets('modo offline desabilita a movimentação com o motivo', (
    tester,
  ) async {
    final cache = ArmazenamentoCacheEmMemoria();
    await CacheOffline(cache)
        .guardar(sessaoGestor.email, 'epis/1', capaceteJson);
    api.responder = (_) => throw const FalhaApi();

    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/epis/1',
      cache: cache,
    );

    final botao = tester.widget<FilledButton>(
      find.ancestor(
        of: find.text('Movimentar estoque'),
        matching: find.byType(FilledButton),
      ),
    );
    expect(botao.onPressed, isNull);
    expect(
      find.text('Cadastros e alterações voltam quando a conexão voltar.'),
      findsOneWidget,
    );
  });
}

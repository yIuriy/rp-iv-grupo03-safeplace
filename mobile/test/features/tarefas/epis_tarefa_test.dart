// Testes do vínculo de EPIs (PUT com a lista inteira, filtro por classificação, seleção atual vinda
// do servidor) e das exigências da atividade.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/features/tarefas/data/tarefa.dart';

import '../../apoio.dart';
import 'apoio_tarefas.dart';

void main() {
  late ApiUsuariosFalsa api;

  Object? responder(Requisicao r) => switch ((r.metodo, r.caminho)) {
    ('PUT', '/tarefas/1/epis') => {
      ...soldaJson,
      'episIds': [5, 6],
    },
    _ => responderTarefas(r),
  };

  setUp(() => api = ApiUsuariosFalsa(responder));

  test('exigências leem tipos e listas com defesa', () {
    final exigencias = ExigenciasTarefa.ler(exigenciasSoldaJson);
    expect(exigencias.nivelPerigo, NivelPerigo.alto);
    expect(exigencias.capacitacoes.map((c) => c.tipo), [
      TipoCapacitacao.certificacao,
      TipoCapacitacao.treinamento,
    ]);
    expect(exigencias.episObrigatorios.single.nome, 'Cinto paraquedista');

    final vazias = ExigenciasTarefa.ler({'capacitacoes': 'x'});
    expect(vazias.classificada, isFalse);
    expect(vazias.capacitacoes, isEmpty);
  });

  testWidgets('Supervisor vê as exigências e não vincula EPIs', (tester) async {
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/tarefas/1',
    );

    await tester.scrollUntilVisible(
      find.text('• Solda segura (Treinamento)'),
      200,
    );
    expect(find.text('• Cinto paraquedista (CA 33333)'), findsOneWidget);
    expect(
      find.text('• NR-35 Trabalho em altura (Certificação)'),
      findsOneWidget,
    );
    expect(find.text('Vincular EPIs'), findsNothing);
  });

  testWidgets('Gestor vincula EPIs com filtro por classificação', (
    tester,
  ) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas/1');

    await tocar(tester, find.text('Vincular EPIs'));
    expect(find.text('1 EPI selecionado no total'), findsOneWidget);

    await tocar(tester, find.text('Todas as classificações'));
    await tocar(tester, find.text('Proteção contra quedas').last);
    expect(find.text('Luva de vaqueta'), findsNothing);

    await tocar(tester, find.text('Proteção contra quedas'));
    await tocar(tester, find.text('Todas as classificações').last);
    await tocar(
      tester,
      find.widgetWithText(CheckboxListTile, 'Luva de vaqueta'),
    );
    expect(find.text('2 EPIs selecionados no total'), findsOneWidget);

    await tocar(tester, find.text('Salvar vínculos'));

    final put = api.recebidas.singleWhere((r) => r.metodo == 'PUT');
    expect(put.corpo, {
      'epiIds': [5, 6],
    });
    expect(
      find.text('EPIs da tarefa atualizados com sucesso (2 vinculados).'),
      findsOneWidget,
    );
  });

  testWidgets('recusa do servidor mantém a seleção', (tester) async {
    api.responder = (r) => r.metodo == 'PUT'
        ? throw falhaComMensagem(404, 'EPI não encontrado: 6')
        : responder(r);
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas/1');

    await tocar(tester, find.text('Vincular EPIs'));
    await tocar(
      tester,
      find.widgetWithText(CheckboxListTile, 'Luva de vaqueta'),
    );
    await tocar(tester, find.text('Salvar vínculos'));

    expect(find.text('EPI não encontrado: 6'), findsOneWidget);
    expect(find.text('2 EPIs selecionados no total'), findsOneWidget);
  });
}

// Testes da classificação (PATCH com o grau, mensagem de sucesso, falha do servidor) e do histórico
// da tarefa, que só o Gestor vê.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/features/tarefas/data/tarefa.dart';

import '../../apoio.dart';
import 'apoio_tarefas.dart';

const historicoJson = [
  {
    'id': 8,
    'tipoOperacao': 'ALTERACAO',
    'dataHora': '2026-11-21T08:00:00',
    'responsavel': 'gestor.demo@safeplace.com',
    'descricao': 'Classificação: de Médio para Alto',
  },
  {
    'id': 7,
    'tipoOperacao': 'CADASTRO',
    'dataHora': '2026-11-20T10:15:30',
    'responsavel': null,
    'descricao': null,
  },
];

void main() {
  late ApiUsuariosFalsa api;

  Object? responder(Requisicao r) => switch ((r.metodo, r.caminho)) {
    ('GET', '/tarefas/1/historico') => historicoJson,
    ('GET', '/tarefas/2/historico') => <Object?>[],
    ('PATCH', '/tarefas/2/classificacao') => {
      ...limpezaJson,
      'nivelPerigo': 'CRITICO',
      'classificada': true,
    },
    _ => responderTarefas(r),
  };

  setUp(() => api = ApiUsuariosFalsa(responder));

  test('histórico com campos ausentes não derruba a leitura', () {
    final registros = lerHistorico([
      {'id': 'x'},
    ]);
    expect(registros.single.id, isNull);
    expect(registros.single.dataHora, isNull);
  });

  testWidgets('Gestor vê o histórico do mais recente ao mais antigo', (
    tester,
  ) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas/1');

    await tester.scrollUntilVisible(find.text('Cadastro'), 200);
    expect(find.text('Alteração'), findsOneWidget);
    expect(find.text('Classificação: de Médio para Alto'), findsOneWidget);
    expect(find.text('Por gestor.demo@safeplace.com'), findsOneWidget);
    expect(find.text('Por responsável não registrado'), findsOneWidget);
    expect(find.text('Reavaliar risco'), findsOneWidget);
  });

  testWidgets('Supervisor não consulta o histórico', (tester) async {
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/tarefas/1',
    );

    expect(find.text('Histórico da tarefa'), findsNothing);
    expect(find.text('Reavaliar risco'), findsNothing);
    expect(
      api.recebidas.where((r) => r.caminho.endsWith('/historico')),
      isEmpty,
    );
  });

  testWidgets('Gestor classifica tarefa pendente', (tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas/2');

    await tocar(tester, find.widgetWithText(FilledButton, 'Classificar'));
    await tocar(tester, find.text('Salvar classificação'));
    expect(find.text('Escolha o grau de periculosidade.'), findsOneWidget);

    await tocar(tester, find.text('Grau de periculosidade'));
    await tocar(tester, find.text('Crítico').last);
    await tocar(tester, find.text('Salvar classificação'));

    final patch = api.recebidas.singleWhere((r) => r.metodo == 'PATCH');
    expect(patch.corpo, {'nivelPerigo': 'CRITICO'});
    expect(
      find.text('Grau de periculosidade atualizado para Crítico.'),
      findsOneWidget,
    );
    expect(
      api.recebidas.where((r) => r.caminho == '/tarefas/2/historico'),
      hasLength(2),
      reason: 'o histórico é buscado de novo depois de classificar',
    );
  });

  testWidgets('recusa do servidor aparece no formulário', (tester) async {
    api.responder = (r) => r.metodo == 'PATCH'
        ? throw falhaComMensagem(
            403,
            'Classificação exige um Gestor de Segurança identificável.',
          )
        : responder(r);
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas/1');

    await tocar(tester, find.text('Reavaliar risco'));
    await tocar(tester, find.text('Salvar classificação'));

    expect(find.text('Não foi possível classificar'), findsOneWidget);
    expect(
      find.text('Classificação exige um Gestor de Segurança identificável.'),
      findsOneWidget,
    );
  });
}

// Testes de widget da tela de listagem de empréstimos (/emprestimos):
// destaque de devolução atrasada, permissão para Gestor e Supervisor,
// filtros, estado vazio e detalhes do empréstimo.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/emprestimos/data/emprestimo.dart';
import 'package:safeplace/features/emprestimos/data/emprestimos_api.dart';
import 'package:safeplace/features/emprestimos/presentation/componentes_emprestimo.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import '../usuarios/apoio_usuarios.dart'
    show abrirApp, campo, sessaoSupervisor, tocar;
import 'apoio_emprestimos.dart';

final emprestimoNoPrazoJson = <String, Object?>{
  'id': 1,
  'epiId': 10,
  'epiNome': 'Óculos de Proteção Ampla Visão',
  'colaboradorId': 3,
  'colaboradorNome': 'João da Silva',
  'responsavelEntregaId': 1,
  'dataRetirada': '2026-11-20T08:00:00',
  'dataDevolucaoPrevista': '2026-11-25',
  'dataDevolucaoReal': null,
  'responsavelDevolucaoId': null,
  'estadoDevolucao': null,
  'status': 'INDISPONIVEL',
  'atrasado': false,
  'termoCautela': 'Termo de cautela assinado digitalmente.',
  'justificativaSubstituicao': null,
};

final emprestimoAtrasadoJson = <String, Object?>{
  'id': 2,
  'epiId': 20,
  'epiNome': 'Luva de Vaqueta Cano Curto',
  'colaboradorId': 4,
  'colaboradorNome': 'Maria Souza',
  'responsavelEntregaId': 1,
  'dataRetirada': '2026-11-10T08:00:00',
  'dataDevolucaoPrevista': '2026-11-20',
  'dataDevolucaoReal': null,
  'responsavelDevolucaoId': null,
  'estadoDevolucao': null,
  'status': 'INDISPONIVEL',
  'atrasado': true,
  'termoCautela': 'Termo de cautela da Luva.',
  'justificativaSubstituicao': null,
};

void main() {
  late ApiEmprestimosFalsa api;

  setUp(() {
    api = ApiEmprestimosFalsa((r) {
      if (r.caminho == '/emprestimos') {
        return [emprestimoNoPrazoJson, emprestimoAtrasadoJson];
      }
      throw UnimplementedError('Chamada inesperada: $r');
    });
  });

  Finder botao(String texto) => find.widgetWithText(FilledButton, texto);

  testWidgets('lista destaca o empréstimo com devolução atrasada', (tester) async {
    final semantica = tester.ensureSemantics();
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');

    expect(find.text('Óculos de Proteção Ampla Visão'), findsOneWidget);
    expect(find.text('Luva de Vaqueta Cano Curto'), findsOneWidget);

    // Aviso do total de atrasos no topo
    expect(
      find.text('1 empréstimo com devolução atrasada.'),
      findsOneWidget,
    );

    // Destaque visual no cartão atrasado
    expect(find.text('Devolução atrasada'), findsOneWidget);
    expect(find.text('Atrasado'), findsOneWidget);
    expect(
      find.descendant(
        of: find.byType(SeloSituacaoEmprestimo),
        matching: find.text('Em posse'),
      ),
      findsOneWidget,
    );

    // Semântica acessível para leitores de tela
    expect(
      find.bySemanticsLabel(
        RegExp('Devolução atrasada, prazo venceu em 20/11/2026'),
      ),
      findsOneWidget,
    );

    semantica.dispose();
  });

  testWidgets('Supervisor consulta a mesma lista e visualiza botões de ação', (tester) async {
    await abrirApp(tester, sessao: sessaoSupervisor, api: api, rota: '/emprestimos');

    expect(find.text('Óculos de Proteção Ampla Visão'), findsOneWidget);
    expect(find.text('Luva de Vaqueta Cano Curto'), findsOneWidget);
    expect(botao('Novo empréstimo'), findsOneWidget);
    expect(find.text('Devolver'), findsNWidgets(2));
  });

  testWidgets('filtra empréstimos pela busca textual local', (tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');

    await tester.enterText(campo('Buscar por colaborador ou EPI'), 'Óculos');
    await tester.pumpAndSettle();

    expect(find.text('Óculos de Proteção Ampla Visão'), findsOneWidget);
    expect(find.text('Luva de Vaqueta Cano Curto'), findsNothing);
  });

  testWidgets('exibe estado vazio quando não há registros', (tester) async {
    api = ApiEmprestimosFalsa((r) => r.caminho == '/emprestimos' ? [] : null);

    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');

    expect(find.text('Nenhum EPI em posse'), findsOneWidget);
    expect(
      find.text(
        'Os empréstimos ativos aparecerão aqui com o colaborador e o prazo de devolução.',
      ),
      findsOneWidget,
    );
  });

  testWidgets('abre diálogo com os detalhes completos do empréstimo', (tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');

    await tocar(tester, find.text('Detalhes').first);

    expect(find.text('Detalhes do empréstimo'), findsOneWidget);
    expect(find.text('Termo de cautela assinado digitalmente.'), findsOneWidget);
    expect(find.textContaining('Código do EPI'), findsOneWidget);

    await tocar(tester, find.text('Fechar'));
    expect(find.text('Detalhes do empréstimo'), findsNothing);
  });

  testWidgets('modo offline: exibe aviso e desabilita ações de escrita', (tester) async {
    final cache = ArmazenamentoCacheEmMemoria();
    const filtro = FiltroEmprestimos(situacao: SituacaoConsulta.pendentes);
    await CacheOffline(cache).guardar(
      sessaoGestor.email,
      filtro.chaveCache,
      [emprestimoNoPrazoJson],
    );

    api.responder = (_) => throw const FalhaApi();

    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/emprestimos',
      cache: cache,
    );

    expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
    expect(find.text('Óculos de Proteção Ampla Visão'), findsOneWidget);
    expect(
      find.text('Ações de escrita voltam quando a conexão voltar.'),
      findsOneWidget,
    );

    final botaoNovo = tester.widget<FilledButton>(botao('Novo empréstimo'));
    expect(botaoNovo.onPressed, isNull);
  });
}

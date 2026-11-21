// Testes de widget do cadastro de EPI: botão só do Gestor, campos obrigatórios, corpo enviado sem
// os opcionais vazios, recusa do backend exibida com os dados mantidos e modo offline.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/epis/data/epi.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import '../usuarios/apoio_usuarios.dart'
    show abrirApp, campo, falhaComMensagem, sessaoSupervisor, tocar;
import 'apoio_epis.dart';
import 'epi_test.dart' show capaceteJson;

void main() {
  late ApiEpisFalsa api;

  Object? responderLista(Requisicao r) => r.caminho == '/epis'
      ? [capaceteJson]
      : throw UnimplementedError('Chamada inesperada: $r');

  setUp(() => api = ApiEpisFalsa(responderLista));

  Iterable<Requisicao> envios() =>
      api.recebidas.where((r) => r.metodo == 'POST');

  Finder botao(String texto) => find.widgetWithText(FilledButton, texto);

  Future<void> abrirFormulario(WidgetTester tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis');
    await tocar(tester, botao('Cadastrar EPI'));
  }

  Future<void> preencherObrigatorios(WidgetTester tester) async {
    await tester.enterText(campo('Nome do EPI'), ' Protetor auricular ');
    await tester.enterText(campo('Número do CA'), '5745');
    await tester.enterText(campo('Validade do CA'), '30062028');
    await tester.enterText(campo('Quantidade inicial'), '40');
    await tester.enterText(campo('Estoque mínimo'), '10');
    await tester.ensureVisible(
      find.widgetWithText(
        DropdownButtonFormField<ClassificacaoEpi>,
        'Classificação',
      ),
    );
    await tocar(
      tester,
      find.widgetWithText(
        DropdownButtonFormField<ClassificacaoEpi>,
        'Classificação',
      ),
    );
    await tocar(tester, find.text('Proteção auditiva').last);
  }

  Future<void> salvar(WidgetTester tester) async {
    await tester.ensureVisible(botao('Cadastrar EPI'));
    await tocar(tester, botao('Cadastrar EPI'));
  }

  testWidgets('Supervisor não vê o botão de cadastro', (tester) async {
    await abrirApp(tester, sessao: sessaoSupervisor, api: api, rota: '/epis');

    expect(find.text('Capacete de Segurança com Aba Frontal'), findsOneWidget);
    expect(botao('Cadastrar EPI'), findsNothing);
  });

  testWidgets('campos obrigatórios vazios não são enviados', (tester) async {
    await abrirFormulario(tester);
    await salvar(tester);

    for (final mensagem in [
      'Informe o nome do EPI.',
      'Informe o número do CA.',
      'Informe a validade do CA.',
      'Escolha a classificação do EPI.',
    ]) {
      expect(find.text(mensagem), findsOneWidget);
    }
    expect(
      find.text('Informe um número inteiro, 0 ou mais.'),
      findsNWidgets(2),
    );
    expect(envios(), isEmpty);
  });

  testWidgets('cadastro envia só os campos preenchidos e volta com aviso', (
    tester,
  ) async {
    await abrirFormulario(tester);
    api.responder = (r) => r.metodo == 'POST'
        ? {...capaceteJson, 'id': 9, 'nome': 'Protetor auricular'}
        : responderLista(r);
    await preencherObrigatorios(tester);
    await tester.enterText(campo('Marca'), ' 3M ');
    await tester.enterText(campo('Data de fabricação'), '01022026');
    await salvar(tester);

    expect(envios().single.caminho, '/epis');
    expect(envios().single.corpo, {
      'nome': 'Protetor auricular',
      'numeroCa': '5745',
      'quantidade': 40,
      'estoqueMinimo': 10,
      'dataValidadeCa': '2028-06-30',
      'classificacao': 'PROTECAO_AUDITIVA',
      'dataFabricacao': '2026-02-01',
      'marca': '3M',
    });
    expect(
      find.text('EPI cadastrado. A listagem foi atualizada.'),
      findsOneWidget,
    );
    expect(
      api.recebidas.where((r) => r.caminho == '/epis' && r.metodo == 'GET'),
      hasLength(2), // leitura inicial e nova leitura depois do cadastro
    );
  });

  testWidgets('data opcional inválida é barrada antes de enviar', (
    tester,
  ) async {
    await abrirFormulario(tester);
    await preencherObrigatorios(tester);
    await tester.enterText(campo('Validade do lote'), '31022030');
    await salvar(tester);

    expect(
      find.text('Informe uma data válida, no formato dd/mm/aaaa.'),
      findsOneWidget,
    );
    expect(envios(), isEmpty);
  });

  testWidgets('recusa do backend aparece e os dados ficam preenchidos', (
    tester,
  ) async {
    await abrirFormulario(tester);
    api.responder = (r) => r.metodo == 'POST'
        ? throw falhaComMensagem(400, 'Número do CA é obrigatório')
        : responderLista(r);
    await preencherObrigatorios(tester);
    await salvar(tester);

    expect(find.text('Cadastro não concluído'), findsOneWidget);
    expect(find.text('Número do CA é obrigatório'), findsOneWidget);
    expect(find.text(' Protetor auricular '), findsOneWidget);
  });

  testWidgets('modo offline desabilita o cadastro com o motivo', (
    tester,
  ) async {
    final cache = ArmazenamentoCacheEmMemoria();
    await CacheOffline(cache)
        .guardar(sessaoGestor.email, 'epis', [capaceteJson]);
    api.responder = (_) => throw const FalhaApi();

    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/epis',
      cache: cache,
    );

    expect(
      tester.widget<FilledButton>(botao('Cadastrar EPI')).onPressed,
      isNull,
    );
    expect(
      find.text('Cadastros e alterações voltam quando a conexão voltar.'),
      findsOneWidget,
    );
  });
}

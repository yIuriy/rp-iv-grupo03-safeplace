import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/shared/widgets/aviso.dart';
import 'package:safeplace/shared/widgets/campos.dart';
import 'package:safeplace/shared/widgets/dialogo_confirmacao.dart';
import 'package:safeplace/shared/widgets/estados.dart';
import 'package:safeplace/shared/widgets/seletor_com_busca.dart';

import '../apoio_tema.dart';

void main() {
  group('Aviso', () {
    testWidgets('mostra título, mensagem e a ação de recuperação', (
      tester,
    ) async {
      var tentativas = 0;
      await montarComTema(
        tester,
        Aviso(
          tom: TomAviso.perigo,
          titulo: 'Não foi possível salvar',
          mensagem: 'Confira a conexão.',
          rotuloAcao: 'Tentar novamente',
          aoAgir: () => tentativas++,
        ),
      );

      expect(find.text('Não foi possível salvar'), findsOneWidget);
      expect(find.text('Confira a conexão.'), findsOneWidget);
      await tester.tap(find.text('Tentar novamente'));
      expect(tentativas, 1);
    });

    testWidgets(
      'anunciar marca o aviso como região viva para o leitor de tela',
      (tester) async {
        await montarComTema(
          tester,
          const Aviso(titulo: 'Salvo', anunciar: true),
        );

        final semantica = tester.getSemantics(find.byType(Aviso));
        expect(semantica.flagsCollection.isLiveRegion, isTrue);
      },
    );

    testWidgets('modo offline mostra a última sincronização', (tester) async {
      await montarComTema(
        tester,
        AvisoOffline(sincronizadoEm: DateTime(2026, 11, 5, 14, 30)),
      );

      expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
      expect(
        find.textContaining('Última sincronização em 05/11/2026 14:30.'),
        findsOneWidget,
      );
    });

    testWidgets(
      'modo offline sem sincronização avisa que os dados não foram atualizados',
      (tester) async {
        await montarComTema(tester, const AvisoOffline());

        expect(
          find.textContaining('Não foi possível atualizar os dados'),
          findsOneWidget,
        );
      },
    );
  });

  group('Diálogo de confirmação', () {
    Future<void> abrir(
      WidgetTester tester,
      Future<void> Function(BuildContext) aoTocar,
    ) async {
      await montarComTema(
        tester,
        Builder(
          builder: (context) => FilledButton(
            onPressed: () => aoTocar(context),
            child: const Text('Abrir'),
          ),
        ),
      );
      await tester.tap(find.text('Abrir'));
      await tester.pumpAndSettle();
    }

    testWidgets('devolve true ao confirmar e false ao desistir', (
      tester,
    ) async {
      final respostas = <bool>[];
      Future<void> pedir(BuildContext context) async => respostas.add(
        await confirmar(
          context,
          titulo: 'Descartar alterações?',
          descricao: 'O que foi preenchido será perdido.',
          rotuloConfirmar: 'Descartar',
        ),
      );

      await abrir(tester, pedir);
      expect(find.text('Descartar alterações?'), findsOneWidget);
      await tester.tap(find.text('Descartar'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Abrir'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Continuar editando'));
      await tester.pumpAndSettle();

      expect(respostas, [true, false]);
    });

    testWidgets(
      'com operação: falha fica no diálogo e a segunda tentativa fecha',
      (tester) async {
        var chamadas = 0;
        bool? resposta;
        Future<void> pedir(BuildContext context) async =>
            resposta = await confirmar(
              context,
              titulo: 'Arquivar ocorrência?',
              descricao: 'Ela sai da lista de ativas.',
              rotuloConfirmar: 'Arquivar',
              aoConfirmar: () async {
                chamadas++;
                if (chamadas == 1) throw Exception('falha');
              },
              descreverFalha: (_) => 'O servidor recusou. Tente de novo.',
            );

        await abrir(tester, pedir);
        await tester.tap(find.text('Arquivar'));
        await tester.pumpAndSettle();

        expect(find.text('O servidor recusou. Tente de novo.'), findsOneWidget);
        expect(find.text('Arquivar ocorrência?'), findsOneWidget);

        await tester.tap(find.text('Arquivar'));
        await tester.pumpAndSettle();

        expect(find.text('Arquivar ocorrência?'), findsNothing);
        expect(chamadas, 2);
        expect(resposta, isTrue);
      },
    );
  });

  group('Campos', () {
    testWidgets('erro da validação local aparece embaixo do campo', (
      tester,
    ) async {
      final formulario = GlobalKey<FormState>();
      await montarComTema(
        tester,
        Form(
          key: formulario,
          child: CampoTexto(
            rotulo: 'Nome completo',
            validador: obrigatorio('Informe o nome completo.'),
          ),
        ),
      );

      expect(formulario.currentState!.validate(), isFalse);
      await tester.pump();

      expect(find.text('Informe o nome completo.'), findsOneWidget);
    });

    testWidgets(
      'erro do servidor aparece e some quando a pessoa altera o campo',
      (tester) async {
        await montarComTema(
          tester,
          const CampoTexto(
            rotulo: 'CPF',
            erroDoServidor: 'Já existe um cadastro com esse CPF.',
          ),
        );

        expect(
          find.text('Já existe um cadastro com esse CPF.'),
          findsOneWidget,
        );
        await tester.enterText(find.byType(TextField), '1');
        await tester.pump();

        expect(find.text('Já existe um cadastro com esse CPF.'), findsNothing);
      },
    );

    testWidgets('seleção mostra as opções e devolve o valor escolhido', (
      tester,
    ) async {
      String? escolhido;
      await montarComTema(
        tester,
        CampoSelecao<String>(
          rotulo: 'Nível de perigo',
          opcoes: const [
            (valor: 'BAIXO', rotulo: 'Baixo'),
            (valor: 'ALTO', rotulo: 'Alto'),
          ],
          aoMudar: (valor) => escolhido = valor,
        ),
      );

      await tester.tap(find.text('Nível de perigo'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Alto').last);
      await tester.pumpAndSettle();

      expect(escolhido, 'ALTO');
    });
  });

  group('Seletor com busca', () {
    const pessoas = ['João da Silva', 'Joana Ribeiro', 'Ana Souza'];

    testWidgets('busca ignora acentos e o item tocado vira o valor', (
      tester,
    ) async {
      String? escolhido;
      await montarComTema(
        tester,
        SeletorComBusca<String>(
          rotulo: 'Colaborador',
          opcoes: pessoas,
          descrever: (p) => p,
          aoMudar: (p) => escolhido = p,
        ),
      );

      await tester.tap(find.text('Toque para escolher'));
      await tester.pumpAndSettle();
      await tester.enterText(find.widgetWithText(TextField, 'Buscar'), 'joao');
      await tester.pumpAndSettle();

      expect(find.text('1 resultado'), findsOneWidget);
      await tester.tap(find.text('João da Silva'));
      await tester.pumpAndSettle();

      expect(escolhido, 'João da Silva');
      expect(find.text('João da Silva'), findsOneWidget);
    });

    testWidgets('busca sem resultado mostra o estado vazio', (tester) async {
      await montarComTema(
        tester,
        SeletorComBusca<String>(
          rotulo: 'Colaborador',
          opcoes: pessoas,
          descrever: (p) => p,
          aoMudar: (_) {},
        ),
      );

      await tester.tap(find.text('Toque para escolher'));
      await tester.pumpAndSettle();
      await tester.enterText(find.widgetWithText(TextField, 'Buscar'), 'zzz');
      await tester.pumpAndSettle();

      expect(find.text('Nada encontrado'), findsOneWidget);
    });

    testWidgets(
      'valida no envio do formulário e mostra a falha de carregamento',
      (tester) async {
        final formulario = GlobalKey<FormState>();
        await montarComTema(
          tester,
          Form(
            key: formulario,
            child: Column(
              children: [
                SeletorComBusca<String>(
                  rotulo: 'Colaborador',
                  opcoes: pessoas,
                  descrever: (p) => p,
                  aoMudar: (_) {},
                  validator: (valor) =>
                      valor == null ? 'Escolha o colaborador.' : null,
                ),
                SeletorComBusca<String>(
                  rotulo: 'EPI',
                  opcoes: const [],
                  descrever: (p) => p,
                  aoMudar: (_) {},
                  falha: 'Não foi possível carregar os EPIs.',
                ),
              ],
            ),
          ),
        );

        formulario.currentState!.validate();
        await tester.pump();

        expect(find.text('Escolha o colaborador.'), findsOneWidget);
        expect(find.text('Não foi possível carregar os EPIs.'), findsOneWidget);
      },
    );
  });

  group('Estados', () {
    testWidgets('estado vazio com ação', (tester) async {
      var acionado = false;
      await montarComTema(
        tester,
        EstadoVazio(
          titulo: 'Nenhum colaborador',
          descricao: 'Cadastre o primeiro colaborador.',
          rotuloAcao: 'Cadastrar colaborador',
          aoAgir: () => acionado = true,
        ),
      );

      await tester.tap(find.text('Cadastrar colaborador'));
      expect(acionado, isTrue);
    });

    Widget conteudo(AsyncValue<List<int>> valor, {VoidCallback? aoTentar}) =>
        SizedBox(
          height: 400,
          child: ConteudoAssincrono<int>(
            valor: valor,
            carregando: 'Carregando números...',
            falha: 'Não foi possível carregar os números.',
            vazio: const EstadoVazio(
              titulo: 'Nenhum número',
              descricao: 'A lista está vazia.',
            ),
            aoTentarDeNovo: aoTentar ?? () {},
            construir: (itens) => Text('Itens: ${itens.join(', ')}'),
          ),
        );

    testWidgets('consulta: carregando, vazia e com dados', (tester) async {
      await montarComTema(tester, conteudo(const AsyncValue.loading()));
      expect(find.text('Carregando números...'), findsOneWidget);

      await montarComTema(tester, conteudo(const AsyncValue.data([])));
      expect(find.text('Nenhum número'), findsOneWidget);

      await montarComTema(tester, conteudo(const AsyncValue.data([1, 2])));
      expect(find.text('Itens: 1, 2'), findsOneWidget);
    });

    testWidgets('consulta com falha oferece tentar novamente', (tester) async {
      var tentativas = 0;
      await montarComTema(
        tester,
        conteudo(
          AsyncValue.error(Exception('rede'), StackTrace.empty),
          aoTentar: () => tentativas++,
        ),
      );

      expect(
        find.text('Não foi possível carregar os números.'),
        findsOneWidget,
      );
      await tester.tap(find.text('Tentar novamente'));
      expect(tentativas, 1);
    });
  });
}

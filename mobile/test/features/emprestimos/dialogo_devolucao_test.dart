// Testes de widget do registro de devolução de EPI (POST /api/emprestimos/{id}/devolucao):
// escolha de condição (APTO, MANUTENCAO, DESCARTE), atualização dinâmica do destino,
// envio correto e tratamento de erro 409 (empréstimo já encerrado).

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/emprestimos/presentation/dialogo_devolucao.dart';

import '../../apoio.dart';
import '../usuarios/apoio_usuarios.dart' show abrirApp, tocar;
import 'apoio_emprestimos.dart';

void main() {
  late ApiEmprestimosFalsa api;

  final emprestimoAtivo = {
    ...emprestimoExemploJson,
    'id': 42,
    'epiNome': 'Protetor Auditivo Plug',
    'colaboradorNome': 'Roberto Dias',
    'atrasado': true,
  };

  setUp(() {
    api = ApiEmprestimosFalsa((r) {
      if (r.caminho == '/emprestimos') {
        return [emprestimoAtivo];
      }
      throw UnimplementedError('Chamada inesperada: $r');
    });
  });

  Finder botao(String texto) => find.widgetWithText(FilledButton, texto);

  testWidgets('fluxo de devolução com validação, destino dinâmico e envio', (tester) async {
    Requisicao? requisicaoDevolucao;

    api = ApiEmprestimosFalsa((r) {
      if (r.caminho == '/emprestimos') return [emprestimoAtivo];
      if (r.metodo == 'POST' && r.caminho == '/emprestimos/42/devolucao') {
        requisicaoDevolucao = r;
        return {
          ...devolucaoExemploJson,
          'emprestimoId': 42,
          'estadoDevolucao': 'APTO',
        };
      }
      throw UnimplementedError('Chamada inesperada: $r');
    });

    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');

    // 1. Abre o diálogo de devolução
    await tocar(tester, find.text('Devolver'));

    expect(find.byType(DialogoDevolucao), findsOneWidget);
    expect(find.text('Registrar devolução'), findsOneWidget);
    expect(
      find.descendant(
        of: find.byType(DialogoDevolucao),
        matching: find.textContaining('Roberto Dias'),
      ),
      findsOneWidget,
    );
    expect(
      find.descendant(
        of: find.byType(DialogoDevolucao),
        matching: find.textContaining('Protetor Auditivo Plug'),
      ),
      findsOneWidget,
    );
    expect(find.textContaining('(prazo vencido)'), findsOneWidget);

    // 2. Tentar confirmar sem selecionar condição gera erro de validação
    await tocar(tester, botao('Confirmar devolução'));
    expect(
      find.text('Informe a condição do item devolvido.'),
      findsOneWidget,
    );
    expect(requisicaoDevolucao, isNull);

    // 3. Selecionar cada condição atualiza a explicação do destino
    await tocar(tester, find.text('Apto para uso'));
    expect(
      find.textContaining('voltará ao saldo disponível do estoque.'),
      findsOneWidget,
    );

    await tocar(tester, find.text('Encaminhar para manutenção'));
    expect(
      find.textContaining('não voltará ao saldo disponível.'),
      findsOneWidget,
    );

    await tocar(tester, find.text('Indicar descarte'));
    expect(
      find.textContaining('baixa definitiva não é registrada nesta tela.'),
      findsOneWidget,
    );

    // 4. Seleciona APTO e confirma
    await tocar(tester, find.text('Apto para uso'));
    await tocar(tester, botao('Confirmar devolução'));

    // 5. Verifica se o corpo enviado corresponde ao contrato da API
    expect(requisicaoDevolucao?.caminho, '/emprestimos/42/devolucao');
    expect(requisicaoDevolucao?.corpo, {'estado': 'APTO'});

    // Diálogo é fechado e feedback exibido
    expect(find.byType(DialogoDevolucao), findsNothing);
    expect(
      find.textContaining('voltou ao saldo disponível'),
      findsOneWidget,
    );
  });

  testWidgets('trata 409 quando empréstimo já foi encerrado por outro operador', (tester) async {
    api = ApiEmprestimosFalsa((r) {
      if (r.caminho == '/emprestimos') return [emprestimoAtivo];
      if (r.metodo == 'POST' && r.caminho == '/emprestimos/42/devolucao') {
        throw FalhaApi(
          status: 409,
          corpo: {
            'status': 409,
            'error': 'Empréstimo Encerrado',
            'message': 'Este empréstimo já se encontra encerrado.',
          },
        );
      }
      throw UnimplementedError('Chamada inesperada: $r');
    });

    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');
    await tocar(tester, find.text('Devolver'));

    await tocar(tester, find.text('Apto para uso'));
    await tocar(tester, botao('Confirmar devolução'));

    expect(find.text('Devolução não registrada'), findsOneWidget);
    expect(
      find.text('Este empréstimo já se encontra encerrado.'),
      findsOneWidget,
    );
  });
}

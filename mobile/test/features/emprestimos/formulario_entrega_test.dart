// Testes de widget do formulário de novo empréstimo (/emprestimos):
// cobertura completa do fluxo de duplicidade (409) com etapa de justificativa
// obrigatória e conclusão com termo de cautela, além de bloqueios 422 da regra de negócio.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/emprestimos/presentation/formulario_entrega_page.dart';

import '../../apoio.dart';
import '../epis/epi_test.dart' show capaceteJson;
import '../usuarios/apoio_usuarios.dart'
    show abrirApp, campo, joaoJson, sessaoGestor, tocar;
import 'apoio_emprestimos.dart';

void main() {
  late ApiEmprestimosFalsa api;

  Finder botao(String texto) => find.widgetWithText(FilledButton, texto);

  testWidgets('fluxo completo de duplicidade (409) com confirmação e justificativa', (tester) async {
    int tentativasEnvio = 0;
    Requisicao? ultimaRequisicao;

    api = ApiEmprestimosFalsa((r) {
      if (r.caminho == '/usuarios/colaboradores') {
        return [joaoJson];
      }
      if (r.caminho == '/epis' || r.caminho == '/emprestimos/epis-disponiveis') {
        return [capaceteJson];
      }
      if (r.metodo == 'POST' && r.caminho == '/emprestimos') {
        tentativasEnvio++;
        ultimaRequisicao = r;
        final corpo = r.corpo as Map<String, Object?>;

        // Na primeira tentativa, simula recusa 409 por duplicidade de tipo em posse
        if (corpo['confirmarSubstituicao'] != true) {
          throw FalhaApi(
            status: 409,
            corpo: {
              'status': 409,
              'error': 'Duplicidade de Posse',
              'message':
                  'Colaborador já possui um EPI deste tipo em posse. Confirme a substituição e informe o motivo.',
              'emprestimoAtivoId': 99,
            },
          );
        }

        // Na segunda tentativa com substituição confirmada, conclui com sucesso
        return {
          ...emprestimoExemploJson,
          'epiNome': 'Capacete de Segurança com Aba Frontal',
          'colaboradorNome': 'João da Silva',
          'confirmarSubstituicao': true,
          'justificativaSubstituicao': corpo['justificativaSubstituicao'],
          'termoCautela':
              'Termo de responsabilidade e cautela registrado sob nº 98765.',
        };
      }
      if (r.caminho == '/emprestimos') {
        return [];
      }
      throw UnimplementedError('Chamada inesperada: $r');
    });

    // 1. Abre a listagem e toca em "Novo empréstimo"
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');
    await tocar(tester, botao('Novo empréstimo'));

    expect(find.byType(FormularioEntregaPage), findsOneWidget);
    expect(find.text('Novo empréstimo'), findsOneWidget);

    // 2. Seleciona colaborador no SeletorComBusca
    await tocar(tester, find.text('Colaborador'));
    await tocar(tester, find.text('João da Silva').last);

    // 3. Seleciona EPI no SeletorComBusca
    await tocar(tester, find.text('EPI'));
    await tocar(
      tester,
      find.text('Capacete de Segurança com Aba Frontal').last,
    );

    // 4. Preenche a data prevista de devolução
    await tester.enterText(
      campo('Data prevista de devolução'),
      '25/11/2026',
    );
    await tester.pumpAndSettle();

    // 5. Envia o formulário
    await tocar(tester, botao('Registrar entrega'));

    // 6. Servidor recusou com 409 -> tela de confirmação de duplicidade
    expect(find.text('Confirmar substituição'), findsNWidgets(2)); // título e botão
    expect(find.text('Item do mesmo tipo em posse'), findsOneWidget);
    expect(
      find.text(
        'Colaborador já possui um EPI deste tipo em posse. Confirme a substituição e informe o motivo.',
      ),
      findsOneWidget,
    );
    expect(campo('Justificativa da substituição'), findsOneWidget);
    expect(tentativasEnvio, 1);

    // 7. Tentativa de confirmar sem justificativa falha na validação local
    await tocar(tester, botao('Confirmar substituição'));
    expect(
      find.text('Informe a justificativa da substituição.'),
      findsOneWidget,
    );

    // 8. Tocar em "Voltar" retorna ao formulário com os dados anteriores preservados
    await tocar(tester, find.text('Voltar'));
    expect(find.text('Novo empréstimo'), findsOneWidget);
    expect(find.text('João da Silva'), findsOneWidget);
    expect(find.text('Capacete de Segurança com Aba Frontal'), findsOneWidget);

    // 9. Submete novamente para voltar à confirmação de duplicidade
    await tocar(tester, botao('Registrar entrega'));
    expect(find.text('Item do mesmo tipo em posse'), findsOneWidget);

    // 10. Preenche a justificativa obrigatória e confirma
    await tester.enterText(
      campo('Justificativa da substituição'),
      'Equipamento anterior quebrou durante o turno.',
    );
    await tester.pumpAndSettle();

    await tocar(tester, botao('Confirmar substituição'));

    // 11. Requisição reenviada com confirmarSubstituicao e justificativa
    expect(tentativasEnvio, 3);
    expect(ultimaRequisicao?.corpo, {
      'colaboradorId': 3,
      'epiId': 1,
      'dataDevolucaoPrevista': '2026-11-25',
      'confirmarSubstituicao': true,
      'justificativaSubstituicao':
          'Equipamento anterior quebrou durante o turno.',
    });

    // 12. Transição para etapa concluída: sucesso e termo de cautela
    expect(find.text('Entrega concluída'), findsOneWidget);
    expect(find.text('Entrega registrada'), findsOneWidget);
    expect(
      find.text('Termo de responsabilidade e cautela registrado sob nº 98765.'),
      findsOneWidget,
    );

    // 13. Fecha a etapa concluída
    await tocar(tester, botao('Fechar'));
    expect(find.byType(FormularioEntregaPage), findsNothing);
  });

  testWidgets('exibe bloqueio 422 com a mensagem do backend', (tester) async {
    api = ApiEmprestimosFalsa((r) {
      if (r.caminho == '/usuarios/colaboradores') return [joaoJson];
      if (r.caminho == '/epis' || r.caminho == '/emprestimos/epis-disponiveis') {
        return [capaceteJson];
      }
      if (r.metodo == 'POST' && r.caminho == '/emprestimos') {
        throw FalhaApi(
          status: 422,
          corpo: {
            'status': 422,
            'error': 'Entrega Impedida',
            'message': 'EPI possui Certificado de Aprovação (CA) vencido.',
          },
        );
      }
      if (r.caminho == '/emprestimos') return [];
      throw UnimplementedError('Chamada inesperada: $r');
    });

    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/emprestimos');
    await tocar(tester, botao('Novo empréstimo'));

    await tocar(tester, find.text('Colaborador'));
    await tocar(tester, find.text('João da Silva').last);

    await tocar(tester, find.text('EPI'));
    await tocar(
      tester,
      find.text('Capacete de Segurança com Aba Frontal').last,
    );

    await tester.enterText(
      campo('Data prevista de devolução'),
      '25/11/2026',
    );
    await tester.pumpAndSettle();

    await tocar(tester, botao('Registrar entrega'));

    expect(find.text('Entrega não registrada'), findsOneWidget);
    expect(
      find.text('EPI possui Certificado de Aprovação (CA) vencido.'),
      findsOneWidget,
    );
  });
}

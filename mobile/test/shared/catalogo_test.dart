import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/app/router.dart';
import 'package:safeplace/core/sessao/controlador_sessao.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:safeplace/shared/catalogo/catalogo_page.dart';
import 'package:safeplace/shared/tema/app_theme.dart';

/// O catálogo (#316) monta todos os componentes compartilhados; se algum quebrar, este teste
/// falha antes de alguma tela de feature.
void main() {
  // A lista do catálogo é o primeiro rolável; campos de texto também rolam por dentro.
  final lista = find.byType(Scrollable).first;

  testWidgets('catálogo monta todas as seções sem erro', (tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 2.625;
    addTearDown(tester.view.reset);

    await tester.pumpWidget(
      MaterialApp(theme: AppTheme.claro(), home: const CatalogoPage()),
    );

    for (final secao in [
      'Tipografia',
      'Cores',
      'Botões',
      'Avisos',
      'Campos',
      'Estados',
    ]) {
      await tester.scrollUntilVisible(find.text(secao), 300, scrollable: lista);
      expect(find.text(secao), findsOneWidget);
    }
    expect(tester.takeException(), isNull);
  });

  testWidgets('validar no catálogo mostra os erros por campo', (tester) async {
    await tester.pumpWidget(
      MaterialApp(theme: AppTheme.claro(), home: const CatalogoPage()),
    );

    // Rola até o formulário de exemplo para ele ser montado e valida como o botão faria.
    await tester.scrollUntilVisible(
      find.text('Validar formulário'),
      300,
      scrollable: lista,
    );
    tester.state<FormState>(find.byType(Form)).validate();
    await tester.pump();

    expect(
      find.text('Informe o nome completo.', skipOffstage: false),
      findsOneWidget,
    );
    expect(
      find.text('Informe um CPF com 11 dígitos.', skipOffstage: false),
      findsOneWidget,
    );
    expect(
      find.text('Escolha o colaborador.', skipOffstage: false),
      findsOneWidget,
    );
  });

  test('catálogo abre sem sessão no modo debug; as outras rotas continuam protegidas', () {
    const semSessao = AsyncValue<EstadoSessao>.data(EstadoSessao());

    expect(redirecionar(semSessao, Rotas.catalogo), isNull);
    expect(redirecionar(semSessao, Rotas.inicio), Rotas.login);
  });
}

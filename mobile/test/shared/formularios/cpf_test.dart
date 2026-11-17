import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/shared/formularios/cpf.dart';
import 'package:safeplace/shared/widgets/campos.dart';

import '../apoio_tema.dart';

void main() {
  group('máscara e dígitos', () {
    test('máscara progressiva, como no web', () {
      expect(mascararCpf(''), '');
      expect(mascararCpf('123'), '123');
      expect(mascararCpf('1234'), '123.4');
      expect(mascararCpf('1234567'), '123.456.7');
      expect(mascararCpf('1234567890'), '123.456.789-0');
      expect(mascararCpf('52998224725'), '529.982.247-25');
    });

    test('descarta o que não é dígito e limita a 11', () {
      expect(digitosDoCpf('529.982.247-25'), '52998224725');
      expect(digitosDoCpf('529982247259999'), '52998224725');
      expect(mascararCpf('abc529x982'), '529.982');
    });

    test('ocultação para listas', () {
      expect(ocultarCpf('52998224725'), '•••.982.247-••');
      expect(ocultarCpf('123'), '123');
    });
  });

  group('validação, com as mensagens do web', () {
    test('CPF válido passa', () {
      expect(validarCpf('529.982.247-25'), isNull);
      expect(validarCpf('11144477735'), isNull);
    });

    test('menos de 11 dígitos', () {
      expect(validarCpf(''), 'Informe um CPF com 11 dígitos.');
      expect(validarCpf(null), 'Informe um CPF com 11 dígitos.');
      expect(validarCpf('529.982.247'), 'Informe um CPF com 11 dígitos.');
    });

    test('dígito verificador errado ou sequência repetida', () {
      expect(validarCpf('529.982.247-26'), 'CPF inválido: confira os dígitos.');
      expect(validarCpf('111.111.111-11'), 'CPF inválido: confira os dígitos.');
      expect(validarCpf('000.000.000-00'), 'CPF inválido: confira os dígitos.');
    });
  });

  testWidgets(
    'campo com a máscara mostra 000.000.000-00 e envia só os dígitos',
    (tester) async {
      final controlador = TextEditingController();
      await montarComTema(
        tester,
        CampoTexto(
          rotulo: 'CPF',
          controlador: controlador,
          formatadores: const [MascaraCpf()],
        ),
      );

      await tester.enterText(find.byType(TextField), '52998224725');

      expect(find.text('529.982.247-25'), findsOneWidget);
      expect(digitosDoCpf(controlador.text), '52998224725');
    },
  );
}

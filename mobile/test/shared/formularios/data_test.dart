import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/leitura.dart';
import 'package:safeplace/shared/formularios/data.dart';

void main() {
  test('formata para a tela e para a API', () {
    final data = DateTime(1988, 4, 2);

    expect(formatarData(data), '02/04/1988');
    expect(dataIso(data), '1988-04-02');
  });

  test('lê dd/mm/aaaa e recusa data incompleta ou inexistente', () {
    expect(lerData('20/04/1988'), DateTime(1988, 4, 20));
    expect(lerData(' 29/02/2024 '), DateTime(2024, 2, 29));
    expect(lerData('29/02/2023'), isNull);
    expect(lerData('31/04/1988'), isNull);
    expect(lerData('20/4/1988'), isNull);
    expect(lerData(''), isNull);
  });

  test('máscara progressiva enquanto a pessoa digita', () {
    expect(mascararData('2'), '2');
    expect(mascararData('204'), '20/4');
    expect(mascararData('20041988'), '20/04/1988');
    expect(mascararData('20/04/19885'), '20/04/1988');

    final saida = const MascaraData().formatEditUpdate(
      TextEditingValue.empty,
      const TextEditingValue(text: '2004'),
    );
    expect(saida.text, '20/04');
    expect(saida.selection.baseOffset, 5);
  });

  test('hoje ignora a hora', () {
    expect(hoje(DateTime(2026, 11, 19, 23, 59)), DateTime(2026, 11, 19));
  });

  test('dataOuNula lê LocalDate e LocalDateTime da API', () {
    expect(dataOuNula('1988-04-20'), DateTime(1988, 4, 20));
    expect(dataOuNula('2026-09-07T10:00:00'), DateTime(2026, 9, 7, 10));
    expect(dataOuNula('20/04/1988'), isNull);
    expect(dataOuNula(20), isNull);
    expect(dataOuNula(null), isNull);
  });
}

// Testes unitários das validações locais do módulo de empréstimos (#320).

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/features/emprestimos/data/emprestimo.dart';
import 'package:safeplace/features/emprestimos/domain/validacao_emprestimo.dart';

void main() {
  final hoje = DateTime(2026, 11, 21);
  final ontem = DateTime(2026, 11, 20);
  final amanha = DateTime(2026, 11, 22);

  group('validarColaborador', () {
    test('exige seleção de colaborador', () {
      expect(
        validarColaborador(null),
        'Selecione o colaborador que recebe o EPI.',
      );
      expect(validarColaborador(12), isNull);
    });
  });

  group('validarEpi', () {
    test('exige seleção de EPI', () {
      expect(validarEpi(null), 'Selecione o EPI que será entregue.');
      expect(validarEpi(5), isNull);
    });
  });

  group('validarDataDevolucaoPrevista', () {
    test('recusa data nula', () {
      expect(
        validarDataDevolucaoPrevista(null, hoje),
        'Informe a data prevista de devolução.',
      );
    });

    test('recusa data anterior a hoje', () {
      expect(
        validarDataDevolucaoPrevista(ontem, hoje),
        'A devolução prevista não pode ser anterior a hoje, dia da entrega.',
      );
    });

    test('aceita hoje e datas futuras', () {
      expect(validarDataDevolucaoPrevista(hoje, hoje), isNull);
      expect(validarDataDevolucaoPrevista(amanha, hoje), isNull);
    });
  });

  group('validarDataDevolucaoTexto', () {
    test('recusa texto vazio', () {
      expect(
        validarDataDevolucaoTexto('', hoje),
        'Informe a data prevista de devolução.',
      );
      expect(
        validarDataDevolucaoTexto('   ', hoje),
        'Informe a data prevista de devolução.',
      );
      expect(
        validarDataDevolucaoTexto(null, hoje),
        'Informe a data prevista de devolução.',
      );
    });

    test('recusa formato inválido ou data inexistente', () {
      expect(
        validarDataDevolucaoTexto('2026-11-21', hoje),
        'Informe uma data válida, no formato dd/mm/aaaa.',
      );
      expect(
        validarDataDevolucaoTexto('31/02/2026', hoje),
        'Informe uma data válida, no formato dd/mm/aaaa.',
      );
    });

    test('recusa data passada em formato texto', () {
      expect(
        validarDataDevolucaoTexto('20/11/2026', hoje),
        'A devolução prevista não pode ser anterior a hoje, dia da entrega.',
      );
    });

    test('aceita data válida e igual ou posterior a hoje', () {
      expect(validarDataDevolucaoTexto('21/11/2026', hoje), isNull);
      expect(validarDataDevolucaoTexto('25/11/2026', hoje), isNull);
    });
  });

  group('validarJustificativa', () {
    test('exige texto não vazio', () {
      expect(
        validarJustificativa(null),
        'Informe a justificativa da substituição.',
      );
      expect(
        validarJustificativa(''),
        'Informe a justificativa da substituição.',
      );
      expect(
        validarJustificativa('   '),
        'Informe a justificativa da substituição.',
      );
      expect(
        validarJustificativa('Item danificado em campo'),
        isNull,
      );
    });
  });

  group('validarEstadoDevolucao', () {
    test('exige condição do item devolvido', () {
      expect(
        validarEstadoDevolucao(null),
        'Informe a condição do item devolvido.',
      );
      expect(validarEstadoDevolucao(EstadoDevolucao.apto), isNull);
      expect(validarEstadoDevolucao(EstadoDevolucao.manutencao), isNull);
      expect(validarEstadoDevolucao(EstadoDevolucao.descarte), isNull);
    });
  });

  group('validarEntrega', () {
    test('reúne erros de campos pendentes', () {
      final erros = validarEntrega(
        colaboradorId: null,
        epiId: null,
        dataDevolucaoPrevista: null,
        dataHoje: hoje,
      );

      expect(erros.temErros, isTrue);
      expect(erros.colaborador, isNotNull);
      expect(erros.epi, isNotNull);
      expect(erros.dataDevolucaoPrevista, isNotNull);
    });

    test('validação passa quando todos os campos estão corretos', () {
      final erros = validarEntrega(
        colaboradorId: 10,
        epiId: 2,
        dataDevolucaoPrevista: amanha,
        dataHoje: hoje,
      );

      expect(erros.temErros, isFalse);
      expect(erros.colaborador, isNull);
      expect(erros.epi, isNull);
      expect(erros.dataDevolucaoPrevista, isNull);
    });
  });
}

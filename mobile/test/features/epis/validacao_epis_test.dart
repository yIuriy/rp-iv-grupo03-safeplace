// Testes das validações locais dos três formulários de EPI: movimentação (portada do web), cadastro
// (regras do `CriarEpiRequest`) e manutenção (data e hora montadas sem `DateTime`).

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/features/epis/data/epi.dart';
import 'package:safeplace/features/epis/data/manutencao.dart';
import 'package:safeplace/features/epis/data/movimentacao.dart';
import 'package:safeplace/features/epis/domain/validacao_cadastro_epi.dart';
import 'package:safeplace/features/epis/domain/validacao_manutencao.dart';
import 'package:safeplace/features/epis/domain/validacao_movimentacao.dart';

void main() {
  group('movimentação', () {
    String? quantidade(
      String? valor,
      TipoMovimentacao tipo, {
      int? saldo = 10,
    }) => validarQuantidadeMovimentacao(valor, tipo: tipo, saldo: saldo);

    test('quantidade precisa ser inteira e maior que zero', () {
      const erro = 'Informe uma quantidade inteira maior que zero.';
      for (final valor in [null, '', '0', '-3', '2.5', 'dez']) {
        expect(
          quantidade(valor, TipoMovimentacao.entrada),
          erro,
          reason: valor,
        );
      }
      expect(quantidade(' 4 ', TipoMovimentacao.entrada), isNull);
    });

    test('saída maior que o saldo é recusada antes de enviar', () {
      expect(
        quantidade('11', TipoMovimentacao.saida),
        'A saída não pode superar o saldo disponível (10).',
      );
      expect(quantidade('10', TipoMovimentacao.saida), isNull);
    });

    test('entrada não tem limite e saldo desconhecido fica com o backend', () {
      expect(quantidade('500', TipoMovimentacao.entrada), isNull);
      expect(quantidade('500', TipoMovimentacao.saida, saldo: null), isNull);
    });

    test('motivo é obrigatório', () {
      expect(validarMotivo('   '), 'Informe o motivo da movimentação.');
      expect(validarMotivo('Reposição'), isNull);
    });
  });

  group('cadastro', () {
    test('nome e CA são obrigatórios', () {
      expect(validarNomeEpi(' '), 'Informe o nome do EPI.');
      expect(validarNumeroCa(null), 'Informe o número do CA.');
      expect(validarNomeEpi('Luva'), isNull);
      expect(validarNumeroCa('12345'), isNull);
    });

    test('quantidade inicial e estoque mínimo aceitam zero', () {
      expect(validarQuantidadeInicial('0'), isNull);
      expect(validarEstoqueMinimo('5'), isNull);
      expect(validarQuantidadeInicial('-1'), isNotNull);
      expect(validarEstoqueMinimo(''), isNotNull);
    });

    test('vida útil é opcional e começa em 1', () {
      expect(validarVidaUtil(''), isNull);
      expect(validarVidaUtil('365'), isNull);
      expect(validarVidaUtil('0'), isNotNull);
    });

    test('validade do CA é obrigatória e as datas de lote são opcionais', () {
      expect(validarValidadeCa(''), 'Informe a validade do CA.');
      expect(validarValidadeCa('31/02/2027'), isNotNull);
      expect(validarValidadeCa('31/12/2027'), isNull);
      expect(validarDataOpcional(''), isNull);
      expect(validarDataOpcional('2027-12-31'), isNotNull);
    });

    test('classificação é obrigatória', () {
      expect(validarClassificacao(null), 'Escolha a classificação do EPI.');
      expect(validarClassificacao(ClassificacaoEpi.protecaoAuditiva), isNull);
    });
  });

  group('manutenção', () {
    test('data e hora obrigatórias e existentes', () {
      expect(validarDataManutencao(''), 'Informe a data da manutenção.');
      expect(validarDataManutencao('30/02/2026'), isNotNull);
      expect(validarDataManutencao('20/11/2026'), isNull);
      expect(validarHoraManutencao(''), 'Informe a hora da manutenção.');
      expect(validarHoraManutencao('24:00'), isNotNull);
      expect(validarHoraManutencao('9:05'), isNotNull);
      expect(validarHoraManutencao('09:05'), isNull);
    });

    test('descrição e resultado obrigatórios', () {
      expect(
        validarDescricaoManutencao(' '),
        'Informe a descrição da manutenção.',
      );
      expect(validarResultado(null), 'Escolha o resultado da manutenção.');
      expect(validarResultado(ResultadoManutencao.reprovado), isNull);
    });

    test('monta o texto ISO só reordenando o que foi digitado', () {
      expect(montarDataHoraIso('20/11/2026', '09:05'), '2026-11-20T09:05');
      expect(montarDataHoraIso(' 01/01/2026 ', ' 00:00 '), '2026-01-01T00:00');
    });
  });
}

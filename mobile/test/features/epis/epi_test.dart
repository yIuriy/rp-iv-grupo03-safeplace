// Testes da leitura do `EpiResponse` em `Epi.ler`: contrato completo, campos ausentes ou de outro
// tipo, enums desconhecidos, igualdade usada pelo seletor e lista inválida.

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/features/epis/data/epi.dart';

const capaceteJson = <String, Object?>{
  'id': 1,
  'nome': 'Capacete de Segurança com Aba Frontal',
  'numeroCa': '12345',
  'quantidade': 25,
  'estoqueMinimo': 5,
  'estoqueCritico': false,
  'status': 'DISPONIVEL',
  'dataValidadeCa': '2027-12-31',
  'vidaUtilDias': 365,
  'descricao': 'Casco de polietileno',
  'classificacao': 'PROTECAO_DE_CABECA',
  'localizacao': 'Almoxarifado A',
  'loteId': 3,
  'numeroLote': 'L-2026-01',
  'notaFiscal': 'NF-889',
  'dataFabricacao': '2026-01-10',
  'validadeLote': '2031-01-10',
  'modeloId': 2,
  'marca': 'MSA',
};

void main() {
  group('Epi.ler', () {
    test('lê o contrato completo do EpiResponse', () {
      final epi = Epi.ler(capaceteJson);

      expect(epi.id, 1);
      expect(epi.nome, 'Capacete de Segurança com Aba Frontal');
      expect(epi.numeroCa, '12345');
      expect(epi.quantidade, 25);
      expect(epi.estoqueMinimo, 5);
      expect(epi.estoqueCritico, isFalse);
      expect(epi.status, StatusEpi.disponivel);
      expect(epi.dataValidadeCa, DateTime(2027, 12, 31));
      expect(epi.vidaUtilDias, 365);
      expect(epi.descricao, 'Casco de polietileno');
      expect(epi.classificacao, ClassificacaoEpi.protecaoDeCabeca);
      expect(epi.localizacao, 'Almoxarifado A');
      expect(epi.numeroLote, 'L-2026-01');
      expect(epi.notaFiscal, 'NF-889');
      expect(epi.dataFabricacao, DateTime(2026, 1, 10));
      expect(epi.validadeLote, DateTime(2031, 1, 10));
      expect(epi.marca, 'MSA');
    });

    test('estoque crítico só com true explícito', () {
      expect(
        Epi.ler({...capaceteJson, 'estoqueCritico': true}).estoqueCritico,
        isTrue,
      );
      expect(
        Epi.ler({...capaceteJson, 'estoqueCritico': 'true'}).estoqueCritico,
        isFalse,
      );
    });

    test('campos ausentes, nulos ou de outro tipo não derrubam a leitura', () {
      final epi = Epi.ler({
        'id': '7',
        'nome': '   ',
        'numeroCa': 12345,
        'quantidade': null,
        'status': 'QUEBRADO',
        'dataValidadeCa': 'amanhã',
        'classificacao': 'PROTECAO_DE_JOELHO',
      });

      expect(epi.id, isNull);
      expect(epi.nome, 'Nome não informado');
      expect(epi.numeroCa, isNull);
      expect(epi.quantidade, isNull);
      expect(epi.estoqueMinimo, isNull);
      expect(epi.estoqueCritico, isFalse);
      expect(epi.status, isNull);
      expect(epi.dataValidadeCa, isNull);
      expect(epi.classificacao, isNull);
      expect(epi.marca, isNull);
    });

    test('registro que não é objeto lança FormatException', () {
      expect(() => Epi.ler('capacete'), throwsFormatException);
    });
  });

  group('enums', () {
    test('todos os status do backend têm rótulo', () {
      expect(StatusEpi.values.map((s) => s.codigo), [
        'DISPONIVEL',
        'EM_USO',
        'EM_MANUTENCAO',
        'ESGOTADO',
        'DESCARTADO',
      ]);
      expect(StatusEpi.ler('EM_MANUTENCAO')?.rotulo, 'Em manutenção');
    });

    test('as nove classificações são lidas pelo código', () {
      expect(ClassificacaoEpi.values, hasLength(9));
      for (final classificacao in ClassificacaoEpi.values) {
        expect(ClassificacaoEpi.ler(classificacao.codigo), classificacao);
      }
    });
  });

  group('igualdade', () {
    test('mesmo id é o mesmo EPI, mesmo com saldo diferente', () {
      expect(
        Epi.ler(capaceteJson),
        Epi.ler({...capaceteJson, 'quantidade': 3}),
      );
    });

    test('sem id, compara nome e CA', () {
      const a = Epi(nome: 'Luva', numeroCa: '1');
      expect(a, const Epi(nome: 'Luva', numeroCa: '1'));
      expect(a, isNot(const Epi(nome: 'Luva', numeroCa: '2')));
      expect(a, isNot(const Epi(id: 1, nome: 'Luva', numeroCa: '1')));
    });
  });

  group('lerEpis', () {
    test('lê cada item da lista', () {
      final epis = lerEpis([
        capaceteJson,
        {...capaceteJson, 'id': 2},
      ]);
      expect(epis.map((e) => e.id), [1, 2]);
    });

    test('resposta que não é lista lança FormatException', () {
      expect(() => lerEpis({'epis': []}), throwsFormatException);
    });
  });
}

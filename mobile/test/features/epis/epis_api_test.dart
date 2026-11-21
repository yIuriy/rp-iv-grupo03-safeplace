// Testes das escritas de `EpisApi` (corpo enviado a cada `Request` do backend, falha repassada à
// tela) e da leitura de movimentações e manutenções, com a data formatada sem `DateTime`.

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/epis/data/epi.dart';
import 'package:safeplace/features/epis/data/epis_api.dart';
import 'package:safeplace/features/epis/data/manutencao.dart';
import 'package:safeplace/features/epis/data/movimentacao.dart';
import 'package:safeplace/features/epis/domain/epi_repository.dart';

import 'apoio_epis.dart';
import 'epi_test.dart' show capaceteJson;

const movimentacaoJson = <String, Object?>{
  'id': 10,
  'epiId': 1,
  'loteId': 3,
  'responsavelId': 1,
  'tipo': 'SAIDA',
  'quantidade': 4,
  'dataHora': '2026-11-20T14:30:00',
  'motivo': 'Troca de turno',
  'saldoAtual': 21,
};

const manutencaoJson = <String, Object?>{
  'id': 5,
  'epiId': 1,
  'dataManutencao': '2026-11-20T09:05:00',
  'tipoManutencao': 'PREVENTIVA',
  'descricao': 'Troca da carneira',
  'resultado': 'APROVADO',
  'responsavelId': 1,
  'statusAtualEpi': 'DISPONIVEL',
};

void main() {
  group('EpisApi', () {
    test('cadastro envia datas ISO e omite opcionais vazios', () async {
      final api = ApiEpisFalsa((_) => capaceteJson);

      final epi = await EpisApi(api).cadastrar(
        DadosCadastroEpi(
          nome: 'Capacete',
          numeroCa: '12345',
          quantidade: 25,
          estoqueMinimo: 5,
          dataValidadeCa: DateTime(2027, 12, 31),
          classificacao: ClassificacaoEpi.protecaoDeCabeca,
          numeroLote: 'L-2026-01',
          validadeLote: DateTime(2031, 1, 10),
        ),
      );

      final requisicao = api.recebidas.single;
      expect(requisicao.metodo, 'POST');
      expect(requisicao.caminho, '/epis');
      expect(requisicao.corpo, {
        'nome': 'Capacete',
        'numeroCa': '12345',
        'quantidade': 25,
        'estoqueMinimo': 5,
        'dataValidadeCa': '2027-12-31',
        'classificacao': 'PROTECAO_DE_CABECA',
        'numeroLote': 'L-2026-01',
        'validadeLote': '2031-01-10',
      });
      expect(epi.id, 1);
    });

    test('movimentação envia tipo, quantidade e motivo', () async {
      final api = ApiEpisFalsa((_) => movimentacaoJson);

      final movimentacao = await EpisApi(api).movimentar(
        1,
        const DadosMovimentacao(
          tipo: TipoMovimentacao.saida,
          quantidade: 4,
          motivo: 'Troca de turno',
        ),
      );

      final requisicao = api.recebidas.single;
      expect(requisicao.caminho, '/epis/1/movimentacoes');
      expect(requisicao.corpo, {
        'tipo': 'SAIDA',
        'quantidade': 4,
        'motivo': 'Troca de turno',
      });
      expect(movimentacao.saldoAtual, 21);
    });

    test('saída maior que o saldo repassa a falha do backend', () async {
      const falha = FalhaApi(
        status: 400,
        corpo: {
          'status': 400,
          'error': 'Bad Request',
          'message': 'Saldo insuficiente',
        },
      );
      final api = ApiEpisFalsa((_) => throw falha);

      expect(
        () => EpisApi(api).movimentar(
          1,
          const DadosMovimentacao(
            tipo: TipoMovimentacao.saida,
            quantidade: 99,
            motivo: 'Teste',
          ),
        ),
        throwsA(same(falha)),
      );
    });

    test('manutenção envia a data como texto e o tipo só quando há', () async {
      final api = ApiEpisFalsa((_) => manutencaoJson);
      final repositorio = EpisApi(api);

      await repositorio.registrarManutencao(
        1,
        const DadosManutencao(
          dataManutencao: '2026-11-20T09:05',
          descricao: 'Troca da carneira',
          resultado: ResultadoManutencao.aprovado,
          tipo: TipoManutencao.preventiva,
        ),
      );
      await repositorio.registrarManutencao(
        1,
        const DadosManutencao(
          dataManutencao: '2026-11-20T09:05',
          descricao: 'Inspeção',
          resultado: ResultadoManutencao.reprovado,
        ),
      );

      expect(api.recebidas.first.caminho, '/epis/1/manutencoes');
      expect(api.recebidas.first.corpo, {
        'dataManutencao': '2026-11-20T09:05',
        'descricao': 'Troca da carneira',
        'resultado': 'APROVADO',
        'tipoManutencao': 'PREVENTIVA',
      });
      expect(api.recebidas.last.corpo, {
        'dataManutencao': '2026-11-20T09:05',
        'descricao': 'Inspeção',
        'resultado': 'REPROVADO',
      });
    });
  });

  group('leituras', () {
    test('movimentação com o contrato completo', () {
      final m = lerMovimentacoes([movimentacaoJson]).single;

      expect(m.tipo, TipoMovimentacao.saida);
      expect(m.quantidade, 4);
      expect(m.dataHora, DateTime(2026, 11, 20, 14, 30));
      expect(m.motivo, 'Troca de turno');
      expect(m.responsavelId, 1);
    });

    test('manutenção com o contrato completo', () {
      final m = lerManutencoes([manutencaoJson]).single;

      expect(m.tipo, TipoManutencao.preventiva);
      expect(m.resultado, ResultadoManutencao.aprovado);
      expect(m.dataManutencao, '2026-11-20T09:05:00');
      expect(m.statusAtualEpi, StatusEpi.disponivel);
    });

    test('campos de outro tipo não derrubam as leituras', () {
      final m = Movimentacao.ler({'tipo': 'TROCA', 'quantidade': '4'});
      final n = Manutencao.ler({'resultado': 'TALVEZ', 'dataManutencao': 7});

      expect(m.tipo, isNull);
      expect(m.quantidade, isNull);
      expect(n.resultado, isNull);
      expect(n.dataManutencao, isNull);
    });
  });

  group('formatarDataHoraIso', () {
    test('recorta data e hora sem converter fuso', () {
      expect(formatarDataHoraIso('2026-11-20T09:05:00'), '20/11/2026 09:05');
      expect(formatarDataHoraIso('2026-01-01T00:00'), '01/01/2026 00:00');
    });

    test('só data e texto fora do formato', () {
      expect(formatarDataHoraIso('2026-11-20'), '20/11/2026');
      expect(formatarDataHoraIso('ontem'), isNull);
      expect(formatarDataHoraIso(null), isNull);
    });
  });
}

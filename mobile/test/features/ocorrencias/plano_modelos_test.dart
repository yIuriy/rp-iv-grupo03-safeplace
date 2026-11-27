// Plano de ação (#321): leitura defensiva da resposta, corpos enviados e validação local com as
// mesmas mensagens do web.

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/features/ocorrencias/data/acao_plano.dart';
import 'package:safeplace/features/ocorrencias/data/ocorrencias_api.dart';
import 'package:safeplace/features/ocorrencias/data/planos_api.dart';
import 'package:safeplace/features/ocorrencias/domain/ocorrencia_repository.dart';
import 'package:safeplace/features/ocorrencias/domain/plano_repository.dart';
import 'package:safeplace/features/ocorrencias/domain/validacao_plano.dart';
import 'package:safeplace/features/ocorrencias/data/ocorrencia.dart';

import 'apoio_plano.dart';

void main() {
  group('AcaoPlano.ler', () {
    test('lê a ação com responsável, alerta e indicadores do servidor', () {
      final acao = AcaoPlano.ler(acaoAtrasadaJson);

      expect(acao.id, 21);
      expect(acao.situacao, SituacaoAcao.aberta);
      expect(acao.aberta, isTrue);
      expect(acao.responsavelTipo, TipoResponsavel.colaborador);
      expect(acao.responsavelNome, 'João da Silva');
      expect(acao.emAtraso, isTrue);
      expect(acao.alertaDevido, isTrue);
      expect(acao.prazo, DateTime(2026, 11, 20));
    });

    test('situação gravada fora do contrato não vira aberta nem concluída', () {
      final acao = AcaoPlano.ler({
        ...acaoAtrasadaJson,
        'situacao': 'PENDENTE',
        'status': 'PENDENTE',
      });

      expect(acao.situacao, isNull);
      expect(acao.aberta, isFalse);
      expect(acao.statusGravado, 'PENDENTE');
    });

    test('sem id ou sem prazo a resposta é recusada', () {
      expect(
        () => AcaoPlano.ler({...acaoAtrasadaJson, 'id': null}),
        throwsFormatException,
      );
      expect(
        () => AcaoPlano.ler({...acaoAtrasadaJson, 'prazo': null}),
        throwsFormatException,
      );
    });
  });

  group('corpos enviados', () {
    test('nova ação leva só o responsável escolhido e a antecedência só com alerta', () {
      final comSetor = corpoDaNovaAcao(
        NovaAcao(
          medidasPreventivas: 'Treinar a equipe',
          prazo: DateTime(2026, 12, 15),
          responsavelTipo: TipoResponsavel.setor,
          responsavelId: 1,
          alertaAtivo: true,
          antecedenciaAlertaDias: 5,
        ),
      );

      expect(comSetor, {
        'medidasPreventivas': 'Treinar a equipe',
        'prazo': '2026-12-15',
        'responsavelSetorId': 1,
        'alertaAtivo': true,
        'antecedenciaAlertaDias': 5,
      });

      final semAlerta = corpoDaNovaAcao(
        NovaAcao(
          medidasCorretivas: 'Trocar a proteção',
          prazo: DateTime(2026, 12, 15),
          responsavelTipo: TipoResponsavel.colaborador,
          responsavelId: 10,
        ),
      );
      expect(semAlerta.containsKey('antecedenciaAlertaDias'), isFalse);
      expect(semAlerta.containsKey('responsavelSetorId'), isFalse);
      expect(semAlerta['responsavelColaboradorId'], 10);
    });

    test('edição da ocorrência não envia o tipo, que não muda', () {
      final corpo = corpoDaAtualizacao(
        const DadosOcorrencia(
          tipo: TipoOcorrencia.acidente,
          dataOcorrencia: '2026-11-10T08:30:00',
          descricao: 'Corte na mão',
          areaId: 3,
          colaboradorId: 10,
          causaRaiz: CausaRaiz.falhaEpi,
        ),
      );

      expect(corpo.containsKey('tipoOcorrencia'), isFalse);
      expect(corpo['descricao'], 'Corte na mão');
      expect(corpo['causaRaiz'], 'FALHA_EPI');
    });
  });

  group('validação local, com as mensagens do web', () {
    final agora = DateTime(2026, 11, 27, 10);

    test('prazo mínimo é o maior entre hoje e o dia do fato', () {
      expect(
        prazoMinimo(dataDoFato: DateTime(2026, 11, 10, 8), agora: agora),
        DateTime(2026, 11, 27),
      );
      expect(
        prazoMinimo(dataDoFato: DateTime(2026, 12, 2, 8), agora: agora),
        DateTime(2026, 12, 2),
      );
    });

    test('nova ação', () {
      final minimo = DateTime(2026, 11, 27);
      expect(
        validarMedidas('', ' '),
        'Informe a medida corretiva, a preventiva ou ambas.',
      );
      expect(validarMedidas('Trocar', null), isNull);
      expect(validarPrazoDaAcao('', minimo), 'Informe o prazo da ação.');
      expect(
        validarPrazoDaAcao('31/02/2026', minimo),
        'Informe uma data válida, no formato dd/mm/aaaa.',
      );
      expect(
        validarPrazoDaAcao('26/11/2026', minimo),
        'O prazo não pode ser anterior a hoje nem à data do fato.',
      );
      expect(validarPrazoDaAcao('27/11/2026', minimo), isNull);
      expect(
        validarTipoDeResponsavel(null),
        'Indique se o responsável é um colaborador ou um setor.',
      );
      expect(
        validarResponsavel(TipoResponsavel.setor, null),
        'Selecione o setor responsável.',
      );
      expect(
        validarResponsavel(TipoResponsavel.colaborador, null),
        'Selecione o colaborador responsável.',
      );
      expect(validarAntecedencia(true, '-1'), startsWith('Com o alerta ativo'));
      expect(validarAntecedencia(true, '0'), isNull);
      expect(validarAntecedencia(false, ''), isNull);
    });

    test('prorrogação', () {
      final atual = DateTime(2026, 11, 30);
      expect(
        validarNovoPrazo('', atual, agora: agora),
        'Informe o novo prazo.',
      );
      expect(
        validarNovoPrazo('30/11/2026', atual, agora: agora),
        'O novo prazo deve ser posterior ao prazo atual.',
      );
      expect(validarNovoPrazo('15/12/2026', atual, agora: agora), isNull);
      expect(
        validarNovoPrazo('25/11/2026', DateTime(2026, 11, 20), agora: agora),
        'O novo prazo não pode ser anterior a hoje.',
      );
      expect(
        validarJustificativa('  '),
        'A justificativa técnica é obrigatória para prorrogar.',
      );
    });

    test('reatribuição recusa o mesmo responsável', () {
      final acao = AcaoPlano.ler(acaoAtrasadaJson);

      expect(
        validarNovoResponsavel(acao, TipoResponsavel.colaborador, null),
        'Selecione o novo responsável.',
      );
      expect(
        validarNovoResponsavel(acao, TipoResponsavel.colaborador, 10),
        'Escolha um responsável diferente do atual.',
      );
      expect(
        validarNovoResponsavel(acao, TipoResponsavel.colaborador, 11),
        isNull,
      );
      expect(validarNovoResponsavel(acao, TipoResponsavel.setor, 10), isNull);
    });
  });
}

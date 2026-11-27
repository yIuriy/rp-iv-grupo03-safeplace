// Testes de unidade do módulo de ocorrências: leitura defensiva das respostas, critérios da consulta,
// corpo do relato enviado ao backend e validações locais (data futura, descrição vazia, período).

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/sessao/sessao.dart';
import 'package:safeplace/features/areas_risco/data/area_risco.dart';
import 'package:safeplace/features/ocorrencias/data/ocorrencia.dart';
import 'package:safeplace/features/ocorrencias/data/ocorrencias_api.dart';
import 'package:safeplace/features/ocorrencias/domain/ocorrencia_repository.dart';
import 'package:safeplace/features/ocorrencias/domain/validacao_relato.dart';

import 'apoio_ocorrencias.dart';

void main() {
  group('Ocorrencia.ler', () {
    test('lê o acidente com os nomes de campo do backend', () {
      final acidente = Ocorrencia.ler(acidenteJson);

      expect(acidente.id, 7);
      expect(acidente.ehAcidente, isTrue);
      expect(acidente.dataOcorrencia, DateTime(2026, 11, 10, 8, 30));
      expect(acidente.status, StatusOcorrencia.aberta);
      expect(acidente.protocoloCat, 'CAT-2026-0007');
      expect(acidente.tipoLesao, 'Corte');
      expect(acidente.relatorPerfil, Perfil.supervisor);
      expect(acidente.relator, 'Joana Ribeiro (Supervisor)');
      expect(descreverCausaRaiz(acidente.causaRaiz), 'Falha de EPI');
      expect(descreverGravidade(acidente.gravidade), 'Alto');
    });

    test('campos ausentes ou estranhos não quebram a leitura', () {
      final ocorrencia = Ocorrencia.ler({
        'tipoOcorrencia': 'incidente',
        'statusOcorrencia': 'Em Triagem',
        'local': 'Pátio',
      });

      expect(ocorrencia.id, isNull);
      expect(ocorrencia.tipo, TipoOcorrencia.incidente);
      expect(ocorrencia.status, StatusOcorrencia.emTriagem);
      expect(ocorrencia.descricao, 'Sem descrição');
      expect(ocorrencia.ondeOcorreu, 'Pátio');
      expect(ocorrencia.relator, 'Não informado');
      expect(descreverCausaRaiz(null), 'Não informada');
      expect(descreverGravidade('DESCONHECIDA'), 'DESCONHECIDA');
    });

    test('lista inválida é recusada', () {
      expect(() => lerOcorrencias({'erro': true}), throwsA(anything));
    });
  });

  group('áreas de risco e setores', () {
    test('lê área com agentes, EPIs e nível de perigo', () {
      final area = AreaRisco.ler(prensaJson);

      expect(area.codigo, 'SET-01');
      expect(area.nivelPerigo, NivelPerigo.alto);
      expect(area.setorId, 1);
      expect(area.agentesRisco, ['Ruído', 'Esmagamento']);
      expect(area.episObrigatorios.single.nome, 'Luva de vaqueta');
    });

    test('área sem campos opcionais usa listas vazias', () {
      final area = AreaRisco.ler({'id': 9});

      expect(area.nome, 'Área sem nome');
      expect(area.nivelPerigo, isNull);
      expect(area.agentesRisco, isEmpty);
      expect(area.episObrigatorios, isEmpty);
    });

    test('setor descreve a localização entre parênteses', () {
      expect(Setor.ler(galpaoJson).descricaoCurta, 'Galpão A (Bloco 2)');
      expect(Setor.ler(depositoJson).descricaoCurta, 'Depósito');
      expect(Setor.ler(galpaoJson).status, NivelPerigo.medio);
    });
  });

  group('FiltroOcorrencias', () {
    test('sem critérios, a consulta vai sem parâmetros', () {
      expect(FiltroOcorrencias.nenhum.vazio, isTrue);
      expect(FiltroOcorrencias.nenhum.chaveCache, 'ocorrencias');
    });

    test('critérios combinados viram parâmetros da consulta', () {
      final filtro = FiltroOcorrencias(
        dataInicio: DateTime(2026, 11, 1),
        dataFim: DateTime(2026, 11, 30),
        setorId: 1,
        colaboradorId: 10,
        tipo: TipoOcorrencia.acidente,
        gravidade: NivelPerigo.critico,
      );

      expect(filtro.consulta, {
        'dataInicio': '2026-11-01',
        'dataFim': '2026-11-30',
        'setorId': '1',
        'colaboradorId': '10',
        'tipoOcorrencia': 'ACIDENTE',
        'gravidade': 'CRITICO',
      });
      expect(
        filtro.chaveCache,
        startsWith('ocorrencias?dataInicio=2026-11-01'),
      );
      expect(
        filtro,
        FiltroOcorrencias(
          dataInicio: DateTime(2026, 11, 1),
          dataFim: DateTime(2026, 11, 30),
          setorId: 1,
          colaboradorId: 10,
          tipo: TipoOcorrencia.acidente,
          gravidade: NivelPerigo.critico,
        ),
      );
    });
  });

  group('corpoDoRelato', () {
    test('incidente leva só os campos preenchidos', () {
      final corpo = corpoDoRelato(
        const DadosOcorrencia(
          tipo: TipoOcorrencia.incidente,
          dataOcorrencia: '2026-11-12T14:00:00',
          descricao: 'Piso molhado',
          areaId: 4,
          colaboradorId: 11,
          situacaoRisco: 'Piso escorregadio',
          potencialDano: 'Queda',
        ),
      );

      expect(corpo, {
        'tipoOcorrencia': 'INCIDENTE',
        'dataOcorrencia': '2026-11-12T14:00:00',
        'descricao': 'Piso molhado',
        'areaId': 4,
        'colaboradorId': 11,
        'situacaoRisco': 'Piso escorregadio',
        'potencialDano': 'Queda',
      });
    });
  });

  group('validação do relato', () {
    final agora = DateTime(2026, 11, 20, 10, 0);

    test('data do fato no futuro é recusada', () {
      expect(
        validarDataDoFato('21/11/2026', agora: agora),
        'A data do fato não pode estar no futuro.',
      );
      expect(validarDataDoFato('20/11/2026', agora: agora), isNull);
      expect(validarDataDoFato('', agora: agora), 'Informe a data do fato.');
      expect(
        validarDataDoFato('31/02/2026', agora: agora),
        'Informe uma data válida, no formato dd/mm/aaaa.',
      );
    });

    test('hora futura só é recusada num fato de hoje', () {
      expect(
        validarHoraDoFato('10:30', data: '20/11/2026', agora: agora),
        'A hora do fato não pode estar no futuro.',
      );
      expect(
        validarHoraDoFato('09:59', data: '20/11/2026', agora: agora),
        isNull,
      );
      expect(
        validarHoraDoFato('23:00', data: '19/11/2026', agora: agora),
        isNull,
      );
      expect(
        validarHoraDoFato('25:00', data: '19/11/2026', agora: agora),
        'Informe uma hora válida, no formato hh:mm.',
      );
    });

    test('descrição vazia ou só com espaços é recusada', () {
      expect(validarDescricaoDoFato('   '), 'Descreva o que aconteceu.');
      expect(validarDescricaoDoFato('Corte na mão'), isNull);
    });

    test('período invertido é recusado e data vazia no filtro passa', () {
      expect(
        validarPeriodo(DateTime(2026, 11, 2), DateTime(2026, 11, 1)),
        'A data final deve ser igual ou posterior à data inicial.',
      );
      expect(
        validarPeriodo(DateTime(2026, 11, 1), DateTime(2026, 11, 1)),
        isNull,
      );
      expect(validarDataDoFiltro(''), isNull);
    });

    test('data e hora digitadas viram o LocalDateTime da API', () {
      expect(
        montarDataHoraDoFato('10/11/2026', '08:30'),
        '2026-11-10T08:30:00',
      );
    });
  });
}

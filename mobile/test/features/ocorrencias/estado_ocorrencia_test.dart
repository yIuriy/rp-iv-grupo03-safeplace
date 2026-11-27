// State e Command da ocorrência (#321): cada situação oferece as operações certas, e cada operação
// chama a rota certa do repositório.

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/features/ocorrencias/data/ocorrencia.dart';
import 'package:safeplace/features/ocorrencias/domain/estado_ocorrencia.dart';
import 'package:safeplace/features/ocorrencias/domain/ocorrencia_repository.dart';
import 'package:safeplace/features/ocorrencias/domain/operacao_ocorrencia.dart';

import 'apoio_ocorrencias.dart';

/// Repositório que só registra qual operação foi chamada e devolve a ocorrência no estado novo.
class _RepositorioGravador implements OcorrenciaRepository {
  final List<String> chamadas = [];

  Ocorrencia _com(String status, {String? protocolo}) => Ocorrencia.ler({
    ...acidenteJson,
    'statusOcorrencia': status,
    'numeroProtocoloCAT': protocolo ?? acidenteJson['numeroProtocoloCAT'],
  });

  @override
  Future<Ocorrencia> enviarParaTriagem(int id) async {
    chamadas.add('triagem $id');
    return _com('EM_TRIAGEM');
  }

  @override
  Future<Ocorrencia> arquivar(int id) async {
    chamadas.add('arquivar $id');
    return _com('ARQUIVADA');
  }

  @override
  Future<Ocorrencia> consolidarCat(int id) async {
    chamadas.add('cat $id');
    return _com('ABERTA', protocolo: 'CAT-2026-11-0042');
  }

  @override
  Future<Ocorrencia> atualizar(int id, DadosOcorrencia dados) =>
      throw UnimplementedError();

  @override
  Future<Ocorrencia> registrar(DadosOcorrencia dados) =>
      throw UnimplementedError();

  @override
  Future<Ocorrencia> buscarPorProtocolo(String protocolo) =>
      throw UnimplementedError();
}

Ocorrencia acidente({
  required String status,
  String? protocolo = 'CAT-2026-0007',
}) => Ocorrencia.ler({
  ...acidenteJson,
  'statusOcorrencia': status,
  'numeroProtocoloCAT': protocolo,
});

void main() {
  group('State: o que cada situação oferece', () {
    test('Aberta: edita e segue para triagem', () {
      final estado = EstadoOcorrencia.de(StatusOcorrencia.aberta);

      expect(estado, isA<EstadoAberta>());
      expect(estado.permiteEdicao, isTrue);
      expect(estado.proximo, StatusOcorrencia.emTriagem);
      expect(estado.operacoes(acidente(status: 'ABERTA')), [
        isA<EnviarParaTriagem>(),
      ]);
    });

    test('Em triagem: edita e pode ser arquivada', () {
      final estado = EstadoOcorrencia.de(StatusOcorrencia.emTriagem);

      expect(estado.permiteEdicao, isTrue);
      expect(estado.proximo, StatusOcorrencia.arquivada);
      expect(estado.operacoes(acidente(status: 'EM_TRIAGEM')), [
        isA<Arquivar>(),
      ]);
    });

    test('Arquivada: fim do fluxo, sem edição nem operações, com o aviso de preservação', () {
      final estado = EstadoOcorrencia.de(StatusOcorrencia.arquivada);

      expect(estado.permiteEdicao, isFalse);
      expect(estado.permiteNovasAcoes, isFalse);
      expect(estado.proximo, isNull);
      expect(estado.operacoes(acidente(status: 'ARQUIVADA')), isEmpty);
      expect(estado.aviso, contains('não há exclusão de registros'));
    });

    test('acidente sem protocolo oferece a emissão da CAT; incidente e acidente com protocolo não', () {
      final aberta = EstadoOcorrencia.de(StatusOcorrencia.aberta);

      expect(aberta.operacoes(acidente(status: 'ABERTA', protocolo: null)), [
        isA<EnviarParaTriagem>(),
        isA<ConsolidarCat>(),
      ]);
      expect(
        aberta.operacoes(
          Ocorrencia.ler({...incidenteJson, 'statusOcorrencia': 'ABERTA'}),
        ),
        [isA<EnviarParaTriagem>()],
      );
      expect(
        EstadoOcorrencia.de(StatusOcorrencia.emTriagem)
            .operacoes(acidente(status: 'EM_TRIAGEM', protocolo: null)),
        [isA<ConsolidarCat>(), isA<Arquivar>()],
      );
    });
  });

  group('Command: cada operação chama a rota certa', () {
    test('triagem, arquivamento e CAT', () async {
      final repositorio = _RepositorioGravador();

      final triada = await const EnviarParaTriagem().executar(repositorio, 7);
      final arquivada = await const Arquivar().executar(repositorio, 7);
      final comCat = await const ConsolidarCat().executar(repositorio, 7);

      expect(repositorio.chamadas, ['triagem 7', 'arquivar 7', 'cat 7']);
      expect(triada.status, StatusOcorrencia.emTriagem);
      expect(arquivada.status, StatusOcorrencia.arquivada);
      expect(
        const ConsolidarCat().mensagemDeSucesso(comCat),
        'Protocolo CAT CAT-2026-11-0042 emitido.',
      );
    });

    test('só arquivar é destrutiva; todas pedem confirmação', () {
      const operacoes = <OperacaoOcorrencia>[
        EnviarParaTriagem(),
        Arquivar(),
        ConsolidarCat(),
      ];

      expect(operacoes.where((o) => o.destrutiva), [isA<Arquivar>()]);
      expect(operacoes.every((o) => o.confirmacao != null), isTrue);
      expect(
        const Arquivar().confirmacao.rotuloConfirmar,
        'Confirmar arquivamento',
      );
    });
  });
}

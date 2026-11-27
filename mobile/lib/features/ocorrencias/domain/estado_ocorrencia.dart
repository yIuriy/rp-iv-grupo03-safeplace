// Situação da ocorrência no padrão State (GoF), #321. O fluxo é o do backend
// (`StatusOcorrencia.podeTransicaoPara`, decisão DS09, seção 2.6):
//
//   ABERTA ──triagem──► EM_TRIAGEM ──arquivar──► ARQUIVADA
//
// Cada estado responde o que vale nele: se permite editar, para qual situação segue e quais
// operações o Gestor tem. A tela de detalhe pergunta ao estado em vez de testar o status em vários
// `if`; acrescentar uma situação nova é acrescentar uma classe aqui.

import '../data/ocorrencia.dart';
import 'operacao_ocorrencia.dart';

sealed class EstadoOcorrencia {
  const EstadoOcorrencia();

  /// Estado correspondente ao status lido da API.
  factory EstadoOcorrencia.de(StatusOcorrencia status) => switch (status) {
    StatusOcorrencia.aberta => const EstadoAberta(),
    StatusOcorrencia.emTriagem => const EstadoEmTriagem(),
    StatusOcorrencia.arquivada => const EstadoArquivada(),
  };

  StatusOcorrencia get status;

  /// Situação seguinte no fluxo, ou `null` no fim.
  StatusOcorrencia? get proximo;

  /// O backend recusa editar ocorrência arquivada ("Ocorrência arquivada não pode ser editada.").
  bool get permiteEdicao;

  /// O plano de ação recebe ações novas enquanto a ocorrência está em acompanhamento.
  bool get permiteNovasAcoes => permiteEdicao;

  /// Operações do Gestor neste estado, na ordem dos botões.
  List<OperacaoOcorrencia> operacoes(Ocorrencia ocorrencia);

  /// Explicação mostrada no lugar das operações, quando não há nenhuma.
  String? get aviso => null;

  /// A CAT só é emitida para acidente ainda sem protocolo (registros antigos ou que falharam).
  static List<OperacaoOcorrencia> _cat(Ocorrencia ocorrencia) =>
      ocorrencia.ehAcidente && ocorrencia.protocoloCat == null
      ? const [ConsolidarCat()]
      : const [];
}

final class EstadoAberta extends EstadoOcorrencia {
  const EstadoAberta();

  @override
  StatusOcorrencia get status => StatusOcorrencia.aberta;

  @override
  StatusOcorrencia get proximo => StatusOcorrencia.emTriagem;

  @override
  bool get permiteEdicao => true;

  @override
  List<OperacaoOcorrencia> operacoes(Ocorrencia ocorrencia) => [
    const EnviarParaTriagem(),
    ...EstadoOcorrencia._cat(ocorrencia),
  ];
}

final class EstadoEmTriagem extends EstadoOcorrencia {
  const EstadoEmTriagem();

  @override
  StatusOcorrencia get status => StatusOcorrencia.emTriagem;

  @override
  StatusOcorrencia get proximo => StatusOcorrencia.arquivada;

  @override
  bool get permiteEdicao => true;

  @override
  List<OperacaoOcorrencia> operacoes(Ocorrencia ocorrencia) => [
    ...EstadoOcorrencia._cat(ocorrencia),
    const Arquivar(),
  ];
}

final class EstadoArquivada extends EstadoOcorrencia {
  const EstadoArquivada();

  @override
  StatusOcorrencia get status => StatusOcorrencia.arquivada;

  @override
  StatusOcorrencia? get proximo => null;

  @override
  bool get permiteEdicao => false;

  @override
  List<OperacaoOcorrencia> operacoes(Ocorrencia ocorrencia) => const [];

  @override
  String get aviso =>
      'O acompanhamento foi encerrado. Os dados e o plano de ação ficam preservados; não há exclusão de registros.';
}

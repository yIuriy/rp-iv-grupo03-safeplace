// Alocação de colaborador em tarefa (RF10, UC02): registro confirmado (`AlocacaoTarefaOutputDTO`),
// consulta preventiva de aptidão (`ConsultaAptidaoOutputDTO`) e os motivos do bloqueio (422).

import '../../../core/api/api_client.dart';
import '../../../core/api/leitura.dart';

class AlocacaoTarefa {
  const AlocacaoTarefa({
    this.id,
    this.tarefaId,
    this.colaboradorId,
    required this.colaboradorNome,
    this.dataAlocacao,
    this.responsavelNome,
    this.observacao,
  });

  final int? id;
  final int? tarefaId;
  final int? colaboradorId;
  final String colaboradorNome;
  final DateTime? dataAlocacao;
  final String? responsavelNome;
  final String? observacao;

  factory AlocacaoTarefa.ler(Object? dado) {
    final b = objeto(dado, 'Alocação inválida');
    return AlocacaoTarefa(
      id: numeroOuNulo(b['id'])?.toInt(),
      tarefaId: numeroOuNulo(b['tarefaId'])?.toInt(),
      colaboradorId: numeroOuNulo(b['colaboradorId'])?.toInt(),
      colaboradorNome: texto(b['colaboradorNome'], 'Colaborador sem nome'),
      dataAlocacao: dataOuNula(b['dataAlocacao']),
      responsavelNome: textoOuNulo(b['responsavelNome']),
      observacao: textoOuNulo(b['observacao']),
    );
  }
}

List<AlocacaoTarefa> lerAlocacoes(Object? dado) =>
    lista(dado, AlocacaoTarefa.ler, 'Esperava uma lista de alocações');

/// Diagnóstico informativo: a confirmação valida tudo de novo no servidor.
class ConsultaAptidao {
  const ConsultaAptidao({required this.apto, this.impedimentos = const []});

  final bool apto;
  final List<String> impedimentos;

  factory ConsultaAptidao.ler(Object? dado) {
    final b = objeto(dado, 'Consulta de aptidão inválida');
    return ConsultaAptidao(
      // Sem o campo, a tela não afirma aptidão.
      apto: b['apto'] == true,
      impedimentos: _textos(b['impedimentos']),
    );
  }
}

List<String> _textos(Object? dado) => [
  for (final item in listaOuVazia(dado, textoOuNulo)) ?item,
];

/// Motivos do bloqueio no corpo do 422 da alocação (campo extra `impedimentos`). Fora desse caso,
/// lista vazia.
List<String> impedimentosDaFalha(Object erro) {
  if (erro is! FalhaApi || erro.status != 422) return const [];
  final corpo = erro.corpo;
  return corpo is Map<String, Object?>
      ? _textos(corpo['impedimentos'])
      : const [];
}

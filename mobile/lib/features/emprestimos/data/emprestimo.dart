// Modelo único de empréstimo de EPI no app (`EmprestimoResponse` de `/api/emprestimos`),
// portado de `frontend/src/features/emprestimos/api.ts` e `formatos.ts`.
// Usa leitura defensiva com as primitivas de `core/api/leitura.dart`.

import '../../../core/api/leitura.dart';

/// Classificação do empréstimo vista do ponto de vista do EPI (`ClassificacaoEmprestimo` do backend).
enum ClassificacaoEmprestimo {
  indisponivel('INDISPONIVEL', 'Em posse'),
  devolucaoAtrasada('DEVOLUCAO_ATRASADA', 'Atrasado'),
  disponivel('DISPONIVEL', 'Devolvido');

  const ClassificacaoEmprestimo(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  /// Valor fora do enum vira `null`.
  static ClassificacaoEmprestimo? ler(Object? valor) {
    for (final c in values) {
      if (c.codigo == valor) return c;
    }
    return null;
  }
}

/// Estado informado na devolução do EPI (`EstadoDevolucao` do backend).
enum EstadoDevolucao {
  apto(
    'APTO',
    'Apto para uso',
    'O empréstimo será encerrado e o item voltará ao saldo disponível do estoque.',
  ),
  manutencao(
    'MANUTENCAO',
    'Encaminhar para manutenção',
    'O empréstimo será encerrado com a indicação de manutenção. O item não voltará ao saldo disponível.',
  ),
  descarte(
    'DESCARTE',
    'Indicar descarte',
    'O empréstimo será encerrado com a indicação de descarte. O item não voltará ao saldo disponível, e a baixa definitiva não é registrada nesta tela.',
  );

  const EstadoDevolucao(this.codigo, this.rotulo, this.destino);

  final String codigo;
  final String rotulo;
  final String destino;

  /// Valor fora do enum vira `null`.
  static EstadoDevolucao? ler(Object? valor) {
    for (final e in values) {
      if (e.codigo == valor) return e;
    }
    return null;
  }
}

/// Filtro de situação da consulta de empréstimos (`GET /api/emprestimos?situacao=...`).
enum SituacaoConsulta {
  pendentes('PENDENTES', 'Em posse'),
  atrasados('ATRASADOS', 'Atrasados'),
  todos('TODOS', 'Todos, incluindo devolvidos');

  const SituacaoConsulta(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;
}

/// Empréstimo de EPI a colaborador com prazos, devolução e termo de cautela.
class Emprestimo {
  const Emprestimo({
    this.id,
    this.epiId,
    required this.epiNome,
    this.colaboradorId,
    required this.colaboradorNome,
    this.responsavelEntregaId,
    this.dataRetirada,
    this.dataDevolucaoPrevista,
    this.dataDevolucaoReal,
    this.responsavelDevolucaoId,
    this.estadoDevolucao,
    this.status,
    this.atrasado = false,
    this.termoCautela = '',
    this.justificativaSubstituicao,
  });

  final int? id;
  final int? epiId;
  final String epiNome;
  final int? colaboradorId;
  final String colaboradorNome;
  final int? responsavelEntregaId;
  final DateTime? dataRetirada;
  final DateTime? dataDevolucaoPrevista;
  final DateTime? dataDevolucaoReal;
  final int? responsavelDevolucaoId;
  final EstadoDevolucao? estadoDevolucao;
  final ClassificacaoEmprestimo? status;
  final bool atrasado;
  final String termoCautela;
  final String? justificativaSubstituicao;

  /// Indica se o empréstimo ainda retém o equipamento (não devolvido).
  bool get emPosse => status != ClassificacaoEmprestimo.disponivel;

  /// Indica se o empréstimo está com prazo estourado.
  bool get estaAtrasado =>
      atrasado || status == ClassificacaoEmprestimo.devolucaoAtrasada;

  factory Emprestimo.ler(Object? dado) {
    final b = objeto(dado, 'Registro de empréstimo inválido');
    final epiId = numeroOuNulo(b['epiId'])?.toInt();
    final colabId = numeroOuNulo(b['colaboradorId'])?.toInt();
    return Emprestimo(
      id: numeroOuNulo(b['id'])?.toInt(),
      epiId: epiId,
      epiNome: texto(b['epiNome'], epiId != null ? 'EPI $epiId' : 'EPI'),
      colaboradorId: colabId,
      colaboradorNome: texto(
        b['colaboradorNome'],
        colabId != null ? 'Colaborador $colabId' : 'Colaborador',
      ),
      responsavelEntregaId:
          numeroOuNulo(b['responsavelEntregaId'])?.toInt(),
      dataRetirada: dataOuNula(b['dataRetirada']),
      dataDevolucaoPrevista: dataOuNula(b['dataDevolucaoPrevista']),
      dataDevolucaoReal: dataOuNula(b['dataDevolucaoReal']),
      responsavelDevolucaoId:
          numeroOuNulo(b['responsavelDevolucaoId'])?.toInt(),
      estadoDevolucao: EstadoDevolucao.ler(b['estadoDevolucao']),
      status: ClassificacaoEmprestimo.ler(b['status']),
      atrasado: b['atrasado'] == true,
      termoCautela: texto(b['termoCautela'], ''),
      justificativaSubstituicao: textoOuNulo(b['justificativaSubstituicao']),
    );
  }

  /// Lê a resposta de `POST /api/emprestimos/{id}/devolucao` (`DevolucaoResponse`),
  /// convertendo-a para `Emprestimo` concluído com status DISPONIVEL.
  factory Emprestimo.lerDevolucao(Object? dado) {
    final b = objeto(dado, 'Registro de devolução inválido');
    final id =
        (numeroOuNulo(b['emprestimoId']) ?? numeroOuNulo(b['id']))?.toInt();
    final epiId = numeroOuNulo(b['epiId'])?.toInt();
    final colabId = numeroOuNulo(b['colaboradorId'])?.toInt();
    return Emprestimo(
      id: id,
      epiId: epiId,
      epiNome: texto(b['epiNome'], epiId != null ? 'EPI $epiId' : 'EPI'),
      colaboradorId: colabId,
      colaboradorNome: texto(
        b['colaboradorNome'],
        colabId != null ? 'Colaborador $colabId' : 'Colaborador',
      ),
      responsavelEntregaId:
          numeroOuNulo(b['responsavelEntregaId'])?.toInt(),
      dataRetirada: dataOuNula(b['dataRetirada']),
      dataDevolucaoPrevista: dataOuNula(b['dataDevolucaoPrevista']),
      dataDevolucaoReal: dataOuNula(b['dataDevolucaoReal']),
      responsavelDevolucaoId:
          numeroOuNulo(b['responsavelDevolucaoId'])?.toInt(),
      estadoDevolucao: EstadoDevolucao.ler(b['estadoDevolucao']),
      status: ClassificacaoEmprestimo.disponivel,
      atrasado: false,
      termoCautela: texto(b['termoCautela'], ''),
      justificativaSubstituicao: textoOuNulo(b['justificativaSubstituicao']),
    );
  }
}

/// Lê uma lista de empréstimos da API.
List<Emprestimo> lerEmprestimos(Object? dado) =>
    lista(dado, Emprestimo.ler, 'Esperava uma lista de empréstimos');

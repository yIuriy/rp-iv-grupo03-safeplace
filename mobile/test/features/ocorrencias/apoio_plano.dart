// Apoio dos testes do plano de ação (#321): ações de exemplo no formato de `PlanoDeAcaoResponse`.

final acaoAtrasadaJson = <String, Object?>{
  'id': 21,
  'idOcorrencia': 7,
  'medidasCorretivas': 'Trocar a proteção da prensa',
  'medidasPreventivas': null,
  'prazo': '2026-11-20',
  'status': 'ABERTA',
  'situacao': 'ABERTA',
  'responsavelTipo': 'COLABORADOR',
  'responsavelId': 10,
  'responsavelNome': 'João da Silva',
  'alertaAtivo': true,
  'antecedenciaAlertaDias': 3,
  'dataAlerta': '2026-11-17',
  'alertaDevido': true,
  'emAtraso': true,
  'gestorNome': 'Gestor Demo',
};

final acaoConcluidaJson = <String, Object?>{
  'id': 22,
  'idOcorrencia': 7,
  'medidasCorretivas': null,
  'medidasPreventivas': 'Treinar a equipe no uso da prensa',
  'prazo': '2026-11-15',
  'status': 'CONCLUIDA',
  'situacao': 'CONCLUIDA',
  'responsavelTipo': 'SETOR',
  'responsavelId': 1,
  'responsavelNome': 'Galpão A',
  'alertaAtivo': false,
  'alertaDevido': false,
  'emAtraso': false,
};

final prorrogacaoJson = <String, Object?>{
  'idAcao': 21,
  'prazoAnterior': '2026-11-10',
  'novoPrazo': '2026-11-20',
  'justificativa': 'Peça de reposição em falta no fornecedor',
};

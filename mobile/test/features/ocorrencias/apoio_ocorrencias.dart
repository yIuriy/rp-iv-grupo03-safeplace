// Apoio dos testes de ocorrências e áreas de risco: respostas de exemplo do backend (um acidente, um
// incidente, duas áreas e dois setores) e o servidor falso que as devolve.

import '../usuarios/apoio_usuarios.dart';

final galpaoJson = <String, Object?>{
  'id': 1,
  'nome': 'Galpão A',
  'descricao': 'Caldeiraria',
  'localizacao': 'Bloco 2',
  'status': 'MEDIO',
};

final depositoJson = <String, Object?>{
  'id': 2,
  'nome': 'Depósito',
  'localizacao': null,
  'status': 'BAIXO',
};

final prensaJson = <String, Object?>{
  'id': 3,
  'codigo': 'SET-01',
  'nome': 'Prensa hidráulica',
  'nivelPerigo': 'ALTO',
  'setorId': 1,
  'agentesRisco': ['Ruído', 'Esmagamento'],
  'episObrigatorios': [
    {'id': 5, 'nome': 'Luva de vaqueta', 'numeroCa': '54321'},
  ],
};

final almoxarifadoJson = <String, Object?>{
  'id': 4,
  'codigo': 'SET-02',
  'nome': 'Almoxarifado',
  'nivelPerigo': 'BAIXO',
  'setorId': 2,
};

final acidenteJson = <String, Object?>{
  'idOcorrencia': 7,
  'tipoOcorrencia': 'ACIDENTE',
  'dataOcorrencia': '2026-11-10T08:30:00',
  'dataRegistro': '2026-11-10T09:00:00',
  'statusOcorrencia': 'ABERTA',
  'descricao': 'Corte na mão ao operar a prensa',
  'numeroProtocoloCAT': 'CAT-2026-0007',
  'gravidade': 'ALTO',
  'causaRaiz': 'FALHA_EPI',
  'tipo': 'Corte',
  'areaId': 3,
  'areaNome': 'Prensa hidráulica',
  'colaboradorId': 10,
  'colaboradorNome': 'João da Silva',
  'relatorId': 2,
  'relatorNome': 'Joana Ribeiro',
  'relatorPerfil': 'SUPERVISOR',
};

final incidenteJson = <String, Object?>{
  'idOcorrencia': 8,
  'tipoOcorrencia': 'INCIDENTE',
  'dataOcorrencia': '2026-11-12T14:00:00',
  'statusOcorrencia': 'EM_TRIAGEM',
  'descricao': 'Piso molhado perto do almoxarifado',
  'situacaoRisco': 'Piso escorregadio',
  'potencialDano': 'Queda',
  'areaId': 4,
  'areaNome': 'Almoxarifado',
  'colaboradorId': 11,
  'colaboradorNome': 'Ana Souza',
  'relatorNome': 'Gestor Demo',
  'relatorPerfil': 'GESTOR_SEGURANCA',
};

/// Servidor com os dados de exemplo. Escritas não são aceitas.
Object? responderOcorrencias(Requisicao r) => switch ((r.metodo, r.caminho)) {
  ('GET', '/ocorrencias') => [acidenteJson, incidenteJson],
  ('GET', '/ocorrencias/7') => acidenteJson,
  ('GET', '/ocorrencias/8') => incidenteJson,
  ('GET', '/areas-risco') => [prensaJson, almoxarifadoJson],
  ('GET', '/setores') => [galpaoJson, depositoJson],
  _ => responderPadrao(r),
};

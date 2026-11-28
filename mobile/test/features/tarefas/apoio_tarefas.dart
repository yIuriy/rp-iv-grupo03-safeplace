// Apoio dos testes de tarefas: registros de exemplo como o backend devolve e um servidor falso que
// responde o módulo e os catálogos usados nos formulários (áreas, setores, EPIs e colaboradores).

import '../usuarios/apoio_usuarios.dart';

export '../usuarios/apoio_usuarios.dart'
    show
        ApiUsuariosFalsa,
        Requisicao,
        abrirApp,
        botao,
        campo,
        falhaComMensagem,
        noCampo,
        servidorFora,
        sessaoSupervisor,
        tocar;

const soldaJson = <String, Object?>{
  'id': 1,
  'descricao': 'Solda em altura na estrutura metálica',
  'nivelPerigo': 'ALTO',
  'classificada': true,
  'dataClassificacao': '2026-11-20T10:15:30',
  'responsavelClassificacaoId': 1,
  'responsavelClassificacaoNome': 'Gestor Demo',
  'areaRiscoId': 3,
  'setoresIds': [1],
  'episIds': [5],
};

const limpezaJson = <String, Object?>{
  'id': 2,
  'descricao': 'Limpeza do almoxarifado',
  'nivelPerigo': null,
  'classificada': false,
  'dataClassificacao': null,
  'responsavelClassificacaoNome': null,
  'areaRiscoId': null,
  'setoresIds': <Object?>[],
  'episIds': <Object?>[],
};

const areaCaldeirariaJson = <String, Object?>{
  'id': 3,
  'codigo': 'AR-03',
  'nome': 'Caldeiraria',
  'nivelPerigo': 'ALTO',
  'setorId': 1,
};

const setorProducaoJson = <String, Object?>{
  'id': 1,
  'nome': 'Produção',
  'localizacao': 'Galpão A',
};

const setorEstoqueJson = <String, Object?>{
  'id': 2,
  'nome': 'Estoque',
  'localizacao': 'Galpão B',
};

const cintoJson = <String, Object?>{
  'id': 5,
  'nome': 'Cinto paraquedista',
  'numeroCa': '33333',
  'quantidade': 8,
  'estoqueMinimo': 2,
  'status': 'DISPONIVEL',
  'classificacao': 'PROTECAO_CONTRA_QUEDAS',
};

const luvaJson = <String, Object?>{
  'id': 6,
  'nome': 'Luva de vaqueta',
  'numeroCa': '44444',
  'quantidade': 20,
  'estoqueMinimo': 5,
  'status': 'DISPONIVEL',
  'classificacao': 'PROTECAO_DE_MEMBROS_SUPERIORES',
};

/// Respostas de leitura do módulo; escritas caem no [UnimplementedError] até o teste trocar.
Object? responderTarefas(Requisicao r) => switch ((r.metodo, r.caminho)) {
  ('GET', '/tarefas') => switch (r.consulta?['nivelPerigo']) {
    null => [soldaJson, limpezaJson],
    'ALTO' => [soldaJson],
    _ => <Object?>[],
  },
  ('GET', '/tarefas/1') => soldaJson,
  ('GET', '/tarefas/2') => limpezaJson,
  ('GET', '/areas-risco') => [areaCaldeirariaJson],
  ('GET', '/setores') => [setorProducaoJson, setorEstoqueJson],
  ('GET', '/epis') => [cintoJson, luvaJson],
  ('GET', '/usuarios/colaboradores') => responderPadrao(r),
  _ => throw UnimplementedError('Chamada inesperada: $r'),
};

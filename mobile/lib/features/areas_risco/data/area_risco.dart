// Modelos somente leitura de áreas de risco e setores (`AreaRiscoResponse` e `SetorResponse`),
// portados de `frontend/src/features/areas-risco/api.ts` (#318). Ocorrências, áreas de risco (#321)
// e tarefas (#322) leem os mesmos registros pelos mesmos `ler`.

import '../../../core/api/leitura.dart';

/// Grau de perigo (`NivelPerigo` do backend). Também é o vocabulário da gravidade do acidente.
enum NivelPerigo {
  baixo('BAIXO', 'Baixo'),
  medio('MEDIO', 'Médio'),
  alto('ALTO', 'Alto'),
  critico('CRITICO', 'Crítico');

  const NivelPerigo(this.codigo, this.rotulo);

  /// Valor usado pela API.
  final String codigo;
  final String rotulo;

  /// Valor fora dos quatro vira `null`, e a tela mostra "Não informado".
  static NivelPerigo? ler(Object? valor) {
    for (final nivel in values) {
      if (nivel.codigo == valor) return nivel;
    }
    return null;
  }
}

/// EPI obrigatório para entrar na área, no resumo que a área traz (`EpiResumoResponse`).
class EpiResumo {
  const EpiResumo({this.id, required this.nome, this.numeroCa, this.status});

  final int? id;
  final String nome;
  final String? numeroCa;
  final String? status;

  factory EpiResumo.ler(Object? dado) {
    final b = objeto(dado, 'EPI obrigatório inválido');
    return EpiResumo(
      id: numeroOuNulo(b['id'])?.toInt(),
      nome: texto(b['nome'], 'EPI sem nome'),
      numeroCa: textoOuNulo(b['numeroCa']),
      status: textoOuNulo(b['status']),
    );
  }
}

/// Área do mapa de riscos. Leitura defensiva: sem [id], a área aparece mas não pode ser escolhida
/// no `AreaRiscoPicker`.
class AreaRisco {
  const AreaRisco({
    this.id,
    this.codigo,
    required this.nome,
    this.descricao,
    this.nivelPerigo,
    this.setorId,
    this.agentesRisco = const [],
    this.limites,
    this.episObrigatorios = const [],
  });

  final int? id;
  final String? codigo;
  final String nome;
  final String? descricao;
  final NivelPerigo? nivelPerigo;
  final int? setorId;
  final List<String> agentesRisco;
  final String? limites;
  final List<EpiResumo> episObrigatorios;

  factory AreaRisco.ler(Object? dado) {
    final b = objeto(dado, 'Área de risco inválida');
    return AreaRisco(
      id: numeroOuNulo(b['id'])?.toInt(),
      codigo: textoOuNulo(b['codigo']),
      nome: texto(b['nome'], 'Área sem nome'),
      descricao: textoOuNulo(b['descricao']),
      nivelPerigo: NivelPerigo.ler(b['nivelPerigo']),
      setorId: numeroOuNulo(b['setorId'])?.toInt(),
      agentesRisco: [
        for (final agente in listaOuVazia(b['agentesRisco'], textoOuNulo))
          ?agente,
      ],
      limites: textoOuNulo(b['limites']),
      episObrigatorios: listaOuVazia(b['episObrigatorios'], EpiResumo.ler),
    );
  }
}

/// Setor físico da planta. [status] é o grau de perigo geral do setor.
class Setor {
  const Setor({
    this.id,
    required this.nome,
    this.descricao,
    this.localizacao,
    this.status,
  });

  final int? id;
  final String nome;
  final String? descricao;
  final String? localizacao;
  final NivelPerigo? status;

  /// Nome com a localização entre parênteses, como o `descreverSetor` do web.
  String get descricaoCurta =>
      localizacao == null ? nome : '$nome ($localizacao)';

  factory Setor.ler(Object? dado) {
    final b = objeto(dado, 'Setor inválido');
    return Setor(
      id: numeroOuNulo(b['id'])?.toInt(),
      nome: texto(b['nome'], 'Setor sem nome'),
      descricao: textoOuNulo(b['descricao']),
      localizacao: textoOuNulo(b['localizacao']),
      status: NivelPerigo.ler(b['status']),
    );
  }
}

List<AreaRisco> lerAreasRisco(Object? dado) =>
    lista(dado, AreaRisco.ler, 'Esperava uma lista de áreas de risco');

List<Setor> lerSetores(Object? dado) =>
    lista(dado, Setor.ler, 'Esperava uma lista de setores');

// Tela `/tarefas/{id}` (UC11): dados da tarefa com a área de risco e os setores pelo nome (catálogo
// da #318). Gestor e Supervisor consultam. Com o servidor fora do ar, mostra a cópia local com um
// único aviso de modo offline, no topo (RNF08). A seta de voltar fica na barra da casca (#335).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/estados.dart';
import '../../areas_risco/data/area_risco.dart';
import '../../areas_risco/data/areas_risco_api.dart';
import '../data/tarefa.dart';
import '../data/tarefas_api.dart';
import 'componentes_tarefa.dart';

/// Rótulo e valor dos dados da tarefa. Área e setores aparecem pelo nome quando o catálogo já
/// chegou, e pelo número enquanto isso.
List<(String, String)> dadosDaTarefa(
  Tarefa tarefa, {
  List<AreaRisco> areas = const [],
  List<Setor> setores = const [],
}) {
  String area(int id) {
    for (final a in areas) {
      if (a.id != id) continue;
      return a.codigo == null ? a.nome : '${a.codigo} · ${a.nome}';
    }
    return 'Área nº $id';
  }

  String setor(int id) {
    for (final s in setores) {
      if (s.id == id) return s.descricaoCurta;
    }
    return 'Setor nº $id';
  }

  final areaId = tarefa.areaRiscoId;
  return [
    ('Última avaliação', descreverAvaliacao(tarefa)),
    (
      'Área de risco',
      areaId == null ? 'Sem área de risco específica' : area(areaId),
    ),
    (
      'Setores',
      tarefa.setoresIds.isEmpty
          ? 'Nenhum setor vinculado'
          : tarefa.setoresIds.map(setor).join(', '),
    ),
  ];
}

class TarefaDetalhePage extends ConsumerWidget {
  const TarefaDetalhePage({super.key, required this.id});

  /// Nulo quando o endereço não traz um número (`/tarefas/abc`).
  final int? id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    final lista = ListView(
      padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
      children: [
        if (id == null)
          const EstadoVazio(
            titulo: 'Tarefa não encontrada',
            descricao: 'O endereço não indica uma tarefa. Volte para a lista e escolha um item.',
          )
        else
          _ConteudoDaTarefa(id: id),
      ],
    );
    if (id == null) return lista;
    return RefreshIndicator(
      onRefresh: () =>
          ref.refresh(tarefaProvider(id).future).then((_) {}, onError: (_) {}),
      child: lista,
    );
  }
}

class _ConteudoDaTarefa extends ConsumerWidget {
  const _ConteudoDaTarefa({required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ConteudoComCache<Tarefa>(
      valor: ref.watch(tarefaProvider(id)),
      carregando: 'Carregando tarefa...',
      falha: 'Não foi possível carregar a tarefa.',
      descreverFalha: descreverFalhaDeConsultaTarefa,
      aoTentarDeNovo: () => ref.invalidate(tarefaProvider(id)),
      construir: (tarefa, somenteLeitura) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [_DadosDaTarefa(tarefa: tarefa)],
      ),
    );
  }
}

class _DadosDaTarefa extends ConsumerWidget {
  const _DadosDaTarefa({required this.tarefa});

  final Tarefa tarefa;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final textos = Theme.of(context).textTheme;
    final rotulo = textos.labelMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    final dados = dadosDaTarefa(
      tarefa,
      areas: ref.watch(areasRiscoProvider).value?.dados ?? const [],
      setores: ref.watch(setoresProvider).value?.dados ?? const [],
    );
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Semantics(
          header: true,
          child: Text(tarefa.descricao, style: textos.headlineSmall),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        EtiquetaNivelPerigo(nivel: tarefa.nivelPerigo),
        for (final (nome, valor) in dados)
          Padding(
            padding: const EdgeInsets.only(top: TokensDimensoes.space8),
            child: MergeSemantics(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(nome, style: rotulo),
                  Text(valor, style: textos.bodyLarge),
                ],
              ),
            ),
          ),
      ],
    );
  }
}

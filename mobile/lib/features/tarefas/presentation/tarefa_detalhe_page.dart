// Tela `/tarefas/{id}` (UC11): dados da tarefa com a área de risco e os setores pelo nome (catálogo
// da #318). Gestor e Supervisor consultam. Com o servidor fora do ar, mostra a cópia local com um
// único aviso de modo offline, no topo (RNF08). A seta de voltar fica na barra da casca (#335).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/estados.dart';
import '../../areas_risco/data/area_risco.dart';
import '../../areas_risco/data/areas_risco_api.dart';
import '../data/alocacao.dart';
import '../data/tarefa.dart';
import '../data/tarefas_api.dart';
import 'componentes_tarefa.dart';
import 'formulario_alocacao_page.dart';
import 'formulario_classificacao_page.dart';
import 'formulario_epis_tarefa_page.dart';
import 'formulario_tarefa_page.dart';

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
      onRefresh: () {
        // As seções buscam de novo sozinhas; a espera é só pela tarefa.
        ref.invalidate(historicoTarefaProvider(id));
        ref.invalidate(alocacoesProvider(id));
        ref.invalidate(exigenciasProvider(id));
        return ref
            .refresh(tarefaProvider(id).future)
            .then((_) {}, onError: (_) {});
      },
      child: lista,
    );
  }
}

class _ConteudoDaTarefa extends ConsumerWidget {
  const _ConteudoDaTarefa({required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final gestor =
        ref.watch(controladorSessaoProvider).value?.sessao?.ehGestor ?? false;
    return ConteudoComCache<Tarefa>(
      valor: ref.watch(tarefaProvider(id)),
      carregando: 'Carregando tarefa...',
      falha: 'Não foi possível carregar a tarefa.',
      descreverFalha: descreverFalhaDeConsultaTarefa,
      aoTentarDeNovo: () => ref.invalidate(tarefaProvider(id)),
      construir: (tarefa, somenteLeitura) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _DadosDaTarefa(tarefa: tarefa),
          _Acoes(tarefa: tarefa, somenteLeitura: somenteLeitura),
          const SizedBox(height: TokensDimensoes.space24),
          _Exigencias(id: id),
          const SizedBox(height: TokensDimensoes.space24),
          SecaoDeLista<AlocacaoTarefa>(
            titulo: 'Alocações',
            falha: 'Não foi possível carregar as alocações.',
            valor: ref.watch(alocacoesProvider(id)),
            aoTentarDeNovo: () => ref.invalidate(alocacoesProvider(id)),
            vazio: const EstadoVazio(
              icone: Icons.group_outlined,
              titulo: 'Nenhuma alocação registrada',
              descricao:
                  'Os colaboradores alocados nesta tarefa aparecerão aqui.',
            ),
            item: (alocacao) => ItemDeSecao(
              quando: alocacao.dataAlocacao,
              titulo: alocacao.colaboradorNome,
              linhas: [
                'Alocado por ${alocacao.responsavelNome ?? 'responsável não registrado'}',
                ?alocacao.observacao,
              ],
            ),
          ),
          // O backend recusa o histórico ao Supervisor (403), então a seção nem aparece.
          if (gestor) ...[
            const SizedBox(height: TokensDimensoes.space24),
            SecaoDeLista<RegistroHistorico>(
              titulo: 'Histórico da tarefa',
              falha: 'Não foi possível carregar o histórico.',
              valor: ref.watch(historicoTarefaProvider(id)),
              aoTentarDeNovo: () => ref.invalidate(historicoTarefaProvider(id)),
              vazio: const EstadoVazio(
                icone: Icons.history,
                titulo: 'Sem registros',
                descricao: 'O cadastro e cada classificação aparecerão aqui, do mais recente ao mais antigo.',
              ),
              item: (registro) => ItemDeSecao(
                quando: registro.dataHora,
                titulo: rotuloDaOperacao(registro.tipoOperacao),
                linhas: [
                  ?registro.descricao,
                  'Por ${registro.responsavel ?? 'responsável não registrado'}',
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}

/// Ações por perfil, conforme a matriz do backend (RNF03): Gestor e Supervisor alocam; só o
/// Gestor classifica, vincula EPIs e edita. Sem conexão, os botões
/// ficam desabilitados com o motivo (RNF08).
class _Acoes extends ConsumerWidget {
  const _Acoes({required this.tarefa, required this.somenteLeitura});

  final Tarefa tarefa;
  final bool somenteLeitura;

  Future<void> _avisar(BuildContext context, String mensagem) async {
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(mensagem)));
  }

  Future<void> _alocar(BuildContext context) async {
    final alocacao = await abrirFormularioAlocacao(context, tarefa: tarefa);
    if (alocacao == null || !context.mounted) return;
    await _avisar(context, mensagemDeAlocacao(alocacao));
  }

  Future<void> _classificar(BuildContext context) async {
    final salva = await abrirFormularioClassificacao(context, tarefa: tarefa);
    if (salva == null || !context.mounted) return;
    await _avisar(context, mensagemDeClassificacao(salva));
  }

  Future<void> _vincularEpis(BuildContext context) async {
    final salva = await abrirFormularioEpisTarefa(context, tarefa: tarefa);
    if (salva == null || !context.mounted) return;
    await _avisar(context, mensagemDeVinculoEpis(salva));
  }

  Future<void> _editar(BuildContext context) async {
    final salva = await abrirFormularioTarefa(context, tarefa: tarefa);
    if (salva == null || !context.mounted) return;
    await _avisar(context, mensagemAoSalvarTarefa(salva, editando: true));
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final gestor =
        ref.watch(controladorSessaoProvider).value?.sessao?.ehGestor ?? false;
    final bloqueado = somenteLeitura || tarefa.id == null;
    return Padding(
      padding: const EdgeInsets.only(top: TokensDimensoes.space16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          FilledButton.icon(
            onPressed: bloqueado ? null : () => _alocar(context),
            icon: const Icon(Icons.person_add_alt),
            label: const Text('Alocar colaborador'),
          ),
          if (gestor) ...[
            const SizedBox(height: TokensDimensoes.space8),
            OutlinedButton.icon(
              onPressed: bloqueado ? null : () => _classificar(context),
              icon: const Icon(Icons.rule),
              label: Text(
                tarefa.classificada ? 'Reavaliar risco' : 'Classificar',
              ),
            ),
            const SizedBox(height: TokensDimensoes.space8),
            OutlinedButton.icon(
              onPressed: bloqueado ? null : () => _vincularEpis(context),
              icon: const Icon(Icons.inventory_2_outlined),
              label: const Text('Vincular EPIs'),
            ),
            const SizedBox(height: TokensDimensoes.space8),
            OutlinedButton.icon(
              onPressed: bloqueado ? null : () => _editar(context),
              icon: const Icon(Icons.edit_outlined),
              label: const Text('Editar tarefa'),
            ),
          ],
          if (somenteLeitura) ...[
            const SizedBox(height: TokensDimensoes.space8),
            Text(
              'Cadastros e alterações voltam quando a conexão voltar.',
              style: Theme.of(context).textTheme.bodyMedium
                  ?.copyWith(color: context.cores.textoSecundario),
            ),
          ],
        ],
      ),
    );
  }
}

/// Exigências da atividade (US06, critério 4): EPIs obrigatórios e capacitações normativas, para o
/// Supervisor conferir antes de alocar.
class _Exigencias extends ConsumerWidget {
  const _Exigencias({required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final textos = Theme.of(context).textTheme;
    final secundario = textos.bodyMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Semantics(
          header: true,
          child: Text('Exigências da atividade', style: textos.titleLarge),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        ref
            .watch(exigenciasProvider(id))
            .when(
              skipLoadingOnReload: true,
              skipLoadingOnRefresh: true,
              loading: () => const EstadoCarregando(
                texto: 'Consultando exigências registradas...',
              ),
              error: (erro, _) => Aviso(
                tom: TomAviso.perigo,
                anunciar: true,
                titulo: 'Não foi possível carregar as exigências.',
                mensagem: descreverFalhaDeConsultaTarefa(erro),
                rotuloAcao: 'Tentar novamente',
                aoAgir: () => ref.invalidate(exigenciasProvider(id)),
              ),
              data: (consultado) {
                final exigencias = consultado.dados;
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('EPIs obrigatórios', style: textos.titleSmall),
                    if (exigencias.episObrigatorios.isEmpty)
                      Text('Nenhum EPI vinculado.', style: secundario)
                    else
                      for (final epi in exigencias.episObrigatorios)
                        Text(
                          epi.numeroCa == null
                              ? '• ${epi.nome}'
                              : '• ${epi.nome} (CA ${epi.numeroCa})',
                        ),
                    const SizedBox(height: TokensDimensoes.space12),
                    Text('Capacitações exigidas', style: textos.titleSmall),
                    if (exigencias.capacitacoes.isEmpty)
                      Text('Nenhuma capacitação exigida.', style: secundario)
                    else
                      for (final capacitacao in exigencias.capacitacoes)
                        Text(
                          '• ${capacitacao.nome} (${capacitacao.tipo.rotulo})',
                        ),
                  ],
                );
              },
            ),
      ],
    );
  }
}

String rotuloDaOperacao(String? tipo) => switch (tipo) {
  'CADASTRO' => 'Cadastro',
  'ALTERACAO' => 'Alteração',
  'REMOCAO' => 'Remoção',
  _ => 'Operação registrada',
};

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

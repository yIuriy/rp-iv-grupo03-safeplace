// Tela `/epis/{id}` (UC06, passo 4): dados do EPI, histórico de movimentações (RNF05) e histórico
// de manutenções. Gestor e Supervisor consultam. Com o servidor fora do ar, mostra a cópia local com
// um único aviso de modo offline, no topo (RNF08).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/offline/cache_offline.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/estados.dart';
import '../data/epi.dart';
import '../data/epis_api.dart';
import '../data/manutencao.dart';
import '../data/movimentacao.dart';
import 'componentes_epi.dart';
import 'formulario_movimentacao_page.dart';

/// Rótulo e valor dos dados do EPI. Campos opcionais vazios ficam de fora.
List<(String, String)> dadosDoEpi(Epi epi) {
  final validade = epi.dataValidadeCa;
  final vidaUtil = epi.vidaUtilDias;
  return [
    (
      'Saldo',
      '${numeroOuNaoInformado(epi.quantidade)} (mínimo ${numeroOuNaoInformado(epi.estoqueMinimo)})',
    ),
    ('Localização', epi.localizacao ?? 'Não informada'),
    ('Classificação', epi.classificacao?.rotulo ?? 'Não informada'),
    (
      'CA',
      '${epi.numeroCa ?? 'Não informado'}'
          '${validade == null ? '' : ', válido até ${formatarData(validade)}'}',
    ),
    if (vidaUtil != null) ('Vida útil', '$vidaUtil dias'),
    if (epi.marca case final marca?) ('Marca', marca),
    if (epi.numeroLote case final lote?) ('Lote', lote),
    if (epi.notaFiscal case final nota?) ('Nota fiscal', nota),
    if (epi.descricao case final descricao?) ('Descrição', descricao),
  ];
}

class EpiDetalhePage extends ConsumerWidget {
  const EpiDetalhePage({super.key, required this.id});

  /// Nulo quando o endereço não traz um número (`/epis/abc`).
  final int? id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    final lista = ListView(
      padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
      children: [
        Align(
          alignment: Alignment.centerLeft,
          child: TextButton.icon(
            onPressed: () => context.go('/epis'),
            icon: const Icon(Icons.arrow_back),
            label: const Text('Voltar para a lista'),
          ),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        if (id == null)
          const EstadoVazio(
            titulo: 'EPI não encontrado',
            descricao: 'O endereço não indica um EPI. Volte para a lista e escolha um item.',
          )
        else
          _ConteudoDoEpi(id: id),
      ],
    );
    if (id == null) return lista;
    return RefreshIndicator(
      onRefresh: () => Future.wait([
        ref.refresh(epiProvider(id).future),
        ref.refresh(movimentacoesProvider(id).future),
        ref.refresh(manutencoesProvider(id).future),
      ]).then((_) {}, onError: (_) {}),
      child: lista,
    );
  }
}

class _ConteudoDoEpi extends ConsumerWidget {
  const _ConteudoDoEpi({required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ConteudoComCache<Epi>(
      valor: ref.watch(epiProvider(id)),
      carregando: 'Carregando EPI...',
      falha: 'Não foi possível carregar o EPI.',
      descreverFalha: descreverFalhaDeConsultaEpi,
      aoTentarDeNovo: () => ref.invalidate(epiProvider(id)),
      construir: (epi, somenteLeitura) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _DadosDoEpi(epi: epi),
          _AcoesDoGestor(epi: epi, somenteLeitura: somenteLeitura),
          const SizedBox(height: TokensDimensoes.space24),
          _Historico<Movimentacao>(
            titulo: 'Histórico de movimentações',
            valor: ref.watch(movimentacoesProvider(id)),
            aoTentarDeNovo: () => ref.invalidate(movimentacoesProvider(id)),
            vazio: const EstadoVazio(
              icone: Icons.swap_vert,
              titulo: 'Sem movimentações registradas',
              descricao: 'Entradas e saídas deste EPI aparecerão aqui com data, hora e motivo.',
            ),
            item: (mov) => _ItemMovimentacao(mov: mov),
          ),
          const SizedBox(height: TokensDimensoes.space24),
          _Historico<Manutencao>(
            titulo: 'Histórico de manutenções',
            valor: ref.watch(manutencoesProvider(id)),
            aoTentarDeNovo: () => ref.invalidate(manutencoesProvider(id)),
            vazio: const EstadoVazio(
              icone: Icons.build_outlined,
              titulo: 'Sem manutenções registradas',
              descricao: 'Manutenções preventivas e corretivas deste EPI aparecerão aqui.',
            ),
            item: (manutencao) => _ItemManutencao(manutencao: manutencao),
          ),
        ],
      ),
    );
  }
}

/// Ações de escrita, só do Gestor (RNF03). O Supervisor não vê os botões. Sem conexão, os botões
/// ficam desabilitados com o motivo (RNF08).
class _AcoesDoGestor extends ConsumerWidget {
  const _AcoesDoGestor({required this.epi, required this.somenteLeitura});

  final Epi epi;
  final bool somenteLeitura;

  Future<void> _movimentar(BuildContext context) async {
    final mov = await abrirFormularioMovimentacao(context, epi: epi);
    if (mov == null || !context.mounted) return;
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(mensagemDeMovimentacao(epi, mov))));
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final gestor =
        ref.watch(controladorSessaoProvider).value?.sessao?.ehGestor ?? false;
    if (!gestor) return const SizedBox.shrink();
    final bloqueado = somenteLeitura || epi.id == null;
    return Padding(
      padding: const EdgeInsets.only(top: TokensDimensoes.space16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          FilledButton.icon(
            onPressed: bloqueado ? null : () => _movimentar(context),
            icon: const Icon(Icons.swap_vert),
            label: const Text('Movimentar estoque'),
          ),
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

class _DadosDoEpi extends StatelessWidget {
  const _DadosDoEpi({required this.epi});

  final Epi epi;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final rotulo = textos.labelMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Semantics(
          header: true,
          child: Text(epi.nome, style: textos.headlineSmall),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        Wrap(
          spacing: TokensDimensoes.space8,
          runSpacing: TokensDimensoes.space4,
          children: [
            EtiquetaStatusEpi(status: epi.status),
            if (epi.estoqueCritico) EtiquetaEstoqueCritico(epi: epi),
          ],
        ),
        for (final (nome, valor) in dadosDoEpi(epi))
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

/// Seção de histórico: título, carregando, falha com "Tentar novamente", vazio ou a lista. O aviso
/// de modo offline fica só no topo da tela, então aqui a cópia local aparece sem repetir o aviso.
class _Historico<T> extends StatelessWidget {
  const _Historico({
    required this.titulo,
    required this.valor,
    required this.aoTentarDeNovo,
    required this.vazio,
    required this.item,
  });

  final String titulo;
  final AsyncValue<Consultado<List<T>>> valor;
  final VoidCallback aoTentarDeNovo;
  final Widget vazio;
  final Widget Function(T item) item;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Semantics(
          header: true,
          child: Text(titulo, style: Theme.of(context).textTheme.titleLarge),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        valor.when(
          skipLoadingOnReload: true,
          skipLoadingOnRefresh: true,
          loading: () =>
              const EstadoCarregando(texto: 'Carregando histórico...'),
          error: (erro, _) => Aviso(
            tom: TomAviso.perigo,
            anunciar: true,
            titulo: 'Não foi possível carregar o histórico.',
            mensagem: descreverFalhaDeConsultaEpi(erro),
            rotuloAcao: 'Tentar novamente',
            aoAgir: aoTentarDeNovo,
          ),
          data: (consultado) => consultado.dados.isEmpty
              ? vazio
              : Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    for (final registro in consultado.dados) ...[
                      Card(
                        margin: EdgeInsets.zero,
                        child: Padding(
                          padding: const EdgeInsets.all(
                            TokensDimensoes.space12,
                          ),
                          child: item(registro),
                        ),
                      ),
                      const SizedBox(height: TokensDimensoes.space8),
                    ],
                  ],
                ),
        ),
      ],
    );
  }
}

class _ItemMovimentacao extends StatelessWidget {
  const _ItemMovimentacao({required this.mov});

  final Movimentacao mov;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final quando = mov.dataHora;
    final quantidade = mov.quantidade;
    final unidades = quantidade == 1 ? 'unidade' : 'unidades';
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          quando == null ? 'Data não informada' : formatarDataHora(quando),
          style: textos.labelMedium?.copyWith(
            color: context.cores.textoSecundario,
          ),
        ),
        Text(
          '${mov.tipo?.rotulo ?? 'Movimentação'} de ${numeroOuNaoInformado(quantidade)} $unidades',
          style: textos.titleSmall,
        ),
        Text(
          'Motivo: ${mov.motivo ?? 'não informado'}.'
          '${mov.saldoAtual == null ? '' : ' Saldo após: ${mov.saldoAtual}.'}',
        ),
      ],
    );
  }
}

class _ItemManutencao extends StatelessWidget {
  const _ItemManutencao({required this.manutencao});

  final Manutencao manutencao;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final tipo = manutencao.tipo?.rotulo ?? 'Tipo não informado';
    final resultado =
        manutencao.resultado?.rotulo.toLowerCase() ?? 'resultado não informado';
    final status = manutencao.statusAtualEpi;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          formatarDataHoraIso(manutencao.dataManutencao) ??
              'Data não informada',
          style: textos.labelMedium?.copyWith(
            color: context.cores.textoSecundario,
          ),
        ),
        Text('$tipo, $resultado', style: textos.titleSmall),
        Text(manutencao.descricao ?? 'Sem descrição.'),
        if (status != null) Text('Status do EPI após: ${status.rotulo}.'),
      ],
    );
  }
}

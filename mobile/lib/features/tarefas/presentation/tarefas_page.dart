// Tela `/tarefas` (RF06, UC11): matriz de periculosidade com filtro por grau (no servidor), busca
// local pela descrição e "puxar para atualizar". Gestor e Supervisor consultam a mesma lista. Com o
// servidor fora do ar, mostra a cópia local do filtro escolhido com o aviso de modo offline (RNF08).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/estados.dart';
import '../data/tarefa.dart';
import '../data/tarefas_api.dart';
import 'componentes_tarefa.dart';

/// Tarefas cuja descrição contém o [termo], sem diferenciar maiúsculas (como o web).
List<Tarefa> filtrarTarefas(List<Tarefa> tarefas, String termo) {
  final busca = termo.trim().toLowerCase();
  if (busca.isEmpty) return tarefas;
  return tarefas
      .where((tarefa) => tarefa.descricao.toLowerCase().contains(busca))
      .toList();
}

class TarefasPage extends ConsumerStatefulWidget {
  const TarefasPage({super.key});

  @override
  ConsumerState<TarefasPage> createState() => _TarefasPageState();
}

class _TarefasPageState extends ConsumerState<TarefasPage> {
  final _busca = TextEditingController();
  String _termo = '';
  NivelPerigo? _nivel;

  @override
  void dispose() {
    _busca.dispose();
    super.dispose();
  }

  void _limpar() {
    _busca.clear();
    setState(() {
      _termo = '';
      _nivel = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = tarefasProvider(_nivel);
    final consulta = ref.watch(provider);
    final gestor =
        ref.watch(controladorSessaoProvider).value?.sessao?.ehGestor ?? false;
    return RefreshIndicator(
      onRefresh: () =>
          ref.refresh(provider.future).then((_) {}, onError: (_) {}),
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Text(
            gestor
                ? 'Consulte a matriz de periculosidade, cadastre atividades operacionais e defina o grau de risco.'
                : 'Consulte a matriz de periculosidade e as diretrizes de risco das atividades operacionais.',
            style: Theme.of(context).textTheme.bodyMedium
                ?.copyWith(color: context.cores.textoSecundario),
          ),
          const SizedBox(height: TokensDimensoes.space16),
          CampoTexto(
            rotulo: 'Buscar tarefa',
            controlador: _busca,
            dica: 'Parte da descrição da atividade',
            acaoTeclado: TextInputAction.search,
            aoMudar: (valor) => setState(() => _termo = valor),
          ),
          const SizedBox(height: TokensDimensoes.space12),
          _FiltroDeGrau(
            nivel: _nivel,
            aoMudar: (nivel) => setState(() => _nivel = nivel),
          ),
          const SizedBox(height: TokensDimensoes.space16),
          ConteudoComCache<List<Tarefa>>(
            valor: consulta,
            carregando: 'Carregando tarefas...',
            falha: 'Não foi possível carregar a matriz de tarefas.',
            descreverFalha: descreverFalhaDeConsultaTarefa,
            aoTentarDeNovo: () => ref.invalidate(provider),
            construir: (tarefas, _) => _ListaDeTarefas(
              tarefas: tarefas,
              visiveis: filtrarTarefas(tarefas, _termo),
              filtrada: _nivel != null || _termo.trim().isNotEmpty,
              aoLimpar: _limpar,
            ),
          ),
        ],
      ),
    );
  }
}

/// "Todos" e os quatro graus, um de cada vez.
class _FiltroDeGrau extends StatelessWidget {
  const _FiltroDeGrau({required this.nivel, required this.aoMudar});

  final NivelPerigo? nivel;
  final ValueChanged<NivelPerigo?> aoMudar;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      container: true,
      label: 'Grau de perigo',
      child: Wrap(
        spacing: TokensDimensoes.space8,
        runSpacing: TokensDimensoes.space4,
        children: [
          ChoiceChip(
            label: const Text('Todos os graus'),
            selected: nivel == null,
            onSelected: (_) => aoMudar(null),
          ),
          for (final opcao in NivelPerigo.values)
            ChoiceChip(
              label: Text(opcao.rotulo),
              selected: nivel == opcao,
              onSelected: (_) => aoMudar(opcao),
            ),
        ],
      ),
    );
  }
}

class _ListaDeTarefas extends StatelessWidget {
  const _ListaDeTarefas({
    required this.tarefas,
    required this.visiveis,
    required this.filtrada,
    required this.aoLimpar,
  });

  final List<Tarefa> tarefas;
  final List<Tarefa> visiveis;
  final bool filtrada;
  final VoidCallback aoLimpar;

  @override
  Widget build(BuildContext context) {
    if (visiveis.isEmpty) {
      if (!filtrada) {
        return const EstadoVazio(
          icone: Icons.task_alt,
          titulo: 'Nenhuma tarefa cadastrada',
          descricao: 'As tarefas operacionais cadastradas aparecerão aqui.',
        );
      }
      return EstadoVazio(
        titulo: 'Nenhuma tarefa encontrada',
        descricao: 'Ajuste o termo de busca ou selecione outro grau de perigo.',
        rotuloAcao: 'Limpar filtros',
        aoAgir: aoLimpar,
      );
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          visiveis.length == 1 ? '1 tarefa' : '${visiveis.length} tarefas',
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const SizedBox(height: TokensDimensoes.space8),
        for (final tarefa in visiveis) ...[
          CartaoTarefa(
            tarefa: tarefa,
            aoTocar: tarefa.id == null
                ? null
                : () => context.go('/tarefas/${tarefa.id}'),
          ),
          const SizedBox(height: TokensDimensoes.space12),
        ],
      ],
    );
  }
}

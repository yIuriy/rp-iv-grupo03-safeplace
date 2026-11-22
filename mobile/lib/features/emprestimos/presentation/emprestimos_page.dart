// Tela principal de empréstimos de EPIs (/emprestimos, RF11, UC12, US11, issue #320).
// Permite listar empréstimos por situação (Em posse, Atrasados, Todos), buscar por colaborador ou EPI,
// iniciar nova entrega e registrar devolução. Gestor e Supervisor operam do mesmo modo.
// Com o servidor fora do ar, exibe cópia offline (RNF08) e desabilita ações de escrita com aviso.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/estados.dart';
import '../data/emprestimo.dart';
import '../data/emprestimos_api.dart';
import 'componentes_emprestimo.dart';
import 'dialogo_devolucao.dart';
import 'formulario_entrega_page.dart';

/// Filtra empréstimos por nome do colaborador ou nome do EPI ignorando maiúsculas e espaços.
List<Emprestimo> filtrarEmprestimos(List<Emprestimo> emprestimos, String termo) {
  final busca = termo.trim().toLowerCase();
  if (busca.isEmpty) return emprestimos;
  return emprestimos.where((emp) {
    final colab = emp.colaboradorNome.toLowerCase();
    final epi = emp.epiNome.toLowerCase();
    return colab.contains(busca) || epi.contains(busca);
  }).toList();
}

class EmprestimosPage extends ConsumerStatefulWidget {
  const EmprestimosPage({super.key});

  @override
  ConsumerState<EmprestimosPage> createState() => _EmprestimosPageState();
}

class _EmprestimosPageState extends ConsumerState<EmprestimosPage> {
  final _busca = TextEditingController();
  String _termo = '';
  SituacaoConsulta _situacao = SituacaoConsulta.pendentes;

  @override
  void dispose() {
    _busca.dispose();
    super.dispose();
  }

  void _limparFiltro() {
    _busca.clear();
    setState(() => _termo = '');
  }

  Future<void> _novoEmprestimo() async {
    final novo = await abrirFormularioEntrega(context);
    if (novo == null || !mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          'Entrega registrada: ${novo.epiNome} para ${novo.colaboradorNome}.',
        ),
      ),
    );
  }

  Future<void> _devolver(Emprestimo emprestimo) async {
    final devolvido = await abrirDialogoDevolucao(context, emprestimo: emprestimo);
    if (devolvido == null || !mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          devolvido.estadoDevolucao == EstadoDevolucao.apto
              ? 'Devolução registrada: ${devolvido.epiNome} voltou ao saldo disponível.'
              : 'Devolução registrada: ${devolvido.epiNome} fora do saldo disponível.',
        ),
      ),
    );
  }

  void _detalhar(Emprestimo emprestimo) {
    showDialog<void>(
      context: context,
      builder: (_) => DialogoDetalhesEmprestimo(emprestimo: emprestimo),
    );
  }

  @override
  Widget build(BuildContext context) {
    final filtro = FiltroEmprestimos(situacao: _situacao);
    final consulta = ref.watch(emprestimosProvider(filtro));
    final somenteLeitura = consulta.value?.somenteLeitura ?? false;
    final cores = context.cores;

    final (vazioTitulo, vazioDescricao) = switch (_situacao) {
      SituacaoConsulta.pendentes => (
        'Nenhum EPI em posse',
        'Os empréstimos ativos aparecerão aqui com o colaborador e o prazo de devolução.',
      ),
      SituacaoConsulta.atrasados => (
        'Nenhum empréstimo atrasado',
        'Empréstimos sem devolução depois da data prevista aparecerão aqui.',
      ),
      SituacaoConsulta.todos => (
        'Nenhum empréstimo registrado',
        'As entregas e devoluções registradas aparecerão aqui.',
      ),
    };

    final totalAtrasados = consulta.value?.dados
            .where((e) => e.estaAtrasado && e.emPosse)
            .length ??
        0;

    return RefreshIndicator(
      onRefresh: () => ref
          .refresh(emprestimosProvider(filtro).future)
          .then((_) {}, onError: (_) {}),
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Text(
            'A entrega retém uma unidade do EPI. Só o item devolvido apto volta ao saldo disponível.',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: cores.textoSecundario,
                ),
          ),
          const SizedBox(height: TokensDimensoes.space12),
          FilledButton.icon(
            onPressed: somenteLeitura ? null : _novoEmprestimo,
            icon: const Icon(Icons.add),
            label: const Text('Novo empréstimo'),
          ),
          if (somenteLeitura) ...[
            const SizedBox(height: TokensDimensoes.space8),
            Text(
              'Ações de escrita voltam quando a conexão voltar.',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: cores.textoSecundario,
                  ),
            ),
          ],
          const SizedBox(height: TokensDimensoes.space16),
          // Seletor de situação
          SegmentedButton<SituacaoConsulta>(
            segments: const [
              ButtonSegment(
                value: SituacaoConsulta.pendentes,
                label: Text('Em posse'),
              ),
              ButtonSegment(
                value: SituacaoConsulta.atrasados,
                label: Text('Atrasados'),
              ),
              ButtonSegment(
                value: SituacaoConsulta.todos,
                label: Text('Todos'),
              ),
            ],
            selected: {_situacao},
            onSelectionChanged: (nova) {
              if (nova.isNotEmpty) {
                setState(() => _situacao = nova.first);
              }
            },
          ),
          const SizedBox(height: TokensDimensoes.space16),
          CampoTexto(
            rotulo: 'Buscar por colaborador ou EPI',
            controlador: _busca,
            dica: 'Parte do nome do colaborador ou EPI',
            acaoTeclado: TextInputAction.search,
            aoMudar: (v) => setState(() => _termo = v),
          ),
          if (totalAtrasados > 0 && _situacao != SituacaoConsulta.atrasados) ...[
            const SizedBox(height: TokensDimensoes.space16),
            Aviso(
              titulo: totalAtrasados == 1
                  ? '1 empréstimo com devolução atrasada.'
                  : '$totalAtrasados empréstimos com devolução atrasada.',
              tom: TomAviso.alerta,
              anunciar: true,
            ),
          ],
          const SizedBox(height: TokensDimensoes.space16),
          ConteudoComCache<List<Emprestimo>>(
            valor: consulta,
            carregando: 'Carregando empréstimos...',
            falha: 'Não foi possível carregar os empréstimos.',
            descreverFalha: descreverFalhaDeConsultaEmprestimos,
            aoTentarDeNovo: () => ref.invalidate(emprestimosProvider(filtro)),
            construir: (emprestimos, _) {
              final visiveis = filtrarEmprestimos(emprestimos, _termo);

              if (emprestimos.isEmpty) {
                return EstadoVazio(
                  titulo: vazioTitulo,
                  descricao: vazioDescricao,
                  icone: Icons.inventory_outlined,
                );
              }

              if (visiveis.isEmpty) {
                return EstadoVazio(
                  titulo: 'Nenhum resultado',
                  descricao:
                      'Nenhum empréstimo corresponde à busca. Ajuste o termo ou limpe o filtro.',
                  icone: Icons.search_off,
                  rotuloAcao: 'Limpar busca',
                  aoAgir: _limparFiltro,
                );
              }

              return Column(
                children: [
                  for (final emp in visiveis)
                    CartaoEmprestimo(
                      emprestimo: emp,
                      somenteLeitura: somenteLeitura,
                      aoDevolver: () => _devolver(emp),
                      aoDetalhar: () => _detalhar(emp),
                    ),
                ],
              );
            },
          ),
        ],
      ),
    );
  }
}

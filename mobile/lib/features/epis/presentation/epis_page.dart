// Tela `/epis` (RF03, UC06, US03): catálogo com saldo, mínimo, status e estoque crítico destacado,
// busca local por nome ou localização e "puxar para atualizar". Gestor e Supervisor consultam a
// mesma lista, e só o Gestor vê o botão de cadastro. Com o servidor fora do ar, mostra a cópia local
// com o aviso de modo offline (RNF08) e o cadastro fica desabilitado.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/estados.dart';
import '../data/epi.dart';
import '../data/epis_api.dart';
import 'componentes_epi.dart';
import 'formulario_cadastro_epi_page.dart';

/// EPIs cujo nome ou localização contém o [termo], sem diferenciar maiúsculas (como o web).
List<Epi> filtrarEpis(List<Epi> epis, String termo) {
  final busca = termo.trim().toLowerCase();
  if (busca.isEmpty) return epis;
  return epis
      .where(
        (epi) =>
            epi.nome.toLowerCase().contains(busca) ||
            (epi.localizacao ?? '').toLowerCase().contains(busca),
      )
      .toList();
}

class EpisPage extends ConsumerStatefulWidget {
  const EpisPage({super.key});

  @override
  ConsumerState<EpisPage> createState() => _EpisPageState();
}

class _EpisPageState extends ConsumerState<EpisPage> {
  final _busca = TextEditingController();
  String _termo = '';

  @override
  void dispose() {
    _busca.dispose();
    super.dispose();
  }

  void _limpar() {
    _busca.clear();
    setState(() => _termo = '');
  }

  Future<void> _cadastrar() async {
    final epi = await abrirFormularioCadastroEpi(context);
    if (epi == null || !mounted) return;
    ScaffoldMessenger.of(context)
        .showSnackBar(const SnackBar(content: Text(mensagemEpiCadastrado)));
  }

  @override
  Widget build(BuildContext context) {
    final consulta = ref.watch(episProvider);
    final somenteLeitura = consulta.value?.somenteLeitura ?? false;
    final gestor =
        ref.watch(controladorSessaoProvider).value?.sessao?.ehGestor ?? false;
    final secundario = Theme.of(context).textTheme.bodyMedium
        ?.copyWith(color: context.cores.textoSecundario);
    return RefreshIndicator(
      onRefresh: () =>
          ref.refresh(episProvider.future).then((_) {}, onError: (_) {}),
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Text(
            gestor
                ? 'Cadastre EPIs, consulte o saldo e o histórico de cada um e registre entradas e saídas de estoque.'
                : 'Consulte o catálogo, o saldo e o histórico de cada EPI. Entradas e saídas são registradas pelo Gestor de Segurança.',
            style: secundario,
          ),
          if (gestor) ...[
            const SizedBox(height: TokensDimensoes.space12),
            FilledButton.icon(
              // Cadastro precisa do servidor: sem conexão, fica desabilitado (RNF08).
              onPressed: somenteLeitura ? null : _cadastrar,
              icon: const Icon(Icons.add),
              label: const Text('Cadastrar EPI'),
            ),
            if (somenteLeitura) ...[
              const SizedBox(height: TokensDimensoes.space8),
              Text(
                'Cadastros e alterações voltam quando a conexão voltar.',
                style: secundario,
              ),
            ],
          ],
          const SizedBox(height: TokensDimensoes.space16),
          CampoTexto(
            rotulo: 'Nome ou localização',
            controlador: _busca,
            dica: 'Parte do nome ou da localização',
            acaoTeclado: TextInputAction.search,
            aoMudar: (valor) => setState(() => _termo = valor),
          ),
          const SizedBox(height: TokensDimensoes.space16),
          ConteudoComCache<List<Epi>>(
            valor: consulta,
            carregando: 'Carregando EPIs...',
            falha: 'Não foi possível carregar o estoque de EPIs.',
            descreverFalha: descreverFalhaDeConsultaEpi,
            aoTentarDeNovo: () => ref.invalidate(episProvider),
            construir: (epis, _) => _ListaDeEpis(
              epis: epis,
              visiveis: filtrarEpis(epis, _termo),
              aoLimpar: _limpar,
            ),
          ),
        ],
      ),
    );
  }
}

/// Contagem de críticos, a lista filtrada ou o estado vazio certo (catálogo vazio ou busca sem
/// resultado).
class _ListaDeEpis extends StatelessWidget {
  const _ListaDeEpis({
    required this.epis,
    required this.visiveis,
    required this.aoLimpar,
  });

  final List<Epi> epis;
  final List<Epi> visiveis;
  final VoidCallback aoLimpar;

  @override
  Widget build(BuildContext context) {
    if (epis.isEmpty) {
      return const EstadoVazio(
        icone: Icons.inventory_2_outlined,
        titulo: 'Nenhum EPI cadastrado',
        descricao:
            'O catálogo aparecerá aqui assim que houver EPIs cadastrados.',
      );
    }
    if (visiveis.isEmpty) {
      return EstadoVazio(
        titulo: 'Nenhum EPI encontrado',
        descricao:
            'Nenhum EPI corresponde à busca. Ajuste o termo ou limpe o filtro.',
        rotuloAcao: 'Limpar busca',
        aoAgir: aoLimpar,
      );
    }
    final criticos = epis.where((epi) => epi.estoqueCritico).length;
    final textos = Theme.of(context).textTheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (criticos > 0) ...[
          Semantics(
            liveRegion: true,
            child: Text(
              '${criticos == 1 ? '1 EPI com estoque crítico' : '$criticos EPIs com estoque crítico'} '
              '(saldo igual ou abaixo do mínimo).',
              style: textos.bodyMedium?.copyWith(color: context.cores.perigo),
            ),
          ),
          const SizedBox(height: TokensDimensoes.space8),
        ],
        Text(
          visiveis.length == 1 ? '1 EPI' : '${visiveis.length} EPIs',
          style: textos.bodySmall,
        ),
        const SizedBox(height: TokensDimensoes.space8),
        for (final epi in visiveis) ...[
          CartaoEpi(
            epi: epi,
            aoTocar: epi.id == null
                ? null
                : () => context.go('/epis/${epi.id}'),
          ),
          const SizedBox(height: TokensDimensoes.space12),
        ],
      ],
    );
  }
}

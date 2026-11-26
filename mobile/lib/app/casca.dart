import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import '../core/sessao/controlador_sessao.dart';
import '../shared/tema/cores_safeplace.dart';
import '../shared/tema/tokens.g.dart';
import '../shared/widgets/dialogo_confirmacao.dart';
import '../shared/widgets/estados.dart';
import 'navegacao.dart';

/// Endereço um nível acima de [local]: `/epis/12` volta a `/epis`, e `/epis` volta ao início.
String rotaAcima(String local) {
  final barra = local.lastIndexOf('/');
  return barra <= 0 ? '/' : local.substring(0, barra);
}

/// Casca das telas autenticadas (#316), como o `AppLayout` do web: barra com o título da tela,
/// botão de menu, "Sair" sempre visível e o menu lateral montado pelo perfil de quem entrou.
///
/// Voltar (#335): numa sub-rota, como `/epis/12`, a barra mostra a seta de voltar no lugar do
/// menu. O voltar do Android leva cada módulo ao início, e só no início fecha o app.
class CascaApp extends ConsumerWidget {
  const CascaApp({super.key, required this.local, required this.filho});

  final String local;
  final Widget filho;

  /// "Sair" apaga a sessão e o cache offline da conta, então pede confirmação antes (#335).
  Future<void> _sair(BuildContext context, WidgetRef ref) async {
    final sair = await confirmar(
      context,
      titulo: 'Sair do SafePlace?',
      descricao:
          'Para voltar, você vai precisar entrar de novo com e-mail e senha. '
          'Os dados guardados neste aparelho para uso offline serão apagados.',
      rotuloConfirmar: 'Sair',
      rotuloCancelar: 'Cancelar',
      perigosa: false,
    );
    if (sair) await ref.read(controladorSessaoProvider.notifier).sair();
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessao = ref.watch(controladorSessaoProvider).value?.sessao;
    if (sessao == null) return const Scaffold(body: SizedBox.shrink());

    final itens = itensPara(sessao.perfil);
    final atual = itemDaRota(local);
    final subrota = atual != null && local != atual.rota;

    return PopScope(
      canPop: local == '/',
      onPopInvokedWithResult: (saiu, _) {
        if (!saiu) context.go(rotaAcima(local));
      },
      child: Scaffold(
        appBar: AppBar(
          leading: subrota
              ? BackButton(onPressed: () => context.go(rotaAcima(local)))
              : null,
          title: Text(atual?.rotuloPara(sessao.perfil) ?? 'SafePlace'),
          actions: [
            TextButton(
              onPressed: () => _sair(context, ref),
              child: const Text('Sair'),
            ),
          ],
        ),
        // ListTile, e não NavigationDrawerDestination: o rótulo quebra linha com zoom de texto
        // (RNF11) em vez de estourar, e o item atual continua anunciado como selecionado.
        drawer: Drawer(
          child: SafeArea(
            child: ListView(
              padding: const EdgeInsets.only(bottom: TokensDimensoes.space16),
              children: [
                _CabecalhoDoMenu(
                  nome: sessao.nome,
                  perfil: sessao.perfil.rotulo,
                ),
                const Divider(height: 1),
                for (final item in itens)
                  _ItemDoMenu(
                    item: item,
                    rotulo: item.rotuloPara(sessao.perfil),
                    atual: item == atual,
                    aoTocar: () {
                      Navigator.of(context).pop();
                      context.go(item.rota);
                    },
                  ),
              ],
            ),
          ),
        ),
        body: SafeArea(child: filho),
      ),
    );
  }
}

class _ItemDoMenu extends StatelessWidget {
  const _ItemDoMenu({
    required this.item,
    required this.rotulo,
    required this.atual,
    required this.aoTocar,
  });

  final ItemMenu item;
  final String rotulo;
  final bool atual;
  final VoidCallback aoTocar;

  @override
  Widget build(BuildContext context) {
    final esquema = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: TokensDimensoes.space12,
        vertical: 2,
      ),
      child: ListTile(
        selected: atual,
        selectedColor: esquema.onPrimaryContainer,
        selectedTileColor: esquema.primaryContainer,
        iconColor: context.cores.textoSecundario,
        shape: const StadiumBorder(),
        leading: Icon(item.icone),
        title: Text(
          rotulo,
          style: Theme.of(context).textTheme.labelLarge
              ?.copyWith(color: atual ? esquema.onPrimaryContainer : null),
        ),
        onTap: aoTocar,
      ),
    );
  }
}

class _CabecalhoDoMenu extends StatelessWidget {
  const _CabecalhoDoMenu({required this.nome, required this.perfil});

  final String nome;
  final String perfil;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.fromLTRB(
        28,
        TokensDimensoes.space16,
        16,
        TokensDimensoes.space16,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'SafePlace',
            style: textos.headlineSmall?.copyWith(
              color: Theme.of(context).colorScheme.primary,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: TokensDimensoes.space8),
          Text(nome, style: textos.titleMedium),
          Text(
            perfil,
            style: textos.bodyMedium?.copyWith(
              color: context.cores.textoSecundario,
            ),
          ),
        ],
      ),
    );
  }
}

/// Tela de um módulo ainda não entregue, como o `FeaturePage` vazio do web. Diz qual issue traz a
/// tela, para quem testa saber que é esperado.
class PaginaEmConstrucao extends StatelessWidget {
  const PaginaEmConstrucao({super.key, required this.item});

  final ItemMenu item;

  @override
  Widget build(BuildContext context) {
    return EstadoVazio(
      icone: Icons.construction_outlined,
      titulo: 'Funcionalidade em desenvolvimento',
      descricao:
          '${item.rotulo} chega ao app na issue #${item.issue}. '
          'Enquanto isso, use a versão web do SafePlace.',
    );
  }
}

/// Endereço desconhecido, como no web: explica e leva de volta ao início.
class PaginaNaoEncontrada extends StatelessWidget {
  const PaginaNaoEncontrada({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('SafePlace')),
      body: EstadoVazio(
        icone: Icons.explore_off_outlined,
        titulo: 'Página não encontrada',
        descricao:
            'O endereço não existe ou não está disponível para o seu perfil.',
        rotuloAcao: 'Voltar ao início',
        aoAgir: () => context.go('/'),
      ),
    );
  }
}

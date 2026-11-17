import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';
import '../../../shared/formularios/cpf.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../data/minha_conta.dart';

/// Atalho do Início para uma operação de cadastro. A casca (app/) diz para onde ele leva, já que
/// a feature não conhece as rotas das outras.
typedef AtalhoInicio = ({String rotulo, IconData icone, VoidCallback aoTocar});

/// Primeira tela depois do login (#316). Mostra os dados da conta com cópia local e os atalhos de
/// cadastro, que ficam desabilitados no modo offline (somente leitura, RNF08).
class InicioPage extends ConsumerWidget {
  const InicioPage({super.key, this.atalhos = const []});

  final List<AtalhoInicio> atalhos;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessao = ref.watch(controladorSessaoProvider).value?.sessao;
    final textos = Theme.of(context).textTheme;
    if (sessao == null) return const SizedBox.shrink();

    return RefreshIndicator(
      onRefresh: () =>
          ref.refresh(minhaContaProvider.future).then((_) {}, onError: (_) {}),
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Text('Olá, ${sessao.nome}', style: textos.headlineMedium),
          Text(
            sessao.perfil.rotulo,
            style: textos.bodyLarge?.copyWith(
              color: context.cores.textoSecundario,
            ),
          ),
          const SizedBox(height: TokensDimensoes.space24),
          ConteudoComCache<MinhaConta>(
            valor: ref.watch(minhaContaProvider),
            carregando: 'Carregando seus dados...',
            falha: 'Não foi possível carregar seus dados.',
            aoTentarDeNovo: () => ref.invalidate(minhaContaProvider),
            construir: (conta, somenteLeitura) => Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _CartaoConta(conta),
                if (atalhos.isNotEmpty) ...[
                  const SizedBox(height: TokensDimensoes.space24),
                  Semantics(
                    header: true,
                    child: Text('Registrar', style: textos.titleLarge),
                  ),
                  const SizedBox(height: TokensDimensoes.space8),
                  for (final atalho in atalhos) ...[
                    FilledButton.icon(
                      // Cadastro precisa do servidor: sem conexão, o botão fica desabilitado.
                      onPressed: somenteLeitura ? null : atalho.aoTocar,
                      icon: Icon(atalho.icone),
                      label: Text(atalho.rotulo),
                    ),
                    const SizedBox(height: TokensDimensoes.space8),
                  ],
                  if (somenteLeitura)
                    Text(
                      'Os cadastros voltam quando a conexão voltar.',
                      style: textos.bodyMedium?.copyWith(
                        color: context.cores.textoSecundario,
                      ),
                    ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _CartaoConta extends StatelessWidget {
  const _CartaoConta(this.conta);

  final MinhaConta conta;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    Widget linha(String rotulo, String valor) => Padding(
      padding: const EdgeInsets.only(top: TokensDimensoes.space8),
      child: MergeSemantics(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              rotulo,
              style: textos.labelMedium?.copyWith(
                color: context.cores.textoSecundario,
              ),
            ),
            Text(valor, style: textos.bodyLarge),
          ],
        ),
      ),
    );

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(TokensDimensoes.space16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Seus dados', style: textos.titleLarge),
            linha('Nome', conta.nome),
            linha('E-mail', conta.email),
            if (conta.cpf != null) linha('CPF', ocultarCpf(conta.cpf!)),
          ],
        ),
      ),
    );
  }
}

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';

/// Tela provisória depois do login. A casca de navegação por perfil da #316 a substitui.
class InicioPage extends ConsumerWidget {
  const InicioPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessao = ref.watch(controladorSessaoProvider).value?.sessao;
    return Scaffold(
      appBar: AppBar(
        title: const Text('SafePlace'),
        actions: [
          TextButton(
            onPressed: () =>
                ref.read(controladorSessaoProvider.notifier).sair(),
            child: const Text('Sair'),
          ),
        ],
      ),
      body: Center(
        child: sessao == null
            ? const SizedBox.shrink()
            : Text(
                'Olá, ${sessao.nome}\n${sessao.perfil.rotulo}',
                textAlign: TextAlign.center,
              ),
      ),
    );
  }
}

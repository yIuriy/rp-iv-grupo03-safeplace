// Aviso de descarte dos formulários em tela cheia (#335). Fechar pelo X, pelo "Cancelar" ou pelo
// voltar do Android com dados digitados pede confirmação antes de perder o preenchimento.

import 'package:material_ui/material_ui.dart';

import 'dialogo_confirmacao.dart';

/// Envolve a tela de um formulário. Com [alterado], fechar a tela abre o diálogo de descarte.
/// Durante o [enviando], a tela não fecha. Os botões de fechar da tela devem chamar
/// `Navigator.maybePop`, que respeita esta guarda. `Navigator.pop` fecha sem perguntar e serve
/// para devolver o resultado depois de salvar.
class GuardaDeDescarte extends StatelessWidget {
  const GuardaDeDescarte({
    super.key,
    required this.alterado,
    required this.enviando,
    required this.child,
  });

  final bool alterado;
  final bool enviando;
  final Widget child;

  Future<void> _perguntar(BuildContext context) async {
    final descartar = await confirmar(
      context,
      titulo: 'Descartar o preenchimento?',
      descricao: 'Os dados preenchidos neste formulário serão perdidos.',
      rotuloConfirmar: 'Descartar',
    );
    if (descartar && context.mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return PopScope<Object?>(
      canPop: !alterado && !enviando,
      onPopInvokedWithResult: (saiu, _) {
        if (!saiu && !enviando) _perguntar(context);
      },
      child: child,
    );
  }
}

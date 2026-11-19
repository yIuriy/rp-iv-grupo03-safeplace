import 'package:material_ui/material_ui.dart';

import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../data/usuario.dart';
import 'colaborador_picker.dart';

/// Tela de exemplo do [ColaboradorPicker] (#317), só no modo debug: um formulário mínimo, como o
/// de registro de ocorrência ou de entrega de EPI, que exige um colaborador e mostra o que seria
/// enviado. Abre pelo catálogo (`/catalogo`) ou em `/usuarios/exemplo-seletor`.
class ExemploColaboradorPickerPage extends StatefulWidget {
  const ExemploColaboradorPickerPage({super.key});

  @override
  State<ExemploColaboradorPickerPage> createState() =>
      _ExemploColaboradorPickerPageState();
}

class _ExemploColaboradorPickerPageState
    extends State<ExemploColaboradorPickerPage> {
  final _form = GlobalKey<FormState>();
  Usuario? _escolhido;
  Usuario? _enviado;

  void _enviar() {
    if (!_form.currentState!.validate()) return;
    setState(() => _enviado = _escolhido);
  }

  @override
  Widget build(BuildContext context) {
    final enviado = _enviado;
    return Form(
      key: _form,
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Text(
            'Exemplo do seletor de colaborador',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: TokensDimensoes.space4),
          Text(
            'Os módulos de ocorrências, empréstimos, alocações e capacitações usam este campo para vincular um colaborador.',
            style: Theme.of(context).textTheme.bodyMedium
                ?.copyWith(color: context.cores.textoSecundario),
          ),
          const SizedBox(height: TokensDimensoes.space24),
          ColaboradorPicker(
            rotulo: 'Colaborador envolvido',
            ajuda: 'Busque pelo nome ou pelo CPF.',
            obrigatorio: true,
            colaboradorId: _escolhido?.id,
            aoMudar: (colaborador) => setState(() => _escolhido = colaborador),
          ),
          const SizedBox(height: TokensDimensoes.space24),
          FilledButton(onPressed: _enviar, child: const Text('Confirmar')),
          if (enviado != null) ...[
            const SizedBox(height: TokensDimensoes.space16),
            Aviso(
              tom: TomAviso.sucesso,
              anunciar: true,
              titulo: 'Colaborador escolhido: ${enviado.nome}',
              mensagem: 'O formulário enviaria colaboradorId = ${enviado.id}.',
            ),
          ],
        ],
      ),
    );
  }
}

// Etapas do fluxo de entrega de EPI: confirmação de duplicidade com justificativa
// obrigatória e tela de conclusão com o termo de cautela registrado.
// Portado de `frontend/src/features/emprestimos/EntregaEtapas.tsx`.

import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../data/emprestimo.dart';
import '../domain/validacao_emprestimo.dart';

/// Etapa 2: Confirmação de duplicidade quando o servidor recusa a entrega (409)
/// porque o colaborador já possui EPI do mesmo tipo em posse.
class DuplicidadeConfirmacaoWidget extends StatelessWidget {
  const DuplicidadeConfirmacaoWidget({
    super.key,
    required this.mensagem,
    this.falha,
    required this.enviando,
    required this.controladorJustificativa,
    required this.formKey,
    required this.aoVoltar,
    required this.aoConfirmar,
  });

  final String mensagem;
  final String? falha;
  final bool enviando;
  final TextEditingController controladorJustificativa;
  final GlobalKey<FormState> formKey;
  final VoidCallback aoVoltar;
  final ValueChanged<String> aoConfirmar;

  void _submeter() {
    if (enviando) return;
    if (formKey.currentState?.validate() ?? false) {
      aoConfirmar(controladorJustificativa.text.trim());
    }
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: formKey,
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Aviso(
            titulo: 'Item do mesmo tipo em posse',
            mensagem: mensagem,
            tom: TomAviso.alerta,
            anunciar: true,
          ),
          const SizedBox(height: TokensDimensoes.space16),
          CampoTexto(
            rotulo: 'Justificativa da substituição',
            controlador: controladorJustificativa,
            linhas: 3,
            habilitado: !enviando,
            ajuda: 'Motivo da nova entrega sem a devolução do item anterior.',
            validador: validarJustificativa,
          ),
          if (falha != null) ...[
            const SizedBox(height: TokensDimensoes.space16),
            Aviso(
              titulo: 'Entrega não registrada',
              mensagem: falha,
              tom: TomAviso.perigo,
              anunciar: true,
            ),
          ],
          const SizedBox(height: TokensDimensoes.space24),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: enviando ? null : aoVoltar,
                  child: const Text('Voltar'),
                ),
              ),
              const SizedBox(width: TokensDimensoes.space12),
              Expanded(
                child: FilledButton.icon(
                  onPressed: enviando ? null : _submeter,
                  icon: enviando
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.check),
                  label: const Text('Confirmar substituição'),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Etapa 3: Conclusão da entrega exibindo confirmação de saída do estoque e o termo de cautela.
class EntregaConcluidaWidget extends StatelessWidget {
  const EntregaConcluidaWidget({
    super.key,
    required this.emprestimo,
    required this.aoFechar,
  });

  final Emprestimo emprestimo;
  final VoidCallback aoFechar;

  @override
  Widget build(BuildContext context) {
    final dataDevolucao = emprestimo.dataDevolucaoPrevista;
    final dataFormatada = dataDevolucao != null
        ? formatarData(dataDevolucao)
        : 'não informada';

    return ListView(
      padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
      children: [
        Aviso(
          titulo: 'Entrega registrada',
          mensagem:
              '${emprestimo.epiNome} está em posse de ${emprestimo.colaboradorNome}, '
              'com devolução prevista para $dataFormatada. Uma unidade saiu do estoque.',
          tom: TomAviso.sucesso,
          anunciar: true,
        ),
        const SizedBox(height: TokensDimensoes.space24),
        Text(
          'Termo de cautela',
          style: Theme.of(context).textTheme.titleMedium
              ?.copyWith(fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        Container(
          padding: const EdgeInsets.all(TokensDimensoes.space16),
          decoration: BoxDecoration(
            color: context.cores.pagina,
            borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
            border: Border.all(color: context.cores.borda),
          ),
          child: Text(
            emprestimo.termoCautela.isNotEmpty
                ? emprestimo.termoCautela
                : 'Termo de cautela não informado.',
            style: Theme.of(context).textTheme.bodyMedium
                ?.copyWith(fontFamily: 'monospace'),
          ),
        ),
        const SizedBox(height: TokensDimensoes.space24),
        FilledButton.icon(
          onPressed: aoFechar,
          icon: const Icon(Icons.check),
          label: const Text('Fechar'),
        ),
      ],
    );
  }
}

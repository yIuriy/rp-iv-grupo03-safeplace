// Diálogo para registrar a devolução de um EPI emprestado (RF11, UC12, US11).
// Pede a condição do item (APTO, MANUTENCAO ou DESCARTE) e informa o destino no estoque.
// Trata o erro 409 quando o empréstimo já foi encerrado por outro operador.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../data/emprestimo.dart';
import '../data/emprestimos_api.dart';
import '../domain/validacao_emprestimo.dart';

/// Abre o diálogo modal de devolução. Devolve o [Emprestimo] encerrado ou `null` se cancelado.
Future<Emprestimo?> abrirDialogoDevolucao(
  BuildContext context, {
  required Emprestimo emprestimo,
}) {
  return showDialog<Emprestimo>(
    context: context,
    builder: (_) => DialogoDevolucao(emprestimo: emprestimo),
  );
}

class DialogoDevolucao extends ConsumerStatefulWidget {
  const DialogoDevolucao({super.key, required this.emprestimo});

  final Emprestimo emprestimo;

  @override
  ConsumerState<DialogoDevolucao> createState() => _DialogoDevolucaoState();
}

class _DialogoDevolucaoState extends ConsumerState<DialogoDevolucao> {
  EstadoDevolucao? _estado;
  String? _erroValidacao;
  String? _falha;
  bool _enviando = false;

  Future<void> _submeter() async {
    final validacao = validarEstadoDevolucao(_estado);
    if (validacao != null) {
      setState(() => _erroValidacao = validacao);
      return;
    }

    final id = widget.emprestimo.id;
    if (id == null) return;

    setState(() {
      _erroValidacao = null;
      _falha = null;
      _enviando = true;
    });

    try {
      final repo = ref.read(emprestimoRepositoryProvider);
      final resultado = await repo.registrarDevolucao(id, _estado!);

      ref.invalidate(emprestimosProvider);
      ref.invalidate(episDisponiveisProvider);

      if (!mounted) return;
      Navigator.of(context).pop(resultado);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeDevolucao(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final emp = widget.emprestimo;
    final cores = context.cores;
    final retirada = emp.dataRetirada != null
        ? formatarData(emp.dataRetirada!)
        : 'não informada';
    final devolucaoPrevista = emp.dataDevolucaoPrevista != null
        ? formatarData(emp.dataDevolucaoPrevista!)
        : 'não informada';

    return AlertDialog(
      title: const Text('Registrar devolução'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              'Confira o item recebido e informe sua condição antes de concluir.',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: cores.textoSecundario,
                  ),
            ),
            const SizedBox(height: TokensDimensoes.space16),
            _ItemDetalhe(rotulo: 'Colaborador', valor: emp.colaboradorNome),
            _ItemDetalhe(
              rotulo: 'Equipamento',
              valor: emp.epiId != null
                  ? '${emp.epiNome} (código ${emp.epiId})'
                  : emp.epiNome,
            ),
            _ItemDetalhe(rotulo: 'Entregue em', valor: retirada),
            _ItemDetalhe(
              rotulo: 'Devolução prevista',
              valor: '$devolucaoPrevista${emp.estaAtrasado ? " (prazo vencido)" : ""}',
            ),
            const SizedBox(height: TokensDimensoes.space16),
            Text(
              'Condição do item',
              style: Theme.of(context).textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: TokensDimensoes.space4),
            RadioGroup<EstadoDevolucao>(
              groupValue: _estado,
              onChanged: (val) {
                if (_enviando) return;
                setState(() {
                  _estado = val;
                  _erroValidacao = null;
                });
              },
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  for (final opcao in EstadoDevolucao.values)
                    RadioListTile<EstadoDevolucao>(
                      value: opcao,
                      contentPadding: EdgeInsets.zero,
                      dense: true,
                      title: Text(opcao.rotulo),
                    ),
                ],
              ),
            ),
            if (_erroValidacao != null) ...[
              const SizedBox(height: TokensDimensoes.space4),
              Text(
                _erroValidacao!,
                style: TextStyle(
                  color: cores.perigo,
                  fontSize: 12,
                ),
              ),
            ],
            if (_estado != null) ...[
              const SizedBox(height: TokensDimensoes.space8),
              Container(
                padding: const EdgeInsets.all(TokensDimensoes.space12),
                decoration: BoxDecoration(
                  color: cores.pagina,
                  borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
                  border: Border.all(color: cores.borda),
                ),
                child: Text(
                  _estado!.destino,
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ),
            ],
            if (_falha != null) ...[
              const SizedBox(height: TokensDimensoes.space12),
              Aviso(
                titulo: 'Devolução não registrada',
                mensagem: _falha,
                tom: TomAviso.perigo,
                anunciar: true,
              ),
            ],
            const SizedBox(height: TokensDimensoes.space8),
            Text(
              'A data, a hora e o responsável pela devolução são registrados na confirmação.',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: cores.textoSecundario,
                    fontStyle: FontStyle.italic,
                  ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: _enviando ? null : () => Navigator.of(context).pop(),
          child: const Text('Cancelar'),
        ),
        FilledButton.icon(
          onPressed: _enviando ? null : _submeter,
          icon: _enviando
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Icon(Icons.check),
          label: const Text('Confirmar devolução'),
        ),
      ],
    );
  }
}

class _ItemDetalhe extends StatelessWidget {
  const _ItemDetalhe({required this.rotulo, required this.valor});

  final String rotulo;
  final String valor;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Text.rich(
        TextSpan(
          style: Theme.of(context).textTheme.bodyMedium,
          children: [
            TextSpan(
              text: '$rotulo: ',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            TextSpan(text: valor),
          ],
        ),
      ),
    );
  }
}

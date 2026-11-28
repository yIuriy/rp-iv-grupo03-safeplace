// Componentes visuais do módulo de empréstimos: etiquetas de situação,
// cartão com destaque de atraso e diálogo de detalhes do empréstimo.

import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../data/emprestimo.dart';

/// Etiqueta visual da situação do empréstimo (Devolvido, Atrasado ou Em posse).
class SeloSituacaoEmprestimo extends StatelessWidget {
  const SeloSituacaoEmprestimo({super.key, required this.emprestimo});

  final Emprestimo emprestimo;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final (rotulo, frente, fundo, icone) = switch (emprestimo.status) {
      ClassificacaoEmprestimo.disponivel => (
        'Devolvido',
        cores.sucesso,
        cores.sucessoFundo,
        Icons.check_circle_outline,
      ),
      _ when emprestimo.estaAtrasado => (
        'Atrasado',
        cores.perigo,
        cores.perigoFundo,
        Icons.error_outline,
      ),
      _ => ('Em posse', cores.info, cores.infoFundo, Icons.info_outline),
    };

    return DecoratedBox(
      decoration: BoxDecoration(
        color: fundo,
        borderRadius: BorderRadius.circular(TokensDimensoes.radius16),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: TokensDimensoes.space8,
          vertical: TokensDimensoes.space4,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icone, size: 14, color: frente),
            const SizedBox(width: TokensDimensoes.space4),
            Text(
              rotulo,
              style: Theme.of(context).textTheme.labelSmall
                  ?.copyWith(color: frente, fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }
}

/// Cartão de um empréstimo na lista, com destaque visual e semântico se atrasado.
class CartaoEmprestimo extends StatelessWidget {
  const CartaoEmprestimo({
    super.key,
    required this.emprestimo,
    required this.aoDevolver,
    required this.aoDetalhar,
    this.somenteLeitura = false,
  });

  final Emprestimo emprestimo;
  final VoidCallback aoDevolver;
  final VoidCallback aoDetalhar;
  final bool somenteLeitura;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final atrasado = emprestimo.estaAtrasado;
    final retirada = emprestimo.dataRetirada != null
        ? formatarData(emprestimo.dataRetirada!)
        : 'não informada';
    final previsao = emprestimo.dataDevolucaoPrevista != null
        ? formatarData(emprestimo.dataDevolucaoPrevista!)
        : 'não informada';

    final rotuloSemantico = [
      'Empréstimo de ${emprestimo.epiNome} para ${emprestimo.colaboradorNome}',
      if (atrasado) 'Devolução atrasada, prazo venceu em $previsao',
    ].join('. ');

    return Semantics(
      label: rotuloSemantico,
      container: true,
      child: Card(
        margin: const EdgeInsets.symmetric(vertical: TokensDimensoes.space8),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
          side: BorderSide(
            color: atrasado ? cores.perigo : cores.borda,
            width: atrasado ? 2 : 1,
          ),
        ),
        child: InkWell(
          onTap: aoDetalhar,
          borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
          child: Padding(
            padding: const EdgeInsets.all(TokensDimensoes.space16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            emprestimo.epiNome,
                            style: Theme.of(context).textTheme.titleMedium
                                ?.copyWith(fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: TokensDimensoes.space4),
                          Text(
                            emprestimo.colaboradorNome,
                            style: Theme.of(context).textTheme.bodyMedium
                                ?.copyWith(color: cores.textoSecundario),
                          ),
                        ],
                      ),
                    ),
                    SeloSituacaoEmprestimo(emprestimo: emprestimo),
                  ],
                ),
                const SizedBox(height: TokensDimensoes.space12),
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        'Entrega: $retirada',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ),
                    Expanded(
                      child: Text(
                        'Previsão: $previsao',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: atrasado ? cores.perigo : null,
                          fontWeight: atrasado
                              ? FontWeight.bold
                              : FontWeight.normal,
                        ),
                      ),
                    ),
                  ],
                ),
                if (atrasado) ...[
                  const SizedBox(height: TokensDimensoes.space8),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: TokensDimensoes.space8,
                      vertical: TokensDimensoes.space4,
                    ),
                    decoration: BoxDecoration(
                      color: cores.perigoFundo,
                      borderRadius: BorderRadius.circular(
                        TokensDimensoes.radius4,
                      ),
                    ),
                    child: Row(
                      children: [
                        Icon(Icons.warning, size: 14, color: cores.perigo),
                        const SizedBox(width: TokensDimensoes.space4),
                        Text(
                          'Devolução atrasada',
                          style: TextStyle(
                            color: cores.perigo,
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton(
                      onPressed: aoDetalhar,
                      child: const Text('Detalhes'),
                    ),
                    if (emprestimo.emPosse) ...[
                      const SizedBox(width: TokensDimensoes.space8),
                      FilledButton(
                        onPressed: somenteLeitura ? null : aoDevolver,
                        child: const Text('Devolver'),
                      ),
                    ],
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Diálogo de detalhes de um empréstimo selecionado.
class DialogoDetalhesEmprestimo extends StatelessWidget {
  const DialogoDetalhesEmprestimo({super.key, required this.emprestimo});

  final Emprestimo emprestimo;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final retirada = emprestimo.dataRetirada != null
        ? formatarData(emprestimo.dataRetirada!)
        : 'não informada';
    final previsao = emprestimo.dataDevolucaoPrevista != null
        ? formatarData(emprestimo.dataDevolucaoPrevista!)
        : 'não informada';
    final devolucaoReal = emprestimo.dataDevolucaoReal != null
        ? formatarData(emprestimo.dataDevolucaoReal!)
        : null;

    return AlertDialog(
      title: const Text('Detalhes do empréstimo'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    emprestimo.epiNome,
                    style: Theme.of(context).textTheme.titleMedium
                        ?.copyWith(fontWeight: FontWeight.bold),
                  ),
                ),
                SeloSituacaoEmprestimo(emprestimo: emprestimo),
              ],
            ),
            const SizedBox(height: TokensDimensoes.space12),
            _LinhaDetalhe(
              rotulo: 'Colaborador',
              valor: emprestimo.colaboradorNome,
            ),
            if (emprestimo.epiId != null)
              _LinhaDetalhe(
                rotulo: 'Código do EPI',
                valor: '${emprestimo.epiId}',
              ),
            _LinhaDetalhe(rotulo: 'Data de entrega', valor: retirada),
            _LinhaDetalhe(
              rotulo: 'Devolução prevista',
              valor: previsao + (emprestimo.estaAtrasado ? ' (atrasada)' : ''),
            ),
            if (devolucaoReal != null)
              _LinhaDetalhe(
                rotulo: 'Devolução realizada',
                valor: devolucaoReal,
              ),
            if (emprestimo.estadoDevolucao != null)
              _LinhaDetalhe(
                rotulo: 'Condição na devolução',
                valor: emprestimo.estadoDevolucao!.rotulo,
              ),
            if (emprestimo.justificativaSubstituicao != null) ...[
              const SizedBox(height: TokensDimensoes.space8),
              _LinhaDetalhe(
                rotulo: 'Justificativa de substituição',
                valor: emprestimo.justificativaSubstituicao!,
              ),
            ],
            const SizedBox(height: TokensDimensoes.space16),
            Text(
              'Termo de cautela',
              style: Theme.of(context).textTheme.titleSmall
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: TokensDimensoes.space4),
            Container(
              padding: const EdgeInsets.all(TokensDimensoes.space12),
              decoration: BoxDecoration(
                color: cores.pagina,
                borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
                border: Border.all(color: cores.borda),
              ),
              child: Text(
                emprestimo.termoCautela.isNotEmpty
                    ? emprestimo.termoCautela
                    : 'Termo de cautela não informado pelo servidor.',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Fechar'),
        ),
      ],
    );
  }
}

class _LinhaDetalhe extends StatelessWidget {
  const _LinhaDetalhe({required this.rotulo, required this.valor});

  final String rotulo;
  final String valor;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
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

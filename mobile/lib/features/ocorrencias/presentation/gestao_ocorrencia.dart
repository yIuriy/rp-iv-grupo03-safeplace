// Operações do Gestor no detalhe da ocorrência (#321): editar dados, enviar para triagem, arquivar
// e emitir o protocolo CAT. A tela não testa o status: pergunta ao `EstadoOcorrencia` (State) quais
// operações existem e executa cada uma (Command) pelo mesmo caminho, o diálogo `confirmar` da #316,
// que mostra "carregando" e a mensagem do backend em caso de recusa. Sem conexão, tudo fica
// desabilitado (RNF08); o Supervisor não vê esta seção.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/tema/app_theme.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/dialogo_confirmacao.dart';
import '../data/ocorrencia.dart';
import '../data/ocorrencias_api.dart';
import '../domain/estado_ocorrencia.dart';
import '../domain/operacao_ocorrencia.dart';
import 'formulario_ocorrencia_page.dart';

class GestaoDaOcorrencia extends ConsumerWidget {
  const GestaoDaOcorrencia({
    super.key,
    required this.ocorrencia,
    required this.somenteLeitura,
  });

  final Ocorrencia ocorrencia;
  final bool somenteLeitura;

  void _avisar(BuildContext context, String mensagem) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text(mensagem)));
  }

  void _recarregar(WidgetRef ref, int id) {
    ref.invalidate(ocorrenciaProvider(id));
    ref.invalidate(ocorrenciasProvider);
  }

  /// Executa o comando com o diálogo de confirmação; só avisa e recarrega se deu certo.
  Future<void> _executar(
    BuildContext context,
    WidgetRef ref,
    OperacaoOcorrencia operacao,
    int id,
  ) async {
    Ocorrencia? atualizada;
    final confirmacao = operacao.confirmacao;
    final concluiu = await confirmar(
      context,
      titulo: confirmacao?.titulo ?? operacao.rotulo,
      descricao: confirmacao?.descricao ?? '',
      rotuloConfirmar: confirmacao?.rotuloConfirmar ?? operacao.rotulo,
      rotuloCancelar: 'Cancelar',
      perigosa: operacao.destrutiva,
      aoConfirmar: () async {
        atualizada = await operacao.executar(
          ref.read(ocorrenciaRepositoryProvider),
          id,
        );
      },
      descreverFalha: descreverFalhaDeOcorrencia,
    );
    if (!concluiu || !context.mounted) return;
    _recarregar(ref, id);
    _avisar(context, operacao.mensagemDeSucesso(atualizada ?? ocorrencia));
  }

  Future<void> _editar(BuildContext context, WidgetRef ref, int id) async {
    final salva = await abrirEdicaoOcorrencia(context, ocorrencia);
    if (salva == null || !context.mounted) return;
    _recarregar(ref, id);
    _avisar(context, 'Alterações salvas.');
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = ocorrencia.id;
    if (id == null) return const SizedBox.shrink();
    final estado = EstadoOcorrencia.de(ocorrencia.status);
    final operacoes = estado.operacoes(ocorrencia);
    final textos = Theme.of(context).textTheme;

    final aviso = estado.aviso;
    if (aviso != null) {
      return Aviso(
        titulo: 'Ocorrência ${estado.status.rotulo.toLowerCase()}',
        mensagem: aviso,
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Semantics(
          header: true,
          child: Text('Acompanhamento', style: textos.titleLarge),
        ),
        if (estado.proximo case final proximo?)
          Padding(
            padding: const EdgeInsets.only(top: TokensDimensoes.space4),
            child: Text(
              'Próxima etapa: ${proximo.rotulo.toLowerCase()}.',
              style: textos.bodyMedium?.copyWith(
                color: context.cores.textoSecundario,
              ),
            ),
          ),
        const SizedBox(height: TokensDimensoes.space12),
        Wrap(
          spacing: TokensDimensoes.space8,
          runSpacing: TokensDimensoes.space8,
          children: [
            if (estado.permiteEdicao)
              OutlinedButton.icon(
                onPressed: somenteLeitura
                    ? null
                    : () => _editar(context, ref, id),
                icon: const Icon(Icons.edit_outlined),
                label: const Text('Editar dados'),
              ),
            for (final operacao in operacoes)
              FilledButton(
                style: operacao.destrutiva ? AppTheme.botaoPerigo : null,
                onPressed: somenteLeitura
                    ? null
                    : () => _executar(context, ref, operacao, id),
                child: Text(operacao.rotulo),
              ),
          ],
        ),
        if (somenteLeitura)
          Padding(
            padding: const EdgeInsets.only(top: TokensDimensoes.space8),
            child: Text(
              'Alterações indisponíveis sem conexão.',
              style: textos.bodyMedium?.copyWith(
                color: context.cores.textoSecundario,
              ),
            ),
          ),
      ],
    );
  }
}

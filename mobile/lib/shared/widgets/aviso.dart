import 'package:material_ui/material_ui.dart';

import '../tema/cores_safeplace.dart';
import '../tema/tokens.g.dart';

/// Tom do aviso; cada um tem cor, fundo e ícone próprios. O texto diz o que aconteceu: a cor
/// sozinha nunca carrega a informação.
enum TomAviso { info, sucesso, alerta, perigo }

/// Aviso com título e mensagem (#316), o equivalente do `Alert` do web.
///
/// [anunciar] faz o leitor de tela ler o aviso assim que ele aparece: use para resultado de
/// operação e falhas, não para avisos fixos da tela. [acao] é um botão opcional de recuperação,
/// como "Tentar novamente".
class Aviso extends StatelessWidget {
  const Aviso({
    super.key,
    required this.titulo,
    this.mensagem,
    this.tom = TomAviso.info,
    this.anunciar = false,
    this.rotuloAcao,
    this.aoAgir,
  }) : assert(
         (rotuloAcao == null) == (aoAgir == null),
         'Ação precisa de rótulo e callback.',
       );

  final String titulo;
  final String? mensagem;
  final TomAviso tom;
  final bool anunciar;
  final String? rotuloAcao;
  final VoidCallback? aoAgir;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final textos = Theme.of(context).textTheme;
    final (frente, fundo, icone) = switch (tom) {
      TomAviso.info => (cores.info, cores.infoFundo, Icons.info_outline),
      TomAviso.sucesso => (
        cores.sucesso,
        cores.sucessoFundo,
        Icons.check_circle_outline,
      ),
      TomAviso.alerta => (
        cores.alerta,
        cores.alertaFundo,
        Icons.warning_amber_outlined,
      ),
      TomAviso.perigo => (cores.perigo, cores.perigoFundo, Icons.error_outline),
    };

    return Semantics(
      liveRegion: anunciar,
      container: true,
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: fundo,
          borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
          border: Border.all(color: frente.withValues(alpha: 0.35)),
        ),
        child: Padding(
          padding: const EdgeInsets.all(TokensDimensoes.space12),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ExcludeSemantics(child: Icon(icone, color: frente)),
              const SizedBox(width: TokensDimensoes.space8),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      titulo,
                      style: textos.labelLarge?.copyWith(color: frente),
                    ),
                    if (mensagem != null) ...[
                      const SizedBox(height: TokensDimensoes.space4),
                      Text(
                        mensagem!,
                        style: textos.bodyMedium?.copyWith(
                          color: TokensCores.textPrimary,
                        ),
                      ),
                    ],
                    if (rotuloAcao != null) ...[
                      const SizedBox(height: TokensDimensoes.space8),
                      OutlinedButton(
                        onPressed: aoAgir,
                        child: Text(rotuloAcao!),
                      ),
                    ],
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Faixa de modo offline (RNF08), com os mesmos textos do `OfflineBanner` do web. Fica no topo das
/// telas que mostram dados do cache; as ações de escrita da tela ficam desabilitadas.
class AvisoOffline extends StatelessWidget {
  const AvisoOffline({super.key, this.sincronizadoEm});

  /// Momento da última leitura bem-sucedida, guardada no cache; nulo quando não se sabe.
  final DateTime? sincronizadoEm;

  @override
  Widget build(BuildContext context) {
    final quando = sincronizadoEm;
    return Aviso(
      tom: TomAviso.alerta,
      anunciar: true,
      titulo: 'Modo offline: exibindo dados locais',
      mensagem:
          'Você está sem conexão com a rede. '
          '${quando == null ? 'Não foi possível atualizar os dados a partir do servidor.' : 'Última sincronização em ${formatarDataHora(quando)}.'}'
          ' Cadastros e alterações voltam quando a conexão voltar.',
    );
  }
}

/// `dd/MM/aaaa HH:mm`, no horário local do aparelho.
String formatarDataHora(DateTime momento) {
  final local = momento.toLocal();
  String dois(int valor) => valor.toString().padLeft(2, '0');
  return '${dois(local.day)}/${dois(local.month)}/${local.year} ${dois(local.hour)}:${dois(local.minute)}';
}

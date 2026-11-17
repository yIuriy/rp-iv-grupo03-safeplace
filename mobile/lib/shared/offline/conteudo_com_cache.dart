import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../tema/tokens.g.dart';
import '../widgets/aviso.dart';
import '../widgets/estados.dart';
import 'cache_offline.dart';

/// Estados de uma consulta com cache (#316): carregando, falha com "Tentar novamente" e dados.
/// Quando os dados são a cópia local, mostra o [AvisoOffline] acima deles e passa
/// `somenteLeitura: true` para a tela desabilitar cadastros e alterações.
class ConteudoComCache<T> extends StatelessWidget {
  const ConteudoComCache({
    super.key,
    required this.valor,
    required this.construir,
    required this.falha,
    required this.aoTentarDeNovo,
    this.carregando = 'Carregando...',
    this.descreverFalha,
  });

  final AsyncValue<Consultado<T>> valor;
  final Widget Function(T dados, bool somenteLeitura) construir;
  final String falha;
  final VoidCallback aoTentarDeNovo;
  final String carregando;
  final String Function(Object erro)? descreverFalha;

  @override
  Widget build(BuildContext context) {
    return valor.when(
      skipLoadingOnReload: true,
      skipLoadingOnRefresh: true,
      loading: () => EstadoCarregando(texto: carregando),
      error: (erro, _) => Aviso(
        tom: TomAviso.perigo,
        anunciar: true,
        titulo: falha,
        mensagem:
            descreverFalha?.call(erro) ??
            'Verifique sua conexão ou tente novamente em alguns instantes.',
        rotuloAcao: 'Tentar novamente',
        aoAgir: aoTentarDeNovo,
      ),
      data: (consultado) => Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (consultado.somenteLeitura) ...[
            AvisoOffline(sincronizadoEm: consultado.sincronizadoEm),
            const SizedBox(height: TokensDimensoes.space16),
          ],
          construir(consultado.dados, consultado.somenteLeitura),
        ],
      ),
    );
  }
}

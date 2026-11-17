import '../core/sessao/armazenamento_sessao.dart';
import '../shared/offline/cache_offline.dart';

/// Ligações que só o app faz, porque o núcleo não conhece o `shared/` (#316): ao sair ou
/// expirar a sessão, apaga a cópia offline da conta (RNF08).
final sobreposicoesDoApp = [
  limpezaDaContaProvider.overrideWith(
    (ref) =>
        (email) => ref.read(cacheOfflineProvider).limparConta(email),
  ),
];

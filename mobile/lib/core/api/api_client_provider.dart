import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../sessao/controlador_sessao.dart';
import 'api_client.dart';
import 'dio_api_client.dart';

/// Cliente HTTP do app. Nos testes, sobrescreva com um fake de [ApiClient].
final apiClientProvider = Provider<ApiClient>((ref) {
  return DioApiClient(
    tokenAtual: () => ref.read(controladorSessaoProvider).value?.sessao?.token,
    aoExpirar: () => ref.read(controladorSessaoProvider.notifier).expirar(),
  );
});

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/core/sessao/controlador_sessao.dart';
import 'package:safeplace/features/autenticacao/data/autenticacao_api.dart';

import '../apoio.dart';

class _ApiFalsa extends Mock implements AutenticacaoApi {}

void main() {
  late ArmazenamentoSessaoEmMemoria armazenamento;
  late _ApiFalsa api;
  late List<String> contasLimpas;

  ProviderContainer criar() {
    final container = ProviderContainer(
      overrides: [
        armazenamentoSessaoProvider.overrideWithValue(armazenamento),
        autenticacaoApiProvider.overrideWithValue(api),
        limpezaDaContaProvider.overrideWithValue(
          (email) async => contasLimpas.add(email),
        ),
      ],
    );
    addTearDown(container.dispose);
    return container;
  }

  setUp(() {
    armazenamento = ArmazenamentoSessaoEmMemoria();
    api = _ApiFalsa();
    contasLimpas = [];
  });

  test('restaura a sessão guardada ao abrir o app', () async {
    armazenamento.sessao = sessaoGestor;
    final estado = await criar().read(controladorSessaoProvider.future);
    expect(estado.sessao?.email, sessaoGestor.email);
  });

  test('entrar guarda a sessão', () async {
    when(() => api.autenticar('g@s.com', '123'))
        .thenAnswer((_) async => sessaoGestor);
    final container = criar();
    await container.read(controladorSessaoProvider.future);

    await container
        .read(controladorSessaoProvider.notifier)
        .entrar('g@s.com', '123');

    expect(
      container.read(controladorSessaoProvider).value?.sessao,
      sessaoGestor,
    );
    expect(armazenamento.sessao, sessaoGestor);
  });

  test('expirar apaga a sessão, limpa a conta e informa o motivo', () async {
    armazenamento.sessao = sessaoGestor;
    final container = criar();
    await container.read(controladorSessaoProvider.future);

    final notifier = container.read(controladorSessaoProvider.notifier);
    await Future.wait([notifier.expirar(), notifier.expirar()]);

    final estado = container.read(controladorSessaoProvider).value;
    expect(estado?.sessao, isNull);
    expect(estado?.motivoSaida, MotivoSaida.expirada);
    expect(armazenamento.sessao, isNull);
    expect(contasLimpas, [sessaoGestor.email]);
  });

  test('sair informa saída manual', () async {
    armazenamento.sessao = sessaoGestor;
    final container = criar();
    await container.read(controladorSessaoProvider.future);

    await container.read(controladorSessaoProvider.notifier).sair();

    expect(
      container.read(controladorSessaoProvider).value?.motivoSaida,
      MotivoSaida.manual,
    );
  });
}

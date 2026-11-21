// Escolha de um EPI nos formulários dos outros módulos: empréstimos (#320), tarefas, áreas de risco
// e capacitações. Usa o `SeletorComBusca` sobre `GET /api/epis`, com a cópia offline da conta.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/seletor_com_busca.dart';
import '../data/epi.dart';
import '../data/epis_api.dart';
import 'componentes_epi.dart';

/// Campo de escolha de EPI que participa do `Form` como qualquer outro.
///
/// A busca confere nome, CA e localização, ignorando maiúsculas e acentos. Por padrão esconde os
/// EPIs descartados, como o `VincularEpisTarefaDialog` do web. [filtro] troca essa regra, por
/// exemplo para oferecer só EPIs com saldo num empréstimo. O EPI já escolhido em [epiId] continua
/// na lista mesmo fora do filtro, para a edição de um registro antigo não perder o vínculo. EPIs
/// sem `id` nunca aparecem, porque não há como vinculá-los.
///
/// ```dart
/// EpiPicker(
///   epiId: _epiId,                     // seleção inicial, por id (opcional)
///   obrigatorio: true,                 // "Escolha o EPI." no validate() do Form
///   aoMudar: (epi) => setState(() => _epiId = epi?.id),
/// )
/// ```
class EpiPicker extends ConsumerWidget {
  const EpiPicker({
    super.key,
    required this.aoMudar,
    this.epiId,
    this.rotulo = 'EPI',
    this.ajuda,
    this.obrigatorio = false,
    this.validador,
    this.filtro = naoDescartado,
    this.habilitado = true,
  });

  final ValueChanged<Epi?> aoMudar;

  /// EPI já escolhido, como vem nos registros dos outros módulos (`epiId`).
  final int? epiId;
  final String rotulo;
  final String? ajuda;

  /// Sem [validador], recusa a falta de escolha com [mensagemObrigatorio].
  final bool obrigatorio;
  final FormFieldValidator<Epi>? validador;
  final bool Function(Epi) filtro;
  final bool habilitado;

  static const mensagemObrigatorio = 'Escolha o EPI.';

  /// Regra padrão do [filtro].
  static bool naoDescartado(Epi epi) => epi.status != StatusEpi.descartado;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final valor = ref.watch(episProvider);
    final consultado = valor.value;
    final opcoes = [
      for (final epi in consultado?.dados ?? const <Epi>[])
        if (epi.id != null && (filtro(epi) || epi.id == epiId)) epi,
    ];
    Epi? inicial;
    for (final opcao in opcoes) {
      if (opcao.id == epiId) inicial = opcao;
    }
    final falhou = valor.hasError && !valor.hasValue;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SeletorComBusca<Epi>(
          // O FormField só lê o valor inicial ao nascer, então é recriado quando a lista chega ou
          // quando a escolha muda por fora.
          key: ValueKey((valor.hasValue, epiId)),
          rotulo: rotulo,
          rotuloBusca: 'Buscar por nome, CA ou localização',
          opcoes: opcoes,
          valor: inicial,
          descrever: (epi) => epi.nome,
          detalhar: detalharEpi,
          termosDeBusca: (epi) => epi.localizacao ?? '',
          aoMudar: aoMudar,
          validator:
              validador ??
              (obrigatorio
                  ? (escolhido) =>
                        escolhido == null ? mensagemObrigatorio : null
                  : null),
          habilitado: habilitado,
          carregando: valor.isLoading && !valor.hasValue,
          falha: falhou ? 'Não foi possível carregar os EPIs.' : null,
          ajuda: consultado != null && consultado.somenteLeitura
              ? 'Lista salva no aparelho. O saldo pode estar desatualizado.'
              : ajuda,
        ),
        if (falhou)
          Align(
            alignment: Alignment.centerLeft,
            child: Padding(
              padding: const EdgeInsets.only(top: TokensDimensoes.space4),
              child: TextButton(
                onPressed: () => ref.invalidate(episProvider),
                child: const Text('Tentar novamente'),
              ),
            ),
          ),
      ],
    );
  }
}

/// Linha de apoio de cada opção: `CA 12345 · saldo 25 · Disponível`.
String detalharEpi(Epi epi) => [
  if (epi.numeroCa case final ca?) 'CA $ca',
  'saldo ${numeroOuNaoInformado(epi.quantidade)}',
  ?epi.status?.rotulo,
].join(' · ');

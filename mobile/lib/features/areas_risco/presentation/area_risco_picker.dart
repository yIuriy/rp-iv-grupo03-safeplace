// Escolha de uma área de risco nos formulários: ocorrências (#318), tarefas (#322) e o que mais
// vincular uma área. Usa o `SeletorComBusca` sobre `GET /api/areas-risco`, com a cópia offline da
// conta, e mostra o setor de cada área lido de `GET /api/setores`.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/seletor_com_busca.dart';
import '../data/area_risco.dart';
import '../data/areas_risco_api.dart';

/// Campo de escolha de área de risco que participa do `Form` como qualquer outro.
///
/// A busca confere nome, código e setor, ignorando maiúsculas e acentos. A ocorrência não tem
/// campo de setor (decisão DS09, seção 2.3): o setor é o da área, por isso ele aparece na linha de
/// apoio de cada opção. [setorId] limita as opções a um setor. A área já escolhida em [areaId]
/// continua na lista mesmo fora do filtro. Áreas sem `id` nunca aparecem.
///
/// ```dart
/// AreaRiscoPicker(
///   areaId: _areaId,                   // seleção inicial, por id (opcional)
///   obrigatorio: true,                 // "Escolha a área de risco." no validate() do Form
///   aoMudar: (area) => setState(() => _areaId = area?.id),
/// )
/// ```
class AreaRiscoPicker extends ConsumerWidget {
  const AreaRiscoPicker({
    super.key,
    required this.aoMudar,
    this.areaId,
    this.setorId,
    this.rotulo = 'Área de risco',
    this.ajuda,
    this.obrigatorio = false,
    this.validador,
    this.habilitado = true,
  });

  final ValueChanged<AreaRisco?> aoMudar;
  final int? areaId;
  final int? setorId;
  final String rotulo;
  final String? ajuda;

  /// Sem [validador], recusa a falta de escolha com [mensagemObrigatorio].
  final bool obrigatorio;
  final FormFieldValidator<AreaRisco>? validador;
  final bool habilitado;

  static const mensagemObrigatorio = 'Escolha a área de risco.';

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final valor = ref.watch(areasRiscoProvider);
    final setores = {
      for (final setor
          in ref.watch(setoresProvider).value?.dados ?? const <Setor>[])
        if (setor.id != null) setor.id!: setor,
    };
    final consultado = valor.value;
    final opcoes = [
      for (final area in consultado?.dados ?? const <AreaRisco>[])
        if (area.id != null &&
            (setorId == null || area.setorId == setorId || area.id == areaId))
          area,
    ];
    AreaRisco? inicial;
    for (final opcao in opcoes) {
      if (opcao.id == areaId) inicial = opcao;
    }
    final falhou = valor.hasError && !valor.hasValue;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SeletorComBusca<AreaRisco>(
          // O FormField só lê o valor inicial ao nascer, então é recriado quando a lista chega ou
          // quando a escolha muda por fora.
          key: ValueKey((valor.hasValue, areaId, setorId)),
          rotulo: rotulo,
          rotuloBusca: 'Buscar por área, código ou setor',
          opcoes: opcoes,
          valor: inicial,
          descrever: (area) => area.nome,
          detalhar: (area) => detalharArea(area, setores[area.setorId]),
          termosDeBusca: (area) =>
              '${area.codigo ?? ''} ${setores[area.setorId]?.nome ?? ''}',
          aoMudar: aoMudar,
          validator:
              validador ??
              (obrigatorio
                  ? (escolhida) =>
                        escolhida == null ? mensagemObrigatorio : null
                  : null),
          habilitado: habilitado,
          carregando: valor.isLoading && !valor.hasValue,
          falha: falhou ? 'Não foi possível carregar as áreas de risco.' : null,
          ajuda: consultado != null && consultado.somenteLeitura
              ? 'Lista salva no aparelho: pode não ter as áreas mais recentes.'
              : ajuda,
        ),
        if (falhou)
          Align(
            alignment: Alignment.centerLeft,
            child: Padding(
              padding: const EdgeInsets.only(top: TokensDimensoes.space4),
              child: TextButton(
                onPressed: () {
                  ref.invalidate(areasRiscoProvider);
                  ref.invalidate(setoresProvider);
                },
                child: const Text('Tentar novamente'),
              ),
            ),
          ),
      ],
    );
  }
}

/// Linha de apoio de cada opção: `SET-01 · Galpão A · perigo alto`.
String detalharArea(AreaRisco area, Setor? setor) => [
  ?area.codigo,
  ?setor?.nome,
  if (area.nivelPerigo case final nivel?)
    'perigo ${nivel.rotulo.toLowerCase()}',
].join(' · ');

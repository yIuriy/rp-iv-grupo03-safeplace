// Escolha do responsável por uma ação do plano (#321): um colaborador ou um setor, nunca os dois
// (contrato da issue #97). Usado na nova ação e na reatribuição.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/seletor_com_busca.dart';
import '../../areas_risco/data/area_risco.dart';
import '../../areas_risco/data/areas_risco_api.dart';
import '../../usuarios/presentation/colaborador_picker.dart';
import '../data/acao_plano.dart';
import '../domain/validacao_plano.dart';

class ResponsavelCampo extends ConsumerWidget {
  const ResponsavelCampo({
    super.key,
    required this.tipo,
    required this.responsavelId,
    required this.aoMudarTipo,
    required this.aoMudarResponsavel,
    this.rotuloTipo = 'Tipo de responsável',
    this.validarEscolha,
    this.habilitado = true,
  });

  final TipoResponsavel? tipo;
  final int? responsavelId;
  final ValueChanged<TipoResponsavel?> aoMudarTipo;
  final ValueChanged<int?> aoMudarResponsavel;
  final String rotuloTipo;

  /// Regra extra da escolha (na reatribuição: diferente do responsável atual).
  final String? Function(int? id)? validarEscolha;
  final bool habilitado;

  String? _validar(int? id) =>
      validarResponsavel(tipo, id) ?? validarEscolha?.call(id);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        CampoSelecao<TipoResponsavel>(
          rotulo: rotuloTipo,
          valor: tipo,
          dica: 'Colaborador ou setor',
          opcoes: [
            for (final t in TipoResponsavel.values)
              (valor: t, rotulo: t.rotulo),
          ],
          aoMudar: (novo) {
            aoMudarTipo(novo);
            aoMudarResponsavel(null);
          },
          validador: validarTipoDeResponsavel,
          habilitado: habilitado,
        ),
        if (tipo != null) const SizedBox(height: TokensDimensoes.space16),
        if (tipo == TipoResponsavel.colaborador)
          ColaboradorPicker(
            key: const ValueKey('responsavel-colaborador'),
            rotulo: 'Colaborador responsável',
            colaboradorId: responsavelId,
            habilitado: habilitado,
            ajuda: 'Ser responsável não dá acesso ao sistema.',
            validador: (colaborador) => _validar(colaborador?.id),
            aoMudar: (colaborador) => aoMudarResponsavel(colaborador?.id),
          ),
        if (tipo == TipoResponsavel.setor) _SetorCampo(this),
      ],
    );
  }
}

class _SetorCampo extends ConsumerWidget {
  const _SetorCampo(this.campo);

  final ResponsavelCampo campo;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final valor = ref.watch(setoresProvider);
    final setores = [
      for (final setor in valor.value?.dados ?? const <Setor>[])
        if (setor.id != null) setor,
    ];
    Setor? inicial;
    for (final setor in setores) {
      if (setor.id == campo.responsavelId) inicial = setor;
    }
    return SeletorComBusca<Setor>(
      key: ValueKey('responsavel-setor-${setores.length}'),
      rotulo: 'Setor responsável',
      opcoes: setores,
      valor: inicial,
      descrever: (setor) => setor.nome,
      detalhar: (setor) => setor.localizacao ?? '',
      habilitado: campo.habilitado,
      carregando: valor.isLoading && !valor.hasValue,
      falha: valor.hasError && !valor.hasValue
          ? 'Não foi possível carregar os setores.'
          : null,
      validator: (setor) => campo._validar(setor?.id),
      aoMudar: (setor) => campo.aoMudarResponsavel(setor?.id),
    );
  }
}

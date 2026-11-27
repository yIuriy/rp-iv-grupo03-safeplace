// Critérios da consulta de ocorrências (issues #128 e #195), enviados ao servidor: período da data
// do fato, setor da área de risco, colaborador envolvido, tipo e gravidade. Período invertido e data
// inexistente são recusados antes do envio.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/campos.dart';
import '../../areas_risco/data/area_risco.dart';
import '../../areas_risco/data/areas_risco_api.dart';
import '../../usuarios/presentation/colaborador_picker.dart';
import '../data/ocorrencia.dart';
import '../domain/ocorrencia_repository.dart';
import '../domain/validacao_relato.dart';

/// Formulário dos critérios. [aoAplicar] recebe os critérios lidos, ou [FiltroOcorrencias.nenhum]
/// ao limpar.
class FiltrosOcorrencias extends ConsumerStatefulWidget {
  const FiltrosOcorrencias({super.key, required this.aoAplicar});

  final ValueChanged<FiltroOcorrencias> aoAplicar;

  @override
  ConsumerState<FiltrosOcorrencias> createState() => _FiltrosOcorrenciasState();
}

class _FiltrosOcorrenciasState extends ConsumerState<FiltrosOcorrencias> {
  final _form = GlobalKey<FormState>();
  final _inicio = TextEditingController();
  final _fim = TextEditingController();
  int? _setorId;
  int? _colaboradorId;
  TipoOcorrencia? _tipo;
  NivelPerigo? _gravidade;

  /// Muda a cada limpeza para recriar as seleções, que só leem o valor inicial ao nascer.
  int _versao = 0;

  @override
  void dispose() {
    _inicio.dispose();
    _fim.dispose();
    super.dispose();
  }

  void _aplicar() {
    if (!_form.currentState!.validate()) return;
    widget.aoAplicar(
      FiltroOcorrencias(
        dataInicio: lerData(_inicio.text),
        dataFim: lerData(_fim.text),
        setorId: _setorId,
        colaboradorId: _colaboradorId,
        tipo: _tipo,
        gravidade: _gravidade,
      ),
    );
  }

  void _limpar() {
    _inicio.clear();
    _fim.clear();
    setState(() {
      _setorId = null;
      _colaboradorId = null;
      _tipo = null;
      _gravidade = null;
      _versao++;
    });
    widget.aoAplicar(FiltroOcorrencias.nenhum);
  }

  @override
  Widget build(BuildContext context) {
    const espaco = SizedBox(height: TokensDimensoes.space16);
    final setores = ref.watch(setoresProvider).value?.dados ?? const <Setor>[];
    return Form(
      key: _form,
      child: Column(
        key: ValueKey(_versao),
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CampoTexto(
            rotulo: 'Data inicial',
            controlador: _inicio,
            dica: 'dd/mm/aaaa',
            teclado: TextInputType.number,
            formatadores: const [MascaraData()],
            validador: validarDataDoFiltro,
          ),
          espaco,
          CampoTexto(
            rotulo: 'Data final',
            controlador: _fim,
            dica: 'dd/mm/aaaa',
            teclado: TextInputType.number,
            formatadores: const [MascaraData()],
            validador: (valor) =>
                validarDataDoFiltro(valor) ??
                validarPeriodo(lerData(_inicio.text), lerData(valor ?? '')),
          ),
          espaco,
          CampoSelecao<int>(
            rotulo: 'Setor',
            valor: _setorId,
            dica: 'Todos os setores',
            opcoes: [
              for (final setor in setores)
                if (setor.id case final id?)
                  (valor: id, rotulo: setor.descricaoCurta),
            ],
            aoMudar: (id) => setState(() => _setorId = id),
          ),
          espaco,
          ColaboradorPicker(
            rotulo: 'Colaborador envolvido',
            colaboradorId: _colaboradorId,
            somenteAtivos: false,
            aoMudar: (colaborador) =>
                setState(() => _colaboradorId = colaborador?.id),
          ),
          espaco,
          CampoSelecao<TipoOcorrencia>(
            rotulo: 'Tipo',
            valor: _tipo,
            dica: 'Acidentes e incidentes',
            opcoes: [
              for (final tipo in TipoOcorrencia.values)
                (valor: tipo, rotulo: tipo.rotulo),
            ],
            aoMudar: (tipo) => setState(() => _tipo = tipo),
          ),
          espaco,
          CampoSelecao<NivelPerigo>(
            rotulo: 'Gravidade',
            valor: _gravidade,
            dica: 'Qualquer gravidade',
            ajuda: 'Só acidentes têm gravidade.',
            opcoes: [
              for (final nivel in NivelPerigo.values)
                (valor: nivel, rotulo: nivel.rotulo),
            ],
            aoMudar: (nivel) => setState(() => _gravidade = nivel),
          ),
          const SizedBox(height: TokensDimensoes.space24),
          FilledButton.icon(
            onPressed: _aplicar,
            icon: const Icon(Icons.filter_list),
            label: const Text('Aplicar filtros'),
          ),
          const SizedBox(height: TokensDimensoes.space8),
          TextButton(onPressed: _limpar, child: const Text('Limpar filtros')),
        ],
      ),
    );
  }
}

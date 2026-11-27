// Formulário de relato de ocorrência (UC05, UC09, #318), para Gestor e Supervisor: tipo, data e hora
// do fato, colaborador envolvido, área de risco e descrição, mais os campos de acidente (causa,
// gravidade, lesão, dano e destino) ou de incidente (situação de risco e potencial de dano). Data
// futura e descrição vazia são recusadas antes do envio. Quando o servidor recusa, a mensagem dele
// aparece e os dados ficam. Fechar com dados digitados pede confirmação (#335).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/formularios/hora.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/guarda_de_descarte.dart';
import '../../areas_risco/data/area_risco.dart';
import '../../areas_risco/presentation/area_risco_picker.dart';
import '../../usuarios/presentation/colaborador_picker.dart';
import '../data/ocorrencia.dart';
import '../data/ocorrencias_api.dart';
import '../domain/ocorrencia_repository.dart';
import '../domain/validacao_relato.dart';

/// Abre o formulário em tela cheia. Devolve a ocorrência registrada, ou `null` se a pessoa desistiu.
Future<Ocorrencia?> abrirFormularioOcorrencia(BuildContext context) {
  return Navigator.of(context, rootNavigator: true).push<Ocorrencia>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => const FormularioOcorrenciaPage(),
    ),
  );
}

/// Aviso depois do registro. O acidente mostra o protocolo CAT gerado pelo servidor.
String mensagemDeRegistro(Ocorrencia ocorrencia) {
  final protocolo = ocorrencia.protocoloCat;
  if (ocorrencia.ehAcidente && protocolo != null) {
    return 'Acidente registrado. Protocolo CAT $protocolo.';
  }
  return '${ocorrencia.tipo.rotulo} registrado.';
}

String? _opcional(TextEditingController campo) {
  final texto = campo.text.trim();
  return texto.isEmpty ? null : texto;
}

class FormularioOcorrenciaPage extends ConsumerStatefulWidget {
  const FormularioOcorrenciaPage({super.key});

  @override
  ConsumerState<FormularioOcorrenciaPage> createState() =>
      _FormularioOcorrenciaPageState();
}

class _FormularioOcorrenciaPageState
    extends ConsumerState<FormularioOcorrenciaPage> {
  final _form = GlobalKey<FormState>();
  final _data = TextEditingController();
  final _hora = TextEditingController();
  final _descricao = TextEditingController();
  final _tipoLesao = TextEditingController();
  final _dano = TextEditingController();
  final _destino = TextEditingController();
  final _situacaoRisco = TextEditingController();
  final _potencialDano = TextEditingController();
  TipoOcorrencia? _tipo;
  int? _colaboradorId;
  int? _areaId;
  CausaRaiz? _causa;
  NivelPerigo? _gravidade;
  bool _enviando = false;
  bool _alterado = false;
  String? _falha;

  @override
  void dispose() {
    for (final campo in [
      _data,
      _hora,
      _descricao,
      _tipoLesao,
      _dano,
      _destino,
      _situacaoRisco,
      _potencialDano,
    ]) {
      campo.dispose();
    }
    super.dispose();
  }

  DadosOcorrencia _dados() {
    final acidente = _tipo == TipoOcorrencia.acidente;
    return DadosOcorrencia(
      tipo: _tipo!,
      dataOcorrencia: montarDataHoraDoFato(_data.text, _hora.text),
      descricao: _descricao.text.trim(),
      areaId: _areaId!,
      colaboradorId: _colaboradorId!,
      causaRaiz: acidente ? _causa : null,
      gravidade: acidente ? _gravidade : null,
      tipoLesao: acidente ? _opcional(_tipoLesao) : null,
      dano: acidente ? _opcional(_dano) : null,
      destino: acidente ? _opcional(_destino) : null,
      situacaoRisco: acidente ? null : _opcional(_situacaoRisco),
      potencialDano: acidente ? null : _opcional(_potencialDano),
    );
  }

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    setState(() => _enviando = true);
    try {
      final ocorrencia = await ref
          .read(ocorrenciaRepositoryProvider)
          .registrar(_dados());
      if (!mounted) return;
      ref.invalidate(ocorrenciasProvider);
      Navigator.of(context).pop<Ocorrencia>(ocorrencia);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeOcorrencia(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    const espaco = SizedBox(height: TokensDimensoes.space16);
    return GuardaDeDescarte(
      alterado: _alterado,
      enviando: _enviando,
      child: Scaffold(
        appBar: AppBar(title: const Text('Registrar ocorrência')),
        body: SafeArea(
          child: Form(
            key: _form,
            onChanged: () {
              if (!_alterado) setState(() => _alterado = true);
            },
            child: ListView(
              padding: const EdgeInsets.all(
                TokensDimensoes.layoutMobilePadding,
              ),
              children: [
                Text(
                  'Descreva o que aconteceu, onde e com quem. Não é preciso investigar a causa raiz agora.',
                  style: Theme.of(context).textTheme.bodyMedium
                      ?.copyWith(color: context.cores.textoSecundario),
                ),
                const SizedBox(height: TokensDimensoes.space24),
                CampoSelecao<TipoOcorrencia>(
                  rotulo: 'Tipo de ocorrência',
                  valor: _tipo,
                  dica: 'Selecione o tipo',
                  ajuda: 'Acidente teve lesão ou dano. Incidente foi um quase acidente ou uma situação de risco.',
                  opcoes: [
                    for (final tipo in TipoOcorrencia.values)
                      (valor: tipo, rotulo: tipo.rotulo),
                  ],
                  aoMudar: (tipo) => setState(() => _tipo = tipo),
                  validador: validarTipoOcorrencia,
                  habilitado: !_enviando,
                ),
                espaco,
                CampoTexto(
                  rotulo: 'Data do fato',
                  controlador: _data,
                  dica: 'dd/mm/aaaa',
                  teclado: TextInputType.number,
                  formatadores: const [MascaraData()],
                  validador: (valor) => validarDataDoFato(valor),
                ),
                espaco,
                CampoTexto(
                  rotulo: 'Hora do fato',
                  controlador: _hora,
                  dica: 'hh:mm',
                  teclado: TextInputType.number,
                  formatadores: const [MascaraHora()],
                  validador: (valor) =>
                      validarHoraDoFato(valor, data: _data.text),
                ),
                espaco,
                ColaboradorPicker(
                  rotulo: 'Colaborador envolvido',
                  colaboradorId: _colaboradorId,
                  obrigatorio: true,
                  habilitado: !_enviando,
                  aoMudar: (colaborador) =>
                      setState(() => _colaboradorId = colaborador?.id),
                ),
                espaco,
                AreaRiscoPicker(
                  areaId: _areaId,
                  obrigatorio: true,
                  habilitado: !_enviando,
                  ajuda: 'O setor do relato é o da área escolhida.',
                  aoMudar: (area) => setState(() => _areaId = area?.id),
                ),
                espaco,
                CampoTexto(
                  rotulo: 'Descrição dos fatos',
                  controlador: _descricao,
                  linhas: 4,
                  ajuda: 'O que aconteceu, onde e como.',
                  validador: validarDescricaoDoFato,
                ),
                if (_tipo == TipoOcorrencia.acidente) ..._camposDoAcidente(),
                if (_tipo == TipoOcorrencia.incidente) ..._camposDoIncidente(),
                if (_falha != null) ...[
                  espaco,
                  Aviso(
                    tom: TomAviso.perigo,
                    anunciar: true,
                    titulo: 'Relato não registrado',
                    mensagem: _falha,
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space24),
                FilledButton.icon(
                  onPressed: _enviando ? null : _enviar,
                  icon: _enviando
                      ? const SizedBox.square(
                          dimension: TokensDimensoes.sizeIcon,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.check),
                  label: const Text('Registrar ocorrência'),
                ),
                const SizedBox(height: TokensDimensoes.space8),
                TextButton(
                  onPressed: _enviando
                      ? null
                      : () => Navigator.of(context).maybePop(),
                  child: const Text('Cancelar'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _titulo(String texto) => Padding(
    padding: const EdgeInsets.only(
      top: TokensDimensoes.space24,
      bottom: TokensDimensoes.space8,
    ),
    child: Semantics(
      header: true,
      child: Text(texto, style: Theme.of(context).textTheme.titleMedium),
    ),
  );

  List<Widget> _camposDoAcidente() => [
    _titulo('Dados do acidente'),
    CampoSelecao<CausaRaiz>(
      rotulo: 'Causa',
      valor: _causa,
      dica: 'Selecione a causa',
      opcoes: [
        for (final causa in CausaRaiz.values)
          (valor: causa, rotulo: causa.rotulo),
      ],
      aoMudar: (causa) => setState(() => _causa = causa),
      validador: validarCausaRaiz,
      habilitado: !_enviando,
    ),
    const SizedBox(height: TokensDimensoes.space16),
    CampoSelecao<NivelPerigo>(
      rotulo: 'Gravidade',
      valor: _gravidade,
      dica: 'Selecione a gravidade',
      ajuda: 'Alta ou crítica exige ação preventiva no plano de ação.',
      opcoes: [
        for (final nivel in NivelPerigo.values)
          (valor: nivel, rotulo: nivel.rotulo),
      ],
      aoMudar: (nivel) => setState(() => _gravidade = nivel),
      validador: validarGravidade,
      habilitado: !_enviando,
    ),
    const SizedBox(height: TokensDimensoes.space16),
    CampoTexto(
      rotulo: 'Tipo de lesão (opcional)',
      controlador: _tipoLesao,
      dica: 'Ex.: corte, queimadura',
    ),
    const SizedBox(height: TokensDimensoes.space16),
    CampoTexto(
      rotulo: 'Dano (opcional)',
      controlador: _dano,
      dica: 'Ex.: lesão na mão direita',
    ),
    const SizedBox(height: TokensDimensoes.space16),
    CampoTexto(
      rotulo: 'Destino do acidentado (opcional)',
      controlador: _destino,
      dica: 'Ex.: ambulatório, hospital',
    ),
  ];

  List<Widget> _camposDoIncidente() => [
    _titulo('Dados do incidente'),
    CampoTexto(
      rotulo: 'Situação de risco',
      controlador: _situacaoRisco,
      dica: 'Ex.: piso escorregadio',
      validador: validarSituacaoRisco,
    ),
    const SizedBox(height: TokensDimensoes.space16),
    CampoTexto(
      rotulo: 'Potencial de dano',
      controlador: _potencialDano,
      dica: 'Ex.: queda com fratura',
      validador: validarPotencialDano,
    ),
  ];
}

// Formulário em etapas de novo empréstimo de EPI (RF11, UC12, US11).
// Gestor de Segurança e Supervisor registram entregas.
// Trata o erro 409 de duplicidade de posse com tela de confirmação e justificativa obrigatória.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../epis/data/epi.dart';
import '../../epis/presentation/epi_picker.dart';
import '../../usuarios/presentation/colaborador_picker.dart';
import '../data/emprestimo.dart';
import '../data/emprestimos_api.dart';
import '../domain/emprestimo_repository.dart';
import '../domain/validacao_emprestimo.dart';
import 'entrega_etapas.dart';

/// Abre a tela cheia de novo empréstimo. Devolve o [Emprestimo] concluído ou `null` se desistiu.
Future<Emprestimo?> abrirFormularioEntrega(BuildContext context) {
  return Navigator.of(context, rootNavigator: true).push<Emprestimo>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => const FormularioEntregaPage(),
    ),
  );
}

enum EtapaEntrega { formulario, duplicidade, concluida }

class FormularioEntregaPage extends ConsumerStatefulWidget {
  const FormularioEntregaPage({super.key});

  @override
  ConsumerState<FormularioEntregaPage> createState() =>
      _FormularioEntregaPageState();
}

class _FormularioEntregaPageState extends ConsumerState<FormularioEntregaPage> {
  final _formKey = GlobalKey<FormState>();
  final _formDuplicidadeKey = GlobalKey<FormState>();

  final _dataDevolucao = TextEditingController();
  final _justificativa = TextEditingController();

  int? _colaboradorId;
  int? _epiId;

  EtapaEntrega _etapa = EtapaEntrega.formulario;
  String _duplicidadeMensagem = '';
  Emprestimo? _emprestimoConcluido;

  String? _falha;
  bool _enviando = false;

  @override
  void dispose() {
    _dataDevolucao.dispose();
    _justificativa.dispose();
    super.dispose();
  }

  Future<void> _enviar({
    bool confirmarSubstituicao = false,
    String? justificativaSubstituicao,
  }) async {
    final colabId = _colaboradorId;
    final epiId = _epiId;
    final data = lerData(_dataDevolucao.text);

    if (colabId == null || epiId == null || data == null) return;

    setState(() {
      _falha = null;
      _enviando = true;
    });

    try {
      final repo = ref.read(emprestimoRepositoryProvider);
      final dados = DadosEntrega(
        colaboradorId: colabId,
        epiId: epiId,
        dataDevolucaoPrevista: data,
        confirmarSubstituicao: confirmarSubstituicao,
        justificativaSubstituicao: justificativaSubstituicao,
      );

      final resultado = await repo.registrarEntrega(dados);

      // Invalida consultas para atualizar listagens
      ref.invalidate(emprestimosProvider);
      ref.invalidate(episDisponiveisProvider);

      if (!mounted) return;
      setState(() {
        _emprestimoConcluido = resultado;
        _etapa = EtapaEntrega.concluida;
        _enviando = false;
      });
    } catch (erro) {
      if (!mounted) return;
      final duplicidade =
          confirmarSubstituicao ? null : duplicidadeDaFalha(erro);

      setState(() {
        _enviando = false;
        if (duplicidade != null) {
          _duplicidadeMensagem = duplicidade;
          _etapa = EtapaEntrega.duplicidade;
        } else {
          _falha = descreverFalhaDeEntrega(erro);
        }
      });
    }
  }

  void _submeterFormularioInicial() {
    if (_enviando) return;
    if (_formKey.currentState?.validate() ?? false) {
      _enviar();
    }
  }

  void _confirmarDuplicidade(String justificativa) {
    _enviar(
      confirmarSubstituicao: true,
      justificativaSubstituicao: justificativa,
    );
  }

  void _voltarDaDuplicidade() {
    setState(() {
      _falha = null;
      _etapa = EtapaEntrega.formulario;
    });
  }

  @override
  Widget build(BuildContext context) {
    final titulo = switch (_etapa) {
      EtapaEntrega.formulario => 'Novo empréstimo',
      EtapaEntrega.duplicidade => 'Confirmar substituição',
      EtapaEntrega.concluida => 'Entrega concluída',
    };

    return Scaffold(
      appBar: AppBar(
        title: Text(titulo),
        leading: _etapa == EtapaEntrega.concluida
            ? const SizedBox.shrink()
            : CloseButton(onPressed: _enviando ? null : () => Navigator.of(context).pop()),
      ),
      body: switch (_etapa) {
        EtapaEntrega.duplicidade => DuplicidadeConfirmacaoWidget(
            mensagem: _duplicidadeMensagem,
            falha: _falha,
            enviando: _enviando,
            controladorJustificativa: _justificativa,
            formKey: _formDuplicidadeKey,
            aoVoltar: _voltarDaDuplicidade,
            aoConfirmar: _confirmarDuplicidade,
          ),
        EtapaEntrega.concluida => EntregaConcluidaWidget(
            emprestimo: _emprestimoConcluido!,
            aoFechar: () => Navigator.of(context).pop(_emprestimoConcluido),
          ),
        EtapaEntrega.formulario => Form(
            key: _formKey,
            child: ListView(
              padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
              children: [
                ColaboradorPicker(
                  colaboradorId: _colaboradorId,
                  obrigatorio: true,
                  habilitado: !_enviando,
                  somenteAtivos: true,
                  aoMudar: (c) => setState(() => _colaboradorId = c?.id),
                ),
                const SizedBox(height: TokensDimensoes.space16),
                EpiPicker(
                  epiId: _epiId,
                  obrigatorio: true,
                  habilitado: !_enviando,
                  filtro: (epi) =>
                      (epi.quantidade ?? 0) > 0 &&
                      epi.status != StatusEpi.descartado,
                  aoMudar: (e) => setState(() => _epiId = e?.id),
                ),
                const SizedBox(height: TokensDimensoes.space16),
                CampoTexto(
                  rotulo: 'Data prevista de devolução',
                  controlador: _dataDevolucao,
                  dica: 'dd/mm/aaaa',
                  ajuda: 'No formato dd/mm/aaaa.',
                  teclado: TextInputType.datetime,
                  formatadores: const [MascaraData()],
                  habilitado: !_enviando,
                  validador: (v) => validarDataDevolucaoTexto(v),
                ),
                if (_falha != null) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  Aviso(
                    titulo: 'Entrega não registrada',
                    mensagem: _falha,
                    tom: TomAviso.perigo,
                    anunciar: true,
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space24),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: _enviando ? null : () => Navigator.of(context).pop(),
                        child: const Text('Cancelar'),
                      ),
                    ),
                    const SizedBox(width: TokensDimensoes.space12),
                    Expanded(
                      child: FilledButton.icon(
                        onPressed: _enviando ? null : _submeterFormularioInicial,
                        icon: _enviando
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child: CircularProgressIndicator(strokeWidth: 2),
                              )
                            : const Icon(Icons.check),
                        label: const Text('Registrar entrega'),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
      },
    );
  }
}

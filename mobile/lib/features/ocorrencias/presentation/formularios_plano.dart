// Formulários do plano de ação (UC07, #321), só do Gestor: nova ação (tela cheia) e, numa ação
// aberta, prorrogar prazo, reatribuir responsável e configurar o alerta (folhas que sobem de baixo).
// A validação local é a do web (`domain/validacao_plano.dart`); quando o servidor recusa, a mensagem
// dele aparece no próprio formulário e os dados ficam. Cada formulário devolve a mensagem de sucesso.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/guarda_de_descarte.dart';
import '../data/acao_plano.dart';
import '../data/ocorrencia.dart';
import '../data/planos_api.dart';
import '../domain/plano_repository.dart';
import '../domain/validacao_plano.dart';
import 'responsavel_campo.dart';

String? _opcional(TextEditingController campo) {
  final texto = campo.text.trim();
  return texto.isEmpty ? null : texto;
}

/// Acidente com gravidade alta ou crítica exige ação preventiva (decisão DS09, seção 2.7).
bool ocorrenciaGrave(Ocorrencia ocorrencia) =>
    ocorrencia.ehAcidente &&
    (ocorrencia.gravidade == 'ALTO' || ocorrencia.gravidade == 'CRITICO');

// ---------------------------------------------------------------------------------------------
// Nova ação
// ---------------------------------------------------------------------------------------------

/// Abre a nova ação em tela cheia. Devolve a mensagem de sucesso, ou `null` se a pessoa desistiu.
Future<String?> abrirNovaAcao(BuildContext context, Ocorrencia ocorrencia) {
  return Navigator.of(context, rootNavigator: true).push<String>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => NovaAcaoPage(ocorrencia: ocorrencia),
    ),
  );
}

class NovaAcaoPage extends ConsumerStatefulWidget {
  const NovaAcaoPage({super.key, required this.ocorrencia});

  final Ocorrencia ocorrencia;

  @override
  ConsumerState<NovaAcaoPage> createState() => _NovaAcaoPageState();
}

class _NovaAcaoPageState extends ConsumerState<NovaAcaoPage> {
  final _form = GlobalKey<FormState>();
  final _corretiva = TextEditingController();
  final _preventiva = TextEditingController();
  final _prazo = TextEditingController();
  final _antecedencia = TextEditingController(text: '3');
  TipoResponsavel? _tipo;
  int? _responsavelId;
  bool _alerta = false;
  bool _enviando = false;
  bool _alterado = false;
  String? _falha;

  @override
  void dispose() {
    for (final campo in [_corretiva, _preventiva, _prazo, _antecedencia]) {
      campo.dispose();
    }
    super.dispose();
  }

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    final id = widget.ocorrencia.id!;
    setState(() => _enviando = true);
    try {
      final acao = await ref
          .read(planoRepositoryProvider)
          .cadastrar(
            id,
            NovaAcao(
              medidasCorretivas: _opcional(_corretiva),
              medidasPreventivas: _opcional(_preventiva),
              prazo: lerData(_prazo.text)!,
              responsavelTipo: _tipo!,
              responsavelId: _responsavelId!,
              alertaAtivo: _alerta,
              antecedenciaAlertaDias: _alerta
                  ? int.parse(_antecedencia.text.trim())
                  : null,
            ),
          );
      if (!mounted) return;
      ref.invalidate(acoesDoPlanoProvider(id));
      Navigator.of(context).pop('Ação #${acao.id} cadastrada no plano.');
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDoPlano(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    const espaco = SizedBox(height: TokensDimensoes.space16);
    final minimo = prazoMinimo(dataDoFato: widget.ocorrencia.dataOcorrencia);
    final grave = ocorrenciaGrave(widget.ocorrencia);
    return GuardaDeDescarte(
      alterado: _alterado,
      enviando: _enviando,
      child: Scaffold(
        appBar: AppBar(title: const Text('Nova ação do plano')),
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
                  'Informe o que será feito, até quando e por quem. É preciso ao menos uma medida.',
                  style: Theme.of(context).textTheme.bodyMedium
                      ?.copyWith(color: context.cores.textoSecundario),
                ),
                if (grave) ...[
                  espaco,
                  const Aviso(
                    tom: TomAviso.alerta,
                    titulo: 'Acidente grave',
                    mensagem: 'Gravidade alta ou crítica exige uma ação preventiva no plano.',
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space24),
                CampoTexto(
                  rotulo: 'Medida corretiva',
                  controlador: _corretiva,
                  linhas: 3,
                  ajuda: 'O que corrige a causa do que aconteceu.',
                  validador: (_) =>
                      validarMedidas(_corretiva.text, _preventiva.text),
                ),
                espaco,
                CampoTexto(
                  rotulo: 'Medida preventiva',
                  controlador: _preventiva,
                  linhas: 3,
                  ajuda: 'O que evita que volte a acontecer.',
                ),
                espaco,
                CampoTexto(
                  rotulo: 'Prazo',
                  controlador: _prazo,
                  dica: 'dd/mm/aaaa',
                  teclado: TextInputType.number,
                  formatadores: const [MascaraData()],
                  ajuda:
                      'A partir de ${formatarData(minimo)} (não antes de hoje nem do dia do fato).',
                  validador: (valor) => validarPrazoDaAcao(valor, minimo),
                ),
                espaco,
                ResponsavelCampo(
                  tipo: _tipo,
                  responsavelId: _responsavelId,
                  habilitado: !_enviando,
                  aoMudarTipo: (tipo) => setState(() => _tipo = tipo),
                  aoMudarResponsavel: (id) =>
                      setState(() => _responsavelId = id),
                ),
                espaco,
                _CamposDoAlerta(
                  ativo: _alerta,
                  antecedencia: _antecedencia,
                  aoMudar: (ativo) => setState(() => _alerta = ativo),
                ),
                if (_falha != null) ...[
                  espaco,
                  Aviso(
                    tom: TomAviso.perigo,
                    anunciar: true,
                    titulo: 'Ação não cadastrada',
                    mensagem: _falha,
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space24),
                _BotaoEnviar(
                  rotulo: 'Cadastrar ação',
                  enviando: _enviando,
                  aoTocar: _enviar,
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
}

// ---------------------------------------------------------------------------------------------
// Operações numa ação aberta: prorrogar, reatribuir e alerta
// ---------------------------------------------------------------------------------------------

Future<String?> _abrirFolha(BuildContext context, Widget formulario) {
  return showModalBottomSheet<String>(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    showDragHandle: true,
    builder: (_) => Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.viewInsetsOf(context).bottom),
      child: formulario,
    ),
  );
}

Future<String?> abrirProrrogacao(BuildContext context, AcaoPlano acao) =>
    _abrirFolha(context, _ProrrogacaoForm(acao: acao));

Future<String?> abrirReatribuicao(BuildContext context, AcaoPlano acao) =>
    _abrirFolha(context, _ReatribuicaoForm(acao: acao));

Future<String?> abrirAlerta(BuildContext context, AcaoPlano acao) =>
    _abrirFolha(context, _AlertaForm(acao: acao));

/// Estrutura comum das folhas: título, campos, falha do servidor e botões. [enviar] devolve a
/// mensagem de sucesso; uma exceção vira o aviso de falha com a mensagem do backend.
class _Folha extends ConsumerStatefulWidget {
  const _Folha({
    required this.titulo,
    required this.rotuloEnviar,
    required this.campos,
    required this.enviar,
  });

  final String titulo;
  final String rotuloEnviar;
  final List<Widget> Function(bool enviando) campos;
  final Future<String> Function(WidgetRef ref) enviar;

  @override
  ConsumerState<_Folha> createState() => _FolhaState();
}

class _FolhaState extends ConsumerState<_Folha> {
  final _form = GlobalKey<FormState>();
  bool _enviando = false;
  String? _falha;

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    setState(() => _enviando = true);
    try {
      final mensagem = await widget.enviar(ref);
      if (mounted) Navigator.of(context).pop(mensagem);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDoPlano(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _form,
      child: ListView(
        shrinkWrap: true,
        padding: const EdgeInsets.fromLTRB(
          TokensDimensoes.layoutMobilePadding,
          0,
          TokensDimensoes.layoutMobilePadding,
          TokensDimensoes.space24,
        ),
        children: [
          Semantics(
            header: true,
            child: Text(
              widget.titulo,
              style: Theme.of(context).textTheme.titleLarge,
            ),
          ),
          const SizedBox(height: TokensDimensoes.space16),
          ...widget.campos(_enviando),
          if (_falha != null) ...[
            const SizedBox(height: TokensDimensoes.space16),
            Aviso(
              tom: TomAviso.perigo,
              anunciar: true,
              titulo: 'Alteração não salva',
              mensagem: _falha,
            ),
          ],
          const SizedBox(height: TokensDimensoes.space24),
          _BotaoEnviar(
            rotulo: widget.rotuloEnviar,
            enviando: _enviando,
            aoTocar: _enviar,
          ),
          const SizedBox(height: TokensDimensoes.space8),
          TextButton(
            onPressed: _enviando ? null : () => Navigator.of(context).pop(),
            child: const Text('Cancelar'),
          ),
        ],
      ),
    );
  }
}

/// Recarrega o plano da ocorrência e o histórico da ação depois de uma alteração.
void recarregarPlano(WidgetRef ref, AcaoPlano acao) {
  if (acao.idOcorrencia case final id?) {
    ref.invalidate(acoesDoPlanoProvider(id));
  }
  ref.invalidate(prorrogacoesProvider(acao.id));
}

class _ProrrogacaoForm extends StatefulWidget {
  const _ProrrogacaoForm({required this.acao});

  final AcaoPlano acao;

  @override
  State<_ProrrogacaoForm> createState() => _ProrrogacaoFormState();
}

class _ProrrogacaoFormState extends State<_ProrrogacaoForm> {
  final _novoPrazo = TextEditingController();
  final _justificativa = TextEditingController();

  @override
  void dispose() {
    _novoPrazo.dispose();
    _justificativa.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final acao = widget.acao;
    return _Folha(
      titulo: 'Prorrogar prazo da ação #${acao.id}',
      rotuloEnviar: 'Prorrogar',
      campos: (_) => [
        Text('Prazo atual: ${formatarData(acao.prazo)}.'),
        const SizedBox(height: TokensDimensoes.space16),
        CampoTexto(
          rotulo: 'Novo prazo',
          controlador: _novoPrazo,
          dica: 'dd/mm/aaaa',
          teclado: TextInputType.number,
          formatadores: const [MascaraData()],
          validador: (valor) => validarNovoPrazo(valor, acao.prazo),
        ),
        const SizedBox(height: TokensDimensoes.space16),
        CampoTexto(
          rotulo: 'Justificativa técnica',
          controlador: _justificativa,
          linhas: 3,
          ajuda: 'Fica registrada no histórico da ação.',
          validador: validarJustificativa,
        ),
      ],
      enviar: (ref) async {
        final novo = lerData(_novoPrazo.text)!;
        await ref
            .read(planoRepositoryProvider)
            .prorrogar(acao.id, novo, _justificativa.text);
        recarregarPlano(ref, acao);
        return 'Prazo da ação #${acao.id} prorrogado para ${formatarData(novo)}.';
      },
    );
  }
}

class _ReatribuicaoForm extends StatefulWidget {
  const _ReatribuicaoForm({required this.acao});

  final AcaoPlano acao;

  @override
  State<_ReatribuicaoForm> createState() => _ReatribuicaoFormState();
}

class _ReatribuicaoFormState extends State<_ReatribuicaoForm> {
  late TipoResponsavel? _tipo = widget.acao.responsavelTipo;
  int? _responsavelId;

  @override
  Widget build(BuildContext context) {
    final acao = widget.acao;
    return _Folha(
      titulo: 'Reatribuir a ação #${acao.id}',
      rotuloEnviar: 'Reatribuir',
      campos: (enviando) => [
        Text('Responsável atual: ${descreverResponsavel(acao)}.'),
        const SizedBox(height: TokensDimensoes.space16),
        ResponsavelCampo(
          tipo: _tipo,
          responsavelId: _responsavelId,
          rotuloTipo: 'Tipo do novo responsável',
          habilitado: !enviando,
          validarEscolha: (id) =>
              _tipo == null ? null : validarNovoResponsavel(acao, _tipo!, id),
          aoMudarTipo: (tipo) => setState(() => _tipo = tipo),
          aoMudarResponsavel: (id) => setState(() => _responsavelId = id),
        ),
      ],
      enviar: (ref) async {
        final atualizada = await ref
            .read(planoRepositoryProvider)
            .reatribuir(acao.id, _tipo!, _responsavelId!);
        recarregarPlano(ref, acao);
        return 'Ação #${acao.id} reatribuída a ${atualizada.responsavelNome ?? 'novo responsável'}. Avise o novo encarregado.';
      },
    );
  }
}

class _AlertaForm extends StatefulWidget {
  const _AlertaForm({required this.acao});

  final AcaoPlano acao;

  @override
  State<_AlertaForm> createState() => _AlertaFormState();
}

class _AlertaFormState extends State<_AlertaForm> {
  late bool _ativo = widget.acao.alertaAtivo;
  late final _antecedencia = TextEditingController(
    text: '${widget.acao.antecedenciaAlertaDias ?? 3}',
  );

  @override
  void dispose() {
    _antecedencia.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final acao = widget.acao;
    return _Folha(
      titulo: 'Alerta de prazo da ação #${acao.id}',
      rotuloEnviar: 'Salvar alerta',
      campos: (_) => [
        _CamposDoAlerta(
          ativo: _ativo,
          antecedencia: _antecedencia,
          aoMudar: (ativo) => setState(() => _ativo = ativo),
        ),
      ],
      enviar: (ref) async {
        final dias = _ativo ? int.parse(_antecedencia.text.trim()) : null;
        await ref
            .read(planoRepositoryProvider)
            .configurarAlerta(acao.id, ativo: _ativo, antecedenciaDias: dias);
        recarregarPlano(ref, acao);
        return _ativo
            ? 'Alerta da ação #${acao.id} configurado para $dias dia(s) antes do prazo.'
            : 'Alerta da ação #${acao.id} desativado.';
      },
    );
  }
}

// ---------------------------------------------------------------------------------------------
// Peças comuns
// ---------------------------------------------------------------------------------------------

/// "Colaborador: Ana Souza" ou "Setor: Galpão A".
String descreverResponsavel(AcaoPlano acao) {
  final nome = acao.responsavelNome ?? 'Não informado';
  final tipo = acao.responsavelTipo;
  return tipo == null ? nome : '${tipo.rotulo}: $nome';
}

class _CamposDoAlerta extends StatelessWidget {
  const _CamposDoAlerta({
    required this.ativo,
    required this.antecedencia,
    required this.aoMudar,
  });

  final bool ativo;
  final TextEditingController antecedencia;
  final ValueChanged<bool> aoMudar;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          title: const Text('Alerta antes do prazo'),
          subtitle: const Text(
            'O Gestor é avisado com a antecedência escolhida.',
          ),
          value: ativo,
          onChanged: aoMudar,
        ),
        if (ativo)
          CampoTexto(
            rotulo: 'Antecedência (dias)',
            controlador: antecedencia,
            teclado: TextInputType.number,
            validador: (valor) => validarAntecedencia(true, valor),
          ),
      ],
    );
  }
}

class _BotaoEnviar extends StatelessWidget {
  const _BotaoEnviar({
    required this.rotulo,
    required this.enviando,
    required this.aoTocar,
  });

  final String rotulo;
  final bool enviando;
  final VoidCallback aoTocar;

  @override
  Widget build(BuildContext context) {
    return FilledButton.icon(
      onPressed: enviando ? null : aoTocar,
      icon: enviando
          ? const SizedBox.square(
              dimension: TokensDimensoes.sizeIcon,
              child: CircularProgressIndicator(strokeWidth: 2),
            )
          : const Icon(Icons.check),
      label: Text(rotulo),
    );
  }
}

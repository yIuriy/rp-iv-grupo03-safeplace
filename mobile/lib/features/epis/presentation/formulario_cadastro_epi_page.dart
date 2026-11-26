// Formulário de cadastro de EPI (RF03), só do Gestor: dados obrigatórios do `CriarEpiRequest`,
// classificação, localização e descrição, e os dados opcionais de lote e modelo. Os textos seguem o
// `DialogoCadastroEpi` do web. Quando o servidor recusa, a mensagem dele aparece e os dados ficam.
// Fechar com dados digitados pede confirmação (#335).

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/guarda_de_descarte.dart';
import '../data/epi.dart';
import '../data/epis_api.dart';
import '../domain/epi_repository.dart';
import '../domain/validacao_cadastro_epi.dart';

/// Abre o formulário em tela cheia. Devolve o EPI cadastrado, ou `null` se a pessoa desistiu.
Future<Epi?> abrirFormularioCadastroEpi(BuildContext context) {
  return Navigator.of(context, rootNavigator: true).push<Epi>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => const FormularioCadastroEpiPage(),
    ),
  );
}

/// Mensagens de falha do envio. Recusas de validação (400) trazem o motivo no `message`.
final descreverFalhaDeCadastroEpi = criarDescritorDeFalha(
  porStatus: {
    400: usarMensagemDaApi(
      'O cadastro foi recusado pelo servidor. Confira os campos obrigatórios.',
    ),
    403: fixa(
      'Seu perfil não tem permissão para cadastrar EPIs. Apenas o Gestor de Segurança cadastra EPIs.',
    ),
  },
  padrao: 'Não foi possível cadastrar o EPI agora. Os dados preenchidos foram mantidos. Tente novamente.',
);

/// Aviso da lista depois do cadastro, o mesmo texto do web.
const mensagemEpiCadastrado = 'EPI cadastrado. A listagem foi atualizada.';

class FormularioCadastroEpiPage extends ConsumerStatefulWidget {
  const FormularioCadastroEpiPage({super.key});

  @override
  ConsumerState<FormularioCadastroEpiPage> createState() =>
      _FormularioCadastroEpiPageState();
}

class _FormularioCadastroEpiPageState
    extends ConsumerState<FormularioCadastroEpiPage> {
  final _form = GlobalKey<FormState>();
  final _nome = TextEditingController();
  final _numeroCa = TextEditingController();
  final _quantidade = TextEditingController();
  final _estoqueMinimo = TextEditingController();
  final _validadeCa = TextEditingController();
  final _vidaUtil = TextEditingController();
  final _localizacao = TextEditingController();
  final _descricao = TextEditingController();
  final _numeroLote = TextEditingController();
  final _notaFiscal = TextEditingController();
  final _dataFabricacao = TextEditingController();
  final _validadeLote = TextEditingController();
  final _marca = TextEditingController();
  ClassificacaoEpi? _classificacao;
  bool _enviando = false;
  bool _alterado = false;
  String? _falha;

  List<TextEditingController> get _controladores => [
    _nome,
    _numeroCa,
    _quantidade,
    _estoqueMinimo,
    _validadeCa,
    _vidaUtil,
    _localizacao,
    _descricao,
    _numeroLote,
    _notaFiscal,
    _dataFabricacao,
    _validadeLote,
    _marca,
  ];

  @override
  void dispose() {
    for (final controlador in _controladores) {
      controlador.dispose();
    }
    super.dispose();
  }

  /// Texto sem espaços nas pontas, ou `null` quando vazio (o campo não vai no corpo).
  static String? _opcional(TextEditingController controlador) {
    final texto = controlador.text.trim();
    return texto.isEmpty ? null : texto;
  }

  static DateTime? _dataOpcional(TextEditingController controlador) {
    final texto = _opcional(controlador);
    return texto == null ? null : lerData(texto);
  }

  DadosCadastroEpi _lerDados() => DadosCadastroEpi(
    nome: _nome.text.trim(),
    numeroCa: _numeroCa.text.trim(),
    quantidade: int.parse(_quantidade.text.trim()),
    estoqueMinimo: int.parse(_estoqueMinimo.text.trim()),
    dataValidadeCa: lerData(_validadeCa.text)!,
    classificacao: _classificacao!,
    vidaUtilDias: int.tryParse(_vidaUtil.text.trim()),
    localizacao: _opcional(_localizacao),
    descricao: _opcional(_descricao),
    numeroLote: _opcional(_numeroLote),
    notaFiscal: _opcional(_notaFiscal),
    dataFabricacao: _dataOpcional(_dataFabricacao),
    validadeLote: _dataOpcional(_validadeLote),
    marca: _opcional(_marca),
  );

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    setState(() => _enviando = true);
    try {
      final epi = await ref.read(epiRepositoryProvider).cadastrar(_lerDados());
      if (!mounted) return;
      ref.invalidate(episProvider);
      Navigator.of(context).pop<Epi>(epi);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeCadastroEpi(erro);
      });
    }
  }

  Widget _numero(
    String rotulo,
    TextEditingController controlador,
    FormFieldValidator<String> validador, {
    String? ajuda,
  }) {
    return CampoTexto(
      rotulo: rotulo,
      controlador: controlador,
      ajuda: ajuda,
      teclado: TextInputType.number,
      formatadores: [FilteringTextInputFormatter.digitsOnly],
      validador: validador,
    );
  }

  Widget _data(
    String rotulo,
    TextEditingController controlador,
    FormFieldValidator<String> validador,
  ) {
    return CampoTexto(
      rotulo: rotulo,
      controlador: controlador,
      dica: 'dd/mm/aaaa',
      teclado: TextInputType.number,
      formatadores: const [MascaraData()],
      validador: validador,
    );
  }

  @override
  Widget build(BuildContext context) {
    const espaco = SizedBox(height: TokensDimensoes.space16);
    final textos = Theme.of(context).textTheme;
    return GuardaDeDescarte(
      alterado: _alterado,
      enviando: _enviando,
      child: Scaffold(
        appBar: AppBar(title: const Text('Cadastrar EPI')),
        body: SafeArea(
          child: Form(
            key: _form,
            onChanged: () {
              if (!_alterado) setState(() => _alterado = true);
            },
            // Coluna em vez de ListView: a lista constrói só o que está na tela, e um campo fora dela
            // escaparia da validação do formulário.
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(
                TokensDimensoes.layoutMobilePadding,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text(
                    'Nome, CA, validade do CA, quantidade inicial, estoque mínimo e classificação são obrigatórios. Os demais campos são opcionais.',
                    style: textos.bodyMedium?.copyWith(
                      color: context.cores.textoSecundario,
                    ),
                  ),
                  const SizedBox(height: TokensDimensoes.space24),
                  CampoTexto(
                    rotulo: 'Nome do EPI',
                    controlador: _nome,
                    validador: validarNomeEpi,
                  ),
                  espaco,
                  CampoTexto(
                    rotulo: 'Número do CA',
                    controlador: _numeroCa,
                    ajuda: 'Certificado de Aprovação emitido pelo Ministério do Trabalho.',
                    validador: validarNumeroCa,
                  ),
                  espaco,
                  _data('Validade do CA', _validadeCa, validarValidadeCa),
                  espaco,
                  _numero(
                    'Quantidade inicial',
                    _quantidade,
                    validarQuantidadeInicial,
                  ),
                  espaco,
                  _numero(
                    'Estoque mínimo',
                    _estoqueMinimo,
                    validarEstoqueMinimo,
                    ajuda: 'Com saldo igual ou abaixo deste valor, o EPI aparece como crítico.',
                  ),
                  espaco,
                  CampoSelecao<ClassificacaoEpi>(
                    rotulo: 'Classificação',
                    valor: _classificacao,
                    dica: 'Selecione a classificação',
                    opcoes: [
                      for (final classificacao in ClassificacaoEpi.values)
                        (valor: classificacao, rotulo: classificacao.rotulo),
                    ],
                    aoMudar: (valor) => setState(() => _classificacao = valor),
                    validador: validarClassificacao,
                    habilitado: !_enviando,
                  ),
                  espaco,
                  _numero(
                    'Vida útil estimada (dias)',
                    _vidaUtil,
                    validarVidaUtil,
                  ),
                  espaco,
                  CampoTexto(rotulo: 'Localização', controlador: _localizacao),
                  espaco,
                  CampoTexto(
                    rotulo: 'Descrição',
                    controlador: _descricao,
                    linhas: 3,
                    ajuda: 'Especificação opcional do equipamento.',
                  ),
                  const SizedBox(height: TokensDimensoes.space24),
                  Text(
                    'Dados opcionais de lote e modelo',
                    style: textos.titleMedium,
                  ),
                  espaco,
                  CampoTexto(
                    rotulo: 'Número do lote',
                    controlador: _numeroLote,
                  ),
                  espaco,
                  CampoTexto(rotulo: 'Nota fiscal', controlador: _notaFiscal),
                  espaco,
                  _data(
                    'Data de fabricação',
                    _dataFabricacao,
                    validarDataOpcional,
                  ),
                  espaco,
                  _data('Validade do lote', _validadeLote, validarDataOpcional),
                  espaco,
                  CampoTexto(rotulo: 'Marca', controlador: _marca),
                  if (_falha != null) ...[
                    espaco,
                    Aviso(
                      tom: TomAviso.perigo,
                      anunciar: true,
                      titulo: 'Cadastro não concluído',
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
                    label: const Text('Cadastrar EPI'),
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
      ),
    );
  }
}

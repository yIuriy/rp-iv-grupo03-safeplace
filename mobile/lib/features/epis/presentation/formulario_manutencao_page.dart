// Formulário de registro de manutenção de EPI (RF04), só do Gestor: data, hora, tipo opcional,
// descrição e resultado. A data enviada é montada pelo texto digitado, sem passar por `DateTime`
// (mesmo cuidado da #250). Quando o servidor recusa, a mensagem dele aparece e os dados ficam.

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../data/epi.dart';
import '../data/epis_api.dart';
import '../data/manutencao.dart';
import '../domain/epi_repository.dart';
import '../domain/validacao_manutencao.dart';

/// Abre o formulário em tela cheia. Devolve a manutenção registrada, ou `null` se a pessoa desistiu.
Future<Manutencao?> abrirFormularioManutencao(
  BuildContext context, {
  required Epi epi,
}) {
  return Navigator.of(context, rootNavigator: true).push<Manutencao>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => FormularioManutencaoPage(epi: epi),
    ),
  );
}

/// Mensagens de falha do envio, as mesmas do web. Recusas de regra (400) trazem o motivo no
/// `message`, que tem a preferência.
final descreverFalhaDeManutencao = criarDescritorDeFalha(
  porStatus: {
    403: fixa('Seu perfil não tem permissão para registrar manutenção.'),
    404: fixa(
      'EPI não encontrado. Ele pode ter sido alterado por outra pessoa. Volte e recarregue a lista.',
    ),
  },
  padrao: 'Não foi possível registrar a manutenção agora. Os dados preenchidos foram mantidos. Tente novamente.',
);

const mensagemManutencaoRegistrada =
    'Manutenção registrada. Situação e histórico foram atualizados.';

/// `hh:mm` enquanto se digita: só números, com os dois-pontos depois da hora.
class _MascaraHora extends TextInputFormatter {
  const _MascaraHora();

  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue antigo,
    TextEditingValue novo,
  ) {
    var digitos = novo.text.replaceAll(RegExp(r'\D'), '');
    if (digitos.length > 4) digitos = digitos.substring(0, 4);
    final texto = digitos.length > 2
        ? '${digitos.substring(0, 2)}:${digitos.substring(2)}'
        : digitos;
    return TextEditingValue(
      text: texto,
      selection: TextSelection.collapsed(offset: texto.length),
    );
  }
}

class FormularioManutencaoPage extends ConsumerStatefulWidget {
  const FormularioManutencaoPage({super.key, required this.epi});

  final Epi epi;

  @override
  ConsumerState<FormularioManutencaoPage> createState() =>
      _FormularioManutencaoPageState();
}

class _FormularioManutencaoPageState
    extends ConsumerState<FormularioManutencaoPage> {
  final _form = GlobalKey<FormState>();
  final _data = TextEditingController();
  final _hora = TextEditingController();
  final _descricao = TextEditingController();
  TipoManutencao? _tipo;
  ResultadoManutencao? _resultado;
  bool _enviando = false;
  String? _falha;

  @override
  void dispose() {
    _data.dispose();
    _hora.dispose();
    _descricao.dispose();
    super.dispose();
  }

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    final epiId = widget.epi.id!;
    setState(() => _enviando = true);
    try {
      final manutencao = await ref
          .read(epiRepositoryProvider)
          .registrarManutencao(
            epiId,
            DadosManutencao(
              dataManutencao: montarDataHoraIso(_data.text, _hora.text),
              descricao: _descricao.text.trim(),
              resultado: _resultado!,
              tipo: _tipo,
            ),
          );
      if (!mounted) return;
      // Aprovado devolve o EPI a Disponível, então status e lista mudam.
      ref.invalidate(episProvider);
      ref.invalidate(epiProvider(epiId));
      ref.invalidate(manutencoesProvider(epiId));
      Navigator.of(context).pop<Manutencao>(manutencao);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeManutencao(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    const espaco = SizedBox(height: TokensDimensoes.space16);
    return Scaffold(
      appBar: AppBar(title: const Text('Registrar manutenção')),
      body: SafeArea(
        child: Form(
          key: _form,
          child: ListView(
            padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
            children: [
              Text(
                '${widget.epi.nome}. Informe data, hora, descrição e resultado. O tipo de manutenção é opcional.',
                style: Theme.of(context).textTheme.bodyMedium
                    ?.copyWith(color: context.cores.textoSecundario),
              ),
              const SizedBox(height: TokensDimensoes.space24),
              CampoTexto(
                rotulo: 'Data da manutenção',
                controlador: _data,
                dica: 'dd/mm/aaaa',
                teclado: TextInputType.number,
                formatadores: const [MascaraData()],
                validador: validarDataManutencao,
              ),
              espaco,
              CampoTexto(
                rotulo: 'Hora da manutenção',
                controlador: _hora,
                dica: 'hh:mm',
                teclado: TextInputType.number,
                formatadores: const [_MascaraHora()],
                validador: validarHoraManutencao,
              ),
              espaco,
              CampoSelecao<TipoManutencao>(
                rotulo: 'Tipo (opcional)',
                valor: _tipo,
                opcoes: [
                  for (final tipo in TipoManutencao.values)
                    (valor: tipo, rotulo: tipo.rotulo),
                ],
                aoMudar: (tipo) => setState(() => _tipo = tipo),
                habilitado: !_enviando,
              ),
              espaco,
              CampoTexto(
                rotulo: 'Descrição da manutenção',
                controlador: _descricao,
                linhas: 3,
                ajuda: 'Descreva o serviço realizado no EPI.',
                validador: validarDescricaoManutencao,
              ),
              espaco,
              CampoSelecao<ResultadoManutencao>(
                rotulo: 'Resultado',
                valor: _resultado,
                dica: 'Selecione o resultado',
                ajuda: 'Aprovado devolve o EPI ao estoque. Reprovado mantém o EPI em manutenção.',
                opcoes: [
                  for (final resultado in ResultadoManutencao.values)
                    (valor: resultado, rotulo: resultado.rotulo),
                ],
                aoMudar: (resultado) => setState(() => _resultado = resultado),
                validador: validarResultado,
                habilitado: !_enviando,
              ),
              if (_falha != null) ...[
                espaco,
                Aviso(
                  tom: TomAviso.perigo,
                  anunciar: true,
                  titulo: 'Manutenção não registrada',
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
                label: const Text('Salvar manutenção'),
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
    );
  }
}

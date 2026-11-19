import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/cpf.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/seletor_com_busca.dart';
import '../data/usuario.dart';
import '../data/usuarios_api.dart';

/// Escolha de um colaborador nos formulários dos outros módulos (#317): ocorrências (#318),
/// empréstimos (#320), alocações (#324) e capacitações (#325).
///
/// Carrega `GET /api/usuarios/colaboradores` uma vez (com a cópia offline da conta, RNF08) e abre o
/// `SeletorComBusca`, que filtra por nome ou CPF, com ou sem máscara e ignorando acentos. A lista
/// mostra o CPF oculto (`•••.982.247-••`), mas a busca confere os dígitos completos.
///
/// ```dart
/// ColaboradorPicker(
///   colaboradorId: _colaboradorId,             // seleção inicial, por id (opcional)
///   obrigatorio: true,                          // "Escolha o colaborador." no validate() do Form
///   aoMudar: (c) => setState(() => _colaboradorId = c?.id),
/// )
/// ```
///
/// Participa do `Form` como qualquer campo. Por padrão só oferece cadastros ativos, como o web faz na
/// entrega de EPI (o backend recusa a entrega a colaborador inativo); `somenteAtivos: false` mostra
/// todos. O já escolhido em [colaboradorId] continua aparecendo mesmo inativo, para a edição de um
/// registro antigo não perder o vínculo. Cadastros sem `id` não aparecem: não há como vinculá-los.
class ColaboradorPicker extends ConsumerWidget {
  const ColaboradorPicker({
    super.key,
    required this.aoMudar,
    this.colaboradorId,
    this.rotulo = 'Colaborador',
    this.ajuda,
    this.obrigatorio = false,
    this.validador,
    this.somenteAtivos = true,
    this.habilitado = true,
  });

  /// Recebe o colaborador escolhido (com `id`, `nome`, `cpf`...).
  final ValueChanged<Usuario?> aoMudar;

  /// Colaborador já escolhido, como vem nos registros dos outros módulos (`colaboradorId`).
  final int? colaboradorId;
  final String rotulo;
  final String? ajuda;

  /// Sem [validador], recusa a falta de escolha com "Escolha o colaborador.".
  final bool obrigatorio;
  final FormFieldValidator<Usuario>? validador;
  final bool somenteAtivos;
  final bool habilitado;

  static const mensagemObrigatorio = 'Escolha o colaborador.';

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final consulta = colaboradoresProvider(FiltroColaboradores.nenhum);
    final valor = ref.watch(consulta);
    final consultado = valor.value;
    final opcoes = [
      for (final colaborador in consultado?.dados ?? const <Usuario>[])
        if (colaborador.id != null &&
            (!somenteAtivos ||
                colaborador.ativo ||
                colaborador.id == colaboradorId))
          colaborador,
    ];
    Usuario? inicial;
    for (final opcao in opcoes) {
      if (opcao.id == colaboradorId) inicial = opcao;
    }
    final falhou = valor.hasError && !valor.hasValue;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        SeletorComBusca<Usuario>(
          // O FormField só lê o valor inicial ao nascer: recria quando a lista chega ou quando a
          // escolha muda por fora.
          key: ValueKey((valor.hasValue, colaboradorId)),
          rotulo: rotulo,
          rotuloBusca: 'Buscar por nome ou CPF',
          opcoes: opcoes,
          valor: inicial,
          descrever: (colaborador) => colaborador.nome,
          detalhar: _detalhar,
          termosDeBusca: (colaborador) => colaborador.cpf == null
              ? ''
              : '${colaborador.cpf} ${mascararCpf(colaborador.cpf!)}',
          aoMudar: aoMudar,
          validator:
              validador ??
              (obrigatorio
                  ? (escolhido) =>
                        escolhido == null ? mensagemObrigatorio : null
                  : null),
          habilitado: habilitado,
          carregando: valor.isLoading && !valor.hasValue,
          falha: falhou ? 'Não foi possível carregar os colaboradores.' : null,
          ajuda: consultado != null && consultado.somenteLeitura
              ? 'Lista salva no aparelho: pode não ter os cadastros mais recentes.'
              : ajuda,
        ),
        if (falhou)
          Align(
            alignment: Alignment.centerLeft,
            child: Padding(
              padding: const EdgeInsets.only(top: TokensDimensoes.space4),
              child: TextButton(
                onPressed: () => ref.invalidate(consulta),
                child: const Text('Tentar novamente'),
              ),
            ),
          ),
      ],
    );
  }

  static String _detalhar(Usuario colaborador) => [
    if (colaborador.cpf case final cpf?) 'CPF ${ocultarCpf(cpf)}',
    ?colaborador.email,
    if (!colaborador.ativo) 'Inativo',
  ].join(' · ');
}

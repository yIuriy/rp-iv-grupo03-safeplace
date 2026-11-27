// Tela `/ocorrencias` (UC05, UC09, #318): lista de ocorrências com filtros enviados ao servidor,
// busca de acidente pelo protocolo CAT e o botão de registrar. Gestor e Supervisor usam a mesma
// tela, e o Supervisor recebe só os próprios relatos (o backend filtra). Com o servidor fora do ar,
// mostra a cópia local com o aviso de modo offline (RNF08), e registro e busca ficam desabilitados.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/estados.dart';
import '../data/ocorrencia.dart';
import '../data/ocorrencias_api.dart';
import '../domain/ocorrencia_repository.dart';
import 'componentes_ocorrencia.dart';
import 'filtros_ocorrencias.dart';
import 'formulario_ocorrencia_page.dart';

class OcorrenciasPage extends ConsumerStatefulWidget {
  const OcorrenciasPage({super.key});

  @override
  ConsumerState<OcorrenciasPage> createState() => _OcorrenciasPageState();
}

class _OcorrenciasPageState extends ConsumerState<OcorrenciasPage> {
  FiltroOcorrencias _filtro = FiltroOcorrencias.nenhum;

  Future<void> _registrar() async {
    final ocorrencia = await abrirFormularioOcorrencia(context);
    if (ocorrencia == null || !mounted) return;
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(mensagemDeRegistro(ocorrencia))));
  }

  void _aplicar(FiltroOcorrencias filtro) => setState(() => _filtro = filtro);

  @override
  Widget build(BuildContext context) {
    final consulta = ref.watch(ocorrenciasProvider(_filtro));
    final somenteLeitura = consulta.value?.somenteLeitura ?? false;
    final gestor =
        ref.watch(controladorSessaoProvider).value?.sessao?.ehGestor ?? false;
    final secundario = Theme.of(context).textTheme.bodyMedium
        ?.copyWith(color: context.cores.textoSecundario);
    final criterios = _filtro.consulta.length;
    return RefreshIndicator(
      onRefresh: () => ref
          .refresh(ocorrenciasProvider(_filtro).future)
          .then((_) {}, onError: (_) {}),
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Text(
            gestor
                ? 'Registre acidentes e incidentes e consulte todos os relatos da empresa.'
                : 'Registre acidentes e incidentes e consulte os relatos que você registrou.',
            style: secundario,
          ),
          const SizedBox(height: TokensDimensoes.space12),
          FilledButton.icon(
            // Registro precisa do servidor: sem conexão, fica desabilitado (RNF08).
            onPressed: somenteLeitura ? null : _registrar,
            icon: const Icon(Icons.assignment_add),
            label: const Text('Registrar ocorrência'),
          ),
          if (somenteLeitura) ...[
            const SizedBox(height: TokensDimensoes.space8),
            Text(
              'Registro e busca por protocolo voltam quando a conexão voltar.',
              style: secundario,
            ),
          ],
          const SizedBox(height: TokensDimensoes.space16),
          _BuscaPorProtocolo(habilitada: !somenteLeitura),
          const SizedBox(height: TokensDimensoes.space8),
          ExpansionTile(
            // Fechado, o painel guarda o que foi digitado.
            maintainState: true,
            tilePadding: EdgeInsets.zero,
            childrenPadding: const EdgeInsets.only(
              bottom: TokensDimensoes.space16,
            ),
            title: const Text('Filtros'),
            subtitle: Text(switch (criterios) {
              0 => 'Nenhum critério aplicado',
              1 => '1 critério aplicado',
              _ => '$criterios critérios aplicados',
            }),
            children: [FiltrosOcorrencias(aoAplicar: _aplicar)],
          ),
          const SizedBox(height: TokensDimensoes.space8),
          ConteudoComCache<List<Ocorrencia>>(
            valor: consulta,
            carregando: 'Carregando ocorrências...',
            falha: 'Não foi possível carregar as ocorrências.',
            descreverFalha: descreverFalhaDeOcorrencia,
            aoTentarDeNovo: () => ref.invalidate(ocorrenciasProvider(_filtro)),
            construir: (ocorrencias, _) => _ListaDeOcorrencias(
              ocorrencias: ocorrencias,
              filtrada: !_filtro.vazio,
              aoLimpar: () => _aplicar(FiltroOcorrencias.nenhum),
            ),
          ),
        ],
      ),
    );
  }
}

/// Lista ou o estado vazio certo: nenhum relato ainda, ou nenhum relato nos critérios.
class _ListaDeOcorrencias extends StatelessWidget {
  const _ListaDeOcorrencias({
    required this.ocorrencias,
    required this.filtrada,
    required this.aoLimpar,
  });

  final List<Ocorrencia> ocorrencias;
  final bool filtrada;
  final VoidCallback aoLimpar;

  @override
  Widget build(BuildContext context) {
    if (ocorrencias.isEmpty && filtrada) {
      return EstadoVazio(
        titulo: 'Nenhuma ocorrência encontrada',
        descricao: 'Nenhuma ocorrência atende aos critérios. Ajuste os filtros ou limpe a busca.',
        rotuloAcao: 'Limpar filtros',
        aoAgir: aoLimpar,
      );
    }
    if (ocorrencias.isEmpty) {
      return const EstadoVazio(
        icone: Icons.assignment_outlined,
        titulo: 'Nenhuma ocorrência registrada',
        descricao: 'Os acidentes e incidentes relatados aparecerão aqui.',
      );
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          ocorrencias.length == 1
              ? '1 ocorrência'
              : '${ocorrencias.length} ocorrências',
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const SizedBox(height: TokensDimensoes.space8),
        for (final ocorrencia in ocorrencias) ...[
          CartaoOcorrencia(
            ocorrencia: ocorrencia,
            aoTocar: ocorrencia.id == null
                ? null
                : () => context.go('/ocorrencias/${ocorrencia.id}'),
          ),
          const SizedBox(height: TokensDimensoes.space12),
        ],
      ],
    );
  }
}

/// Busca de acidente pelo número da CAT. Encontrado, abre o detalhe. Sem resultado, mostra o motivo
/// abaixo do campo.
class _BuscaPorProtocolo extends ConsumerStatefulWidget {
  const _BuscaPorProtocolo({required this.habilitada});

  final bool habilitada;

  @override
  ConsumerState<_BuscaPorProtocolo> createState() => _BuscaPorProtocoloState();
}

class _BuscaPorProtocoloState extends ConsumerState<_BuscaPorProtocolo> {
  final _protocolo = TextEditingController();
  bool _buscando = false;
  String? _falha;

  @override
  void dispose() {
    _protocolo.dispose();
    super.dispose();
  }

  Future<void> _buscar() async {
    final protocolo = _protocolo.text.trim();
    if (protocolo.isEmpty) {
      setState(() => _falha = 'Informe o protocolo CAT do acidente.');
      return;
    }
    setState(() {
      _buscando = true;
      _falha = null;
    });
    try {
      final ocorrencia = await ref
          .read(ocorrenciaRepositoryProvider)
          .buscarPorProtocolo(protocolo);
      if (!mounted) return;
      setState(() => _buscando = false);
      if (ocorrencia.id != null) context.go('/ocorrencias/${ocorrencia.id}');
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _buscando = false;
        _falha = descreverFalhaDeBusca(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final habilitada = widget.habilitada && !_buscando;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        CampoTexto(
          rotulo: 'Protocolo CAT',
          controlador: _protocolo,
          ajuda: 'Só acidentes têm protocolo.',
          habilitado: habilitada,
          acaoTeclado: TextInputAction.search,
          aoEnviar: (_) => _buscar(),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        OutlinedButton.icon(
          onPressed: habilitada ? _buscar : null,
          icon: const Icon(Icons.search),
          label: const Text('Buscar por protocolo'),
        ),
        if (_falha case final falha?) ...[
          const SizedBox(height: TokensDimensoes.space8),
          Aviso(
            tom: TomAviso.perigo,
            anunciar: true,
            titulo: 'Acidente não encontrado',
            mensagem: falha,
          ),
        ],
      ],
    );
  }
}

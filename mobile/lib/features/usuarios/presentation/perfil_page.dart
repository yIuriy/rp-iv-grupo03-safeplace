import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/offline/cache_offline.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/estados.dart';
import '../data/acao_usuario.dart';
import '../data/usuario.dart';
import '../data/usuarios_api.dart';
import 'componentes.dart';

/// Tela `/perfil`, como a `PerfilPage` do web: dados cadastrais da conta logada, só para consulta
/// (`GET /api/usuarios/me`), e as ações recentes da pessoa na trilha de auditoria (RNF05). A
/// atualização de cadastros continua nas rotas por papel de RF23.
class PerfilPage extends ConsumerStatefulWidget {
  const PerfilPage({super.key});

  @override
  ConsumerState<PerfilPage> createState() => _PerfilPageState();
}

class _PerfilPageState extends ConsumerState<PerfilPage> {
  int _limite = limiteHistoricoPadrao;

  Future<void> _atualizar() => Future.wait([
    ref.refresh(meuPerfilProvider.future),
    ref.refresh(historicoProvider(_limite).future),
  ]).then((_) {}, onError: (_) {});

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    return RefreshIndicator(
      onRefresh: _atualizar,
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          Text(
            'Dados cadastrais da sua conta no SafePlace, somente para consulta.',
            style: textos.bodyMedium?.copyWith(
              color: context.cores.textoSecundario,
            ),
          ),
          const SizedBox(height: TokensDimensoes.space16),
          ConteudoComCache<Usuario>(
            valor: ref.watch(meuPerfilProvider),
            carregando: 'Carregando perfil...',
            falha: 'Não foi possível carregar o seu perfil.',
            descreverFalha: descreverFalhaDeConsulta,
            aoTentarDeNovo: () => ref.invalidate(meuPerfilProvider),
            construir: (perfil, _) => _DadosDoPerfil(perfil),
          ),
          const SizedBox(height: TokensDimensoes.space32),
          _HistoricoDeAcoes(
            limite: _limite,
            aoVerMais: () => setState(
              () => _limite = ajustarLimiteHistorico(
                _limite + limiteHistoricoPadrao,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _DadosDoPerfil extends StatelessWidget {
  const _DadosDoPerfil(this.perfil);

  final Usuario perfil;

  @override
  Widget build(BuildContext context) {
    final nascimento = perfil.dataNascimento;
    final criadoEm = perfil.criadoEm;
    return Card(
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(TokensDimensoes.space16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Semantics(
              header: true,
              child: Text(
                'Seus dados',
                style: Theme.of(context).textTheme.titleLarge,
              ),
            ),
            LinhaDado.texto(rotulo: 'Nome completo', texto: perfil.nome),
            LinhaDado.texto(
              rotulo: 'Perfil de acesso',
              texto: perfil.perfil?.rotulo ?? 'Colaborador',
            ),
            LinhaDado.texto(
              rotulo: 'E-mail de acesso',
              texto: perfil.email ?? naoInformado,
            ),
            LinhaDado(
              rotulo: 'CPF',
              valor: CpfOculto(cpf: perfil.cpf, nome: perfil.nome),
            ),
            LinhaDado.texto(
              rotulo: 'Data de nascimento',
              texto: nascimento == null
                  ? naoInformado
                  : formatarData(nascimento),
            ),
            LinhaDado.texto(
              rotulo: 'Cadastrado em',
              texto: criadoEm == null ? naoInformado : formatarData(criadoEm),
            ),
          ],
        ),
      ),
    );
  }
}

/// "Minhas ações recentes": as últimas ações registradas com o e-mail da pessoa, com "Ver mais"
/// até o máximo de 100 que o backend aceita.
class _HistoricoDeAcoes extends ConsumerWidget {
  const _HistoricoDeAcoes({required this.limite, required this.aoVerMais});

  final int limite;
  final VoidCallback aoVerMais;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final textos = Theme.of(context).textTheme;
    final consulta = historicoProvider(limite);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Semantics(
          header: true,
          child: Text('Minhas ações recentes', style: textos.titleLarge),
        ),
        const SizedBox(height: TokensDimensoes.space4),
        Text(
          'As $limite ações mais recentes registradas com o seu e-mail na trilha de auditoria.',
          style: textos.bodyMedium?.copyWith(
            color: context.cores.textoSecundario,
          ),
        ),
        const SizedBox(height: TokensDimensoes.space12),
        // A faixa de offline já aparece nos dados do perfil; aqui só a lista.
        ref
            .watch(consulta)
            .when(
              skipLoadingOnReload: true,
              skipLoadingOnRefresh: true,
              loading: () =>
                  const EstadoCarregando(texto: 'Carregando ações...'),
              error: (erro, _) => Aviso(
                tom: TomAviso.perigo,
                anunciar: true,
                titulo: 'Não foi possível carregar as suas ações.',
                mensagem: descreverFalhaDeConsulta(erro),
                rotuloAcao: 'Tentar novamente',
                aoAgir: () => ref.invalidate(consulta),
              ),
              data: (Consultado<List<AcaoUsuario>> consultado) {
                final acoes = consultado.dados;
                if (acoes.isEmpty) {
                  return const EstadoVazio(
                    icone: Icons.history,
                    titulo: 'Nenhuma ação registrada',
                    descricao: 'Cadastros e alterações que você fizer aparecerão aqui.',
                  );
                }
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    for (final acao in acoes) ...[
                      _ItemDaAcao(acao),
                      const SizedBox(height: TokensDimensoes.space8),
                    ],
                    // Veio o que se pediu: pode haver mais, até o máximo do backend.
                    if (acoes.length >= limite &&
                        limite < limiteHistoricoMaximo &&
                        !consultado.somenteLeitura)
                      OutlinedButton(
                        onPressed: aoVerMais,
                        child: const Text('Ver mais ações'),
                      ),
                  ],
                );
              },
            ),
      ],
    );
  }
}

class _ItemDaAcao extends StatelessWidget {
  const _ItemDaAcao(this.acao);

  final AcaoUsuario acao;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final quando = acao.dataHora;
    return Card(
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(TokensDimensoes.space12),
        child: MergeSemantics(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Wrap(
                spacing: TokensDimensoes.space8,
                runSpacing: TokensDimensoes.space4,
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  _EtiquetaOperacao(acao),
                  Text(acao.registro, style: textos.titleSmall),
                ],
              ),
              const SizedBox(height: TokensDimensoes.space4),
              Text(
                quando == null
                    ? 'Data não informada'
                    : formatarDataHora(quando),
                style: textos.bodySmall?.copyWith(
                  color: context.cores.textoSecundario,
                ),
              ),
              const SizedBox(height: TokensDimensoes.space4),
              Text(
                acao.descricao ?? 'Sem descrição',
                style: acao.descricao == null
                    ? textos.bodyMedium?.copyWith(
                        color: context.cores.textoSecundario,
                      )
                    : textos.bodyMedium,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Operação com cor e texto, como o `Badge` do web: cadastro em verde, alteração em azul, remoção em
/// vermelho; desconhecida aparece como veio, sem cor.
class _EtiquetaOperacao extends StatelessWidget {
  const _EtiquetaOperacao(this.acao);

  final AcaoUsuario acao;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final (frente, fundo) = switch (acao.tipo) {
      TipoOperacao.cadastro => (cores.sucesso, cores.sucessoFundo),
      TipoOperacao.alteracao => (cores.info, cores.infoFundo),
      TipoOperacao.remocao => (cores.perigo, cores.perigoFundo),
      null => (TokensCores.textPrimary, cores.pagina),
    };
    return DecoratedBox(
      decoration: BoxDecoration(
        color: fundo,
        borderRadius: BorderRadius.circular(TokensDimensoes.radius16),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: TokensDimensoes.space8,
          vertical: 2,
        ),
        child: Text(
          acao.rotuloOperacao,
          style: Theme.of(context).textTheme.labelMedium
              ?.copyWith(color: frente),
        ),
      ),
    );
  }
}

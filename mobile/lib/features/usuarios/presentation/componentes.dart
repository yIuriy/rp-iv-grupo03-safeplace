import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/formularios/cpf.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../data/usuario.dart';

/// Mensagem das consultas do módulo quando o servidor responde com erro.
final descreverFalhaDeConsulta = criarDescritorDeFalha(
  padrao: 'Verifique sua conexão ou tente novamente em alguns instantes.',
);

const naoInformado = 'Não informado';

/// Rótulo e valor de um dado cadastral, lidos juntos pelo leitor de tela.
class LinhaDado extends StatelessWidget {
  const LinhaDado({super.key, required this.rotulo, required this.valor});

  final String rotulo;
  final Widget valor;

  LinhaDado.texto({super.key, required this.rotulo, required String texto})
    : valor = Text(texto);

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.only(top: TokensDimensoes.space8),
      child: MergeSemantics(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              rotulo,
              style: textos.labelMedium?.copyWith(
                color: context.cores.textoSecundario,
              ),
            ),
            DefaultTextStyle.merge(style: textos.bodyLarge, child: valor),
          ],
        ),
      ),
    );
  }
}

/// CPF com início e fim cobertos (`•••.456.789-••`), como o `CpfOculto` do web: a listagem serve
/// para localizar o cadastro, e o número inteiro aparece só quando alguém pede.
class CpfOculto extends StatefulWidget {
  const CpfOculto({super.key, required this.cpf, required this.nome});

  final String? cpf;
  final String nome;

  @override
  State<CpfOculto> createState() => _CpfOcultoState();
}

class _CpfOcultoState extends State<CpfOculto> {
  bool _visivel = false;

  @override
  Widget build(BuildContext context) {
    final cpf = widget.cpf;
    if (cpf == null) return const Text(naoInformado);
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Flexible(child: Text(_visivel ? mascararCpf(cpf) : ocultarCpf(cpf))),
        IconButton(
          tooltip: '${_visivel ? 'Ocultar' : 'Exibir'} CPF de ${widget.nome}',
          isSelected: _visivel,
          icon: Icon(
            _visivel
                ? Icons.visibility_off_outlined
                : Icons.visibility_outlined,
          ),
          onPressed: () => setState(() => _visivel = !_visivel),
        ),
      ],
    );
  }
}

/// Situação do cadastro. Texto e cor juntos: a cor sozinha não carrega a informação.
class EtiquetaSituacao extends StatelessWidget {
  const EtiquetaSituacao({super.key, required this.ativo});

  final bool ativo;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final (frente, fundo) = ativo
        ? (cores.sucesso, cores.sucessoFundo)
        : (cores.alerta, cores.alertaFundo);
    return DecoratedBox(
      decoration: BoxDecoration(
        color: fundo,
        borderRadius: BorderRadius.circular(TokensDimensoes.radius16),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: TokensDimensoes.space8,
          vertical: TokensDimensoes.space4,
        ),
        child: Text(
          ativo ? 'Ativo' : 'Inativo',
          style: Theme.of(context).textTheme.labelMedium
              ?.copyWith(color: frente),
        ),
      ),
    );
  }
}

/// Um cadastro da lista: nome, situação, CPF oculto, nascimento, e-mail e "Editar". Nunca mostra
/// credenciais (a API não as devolve em listas). [aoEditar] nulo desabilita a edição (modo offline
/// ou cadastro sem id).
class CartaoUsuario extends StatelessWidget {
  const CartaoUsuario({super.key, required this.usuario, this.aoEditar});

  final Usuario usuario;
  final VoidCallback? aoEditar;

  @override
  Widget build(BuildContext context) {
    final nascimento = usuario.dataNascimento;
    return Card(
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(TokensDimensoes.space16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Semantics(
                    header: true,
                    child: Text(
                      usuario.nome,
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                  ),
                ),
                const SizedBox(width: TokensDimensoes.space8),
                EtiquetaSituacao(ativo: usuario.ativo),
              ],
            ),
            LinhaDado(
              rotulo: 'CPF',
              valor: CpfOculto(cpf: usuario.cpf, nome: usuario.nome),
            ),
            LinhaDado.texto(
              rotulo: 'Nascimento',
              texto: nascimento == null
                  ? naoInformado
                  : formatarData(nascimento),
            ),
            LinhaDado.texto(
              rotulo: 'E-mail',
              texto: usuario.email ?? naoInformado,
            ),
            const SizedBox(height: TokensDimensoes.space12),
            Align(
              alignment: Alignment.centerRight,
              child: OutlinedButton.icon(
                onPressed: aoEditar,
                icon: const Icon(Icons.edit_outlined),
                label: Text('Editar', semanticsLabel: 'Editar ${usuario.nome}'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Corpo comum das listas de supervisores e colaboradores: explicação, botão de cadastro (desligado
/// no modo offline, com o motivo), a lista ou o estado vazio.
class ListaDeCadastros extends StatelessWidget {
  const ListaDeCadastros({
    super.key,
    required this.descricao,
    required this.rotuloCadastrar,
    required this.aoCadastrar,
    required this.usuarios,
    required this.somenteLeitura,
    required this.vazio,
    required this.aoEditar,
  });

  final String descricao;
  final String rotuloCadastrar;
  final VoidCallback aoCadastrar;
  final List<Usuario> usuarios;
  final bool somenteLeitura;
  final Widget vazio;
  final ValueChanged<Usuario> aoEditar;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          descricao,
          style: textos.bodyMedium?.copyWith(
            color: context.cores.textoSecundario,
          ),
        ),
        const SizedBox(height: TokensDimensoes.space12),
        FilledButton.icon(
          // Cadastro precisa do servidor: sem conexão, fica desabilitado (RNF08).
          onPressed: somenteLeitura ? null : aoCadastrar,
          icon: const Icon(Icons.add),
          label: Text(rotuloCadastrar),
        ),
        if (somenteLeitura) ...[
          const SizedBox(height: TokensDimensoes.space8),
          Text(
            'Cadastros e alterações voltam quando a conexão voltar.',
            style: textos.bodyMedium?.copyWith(
              color: context.cores.textoSecundario,
            ),
          ),
        ],
        const SizedBox(height: TokensDimensoes.space16),
        if (usuarios.isEmpty)
          vazio
        else ...[
          Semantics(
            liveRegion: true,
            child: Text(
              usuarios.length == 1
                  ? '1 cadastro'
                  : '${usuarios.length} cadastros',
              style: textos.bodySmall,
            ),
          ),
          const SizedBox(height: TokensDimensoes.space8),
          for (final usuario in usuarios) ...[
            CartaoUsuario(
              usuario: usuario,
              aoEditar: somenteLeitura || usuario.id == null
                  ? null
                  : () => aoEditar(usuario),
            ),
            const SizedBox(height: TokensDimensoes.space12),
          ],
        ],
      ],
    );
  }
}

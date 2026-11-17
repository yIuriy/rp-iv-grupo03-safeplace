# SafePlace mobile (Flutter)

Cliente Android e iOS do SafePlace. Usa o mesmo backend do projeto (`/api`), sem alterações nele. Preparação da máquina: `docs/flutter/configuracao-ambiente.md`.

```bash
flutter pub get
flutter run                                              # emulador Android, API em 10.0.2.2:8080
flutter run --dart-define=API_URL=http://localhost:8080/api   # celular por USB, após adb reverse tcp:8080 tcp:8080
dart format . && flutter analyze && flutter test
```

## Estrutura

```
lib/
  app/        composição: router (a casca por perfil entra aqui na #316)
  core/       config, cliente HTTP, erros, sessão; não importa nenhuma feature
  shared/     widgets e cache offline (#316)
  features/<modulo>/
    domain/        interfaces (portas) e validação local
    data/          modelos (X.ler) e implementações que chamam a API
    presentation/  telas e diálogos
```

Regras de dependência: `features` → `core`; `core` nunca → `features`; só `app/` junta as features. Só `core/api/dio_api_client.dart` conhece o dio.

## Interfaces para as features

**Cliente HTTP** (`core/api/api_client.dart`): dependa da interface `ApiClient`, lida pelo `apiClientProvider` (`core/api/api_client_provider.dart`). Os métodos (`get`, `post`, `put`, `patch`, `delete`) recebem caminhos relativos a `/api`, devolvem o corpo JSON sem tipo e lançam `FalhaApi` (`status`, `corpo`; `status` nulo = sem resposta). O token vai sozinho; um 401 fora do login encerra a sessão e volta ao login, então a tela não trata 401. Se a feature precisar de algo novo (download binário dos relatórios, na #326), acrescente o método na interface e no `DioApiClient`.

Cada feature declara a sua porta em `domain/` e a implementa em `data/`, como `Autenticador` e `AutenticacaoApi`:

```dart
// domain/epis_repositorio.dart
abstract interface class EpisRepositorio {
  Future<List<Epi>> listar();
}

// data/epis_api.dart
class EpisApi implements EpisRepositorio {
  const EpisApi(this._client);
  final ApiClient _client;

  @override
  Future<List<Epi>> listar() async =>
      lista(await _client.get('/epis'), Epi.ler, 'Lista de EPIs inválida');
}

final episRepositorioProvider =
    Provider<EpisRepositorio>((ref) => EpisApi(ref.read(apiClientProvider)));
```

**Leitura das respostas** (`core/api/leitura.dart`): `objeto`, `lista`, `listaOuVazia`, `texto`, `textoOuNulo`, `numeroOuNulo`. Cada modelo tem um `factory X.ler(Object? dado)` que usa essas funções; campo opcional ausente não derruba a tela.

**Mensagens de erro** (`core/api/erros.dart`): cada tela cria o seu descritor e sobrescreve só os status que precisa. 400, 404, 409 e 422 já usam o `message` do backend; falha de rede vira aviso de conexão. Para os campos extras de um 409 ou 422, leia `(erro as FalhaApi).corpo`.

```dart
final _descrever = criarDescritorDeFalha(
  porStatus: {409: usarMensagemDaApi('Já existe um colaborador com esse CPF.')},
  padrao: 'Não foi possível salvar o colaborador.',
);
// catch (erro) { setState(() => _falha = _descrever(erro)); }
```

**Sessão** (`core/sessao/`): `ref.watch(controladorSessaoProvider).value?.sessao` devolve `Sessao` (`token`, `email`, `nome`, `perfil`, `ehGestor`) ou `null`. Use `perfil` para mostrar ou esconder ações; o backend continua sendo quem autoriza. `iniciar(sessao)` abre a sessão (o login chama depois de autenticar) e `sair()` a encerra. O cache offline registra a limpeza da conta sobrescrevendo `limpezaDaContaProvider`.

## Tema e componentes compartilhados (#316)

**Tema** (`shared/tema/`): `AppTheme.claro()` monta o `ThemeData` a partir de `tokens.g.dart`, gerado do `frontend/design-system/tokens.json`. Depois de mudar o `tokens.json`, rode `dart run tool/gerar_tokens.dart`; um teste falha se esquecer. Não use cor nem medida solta nas telas:

- cores do Material: `Theme.of(context).colorScheme` (`primary` é o pinho, `error` o vermelho);
- cores que o Material não tem: `context.cores` (`alerta`, `perigoFundo`, `destaque`, `textoSecundario`...);
- medidas: `TokensDimensoes.space16`, `radius8`, `sizeControl`...;
- texto: `Theme.of(context).textTheme` (`headlineMedium` = Heading MD em Manrope, `bodyLarge` = Body MD em Inter, `labelLarge` = Label MD).

Botões, como no web: `FilledButton` (Primary, confirma), `OutlinedButton` (Secondary), `TextButton` (Quiet, cancela) e `FilledButton(style: AppTheme.botaoPerigo)` (Danger, operação destrutiva).

**Componentes** (`shared/widgets/` e `shared/formularios/`):

| Widget | Para quê |
| --- | --- |
| `Aviso(tom:, titulo:, mensagem:, anunciar:, rotuloAcao:, aoAgir:)` | Resultado de operação, falha com "Tentar novamente", alerta fixo (`TomAviso.info`, `sucesso`, `alerta`, `perigo`) |
| `AvisoOffline(sincronizadoEm:)` | Faixa de modo offline no topo da tela que mostra cache |
| `confirmar(context, titulo:, descricao:, rotuloConfirmar:, aoConfirmar:, descreverFalha:)` | Confirmação antes de descartar, remover ou arquivar; com `aoConfirmar`, executa a operação mostrando carregando e o erro dentro do diálogo |
| `CampoTexto(rotulo:, validador:, erroDoServidor:, linhas:, somenteLeitura:, habilitado:)` | Texto e área de texto; `erroDoServidor` mostra o erro de um 409/422 no campo certo e some quando a pessoa edita |
| `CampoSelecao<T>(rotulo:, opcoes: [(valor:, rotulo:)], aoMudar:)` | Poucas opções fixas (nível de perigo, status) |
| `SeletorComBusca<T>(rotulo:, opcoes:, descrever:, aoMudar:, validator:, carregando:, falha:)` | Listas longas (colaborador, EPI, área): abre uma lista com busca que ignora acentos |
| `ConteudoAssincrono<T>(valor: ref.watch(provider), construir:, vazio:, falha:, aoTentarDeNovo:)` | Os quatro estados de uma consulta: carregando, falha, vazia e com dados |
| `EstadoVazio`, `EstadoCarregando` | Lista sem resultado (com ação opcional) e carregamento com texto |
| `MascaraCpf`, `validarCpf`, `digitosDoCpf`, `ocultarCpf` | CPF `000.000.000-00` na tela, 11 dígitos no envio, mesmas mensagens do web |

**Catálogo**: em debug, `flutter run --route=/catalogo` abre todos os componentes e seus estados, sem precisar de login. No Git Bash, use `MSYS_NO_PATHCONV=1 flutter run --route=/catalogo`, senão o `/catalogo` vira caminho do Windows. Para testar uma tela com o tema, use `montarComTema(tester, widget)` de `test/shared/apoio_tema.dart`.

**Testes**: `test/apoio.dart` tem `sessaoGestor`, `falhaHttp(status, corpo)` e `ArmazenamentoSessaoEmMemoria`. Teste a classe de `data/` com um mock de `ApiClient` e as telas sobrescrevendo o provider da porta da feature com um mock (`mocktail`), mais `armazenamentoSessaoProvider` com `ArmazenamentoSessaoEmMemoria(sessao)`. `AdaptadorFalso` serve só para testar o `DioApiClient`.

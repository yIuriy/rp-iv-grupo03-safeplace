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
  core/       config, cliente HTTP, erros, sessão, router
  shared/     widgets e cache offline (#316)
  features/<modulo>/
    data/          modelos (X.ler) e chamadas da API
    domain/        validação local
    presentation/  telas e diálogos
```

## Interfaces para as features

**Cliente HTTP** (`core/api/api_client.dart`): leia o `Dio` pelo `apiClientProvider` e use caminhos relativos a `/api`. O token vai sozinho; um 401 fora do login encerra a sessão e volta ao login, então a tela não trata 401.

```dart
final epiApiProvider = Provider((ref) => EpiApi(ref.read(apiClientProvider)));

class EpiApi {
  const EpiApi(this._client);
  final Dio _client;

  Future<List<Epi>> listar() async {
    final r = await _client.get<Object?>('/epis');
    return lista(r.data, Epi.ler, 'Lista de EPIs inválida');
  }
}
```

**Leitura das respostas** (`core/api/leitura.dart`): `objeto`, `lista`, `listaOuVazia`, `texto`, `textoOuNulo`, `numeroOuNulo`. Cada modelo tem um `factory X.ler(Object? dado)` que usa essas funções; campo opcional ausente não derruba a tela.

**Mensagens de erro** (`core/api/erros.dart`): cada tela cria o seu descritor e sobrescreve só os status que precisa. 400, 404, 409 e 422 já usam o `message` do backend; falha de rede vira aviso de conexão. Para os campos extras de um 409 ou 422, use `corpoDoErro(erro)` ou `(erro as DioException).response?.data`.

```dart
final _descrever = criarDescritorDeFalha(
  porStatus: {409: usarMensagemDaApi('Já existe um colaborador com esse CPF.')},
  padrao: 'Não foi possível salvar o colaborador.',
);
// catch (erro) { setState(() => _falha = _descrever(erro)); }
```

**Sessão** (`core/sessao/`): `ref.watch(controladorSessaoProvider).value?.sessao` devolve `Sessao` (`token`, `email`, `nome`, `perfil`, `ehGestor`) ou `null`. Use `perfil` para mostrar ou esconder ações; o backend continua sendo quem autoriza. `sair()` encerra a sessão. O cache offline registra a limpeza da conta sobrescrevendo `limpezaDaContaProvider`.

**Testes**: `test/apoio.dart` tem `AdaptadorFalso` (respostas HTTP sem rede), `falhaHttp(status, corpo)` e `sessaoGestor`. Para telas, sobrescreva `armazenamentoSessaoProvider` com `ArmazenamentoSessaoEmMemoria(sessao)` e a API da feature com um mock (`mocktail`).

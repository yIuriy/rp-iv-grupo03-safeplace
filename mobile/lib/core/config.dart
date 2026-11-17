/// URL base da API, definida no build por `--dart-define=API_URL=...`.
///
/// O padrão `10.0.2.2` é o `localhost` da máquina de desenvolvimento visto de dentro do emulador
/// Android. Os outros cenários estão em `docs/flutter/configuracao-ambiente.md`.
const urlDaApi = String.fromEnvironment(
  'API_URL',
  defaultValue: 'http://10.0.2.2:8080/api',
);

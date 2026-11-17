# Ambiente de desenvolvimento do cliente Flutter

Passo a passo para preparar a máquina antes de pegar as issues do cliente Flutter (#315 a #326). O app fica em `mobile/` e usa o **mesmo backend** do projeto, sem alterações nele.

> **iOS:** compilar para iOS exige macOS com Xcode. No Windows e no Ubuntu, desenvolva e teste no **Android** (emulador ou celular). O código Dart é o mesmo para as duas plataformas.

## O que instalar

| Ferramenta | Para quê |
|---|---|
| Git | Clonar o repositório e o SDK |
| Flutter SDK (canal `stable`) | Compilar, analisar e testar o app; já inclui o Dart |
| Android Studio | Android SDK, ferramentas de linha de comando e emulador |
| Docker | Subir o backend e o PostgreSQL (já usado no projeto) |
| VS Code ou Android Studio | Editor, com as extensões **Flutter** e **Dart** |

## Ubuntu (22.04 ou 24.04)

### 1. Pacotes do sistema

```bash
sudo apt update
sudo apt install -y curl git unzip xz-utils zip libglu1-mesa
```

### 2. Flutter SDK

Escolha **uma** das opções.

**Opção A, snap (mais simples):**

```bash
sudo snap install flutter --classic
flutter --version
```

**Opção B, manual:**

```bash
mkdir -p ~/development
git clone https://github.com/flutter/flutter.git -b stable ~/development/flutter
echo 'export PATH="$HOME/development/flutter/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
flutter --version
```

Se você usa zsh, troque `~/.bashrc` por `~/.zshrc`.

### 3. Android Studio e Android SDK

```bash
sudo snap install android-studio --classic
```

1. Abra o Android Studio e conclua o assistente inicial (instala o Android SDK).
2. Em **Settings > Languages & Frameworks > Android SDK > SDK Tools**, marque **Android SDK Command-line Tools** e **Android SDK Platform-Tools** e aplique.
3. Aceite as licenças:

```bash
flutter doctor --android-licenses
```

### 4. Aceleração do emulador (KVM)

Sem KVM, o emulador fica muito lento.

```bash
sudo apt install -y qemu-kvm
sudo usermod -aG kvm $USER
```

Saia da sessão e entre de novo para o grupo `kvm` valer.

### 5. Docker

Se ainda não tiver, instale o Docker Engine e o plugin Compose pelo guia oficial (`docs.docker.com/engine/install/ubuntu`) e adicione seu usuário ao grupo `docker`.

## Windows (10 ou 11, 64 bits)

### 1. Git

Instale o **Git for Windows** (`git-scm.com`). Use o PowerShell ou o Git Bash nos comandos abaixo.

### 2. Flutter SDK

1. Baixe o zip do canal `stable` em `docs.flutter.dev/get-started/install/windows`, ou clone:

   ```powershell
   git clone https://github.com/flutter/flutter.git -b stable C:\src\flutter
   ```

2. Use um caminho **sem espaços e sem acentos**, como `C:\src\flutter`. Não use `C:\Program Files`.
3. Adicione `C:\src\flutter\bin` ao **Path** do usuário: menu Iniciar > "Editar as variáveis de ambiente para sua conta" > `Path` > Novo.
4. Abra um **novo** terminal e confira:

   ```powershell
   flutter --version
   ```

### 3. Android Studio e Android SDK

1. Instale o Android Studio (`developer.android.com/studio`) e conclua o assistente inicial.
2. Em **Settings > Languages & Frameworks > Android SDK > SDK Tools**, marque **Android SDK Command-line Tools** e **Android SDK Platform-Tools** e aplique.
3. Aceite as licenças:

   ```powershell
   flutter doctor --android-licenses
   ```

### 4. Aceleração do emulador

Ative a **Plataforma do Hipervisor do Windows** em "Ativar ou desativar recursos do Windows" e reinicie. A virtualização também precisa estar ligada na BIOS/UEFI (Intel VT-x ou AMD-V).

### 5. Docker

Instale o **Docker Desktop** com o backend WSL 2.

### 6. Celular físico (opcional)

Para depurar por USB no Windows, instale o driver USB do fabricante (ou o **Google USB Driver**, em SDK Tools).

## Os dois sistemas

### 1. Conferir o ambiente

```bash
flutter doctor
```

As linhas **Flutter** e **Android toolchain** precisam estar com ✓. Avisos de Chrome, Linux desktop, Visual Studio ou Xcode podem ser ignorados: o app é só Android e iOS.

### 2. Criar o emulador

No Android Studio, abra **Device Manager > Create Virtual Device**, escolha um Pixel com imagem de sistema recente (API 34 ou superior, `x86_64`) e inicie. Confira:

```bash
flutter devices
```

### 3. Editor

No VS Code, instale as extensões **Flutter** e **Dart** (Dart Code). No Android Studio, instale o plugin **Flutter** (que traz o Dart).

### 4. Subir o backend

Siga a seção de execução do `README.md` da raiz: criar o `.env` com a `JWT_SECRET` e subir com Docker.

```bash
docker compose up -d postgres
docker compose up -d --build backend
```

O backend responde em `http://localhost:8080`. Swagger em `http://localhost:8080/docs`.

### 5. Endereço da API no app

O app recebe a URL da API por `--dart-define=API_URL=...`.

| Onde o app roda | `API_URL` |
|---|---|
| Emulador Android | `http://10.0.2.2:8080/api` (padrão; `10.0.2.2` é o `localhost` da sua máquina visto do emulador) |
| Celular por USB | `http://localhost:8080/api`, depois de rodar `adb reverse tcp:8080 tcp:8080` |
| Celular pelo Wi-Fi | `http://<IP-da-sua-máquina>:8080/api`, na mesma rede; no Windows, libere a porta 8080 no firewall |

O backend usa HTTP sem TLS em desenvolvimento. O projeto `mobile/` já libera tráfego HTTP em modo debug; não altere isso para o build de release.

### 6. Rodar o app

Depois que a #315 estiver mesclada:

```bash
cd mobile
flutter pub get
flutter run
```

Contas de demonstração (Gestor e Supervisor): veja o primeiro comentário de `backend/src/main/resources/db/migration/V17__inserir_usuarios_demonstracao.sql`.

### 7. Antes de abrir o PR

```bash
cd mobile
dart format .
flutter analyze
flutter test
```

Os três precisam passar sem erros. Siga as regras do repositório: commits atômicos, mensagens no padrão `tipo(escopo): descrição (#issue)` e nenhuma alteração no backend.

## Problemas comuns

| Sintoma | Solução |
|---|---|
| `flutter: command not found` | O `bin` do Flutter não está no PATH; abra um terminal novo depois de ajustar. |
| `Android license status unknown` | `flutter doctor --android-licenses`. |
| `cmdline-tools component is missing` | Instale **Android SDK Command-line Tools** no SDK Manager. |
| Emulador muito lento ou não inicia | Ubuntu: KVM e grupo `kvm`. Windows: Plataforma do Hipervisor e virtualização na BIOS. |
| `Connection refused` ao logar | Backend fora do ar (`docker compose ps`) ou `API_URL` errada para o seu dispositivo (tabela acima). |
| `Cleartext HTTP traffic not permitted` | Rodando em release com HTTP; use `flutter run` (debug). |
| Login devolve 401 | Conta ou senha erradas, ou banco sem a migração V17 aplicada. |

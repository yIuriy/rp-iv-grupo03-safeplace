#!/usr/bin/env python3
"""
Coletor de métricas de código do SafePlace.

Mede apenas arquivos versionados (git ls-files), para que duas execuções em commits diferentes
sejam comparáveis. Usa só a biblioteca padrão do Python.

Uso:
    python scripts/metricas/coletar_metricas.py --saida metricas.json
    python scripts/metricas/coletar_metricas.py --comparar base.json atual.json

Resultados de testes e cobertura são lidos de backend/target (surefire e JaCoCo) se existirem;
rode `./mvnw test` no backend antes para preenchê-los. ESLint e build do frontend são executados
quando --frontend-ferramentas é informado (exige node_modules instalado).
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
PACOTE_BASE = "br/edu/safeplace/backend/"
JAVA_MAIN = "backend/src/main/java/"
JAVA_TEST = "backend/src/test/java/"

PALAVRAS_NAO_METODO = {"if", "for", "while", "switch", "catch", "synchronized", "return", "new",
                       "else", "do", "try", "throw", "case", "super", "this"}
LIMITE_METODO_LONGO = 30
LIMITE_CLASSE_GRANDE = 300
LIMITE_PARAMETROS = 5
JANELA_DUPLICACAO = 6


# ---------------------------------------------------------------- utilidades

def git(*args: str) -> str:
    return subprocess.run(["git", *args], cwd=RAIZ, capture_output=True, text=True,
                          encoding="utf-8", check=True).stdout


def arquivos_versionados(prefixo: str, extensoes: tuple[str, ...]) -> list[Path]:
    saida = git("ls-files", "-z", "--", prefixo)
    return sorted(RAIZ / p for p in saida.split("\0") if p and p.endswith(extensoes))


def ler(caminho: Path) -> str:
    return caminho.read_text(encoding="utf-8", errors="replace")


def remover_comentarios_e_strings(fonte: str) -> str:
    """Substitui comentários e literais por espaços, preservando quebras de linha."""
    saida, i, n = [], 0, len(fonte)
    while i < n:
        c = fonte[i]
        if fonte.startswith("//", i):
            fim = fonte.find("\n", i)
            fim = n if fim == -1 else fim
            saida.append(" " * (fim - i)); i = fim
        elif fonte.startswith("/*", i):
            fim = fonte.find("*/", i + 2)
            fim = n if fim == -1 else fim + 2
            saida.append("".join("\n" if ch == "\n" else " " for ch in fonte[i:fim])); i = fim
        elif fonte.startswith('"""', i):
            fim = fonte.find('"""', i + 3)
            fim = n if fim == -1 else fim + 3
            saida.append('""' + "".join("\n" if ch == "\n" else " " for ch in fonte[i + 2:fim - 2]) + '""'); i = fim
        elif c in "\"'":
            j = i + 1
            while j < n and fonte[j] != c and fonte[j] != "\n":
                j += 2 if fonte[j] == "\\" else 1
            saida.append(c + " " * max(0, j - i - 1) + c); i = j + 1
        else:
            saida.append(c); i += 1
    return "".join(saida)


def contar_linhas(fonte: str, comentario_linha: str = "//") -> dict:
    total = branco = comentario = 0
    em_bloco = False
    for linha in fonte.splitlines():
        total += 1
        s = linha.strip()
        if not s:
            branco += 1
            continue
        if em_bloco:
            comentario += 1
            if "*/" in s:
                em_bloco = False
            continue
        if s.startswith("/*"):
            comentario += 1
            em_bloco = "*/" not in s
            continue
        if comentario_linha and s.startswith(comentario_linha):
            comentario += 1
    return {"total": total, "codigo": total - branco - comentario, "comentario": comentario, "branco": branco}


def somar(dicts: list[dict]) -> dict:
    acumulado: Counter = Counter()
    for d in dicts:
        acumulado.update(d)
    return dict(acumulado)


# ---------------------------------------------------------------- Java

def modulo_de(pacote: str) -> str:
    """Agrupa pacotes no nível arquitetural: domain/epi, application/usecase, adapters/in/web..."""
    partes = pacote.split(".")
    if partes[:4] == ["br", "edu", "safeplace", "backend"]:
        partes = partes[4:]
    if not partes:
        return "(raiz)"
    if partes[0] in ("application", "adapters") and len(partes) >= 3 and partes[1] in ("port", "in", "out", "dto"):
        return "/".join(partes[:3])
    return "/".join(partes[:2])


RE_TIPO = re.compile(r"\b(?:(abstract)\s+)?(?:(?:public|protected|private|static|final|sealed|non-sealed)\s+)*"
                     r"(class|interface|enum|record|@interface)\s+([A-Z]\w*)")
RE_CABECALHO = re.compile(
    r"(?:(?:public|protected|private|static|final|synchronized|abstract|default|native)\s+)*"
    r"(?:<[^>]*>\s*)?([\w<>\[\],.?]+(?:\s*<[^{};]*?>)?\s+)?([a-zA-Z_]\w*)\s*\(((?:[^()]|\([^()]*\))*)\)\s*"
    r"(?:throws\s+[\w.,\s]+)?\{", re.S)
RE_DECISAO = re.compile(r"\bif\b|\bfor\b|\bwhile\b|\bcase\b|\bcatch\b|&&|\|\||\?(?![\s]*(?:extends|super)\b)(?=[^>]*[:;])")


def fechar_chave(texto: str, abre: int) -> int:
    profundidade = 0
    for i in range(abre, len(texto)):
        if texto[i] == "{":
            profundidade += 1
        elif texto[i] == "}":
            profundidade -= 1
            if profundidade == 0:
                return i
    return len(texto) - 1


def analisar_java(caminho: Path) -> dict:
    fonte = ler(caminho)
    limpo = remover_comentarios_e_strings(fonte)
    rel = caminho.relative_to(RAIZ).as_posix()
    pacote_m = re.search(r"^\s*package\s+([\w.]+)\s*;", limpo, re.M)
    pacote = pacote_m.group(1) if pacote_m else ""
    imports = re.findall(r"^\s*import\s+(?:static\s+)?([\w.]+)(?:\.\*)?\s*;", limpo, re.M)

    tipos = [{"tipo": "abstract class" if m.group(1) else m.group(2), "nome": m.group(3)}
             for m in RE_TIPO.finditer(limpo)]
    nomes_tipos = {t["nome"] for t in tipos}

    metodos = []
    for m in RE_CABECALHO.finditer(limpo):
        retorno, nome, params = (m.group(1) or "").strip(), m.group(2), m.group(3)
        if nome in PALAVRAS_NAO_METODO or retorno in PALAVRAS_NAO_METODO or retorno.startswith("new"):
            continue
        construtor = not retorno and nome in nomes_tipos
        if not retorno and not construtor:
            continue
        abre = m.end() - 1
        fecha = fechar_chave(limpo, abre)
        corpo = limpo[abre:fecha + 1]
        linhas_corpo = [l for l in corpo.splitlines() if l.strip() and l.strip() not in "{}"]
        parametros = [p for p in re.split(r",(?![^<]*>)", params) if p.strip()]
        metodos.append({
            "nome": nome, "construtor": construtor, "linhas": len(linhas_corpo),
            "parametros": len(parametros), "complexidade": 1 + len(RE_DECISAO.findall(corpo)),
            "linha": limpo.count("\n", 0, m.start()) + 1,
        })

    principal = tipos[0]["nome"] if tipos else caminho.stem
    return {
        "arquivo": rel, "pacote": pacote, "modulo": modulo_de(pacote), "imports": imports,
        "linhas": contar_linhas(fonte), "tipos": tipos, "metodos": metodos, "principal": principal,
        "limpo": limpo,
        "contagens": {
            "if": len(re.findall(r"\bif\s*\(", limpo)),
            "switch": len(re.findall(r"\bswitch\s*\(", limpo)),
            "instanceof": len(re.findall(r"\binstanceof\b", limpo)),
            "comparacao_null": len(re.findall(r"[!=]=\s*null\b|\bnull\s*[!=]=", limpo)),
            "security_context_holder": len(re.findall(r"\bSecurityContextHolder\b", limpo)),
            "requireNonNull": len(re.findall(r"\brequireNonNull\b", limpo)),
            "endpoints": len(re.findall(r"@(?:Get|Post|Put|Patch|Delete)Mapping\b", fonte)),
            "exception_handlers": len(re.findall(r"@ExceptionHandler\b", fonte)),
            "controller_advices": len(re.findall(r"@RestControllerAdvice\b", fonte)),
            "anotacao_service": len(re.findall(r"@Service\b", fonte)),
            "anotacao_transactional": len(re.findall(r"@Transactional\b", fonte)),
            "throw_new": len(re.findall(r"\bthrow\s+new\b", limpo)),
        },
    }


def duplicacao(arquivos: list[dict]) -> dict:
    """Percentual de linhas em blocos de JANELA_DUPLICACAO linhas normalizadas repetidos."""
    linhas_por_arquivo = []
    for a in arquivos:
        normalizadas = []
        for linha in a["limpo"].splitlines():
            s = re.sub(r"\s+", " ", linha.strip())
            if not s or s in ("{", "}", "});", ");", "};") or s.startswith(("import ", "package ", "@")):
                continue
            normalizadas.append(s)
        linhas_por_arquivo.append(normalizadas)
    ocorrencias = Counter()
    for linhas in linhas_por_arquivo:
        for i in range(len(linhas) - JANELA_DUPLICACAO + 1):
            ocorrencias[hashlib.md5("\n".join(linhas[i:i + JANELA_DUPLICACAO]).encode()).hexdigest()] += 1
    duplicadas = total = 0
    blocos_repetidos = sum(1 for v in ocorrencias.values() if v > 1)
    for linhas in linhas_por_arquivo:
        marcadas = [False] * len(linhas)
        for i in range(len(linhas) - JANELA_DUPLICACAO + 1):
            h = hashlib.md5("\n".join(linhas[i:i + JANELA_DUPLICACAO]).encode()).hexdigest()
            if ocorrencias[h] > 1:
                for k in range(i, i + JANELA_DUPLICACAO):
                    marcadas[k] = True
        duplicadas += sum(marcadas)
        total += len(linhas)
    return {"janela_linhas": JANELA_DUPLICACAO, "linhas_normalizadas": total, "linhas_duplicadas": duplicadas,
            "percentual": round(100 * duplicadas / total, 2) if total else 0.0,
            "blocos_repetidos_distintos": blocos_repetidos}


def acoplamento(arquivos: list[dict]) -> dict:
    """Ce/Ca/instabilidade por módulo, a partir dos imports internos (mesmo pacote não conta)."""
    eferente: dict[str, set] = defaultdict(set)
    aferente: dict[str, set] = defaultdict(set)
    modulos = {a["modulo"] for a in arquivos}
    for a in arquivos:
        for imp in a["imports"]:
            if not imp.startswith("br.edu.safeplace.backend."):
                continue
            destino = modulo_de(imp.rsplit(".", 1)[0])
            if destino != a["modulo"]:
                eferente[a["modulo"]].add(destino)
                aferente[destino].add(a["modulo"])
    resultado = {}
    for m in sorted(modulos):
        ce, ca = len(eferente[m]), len(aferente[m])
        resultado[m] = {"Ce": ce, "Ca": ca, "I": round(ce / (ce + ca), 2) if ce + ca else 0.0}
    return resultado


def violacoes_camadas(arquivos: list[dict]) -> dict:
    regras = {
        "domain_importa_framework": lambda a, i: a["modulo"].startswith("domain") and i.startswith(
            ("org.springframework", "jakarta.persistence", "jakarta.validation", "org.hibernate")),
        "domain_importa_application_ou_adapters": lambda a, i: a["modulo"].startswith("domain") and i.startswith(
            ("br.edu.safeplace.backend.application", "br.edu.safeplace.backend.adapters")),
        "application_importa_adapters": lambda a, i: a["modulo"].startswith("application") and i.startswith(
            "br.edu.safeplace.backend.adapters"),
        "application_importa_spring_security": lambda a, i: a["modulo"].startswith("application") and i.startswith(
            "org.springframework.security"),
        "adapters_in_importa_adapters_out": lambda a, i: a["modulo"].startswith("adapters/in") and i.startswith(
            "br.edu.safeplace.backend.adapters.out"),
    }
    saida = {}
    for nome, regra in regras.items():
        arquivos_violando = sorted({a["arquivo"] for a in arquivos for i in a["imports"] if regra(a, i)})
        saida[nome] = {"arquivos": len(arquivos_violando), "lista": arquivos_violando}
    return saida


def nomenclatura(arquivos: list[dict]) -> dict:
    nomes = [a["principal"] for a in arquivos]
    sufixos = ["Port", "Porta", "UseCase", "UseCaseImpl", "CasoDeUso", "Service", "Controller", "Controlador",
               "Adapter", "Adaptador", "Request", "Requisicao", "Response", "Resposta", "ExceptionHandler",
               "TratadorExcecoes", "InputDTO", "OutputDTO", "EntradaDTO", "SaidaDTO", "Entity", "Entidade"]
    return {s: sum(1 for n in nomes if n.endswith(s)) for s in sufixos}


def metricas_java(prefixo: str) -> tuple[dict, list[dict]]:
    arquivos = [analisar_java(p) for p in arquivos_versionados(prefixo, (".java",))]
    metodos = [dict(m, arquivo=a["arquivo"]) for a in arquivos for m in a["metodos"]]
    tipos = Counter(t["tipo"] for a in arquivos for t in a["tipos"])
    complexidades = [m["complexidade"] for m in metodos if not m["construtor"]]
    construtores_por_classe = Counter()
    for a in arquivos:
        construtores_por_classe[a["arquivo"]] = sum(1 for m in a["metodos"] if m["construtor"])
    por_modulo = defaultdict(lambda: {"arquivos": 0, "linhas_codigo": 0, "metodos": 0, "complexidade_total": 0})
    for a in arquivos:
        pm = por_modulo[a["modulo"]]
        pm["arquivos"] += 1
        pm["linhas_codigo"] += a["linhas"]["codigo"]
        pm["metodos"] += len(a["metodos"])
        pm["complexidade_total"] += sum(m["complexidade"] for m in a["metodos"])
    camada = defaultdict(lambda: {"arquivos": 0, "linhas_codigo": 0})
    for a in arquivos:
        c = camada[a["modulo"].split("/")[0]]
        c["arquivos"] += 1
        c["linhas_codigo"] += a["linhas"]["codigo"]

    def top(itens, chave, n=10):
        return sorted(itens, key=chave, reverse=True)[:n]

    resumo = {
        "arquivos": len(arquivos),
        "linhas": somar([a["linhas"] for a in arquivos]),
        "tipos": dict(tipos),
        "metodos": len([m for m in metodos if not m["construtor"]]),
        "construtores": len([m for m in metodos if m["construtor"]]),
        "complexidade_metodos": {
            "total": sum(complexidades),
            "media": round(sum(complexidades) / len(complexidades), 2) if complexidades else 0,
            "maxima": max(complexidades, default=0),
            "acima_de_10": sum(1 for c in complexidades if c > 10),
            "acima_de_5": sum(1 for c in complexidades if c > 5),
        },
        "metodos_longos": sum(1 for m in metodos if m["linhas"] > LIMITE_METODO_LONGO),
        "metodos_muitos_parametros": sum(1 for m in metodos if m["parametros"] > LIMITE_PARAMETROS),
        "classes_grandes": sum(1 for a in arquivos if a["linhas"]["codigo"] > LIMITE_CLASSE_GRANDE),
        "classes_3_ou_mais_construtores": sum(1 for v in construtores_por_classe.values() if v >= 3),
        "contagens": somar([a["contagens"] for a in arquivos]),
        "por_camada": dict(sorted(camada.items())),
        "por_modulo": dict(sorted(por_modulo.items())),
        "acoplamento": acoplamento(arquivos),
        "violacoes_camadas": violacoes_camadas(arquivos),
        "duplicacao": duplicacao(arquivos),
        "nomenclatura_sufixos": nomenclatura(arquivos),
        "top_arquivos_maiores": [{"arquivo": a["arquivo"], "linhas_codigo": a["linhas"]["codigo"],
                                  "metodos": len(a["metodos"])}
                                 for a in top(arquivos, lambda a: a["linhas"]["codigo"])],
        "top_metodos_complexos": [{"metodo": f'{Path(m["arquivo"]).stem}.{m["nome"]}', "linha": m["linha"],
                                   "complexidade": m["complexidade"], "linhas": m["linhas"]}
                                  for m in top(metodos, lambda m: m["complexidade"])],
        "top_metodos_longos": [{"metodo": f'{Path(m["arquivo"]).stem}.{m["nome"]}', "linha": m["linha"],
                                "linhas": m["linhas"], "complexidade": m["complexidade"]}
                               for m in top(metodos, lambda m: m["linhas"])],
        "top_mais_construtores": [{"arquivo": Path(k).stem, "construtores": v}
                                  for k, v in construtores_por_classe.most_common(10) if v >= 2],
    }
    return resumo, arquivos


def metricas_testes_java(arquivos_teste: list[dict]) -> dict:
    texto = {a["arquivo"]: ler(RAIZ / a["arquivo"]) for a in arquivos_teste}
    integracao = [f for f, t in texto.items() if "@EnabledIf(\"bancoDisponivel\")" in t or "PostgresDeTeste" in t]
    return {
        "classes_de_teste": sum(1 for t in texto.values() if re.search(r"@(Test|ParameterizedTest)\b", t)),
        "metodos_test": sum(len(re.findall(r"@Test\b", t)) for t in texto.values()),
        "metodos_parameterized": sum(len(re.findall(r"@ParameterizedTest\b", t)) for t in texto.values()),
        "classes_integracao_postgres": len([f for f in integracao if "suporte/" not in f]),
        "classes_webmvctest": sum(1 for t in texto.values() if "@WebMvcTest" in t),
        "classes_springboottest": sum(1 for t in texto.values() if "@SpringBootTest" in t),
        "usos_mockito_mock": sum(len(re.findall(r"\bmock\(|@Mock\b|@MockitoBean\b", t)) for t in texto.values()),
    }


def resultados_surefire() -> dict | None:
    pasta = RAIZ / "backend/target/surefire-reports"
    arquivos = list(pasta.glob("TEST-*.xml")) if pasta.exists() else []
    if not arquivos:
        return None
    total = Counter()
    for arq in arquivos:
        raiz = ET.parse(arq).getroot()
        for campo in ("tests", "failures", "errors", "skipped"):
            total[campo] += int(raiz.get(campo, 0))
        total["tempo_s"] += float(raiz.get("time", 0))
    total["tempo_s"] = round(total["tempo_s"], 1)
    total["classes"] = len(arquivos)
    return dict(total)


def cobertura_jacoco() -> dict | None:
    caminho = RAIZ / "backend/target/site/jacoco/jacoco.csv"
    if not caminho.exists():
        return None
    contadores = ["INSTRUCTION", "BRANCH", "LINE", "COMPLEXITY", "METHOD"]
    total = Counter()
    por_pacote = defaultdict(Counter)
    classes = 0
    with caminho.open(encoding="utf-8") as f:
        for linha in csv.DictReader(f):
            classes += 1
            pacote = modulo_de(linha["PACKAGE"])
            for c in contadores:
                for tipo in ("MISSED", "COVERED"):
                    valor = int(linha[f"{c}_{tipo}"])
                    total[f"{c}_{tipo}"] += valor
                    por_pacote[pacote][f"{c}_{tipo}"] += valor

    def pct(cnt, c):
        coberto, perdido = cnt[f"{c}_COVERED"], cnt[f"{c}_MISSED"]
        return round(100 * coberto / (coberto + perdido), 1) if coberto + perdido else None

    return {
        "classes_medidas": classes,
        "percentual": {c.lower(): pct(total, c) for c in contadores},
        "complexidade_ciclomatica_bytecode": total["COMPLEXITY_COVERED"] + total["COMPLEXITY_MISSED"],
        "por_modulo_linhas_pct": {p: pct(c, "LINE") for p, c in sorted(por_pacote.items())},
    }


# ---------------------------------------------------------------- frontend

def metricas_frontend(ferramentas: bool) -> dict:
    fontes = arquivos_versionados("frontend/src", (".ts", ".tsx"))
    estilos = arquivos_versionados("frontend/src", (".css",))
    testes = arquivos_versionados("frontend/tests", (".ts", ".tsx"))
    textos = {p: ler(p) for p in fontes}
    tudo = "\n".join(textos.values())
    componentes = len(re.findall(r"^(?:export\s+)?(?:default\s+)?function\s+[A-Z]\w*\s*\(", tudo, re.M)) + \
        len(re.findall(r"^(?:export\s+)?const\s+[A-Z]\w*\s*[:=][^\n]*=>", tudo, re.M))
    maiores = sorted(((p.relative_to(RAIZ).as_posix(), contar_linhas(t)["codigo"]) for p, t in textos.items()),
                     key=lambda x: x[1], reverse=True)[:10]
    texto_testes = "\n".join(ler(p) for p in testes)
    resultado = {
        "arquivos_ts_tsx": len(fontes),
        "arquivos_tsx": sum(1 for p in fontes if p.suffix == ".tsx"),
        "linhas_ts_tsx": somar([contar_linhas(t) for t in textos.values()]),
        "linhas_css": somar([contar_linhas(ler(p), comentario_linha="") for p in estilos]),
        "features": len({p.relative_to(RAIZ / "frontend/src/features").parts[0]
                         for p in fontes if "features" in p.parts}),
        "componentes": componentes,
        "hooks_customizados": len(re.findall(r"^export\s+function\s+use[A-Z]\w*", tudo, re.M)),
        "useState": len(re.findall(r"\buseState\b\s*[<(]", tudo)),
        "useEffect": len(re.findall(r"\buseEffect\s*\(", tudo)),
        "tipo_any": len(re.findall(r":\s*any\b|<any>|as any\b", tudo)),
        "top_arquivos_maiores": [{"arquivo": a, "linhas_codigo": l} for a, l in maiores],
        "testes_playwright": {"arquivos_spec": sum(1 for p in testes if p.name.endswith(".spec.ts")),
                              "casos": len(re.findall(r"^\s*test\(", texto_testes, re.M))},
    }
    if ferramentas:
        resultado["eslint"] = executar_eslint()
        resultado["build"] = executar_build()
    return resultado


def executar_npx(args: list[str]) -> subprocess.CompletedProcess:
    comando = ["npx.cmd" if sys.platform == "win32" else "npx", *args]
    return subprocess.run(comando, cwd=RAIZ / "frontend", capture_output=True, text=True, encoding="utf-8")


def executar_eslint() -> dict:
    proc = executar_npx(["eslint", ".", "-f", "json"])
    try:
        dados = json.loads(proc.stdout)
    except json.JSONDecodeError:
        return {"erro": proc.stderr.strip()[:300]}
    regras = Counter(m.get("ruleId") or "parse" for arq in dados for m in arq["messages"])
    return {"erros": sum(a["errorCount"] for a in dados), "avisos": sum(a["warningCount"] for a in dados),
            "por_regra": dict(regras.most_common())}


def executar_build() -> dict:
    tsc = executar_npx(["tsc", "-b"])
    erros_tsc = len(re.findall(r"error TS\d+", tsc.stdout + tsc.stderr))
    vite = executar_npx(["vite", "build"])
    dist = RAIZ / "frontend/dist/assets"
    tamanhos = {}
    if vite.returncode == 0 and dist.exists():
        for ext in ("js", "css"):
            tamanhos[f"{ext}_kb"] = round(sum(p.stat().st_size for p in dist.glob(f"*.{ext}")) / 1024, 1)
    return {"erros_tsc": erros_tsc, "vite_ok": vite.returncode == 0, **tamanhos}


# ---------------------------------------------------------------- coleta e comparação

def coletar(ferramentas: bool) -> dict:
    main_resumo, _ = metricas_java(JAVA_MAIN)
    _, arquivos_teste = metricas_java(JAVA_TEST)
    commit = git("rev-parse", "HEAD").strip()
    tag = subprocess.run(["git", "describe", "--tags", "--exact-match"], cwd=RAIZ, capture_output=True,
                         text=True).stdout.strip() or None
    return {
        "meta": {"commit": commit, "tag": tag, "branch": git("rev-parse", "--abbrev-ref", "HEAD").strip(),
                 "coletado_em": datetime.now(timezone.utc).isoformat(timespec="seconds"),
                 "commits_ate_aqui": int(git("rev-list", "--count", "HEAD").strip()),
                 "limites": {"metodo_longo_linhas": LIMITE_METODO_LONGO,
                             "classe_grande_linhas": LIMITE_CLASSE_GRANDE,
                             "parametros": LIMITE_PARAMETROS}},
        "backend_main": main_resumo,
        "backend_testes": {"linhas": somar([a["linhas"] for a in arquivos_teste]),
                           "arquivos": len(arquivos_teste), **metricas_testes_java(arquivos_teste),
                           "resultado_ultima_execucao": resultados_surefire()},
        "cobertura_jacoco": cobertura_jacoco(),
        "migracoes_flyway": len(arquivos_versionados("backend/src/main/resources/db/migration", (".sql",))),
        "frontend": metricas_frontend(ferramentas),
    }


def achatar(d, prefixo=""):
    saida = {}
    for k, v in d.items():
        chave = f"{prefixo}{k}"
        if isinstance(v, dict):
            saida.update(achatar(v, chave + "."))
        elif isinstance(v, (int, float)) and not isinstance(v, bool):
            saida[chave] = v
    return saida


def comparar(base: dict, atual: dict) -> str:
    a, b = achatar(base), achatar(atual)
    ignorar = ("meta.", ".por_modulo.", ".acoplamento.", "por_modulo_linhas_pct", ".por_regra.")
    linhas = [f"Base: {base['meta'].get('tag') or base['meta']['commit'][:7]}  →  "
              f"Atual: {atual['meta'].get('tag') or atual['meta']['commit'][:7]}", "",
              "| Métrica | Base | Atual | Δ |", "| --- | ---: | ---: | ---: |"]
    for chave in sorted(set(a) | set(b)):
        if any(i in chave for i in ignorar):
            continue
        va, vb = a.get(chave), b.get(chave)
        if va == vb:
            continue
        delta = "" if va is None or vb is None else f"{vb - va:+g}"
        linhas.append(f"| {chave} | {va if va is not None else '—'} | {vb if vb is not None else '—'} | {delta} |")
    return "\n".join(linhas)


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")  # consoles Windows usam cp1252 por padrão
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--saida", type=Path, help="arquivo JSON de saída (padrão: stdout)")
    parser.add_argument("--frontend-ferramentas", action="store_true", help="executa ESLint, tsc e vite build")
    parser.add_argument("--comparar", nargs=2, type=Path, metavar=("BASE", "ATUAL"),
                        help="compara dois JSON gerados por este script e imprime tabela Markdown")
    args = parser.parse_args()
    if args.comparar:
        base, atual = (json.loads(p.read_text(encoding="utf-8")) for p in args.comparar)
        print(comparar(base, atual))
        return
    dados = coletar(args.frontend_ferramentas)
    texto = json.dumps(dados, ensure_ascii=False, indent=2)
    if args.saida:
        args.saida.parent.mkdir(parents=True, exist_ok=True)
        args.saida.write_text(texto + "\n", encoding="utf-8")
    else:
        print(texto)


if __name__ == "__main__":
    main()

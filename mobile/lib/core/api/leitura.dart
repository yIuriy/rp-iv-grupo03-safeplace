// Primitivas de leitura das respostas da API (Adapter), espelhando `frontend/src/shared/api/leitura.ts`.
// Os construtores `X.ler` de cada feature usam estas funções para que o mesmo campo seja lido do
// mesmo jeito em todas as telas e um campo ausente não derrube a tela.

/// Objeto JSON já conferido, com os campos ainda sem tipo.
typedef Bruto = Map<String, Object?>;

/// Texto com conteúdo, como veio. Vazio, só espaços, ausente ou de outro tipo vira `null`.
String? textoOuNulo(Object? valor) =>
    valor is String && valor.trim().isNotEmpty ? valor : null;

/// Como [textoOuNulo], mas com a reserva da tela no lugar de `null`.
String texto(Object? valor, String reserva) => textoOuNulo(valor) ?? reserva;

/// Número como veio. Ausência e qualquer outro tipo (inclusive texto numérico) viram `null`.
num? numeroOuNulo(Object? valor) => valor is num ? valor : null;

/// Data (`aaaa-mm-dd`, o `LocalDate` do backend) ou data e hora ISO (`LocalDateTime`), no horário
/// local. Ausência, outro tipo ou texto fora do formato viram `null`.
DateTime? dataOuNula(Object? valor) =>
    valor is String ? DateTime.tryParse(valor.trim()) : null;

/// Objeto JSON. Fora disso, lança [FormatException]: a resposta não segue o contrato da API.
Bruto objeto(Object? dado, String mensagem) {
  if (dado is Map<String, Object?>) return dado;
  throw FormatException(mensagem);
}

/// Lista obrigatória, com cada item passando por [ler]. Se não for lista, lança [FormatException].
List<T> lista<T>(Object? dado, T Function(Object? item) ler, String mensagem) {
  if (dado is! List) throw FormatException(mensagem);
  return dado.map(ler).toList();
}

/// Lista opcional: ausência ou outro tipo viram lista vazia.
List<T> listaOuVazia<T>(Object? dado, T Function(Object? item) ler) =>
    dado is List ? dado.map(ler).toList() : <T>[];

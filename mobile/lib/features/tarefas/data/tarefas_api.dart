// Leituras de `/api/tarefas` com cache offline somente leitura por conta (RNF08). Camada de lista
// publicada primeiro (#324) para capacitações (#325) e relatórios (#326):
//
// ```dart
// ref.watch(tarefasProvider(null))             // GET /api/tarefas
// ref.watch(tarefasProvider(NivelPerigo.alto)) // GET /api/tarefas?nivelPerigo=ALTO
// ref.watch(tarefaProvider(3))                 // GET /api/tarefas/3
// ```
//
// Gestor e Supervisor consultam; cada provider devolve `Consultado<T>` para o `ConteudoComCache`.

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../shared/offline/cache_offline.dart';
import 'tarefa.dart';

/// Matriz de periculosidade, filtrada no servidor pelo grau (`null` traz todas).
final tarefasProvider = FutureProvider.autoDispose
    .family<Consultado<List<Tarefa>>, NivelPerigo?>(
      (ref, nivel) => consultarComCache(
        ref,
        chave: nivel == null
            ? 'tarefas'
            : 'tarefas?nivelPerigo=${nivel.codigo}',
        buscar: (api) => api.get(
          '/tarefas',
          consulta: nivel == null ? null : {'nivelPerigo': nivel.codigo},
        ),
        ler: lerTarefas,
      ),
      retry: (_, _) => null,
    );

final tarefaProvider = FutureProvider.autoDispose
    .family<Consultado<Tarefa>, int>(
      (ref, id) => consultarComCache(
        ref,
        chave: 'tarefas/$id',
        buscar: (api) => api.get('/tarefas/$id'),
        ler: Tarefa.ler,
      ),
      retry: (_, _) => null,
    );

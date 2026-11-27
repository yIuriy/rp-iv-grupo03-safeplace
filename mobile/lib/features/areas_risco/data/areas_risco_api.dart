// Leituras de `/api/areas-risco` e `/api/setores` com cache offline somente leitura por conta
// (RNF08). Camada publicada na #318 para as issues de áreas de risco (#321) e tarefas (#322):
//
// ```dart
// ref.watch(areasRiscoProvider)   // GET /api/areas-risco
// ref.watch(areaRiscoProvider(3)) // GET /api/areas-risco/3
// ref.watch(setoresProvider)      // GET /api/setores
// ref.watch(setorProvider(1))     // GET /api/setores/1
// ```
//
// Cada provider devolve `Consultado<T>`, que a tela mostra com `ConteudoComCache`. As escritas
// (cadastro, edição, histórico) ficam para a #321.

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../shared/offline/cache_offline.dart';
import 'area_risco.dart';

/// Mapa de riscos completo. Também alimenta o `AreaRiscoPicker`.
final areasRiscoProvider =
    FutureProvider.autoDispose<Consultado<List<AreaRisco>>>(
      (ref) => consultarComCache(
        ref,
        chave: 'areas-risco',
        buscar: (api) => api.get('/areas-risco'),
        ler: lerAreasRisco,
      ),
      retry: (_, _) => null,
    );

final areaRiscoProvider = FutureProvider.autoDispose
    .family<Consultado<AreaRisco>, int>(
      (ref, id) => consultarComCache(
        ref,
        chave: 'areas-risco/$id',
        buscar: (api) => api.get('/areas-risco/$id'),
        ler: AreaRisco.ler,
      ),
      retry: (_, _) => null,
    );

/// Setores físicos. Também alimentam o filtro por setor das ocorrências.
final setoresProvider = FutureProvider.autoDispose<Consultado<List<Setor>>>(
  (ref) => consultarComCache(
    ref,
    chave: 'setores',
    buscar: (api) => api.get('/setores'),
    ler: lerSetores,
  ),
  retry: (_, _) => null,
);

final setorProvider = FutureProvider.autoDispose.family<Consultado<Setor>, int>(
  (ref, id) => consultarComCache(
    ref,
    chave: 'setores/$id',
    buscar: (api) => api.get('/setores/$id'),
    ler: Setor.ler,
  ),
  retry: (_, _) => null,
);

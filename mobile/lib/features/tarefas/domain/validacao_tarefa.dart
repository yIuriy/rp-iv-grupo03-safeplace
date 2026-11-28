// Validação local do cadastro e da edição de tarefa, portada de `validarTarefa` do
// `frontend/src/features/tarefas/TarefaFormDialog.tsx`: só a descrição é obrigatória.

String? validarDescricaoTarefa(String? valor) => (valor ?? '').trim().isEmpty
    ? 'A descrição da atividade é obrigatória.'
    : null;

import 'package:material_ui/material_ui.dart';

import '../core/sessao/sessao.dart';

/// Um destino do menu (#316). [perfis] diz quem vê o item e quem pode abrir a rota; o backend
/// continua sendo quem autoriza cada operação (RNF03). [issue] é a issue do cliente Flutter que
/// entrega a tela; até lá, a rota mostra a página "em construção".
class ItemMenu {
  const ItemMenu({
    required this.rota,
    required this.rotulo,
    required this.icone,
    required this.issue,
    this.perfis = const {Perfil.gestorSeguranca, Perfil.supervisor},
    this.rotuloSupervisor,
  });

  final String rota;
  final String rotulo;
  final IconData icone;
  final int issue;
  final Set<Perfil> perfis;

  /// Rótulo diferente para o Supervisor, como no web ("Colaboradores" em vez de "Usuários").
  final String? rotuloSupervisor;

  String rotuloPara(Perfil perfil) =>
      perfil == Perfil.supervisor ? (rotuloSupervisor ?? rotulo) : rotulo;

  bool permite(Perfil perfil) => perfis.contains(perfil);
}

/// Menu do app, na ordem em que aparece. Mesmos módulos do web (`frontend/src/app/AppLayout.tsx`);
/// os alertas e notificações são só do Gestor, como o sino do web.
const itensDoMenu = <ItemMenu>[
  ItemMenu(rota: '/', rotulo: 'Início', icone: Icons.home_outlined, issue: 316),
  ItemMenu(
    rota: '/ocorrencias',
    rotulo: 'Ocorrências',
    icone: Icons.assignment_outlined,
    issue: 318,
  ),
  ItemMenu(
    rota: '/epis',
    rotulo: 'EPIs',
    icone: Icons.inventory_2_outlined,
    issue: 319,
  ),
  ItemMenu(
    rota: '/emprestimos',
    rotulo: 'Empréstimos de EPIs',
    icone: Icons.handshake_outlined,
    issue: 320,
  ),
  ItemMenu(
    rota: '/usuarios',
    rotulo: 'Usuários',
    rotuloSupervisor: 'Colaboradores',
    icone: Icons.people_outline,
    issue: 317,
  ),
  ItemMenu(
    rota: '/areas-risco',
    rotulo: 'Áreas de risco',
    icone: Icons.place_outlined,
    issue: 323,
  ),
  ItemMenu(
    rota: '/tarefas',
    rotulo: 'Tarefas',
    icone: Icons.task_alt,
    issue: 324,
  ),
  ItemMenu(
    rota: '/capacitacoes',
    rotulo: 'Capacitações',
    icone: Icons.school_outlined,
    issue: 325,
  ),
  ItemMenu(
    rota: '/alertas',
    rotulo: 'Alertas',
    icone: Icons.notifications_outlined,
    issue: 322,
    perfis: {Perfil.gestorSeguranca},
  ),
  ItemMenu(
    rota: '/relatorios',
    rotulo: 'Relatórios',
    icone: Icons.bar_chart_outlined,
    issue: 326,
  ),
  ItemMenu(
    rota: '/perfil',
    rotulo: 'Meu perfil',
    icone: Icons.account_circle_outlined,
    issue: 317,
  ),
];

/// Itens que o perfil vê, na ordem do menu.
List<ItemMenu> itensPara(Perfil perfil) => [
  for (final item in itensDoMenu)
    if (item.permite(perfil)) item,
];

/// Item dono da rota (a própria ou uma sub-rota, como `/ocorrencias/12`), ou `null`.
ItemMenu? itemDaRota(String local) {
  ItemMenu? encontrado;
  for (final item in itensDoMenu) {
    final dono = item.rota == '/'
        ? local == '/'
        : local == item.rota || local.startsWith('${item.rota}/');
    if (dono) encontrado = item;
  }
  return encontrado;
}

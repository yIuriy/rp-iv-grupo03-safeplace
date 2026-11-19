import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/sessao/sessao.dart';
import 'package:safeplace/features/usuarios/data/acao_usuario.dart';
import 'package:safeplace/features/usuarios/data/usuario.dart';
import 'package:safeplace/features/usuarios/data/usuarios_api.dart';

import 'apoio_usuarios.dart';

void main() {
  group('Usuario.ler', () {
    test('lê o contrato completo do UsuarioResponse', () {
      final usuario = Usuario.ler(gestorJson);

      expect(usuario.id, 1);
      expect(usuario.nome, 'Gestor Demo');
      expect(usuario.cpf, '39053344705');
      expect(usuario.dataNascimento, DateTime(1980, 1, 15));
      expect(usuario.email, 'gestor.demo@safeplace.com');
      expect(usuario.perfil, Perfil.gestorSeguranca);
      expect(usuario.ativo, isTrue);
      expect(usuario.criadoEm, DateTime(2026, 9, 7, 10));
    });

    test('colaborador vem sem perfil', () {
      final colaborador = Usuario.ler(joaoJson);

      expect(colaborador.perfil, isNull);
      expect(colaborador.ehSupervisor, isFalse);
    });

    test('campos ausentes, nulos ou de outro tipo não derrubam a leitura', () {
      final usuario = Usuario.ler({
        'id': '7',
        'nome': '   ',
        'cpf': 52998224725,
        'dataNascimento': 'ontem',
        'perfil': 'DESCONHECIDO',
      });

      expect(usuario.id, isNull);
      expect(usuario.nome, 'Nome não informado');
      expect(usuario.cpf, isNull);
      expect(usuario.dataNascimento, isNull);
      expect(usuario.email, isNull);
      expect(usuario.perfil, isNull);
      expect(usuario.ativo, isTrue, reason: 'só false explícito é inativo');
      expect(usuario.criadoEm, isNull);
    });

    test('resposta fora do contrato lança FormatException', () {
      expect(() => Usuario.ler('texto'), throwsFormatException);
      expect(() => lerUsuarios({'itens': []}), throwsFormatException);
    });

    test('mesmo id é o mesmo cadastro, mesmo com dados novos', () {
      expect(
        Usuario.ler(joaoJson),
        Usuario.ler({...joaoJson, 'nome': 'João S.'}),
      );
      expect(Usuario.ler(joaoJson), isNot(Usuario.ler(anaJson)));
    });
  });

  test('UsuarioCriado lê a senha inicial do cadastro de Supervisor', () {
    final criado = UsuarioCriado.ler({
      ...supervisorJson,
      'senhaInicial': 'Xk7!pQ2#mA9z',
    });

    expect(criado.usuario.nome, 'Joana Ribeiro');
    expect(criado.senhaInicial, 'Xk7!pQ2#mA9z');
    expect(UsuarioCriado.ler(joaoJson).senhaInicial, isNull);
  });

  test('a aba de supervisores ignora gestores e colaboradores', () {
    final supervisores = lerSupervisores([
      gestorJson,
      supervisorJson,
      joaoJson,
    ]);

    expect(supervisores.map((u) => u.nome), ['Joana Ribeiro']);
  });

  group('AcaoUsuario', () {
    test('lê a ação e descreve o registro em português', () {
      final acoes = lerAcoes(historicoJson);

      expect(acoes.first.tipo, TipoOperacao.cadastro);
      expect(acoes.first.rotuloOperacao, 'Cadastro');
      expect(acoes.first.registro, 'Supervisor 2');
      expect(acoes.first.dataHora, DateTime(2026, 10, 5, 20, 53, 27));
      expect(acoes.last.registro, 'Empréstimo de EPI 4');
      expect(acoes.last.descricao, isNull);
    });

    test('operação e entidade desconhecidas aparecem como vieram', () {
      final acao = AcaoUsuario.ler({
        'tipoOperacao': 'ARQUIVAMENTO',
        'entidade': 'Relatorio',
      });

      expect(acao.tipo, isNull);
      expect(acao.rotuloOperacao, 'ARQUIVAMENTO');
      expect(acao.registro, 'Relatorio');
    });

    test('ação sem campos não derruba a lista', () {
      final acao = AcaoUsuario.ler(<String, Object?>{});

      expect(acao.rotuloOperacao, 'Operação não informada');
      expect(acao.registro, 'Registro');
      expect(acao.dataHora, isNull);
    });
  });

  group('FiltroColaboradores', () {
    test('normaliza o que foi digitado e vazio vira sem filtro', () {
      final filtro = FiltroColaboradores(
        nome: '  joão ',
        cpf: '111.444.777-35',
      );

      expect(filtro.nome, 'joão');
      expect(filtro.cpf, '11144477735');
      expect(filtro.consulta, {'nome': 'joão', 'cpf': '11144477735'});
      expect(
        FiltroColaboradores(nome: ' ', cpf: ''),
        FiltroColaboradores.nenhum,
      );
      expect(FiltroColaboradores.nenhum.vazio, isTrue);
    });

    test('cada busca tem a sua chave de cache', () {
      expect(FiltroColaboradores.nenhum.chaveCache, 'usuarios/colaboradores');
      expect(
        FiltroColaboradores(nome: 'Ana').chaveCache,
        'usuarios/colaboradores?nome=Ana',
      );
    });
  });

  test('limite do histórico fica entre 1 e 100', () {
    expect(ajustarLimiteHistorico(0), 1);
    expect(ajustarLimiteHistorico(20), 20);
    expect(ajustarLimiteHistorico(500), 100);
  });
}

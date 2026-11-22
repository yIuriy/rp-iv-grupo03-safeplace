// Testes unitários do cliente de API de empréstimos (#320): chamadas de entrega, devolução,
// descritores de falha (409, 422, etc.) e filtros.

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/emprestimos/data/emprestimo.dart';
import 'package:safeplace/features/emprestimos/data/emprestimos_api.dart';
import 'package:safeplace/features/emprestimos/domain/emprestimo_repository.dart';

import 'apoio_emprestimos.dart';

void main() {
  group('EmprestimosApi', () {
    test('registrarEntrega envia payload correto para POST /emprestimos', () async {
      late ApiEmprestimosFalsa api;
      api = ApiEmprestimosFalsa((r) {
        expect(r.metodo, 'POST');
        expect(r.caminho, '/emprestimos');
        expect(r.corpo, {
          'epiId': 2,
          'colaboradorId': 3,
          'dataDevolucaoPrevista': '2026-11-25',
          'confirmarSubstituicao': true,
          'justificativaSubstituicao': 'Item extraviado',
        });
        return emprestimoExemploJson;
      });

      final service = EmprestimosApi(api);
      final resultado = await service.registrarEntrega(
        DadosEntrega(
          epiId: 2,
          colaboradorId: 3,
          dataDevolucaoPrevista: DateTime(2026, 11, 25),
          confirmarSubstituicao: true,
          justificativaSubstituicao: 'Item extraviado',
        ),
      );

      expect(resultado.id, 1);
      expect(resultado.epiNome, 'Luva de Vaqueta');
      expect(api.recebidas.length, 1);
    });

    test('registrarDevolucao envia POST /emprestimos/{id}/devolucao com estado', () async {
      late ApiEmprestimosFalsa api;
      api = ApiEmprestimosFalsa((r) {
        expect(r.metodo, 'POST');
        expect(r.caminho, '/emprestimos/1/devolucao');
        expect(r.corpo, {'estado': 'APTO'});
        return devolucaoExemploJson;
      });

      final service = EmprestimosApi(api);
      final resultado = await service.registrarDevolucao(1, EstadoDevolucao.apto);

      expect(resultado.id, 1);
      expect(resultado.status, ClassificacaoEmprestimo.disponivel);
      expect(resultado.estadoDevolucao, EstadoDevolucao.apto);
      expect(api.recebidas.length, 1);
    });

    test('buscarPorId envia GET /emprestimos/{id}', () async {
      late ApiEmprestimosFalsa api;
      api = ApiEmprestimosFalsa((r) {
        expect(r.metodo, 'GET');
        expect(r.caminho, '/emprestimos/1');
        return emprestimoExemploJson;
      });

      final service = EmprestimosApi(api);
      final resultado = await service.buscarPorId(1);

      expect(resultado.id, 1);
      expect(resultado.epiNome, 'Luva de Vaqueta');
    });
  });

  group('Descritores de Falha e Duplicidade', () {
    test('duplicidadeDaFalha extrai mensagem em erro 409', () {
      final erro409 = FalhaApi(
        status: 409,
        corpo: {
          'status': 409,
          'message': 'Colaborador já possui EPI do mesmo tipo.',
        },
      );
      expect(
        duplicidadeDaFalha(erro409),
        'Colaborador já possui EPI do mesmo tipo.',
      );

      final erro409SemMsg = FalhaApi(status: 409, corpo: {'status': 409});
      expect(
        duplicidadeDaFalha(erro409SemMsg),
        contains('O colaborador já possui um EPI do mesmo tipo em posse'),
      );

      final erro500 = FalhaApi(status: 500, corpo: {});
      expect(duplicidadeDaFalha(erro500), isNull);
    });

    test('descreverFalhaDeEntrega respeita status da API', () {
      final erro422 = FalhaApi(
        status: 422,
        corpo: {'message': 'EPI com CA vencido.'},
      );
      expect(descreverFalhaDeEntrega(erro422), 'EPI com CA vencido.');

      final erro403 = FalhaApi(status: 403, corpo: {});
      expect(
        descreverFalhaDeEntrega(erro403),
        'Seu perfil não tem permissão para registrar entregas de EPI.',
      );

      final semRede = const FalhaApi();
      expect(descreverFalhaDeEntrega(semRede), contains('conexão'));
    });

    test('descreverFalhaDeDevolucao trata 409 e 404 como empréstimo não mais ativo', () {
      final erro409 = FalhaApi(status: 409, corpo: {});
      expect(
        descreverFalhaDeDevolucao(erro409),
        contains('Este empréstimo não está mais ativo'),
      );

      final erro403 = FalhaApi(status: 403, corpo: {});
      expect(
        descreverFalhaDeDevolucao(erro403),
        'Seu perfil não tem permissão para registrar devoluções.',
      );
    });
  });

  group('FiltroEmprestimos', () {
    test('gera parâmetros de query e chave de cache corretamente', () {
      const filtroPendentes = FiltroEmprestimos(
        situacao: SituacaoConsulta.pendentes,
      );
      expect(filtroPendentes.consulta, {'situacao': 'PENDENTES'});
      expect(filtroPendentes.chaveCache, 'emprestimos?situacao=PENDENTES');

      const filtroComColaborador = FiltroEmprestimos(
        situacao: SituacaoConsulta.atrasados,
        colaboradorId: 7,
      );
      expect(filtroComColaborador.consulta, {
        'situacao': 'ATRASADOS',
        'colaboradorId': '7',
      });
      expect(
        filtroComColaborador.chaveCache,
        'emprestimos?situacao=ATRASADOS&colaboradorId=7',
      );
    });

    test('igualdade e hashCode', () {
      const f1 = FiltroEmprestimos(situacao: SituacaoConsulta.todos, colaboradorId: 2);
      const f2 = FiltroEmprestimos(situacao: SituacaoConsulta.todos, colaboradorId: 2);
      const f3 = FiltroEmprestimos(situacao: SituacaoConsulta.pendentes);

      expect(f1, equals(f2));
      expect(f1.hashCode, equals(f2.hashCode));
      expect(f1, isNot(equals(f3)));
    });
  });
}

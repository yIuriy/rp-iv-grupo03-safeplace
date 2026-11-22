// Testes unitários dos modelos do módulo de empréstimos (#320): leitura defensiva,
// conversão de respostas (EmprestimoResponse e DevolucaoResponse) e enumerações.

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/features/emprestimos/data/emprestimo.dart';

void main() {
  group('ClassificacaoEmprestimo', () {
    test('lê os três valores do backend', () {
      expect(
        ClassificacaoEmprestimo.ler('INDISPONIVEL'),
        ClassificacaoEmprestimo.indisponivel,
      );
      expect(
        ClassificacaoEmprestimo.ler('DEVOLUCAO_ATRASADA'),
        ClassificacaoEmprestimo.devolucaoAtrasada,
      );
      expect(
        ClassificacaoEmprestimo.ler('DISPONIVEL'),
        ClassificacaoEmprestimo.disponivel,
      );
      expect(ClassificacaoEmprestimo.ler('OUTRO'), isNull);
      expect(ClassificacaoEmprestimo.ler(null), isNull);
    });

    test('códigos e rótulos correspondem ao backend e frontend', () {
      expect(ClassificacaoEmprestimo.indisponivel.codigo, 'INDISPONIVEL');
      expect(ClassificacaoEmprestimo.indisponivel.rotulo, 'Em posse');
      expect(ClassificacaoEmprestimo.devolucaoAtrasada.codigo, 'DEVOLUCAO_ATRASADA');
      expect(ClassificacaoEmprestimo.devolucaoAtrasada.rotulo, 'Atrasado');
      expect(ClassificacaoEmprestimo.disponivel.codigo, 'DISPONIVEL');
      expect(ClassificacaoEmprestimo.disponivel.rotulo, 'Devolvido');
    });
  });

  group('EstadoDevolucao', () {
    test('lê os estados válidos e ignora inválidos', () {
      expect(EstadoDevolucao.ler('APTO'), EstadoDevolucao.apto);
      expect(EstadoDevolucao.ler('MANUTENCAO'), EstadoDevolucao.manutencao);
      expect(EstadoDevolucao.ler('DESCARTE'), EstadoDevolucao.descarte);
      expect(EstadoDevolucao.ler('INVALIDO'), isNull);
      expect(EstadoDevolucao.ler(null), isNull);
    });

    test('possui destino e opções textuais consistentes', () {
      expect(EstadoDevolucao.apto.rotulo, 'Apto para uso');
      expect(EstadoDevolucao.apto.destino, contains('voltará ao saldo disponível'));
      expect(EstadoDevolucao.manutencao.rotulo, 'Encaminhar para manutenção');
      expect(EstadoDevolucao.manutencao.destino, contains('não voltará ao saldo disponível'));
      expect(EstadoDevolucao.descarte.rotulo, 'Indicar descarte');
      expect(EstadoDevolucao.descarte.destino, contains('baixa definitiva não é registrada nesta tela'));
    });
  });

  group('Emprestimo.ler', () {
    final emprestimoJsonCompleto = <String, Object?>{
      'id': 10,
      'epiId': 2,
      'epiNome': 'Óculos de Proteção',
      'colaboradorId': 5,
      'colaboradorNome': 'Carlos Oliveira',
      'responsavelEntregaId': 1,
      'dataRetirada': '2026-11-20T08:30:00',
      'dataDevolucaoPrevista': '2026-11-25',
      'dataDevolucaoReal': null,
      'responsavelDevolucaoId': null,
      'estadoDevolucao': null,
      'status': 'INDISPONIVEL',
      'atrasado': false,
      'termoCautela': 'Termo de responsabilidade assinado.',
      'justificativaSubstituicao': null,
    };

    test('lê registro completo da API', () {
      final emprestimo = Emprestimo.ler(emprestimoJsonCompleto);

      expect(emprestimo.id, 10);
      expect(emprestimo.epiId, 2);
      expect(emprestimo.epiNome, 'Óculos de Proteção');
      expect(emprestimo.colaboradorId, 5);
      expect(emprestimo.colaboradorNome, 'Carlos Oliveira');
      expect(emprestimo.responsavelEntregaId, 1);
      expect(emprestimo.dataRetirada, DateTime(2026, 11, 20, 8, 30));
      expect(emprestimo.dataDevolucaoPrevista, DateTime(2026, 11, 25));
      expect(emprestimo.dataDevolucaoReal, isNull);
      expect(emprestimo.responsavelDevolucaoId, isNull);
      expect(emprestimo.estadoDevolucao, isNull);
      expect(emprestimo.status, ClassificacaoEmprestimo.indisponivel);
      expect(emprestimo.atrasado, isFalse);
      expect(emprestimo.emPosse, isTrue);
      expect(emprestimo.estaAtrasado, isFalse);
      expect(emprestimo.termoCautela, 'Termo de responsabilidade assinado.');
      expect(emprestimo.justificativaSubstituicao, isNull);
    });

    test('leitura defensiva: campos ausentes e nulos não derrubam e recebem reservas', () {
      final jsonVazio = <String, Object?>{
        'id': 'não-é-numero',
        'epiId': 3,
        'colaboradorId': 7,
      };

      final emprestimo = Emprestimo.ler(jsonVazio);

      expect(emprestimo.id, isNull);
      expect(emprestimo.epiId, 3);
      expect(emprestimo.epiNome, 'EPI 3');
      expect(emprestimo.colaboradorId, 7);
      expect(emprestimo.colaboradorNome, 'Colaborador 7');
      expect(emprestimo.responsavelEntregaId, isNull);
      expect(emprestimo.dataRetirada, isNull);
      expect(emprestimo.dataDevolucaoPrevista, isNull);
      expect(emprestimo.status, isNull);
      expect(emprestimo.atrasado, isFalse);
      expect(emprestimo.termoCautela, '');
      expect(emprestimo.justificativaSubstituicao, isNull);
    });

    test('reconhece atraso por flag booleana ou status DEVOLUCAO_ATRASADA', () {
      final atrasadoPorFlag = Emprestimo.ler({
        'id': 1,
        'epiNome': 'Luva',
        'colaboradorNome': 'João',
        'status': 'INDISPONIVEL',
        'atrasado': true,
      });
      expect(atrasadoPorFlag.estaAtrasado, isTrue);

      final atrasadoPorStatus = Emprestimo.ler({
        'id': 2,
        'epiNome': 'Luva',
        'colaboradorNome': 'João',
        'status': 'DEVOLUCAO_ATRASADA',
        'atrasado': false,
      });
      expect(atrasadoPorStatus.estaAtrasado, isTrue);
    });

    test('recusa dado que não seja objeto JSON', () {
      expect(() => Emprestimo.ler('texto'), throwsFormatException);
      expect(() => Emprestimo.ler(null), throwsFormatException);
    });
  });

  group('Emprestimo.lerDevolucao', () {
    test('lê DevolucaoResponse e converte para Emprestimo devolvido', () {
      final devolucaoJson = <String, Object?>{
        'emprestimoId': 15,
        'epiId': 4,
        'epiNome': 'Capacete',
        'colaboradorId': 8,
        'colaboradorNome': 'Mariana Souza',
        'estadoDevolucao': 'APTO',
        'destinoAprovado': 'ESTOQUE',
        'saldoPersistido': 12,
        'dataDevolucaoReal': '2026-11-21T15:00:00',
        'responsavelDevolucaoId': 2,
      };

      final emprestimo = Emprestimo.lerDevolucao(devolucaoJson);

      expect(emprestimo.id, 15);
      expect(emprestimo.epiId, 4);
      expect(emprestimo.epiNome, 'Capacete');
      expect(emprestimo.colaboradorId, 8);
      expect(emprestimo.colaboradorNome, 'Mariana Souza');
      expect(emprestimo.estadoDevolucao, EstadoDevolucao.apto);
      expect(emprestimo.status, ClassificacaoEmprestimo.disponivel);
      expect(emprestimo.emPosse, isFalse);
      expect(emprestimo.atrasado, isFalse);
      expect(emprestimo.dataDevolucaoReal, DateTime(2026, 11, 21, 15, 0));
      expect(emprestimo.responsavelDevolucaoId, 2);
    });
  });

  group('lerEmprestimos', () {
    test('lê lista de registros com sucesso', () {
      final listaJson = [
        {
          'id': 1,
          'epiNome': 'Item 1',
          'colaboradorNome': 'Ana',
          'status': 'INDISPONIVEL',
        },
        {
          'id': 2,
          'epiNome': 'Item 2',
          'colaboradorNome': 'Beto',
          'status': 'DISPONIVEL',
        },
      ];

      final lista = lerEmprestimos(listaJson);
      expect(lista.length, 2);
      expect(lista.first.id, 1);
      expect(lista.last.id, 2);
    });

    test('recusa dado que não seja lista', () {
      expect(() => lerEmprestimos({'id': 1}), throwsFormatException);
    });
  });
}

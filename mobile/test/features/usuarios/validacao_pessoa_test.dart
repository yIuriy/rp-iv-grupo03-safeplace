import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/usuarios/domain/validacao_pessoa.dart';

import 'apoio_usuarios.dart';

void main() {
  group('validação local (mesmas mensagens do web)', () {
    test('nome obrigatório', () {
      expect(validarNome('  '), 'Informe o nome completo.');
      expect(validarNome('Ana'), isNull);
    });

    test('data de nascimento: obrigatória, válida e no passado', () {
      final agora = DateTime(2026, 11, 19, 15);

      expect(
        validarDataNascimento('', agora: agora),
        'Informe a data de nascimento.',
      );
      expect(
        validarDataNascimento('31/02/1990', agora: agora),
        'Informe uma data válida, no formato dd/mm/aaaa.',
      );
      expect(
        validarDataNascimento('19/11/2026', agora: agora),
        'A data de nascimento deve estar no passado.',
      );
      expect(validarDataNascimento('18/11/2026', agora: agora), isNull);
    });

    test(
      'e-mail obrigatório para o Supervisor e opcional para o Colaborador',
      () {
        expect(
          validarEmail('', obrigatorio: true),
          'Informe o e-mail corporativo usado no login.',
        );
        expect(validarEmail('', obrigatorio: false), isNull);
        expect(
          validarEmail('joana@', obrigatorio: false),
          'Informe um e-mail válido.',
        );
        expect(
          validarEmail('joana@empresa', obrigatorio: true),
          'Informe um e-mail válido.',
        );
        expect(
          validarEmail('.joana@empresa.com', obrigatorio: true),
          isNotNull,
        );
        expect(
          validarEmail('joana.ribeiro@safeplace.com', obrigatorio: true),
          isNull,
        );
      },
    );
  });

  group('erro do servidor no campo certo', () {
    test('409 de CPF duplicado vai para o CPF', () {
      final erro = erroDeCampo(
        falhaComMensagem(409, 'CPF já cadastrado no sistema: 52998224725'),
      );

      expect(erro?.campo, CampoPessoa.cpf);
      expect(erro?.mensagem, 'Já existe um cadastro com este CPF.');
    });

    test(
      '409 de e-mail duplicado vai para o e-mail, mesmo com "cpf" no endereço',
      () {
        final erro = erroDeCampo(
          falhaComMensagem(
            409,
            'Email já cadastrado no sistema: cpf@safeplace.com',
          ),
        );

        expect(erro?.campo, CampoPessoa.email);
        expect(
          erro?.mensagem,
          'Este e-mail já está em uso por outro cadastro.',
        );
      },
    );

    test('400 do Bean Validation usa o nome do campo da API', () {
      final erro = erroDeCampo(
        falhaComMensagem(
          400,
          'dataNascimento: Data de nascimento deve estar no passado',
        ),
      );

      expect(erro?.campo, CampoPessoa.dataNascimento);
      expect(erro?.mensagem, 'Data de nascimento deve estar no passado');
    });

    test('400 de CPF inválido do CpfValidador vai para o CPF', () {
      final erro = erroDeCampo(
        falhaComMensagem(
          400,
          'CPF inválido: sequência de dígitos repetidos.',
          erro: 'CPF Inválido',
        ),
      );

      expect(erro?.campo, CampoPessoa.cpf);
    });

    test('o resto fica no aviso geral', () {
      expect(erroDeCampo(falhaComMensagem(409, 'Outro conflito')), isNull);
      expect(
        erroDeCampo(falhaComMensagem(400, 'senha: remova o campo')),
        isNull,
      );
      expect(erroDeCampo(const FalhaApi()), isNull);
      expect(erroDeCampo(falhaComMensagem(500, 'CPF')), isNull);
      expect(erroDeCampo(Exception('x')), isNull);
    });
  });
}

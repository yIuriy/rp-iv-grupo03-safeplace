import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/features/usuarios/data/usuarios_api.dart';
import 'package:safeplace/features/usuarios/domain/cadastro_de_usuarios.dart';

import 'apoio_usuarios.dart';

void main() {
  late ApiUsuariosFalsa api;

  final dados = DadosPessoa(
    nome: 'Joana Ribeiro',
    cpf: '529.982.247-25',
    dataNascimento: DateTime(1988, 4, 20),
    email: 'joana.ribeiro@safeplace.com',
  );

  setUp(() {
    api = ApiUsuariosFalsa((r) => {...supervisorJson, 'senhaInicial': 'S3nh@'});
  });

  test(
    'cadastro de supervisor envia CPF só com dígitos e devolve a senha inicial',
    () async {
      final criado = await UsuariosApi(api).cadastrarSupervisor(dados);

      final requisicao = api.recebidas.single;
      expect(requisicao.metodo, 'POST');
      expect(requisicao.caminho, '/usuarios/supervisores');
      expect(requisicao.corpo, {
        'nome': 'Joana Ribeiro',
        'cpf': '52998224725',
        'dataNascimento': '1988-04-20',
        'email': 'joana.ribeiro@safeplace.com',
      });
      expect(criado.senhaInicial, 'S3nh@');
    },
  );

  test(
    'cadastro de colaborador não envia senha, perfil nem e-mail vazio',
    () async {
      api.responder = (_) => joaoJson;

      await UsuariosApi(api).cadastrarColaborador(
        DadosPessoa(
          nome: 'João da Silva',
          cpf: cpfJoao,
          dataNascimento: DateTime(1992, 6, 18),
        ),
      );

      final requisicao = api.recebidas.single;
      expect(requisicao.caminho, '/usuarios/colaboradores');
      expect(requisicao.corpo, {
        'nome': 'João da Silva',
        'cpf': cpfJoao,
        'dataNascimento': '1992-06-18',
      });
    },
  );

  test('edição envia só nome, data de nascimento e e-mail, sem CPF', () async {
    api.responder = (_) => joaoJson;

    await UsuariosApi(api).atualizarColaborador(10, dados);
    await UsuariosApi(api).atualizarSupervisor(2, dados);

    expect(api.recebidas.map((r) => '${r.metodo} ${r.caminho}'), [
      'PUT /usuarios/colaboradores/10',
      'PUT /usuarios/supervisores/2',
    ]);
    for (final requisicao in api.recebidas) {
      expect(requisicao.corpo, {
        'nome': 'Joana Ribeiro',
        'dataNascimento': '1988-04-20',
        'email': 'joana.ribeiro@safeplace.com',
      });
    }
  });

  test('a falha da API chega a quem chamou', () async {
    api.responder = (_) =>
        throw falhaComMensagem(409, 'CPF já cadastrado no sistema: 1');

    await expectLater(
      UsuariosApi(api).cadastrarColaborador(dados),
      throwsA(isA<FalhaApi>().having((f) => f.status, 'status', 409)),
    );
  });
}

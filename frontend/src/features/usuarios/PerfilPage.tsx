// Tela /perfil: dados cadastrais do Gestor de Segurança ou do Supervisor logado, somente para
// consulta (GET /api/usuarios/me), e as ações recentes da pessoa na trilha de auditoria (RNF05). A
// atualização de cadastros continua nas rotas por papel de RF23.

import { Alert, Button } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { rotuloDoPerfil } from '../autenticacao/sessao'
import { buscarMeuPerfil, type MeuPerfil, type PerfilUsuario } from './api'
import { CpfOculto } from './CpfOculto'
import { formatarData } from './formatos'
import { HistoricoAcoes } from './HistoricoAcoes'
import './perfil.css'

function rotuloDoPerfilUsuario(perfil: PerfilUsuario | null): string {
  if (!perfil) return 'Colaborador'
  return rotuloDoPerfil[perfil] ?? perfil
}

function Dados({ perfil }: { perfil: MeuPerfil }) {
  return <dl className="sp-detalhes sp-perfil-dados">
    <div><dt>Nome completo</dt><dd>{perfil.nome}</dd></div>
    <div><dt>Perfil de acesso</dt><dd>{rotuloDoPerfilUsuario(perfil.perfil)}</dd></div>
    <div><dt>E-mail de acesso</dt><dd>{perfil.email ?? 'Não informado'}</dd></div>
    <div><dt>CPF</dt><dd><CpfOculto cpf={perfil.cpf} nome={perfil.nome} /></dd></div>
    <div><dt>Data de nascimento</dt><dd>{formatarData(perfil.dataNascimento)}</dd></div>
    <div><dt>Cadastrado em</dt><dd>{perfil.criadoEm ? formatarData(perfil.criadoEm.slice(0, 10)) : 'Não informado'}</dd></div>
  </dl>
}

export function PerfilPage() {
  const consulta = useConsulta('meu-perfil', buscarMeuPerfil)

  return <FeaturePage title="Meu perfil" description="Dados cadastrais da sua conta no SafePlace, somente para consulta.">
    {consulta.erro
      ? <>
        <Alert tone="danger" live="assertive" title="Não foi possível carregar o seu perfil.">
          Verifique sua conexão ou tente novamente em alguns instantes.
        </Alert>
        <div><Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button></div>
      </>
      : consulta.dados === null
        ? <p role="status">Carregando perfil...</p>
        : <Dados perfil={consulta.dados} />}
    <HistoricoAcoes />
  </FeaturePage>
}

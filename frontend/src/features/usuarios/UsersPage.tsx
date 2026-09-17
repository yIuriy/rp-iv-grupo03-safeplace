import { useState } from 'react'
import { Tabs } from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { ColaboradoresPainel } from './ColaboradoresPainel'
import { SupervisoresPainel } from './SupervisoresPainel'

/**
 * Mesma matriz de RF23 aplicada pelo `SecurityConfig`: o Gestor gerencia supervisores e
 * colaboradores; o Supervisor gerencia apenas colaboradores.
 */
export function UsersPage() {
  const sessao = useSessaoAtiva()
  const [aba, setAba] = useState('supervisores')
  const gestor = sessao.perfil === 'GESTOR_SEGURANCA'

  if (!gestor) {
    return <FeaturePage title="Usuários"
      description="Cadastre e consulte os colaboradores sob sua responsabilidade. Colaboradores não têm conta de acesso.">
      <ColaboradoresPainel />
    </FeaturePage>
  }

  return <FeaturePage title="Usuários" description="Gerencie as contas de supervisores e os cadastros de colaboradores.">
    <Tabs label="Tipo de cadastro" value={aba} onValueChange={setAba} items={[
      { id: 'supervisores', label: 'Supervisores', content: aba === 'supervisores' ? <SupervisoresPainel /> : null },
      { id: 'colaboradores', label: 'Colaboradores', content: aba === 'colaboradores' ? <ColaboradoresPainel /> : null },
    ]} />
  </FeaturePage>
}

// Tela /emprestimos, menu "Empréstimos de EPIs" (RF11 e RF21; UC12 e UC10; US11 e US21; issue #110):
// Supervisor e Gestor registram entregas e devoluções e acompanham posse e atrasos; só o Gestor vê a
// aba de projeções de substituição e lista de reposição, como pede a issue.

import { useState } from 'react'
import { Tabs } from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { EmprestimosPainel } from './EmprestimosPainel'
import { ProjecoesPainel } from './ProjecoesPainel'
import '../estoque/estoque.css'
import './emprestimos.css'

export function EmprestimosPage() {
  const sessao = useSessaoAtiva()
  const [aba, setAba] = useState('emprestimos')

  if (sessao.perfil !== 'GESTOR_SEGURANCA') {
    return <FeaturePage title="Empréstimos de EPIs"
      description="Registre entregas e devoluções de EPIs aos colaboradores e acompanhe o que está em posse ou atrasado.">
      <EmprestimosPainel />
    </FeaturePage>
  }

  return <FeaturePage title="Empréstimos de EPIs"
    description="Registre entregas e devoluções, acompanhe posse e atrasos e consulte a previsão de substituição dos EPIs.">
    <Tabs label="Seções de empréstimos" value={aba} onValueChange={setAba} items={[
      { id: 'emprestimos', label: 'Empréstimos', content: aba === 'emprestimos' ? <EmprestimosPainel /> : null },
      { id: 'projecoes', label: 'Projeções de substituição', content: aba === 'projecoes' ? <ProjecoesPainel /> : null },
    ]} />
  </FeaturePage>
}

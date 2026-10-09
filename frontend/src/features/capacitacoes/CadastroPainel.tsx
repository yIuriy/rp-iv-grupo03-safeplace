// Aba "Cadastro" (RF10, UC02, issue #192), exclusiva do Gestor de Segurança: registra certificações,
// treinamentos e exigências de capacitação. A validação local repete a da API, que valida de novo.

import { hojeIso } from '../areas-risco/validacaoInspecao'
import { CertificacaoForm } from './CertificacaoForm'
import { ExigenciaForm } from './ExigenciaForm'
import { TreinamentoForm } from './TreinamentoForm'

export function CadastroPainel() {
  const hoje = hojeIso()
  return <div className="sp-painel">
    <p className="sp-texto-secundario">
      Cada cadastro fica registrado na trilha de auditoria. Certificações e treinamentos entram nos alertas de vencimento
      e nas exigências que bloqueiam a alocação em tarefas.
    </p>
    <CertificacaoForm hoje={hoje} />
    <TreinamentoForm hoje={hoje} />
    <ExigenciaForm />
  </div>
}

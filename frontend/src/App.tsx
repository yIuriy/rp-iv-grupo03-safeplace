// Raiz da aplicação: define as rotas. /login é pública; as telas de funcionalidade ficam dentro de
// ExigirSessao (exige login) e AppLayout (cabeçalho e menu). O catálogo do design system segue
// livre.

import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './app/AppLayout'
import { AutenticacaoProvider } from './features/autenticacao/AutenticacaoProvider'
import { ExigirSessao } from './features/autenticacao/ExigirSessao'
import { LoginPage } from './features/autenticacao/LoginPage'
import { UsersPage } from './features/usuarios/UsersPage'
import { PerfilPage } from './features/usuarios/PerfilPage'
import { OccurrencesPage } from './features/ocorrencias/OccurrencesPage'
import { EpisUnificadosPage } from './features/epis/EpisUnificadosPage'
import { EmprestimosPage } from './features/emprestimos/EmprestimosPage'
import { RiskAreasPage } from './features/areas-risco/RiskAreasPage'
import { TasksPage } from './features/tarefas/TasksPage'
import { CapacitacoesPage } from './features/capacitacoes/CapacitacoesPage'
import { Catalog } from './features/design-system/Catalog'
import { RiskAreasExample } from './features/design-system/RiskAreasExample'

function App() {
  return <BrowserRouter><AutenticacaoProvider><Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route element={<ExigirSessao />}>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/usuarios" replace />} />
        <Route path="usuarios" element={<UsersPage />} />
        <Route path="perfil" element={<PerfilPage />} />
        <Route path="capacitacoes" element={<CapacitacoesPage />} />
        <Route path="ocorrencias" element={<OccurrencesPage />} />
        <Route path="epis" element={<EpisUnificadosPage />} />
        <Route path="estoque" element={<Navigate to="/epis" replace />} />
        <Route path="emprestimos" element={<EmprestimosPage />} />
        <Route path="areas-risco" element={<RiskAreasPage />} />
        <Route path="tarefas" element={<TasksPage />} />
        <Route path="*" element={<><h1 className="sp-type-heading-md">Página não encontrada</h1><Link to="/usuarios">Voltar ao início</Link></>} />
      </Route>
    </Route>
    <Route path="/design-system" element={<Catalog />} />
    <Route path="/design-system/examples/risk-areas" element={<RiskAreasExample />} />
  </Routes></AutenticacaoProvider></BrowserRouter>
}

export default App

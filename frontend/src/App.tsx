import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './app/AppLayout'
import { AutenticacaoProvider } from './features/autenticacao/AutenticacaoProvider'
import { ExigirSessao } from './features/autenticacao/ExigirSessao'
import { LoginPage } from './features/autenticacao/LoginPage'
import { UsersPage } from './features/usuarios/UsersPage'
import { OccurrencesPage } from './features/ocorrencias/OccurrencesPage'
import { EpiPage } from './features/epis/EpiPage'
import { RiskAreasPage } from './features/areas-risco/RiskAreasPage'
import { TasksPage } from './features/tarefas/TasksPage'
import { Catalog } from './features/design-system/Catalog'
import { RiskAreasExample } from './features/design-system/RiskAreasExample'

function App() {
  return <BrowserRouter><AutenticacaoProvider><Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route element={<ExigirSessao />}>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/usuarios" replace />} />
        <Route path="usuarios" element={<UsersPage />} />
        <Route path="ocorrencias" element={<OccurrencesPage />} />
        <Route path="epis" element={<EpiPage />} />
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

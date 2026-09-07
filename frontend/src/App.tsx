import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './app/AppLayout'
import { UsersPage } from './features/usuarios/UsersPage'
import { OccurrencesPage } from './features/ocorrencias/OccurrencesPage'
import { EpiPage } from './features/epis/EpiPage'
import { RiskAreasPage } from './features/areas-risco/RiskAreasPage'
import { TasksPage } from './features/tarefas/TasksPage'
import { Catalog } from './features/design-system/Catalog'
import { RiskAreasExample } from './features/design-system/RiskAreasExample'

function App() {
  return <BrowserRouter><Routes>
    <Route element={<AppLayout />}>
      <Route index element={<Navigate to="/usuarios" replace />} />
      <Route path="usuarios" element={<UsersPage />} />
      <Route path="ocorrencias" element={<OccurrencesPage />} />
      <Route path="epis" element={<EpiPage />} />
      <Route path="areas-risco" element={<RiskAreasPage />} />
      <Route path="tarefas" element={<TasksPage />} />
      <Route path="*" element={<><h1 className="sp-type-heading-md">Página não encontrada</h1><Link to="/usuarios">Voltar ao início</Link></>} />
    </Route>
    <Route path="/design-system" element={<Catalog />} />
    <Route path="/design-system/examples/risk-areas" element={<RiskAreasExample />} />
  </Routes></BrowserRouter>
}

export default App

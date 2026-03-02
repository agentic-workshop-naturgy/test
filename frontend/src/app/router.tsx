import { Routes, Route, Navigate } from 'react-router-dom'
import { AppShell } from '../shared/ui/AppShell'
import { SupplyPointsPage } from '../features/supply-points/pages/SupplyPointsPage'
import { ReadingsPage } from '../features/readings/pages/ReadingsPage'
import { TariffsPage } from '../features/tariffs/pages/TariffsPage'
import { FactorsPage } from '../features/factors/pages/FactorsPage'
import { TaxesPage } from '../features/taxes/pages/TaxesPage'
import { BillingPage } from '../features/billing/pages/BillingPage'

export function AppRouter() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<Navigate to="/supply-points" replace />} />
        <Route path="/supply-points" element={<SupplyPointsPage />} />
        <Route path="/readings" element={<ReadingsPage />} />
        <Route path="/tariffs" element={<TariffsPage />} />
        <Route path="/factors" element={<FactorsPage />} />
        <Route path="/taxes" element={<TaxesPage />} />
        <Route path="/billing" element={<BillingPage />} />
      </Routes>
    </AppShell>
  )
}

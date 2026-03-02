# RUNBOOK — GAS Workshop: Backend + Frontend

## Prerequisitos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java JDK    | 17             |
| Maven       | 3.8+           |
| Node.js     | 18+            |
| npm         | 9+             |

## 1. Arrancar el Backend (Spring Boot + H2)

```bash
# Desde la raíz del repositorio:
cd backend
mvn spring-boot:run
```

El backend arranca en **http://localhost:8080**.

- Seed automático desde `_data/db/samples/*.csv` al iniciar.
- Consola H2: http://localhost:8080/h2-console  
  - JDBC URL: `jdbc:h2:mem:gasdb`
  - User: `sa` / Password: *(vacío)*

### Endpoints disponibles

| Recurso               | Base path                       |
|-----------------------|---------------------------------|
| Puntos de suministro  | `/api/gas/supply-points`        |
| Lecturas              | `/api/gas/readings`             |
| Tarifas               | `/api/gas/tariffs`              |
| Factores conversión   | `/api/gas/factors`              |
| IVA / Impuestos       | `/api/gas/taxes`                |
| Facturación           | `POST /api/gas/billing/{period}`|
| Facturas              | `/api/gas/invoices`             |
| PDF factura           | `GET /api/gas/invoices/{id}/pdf`|

---

## 2. Arrancar el Frontend (React + Vite)

```bash
# Desde la raíz del repositorio:
cd frontend
npm install          # solo la primera vez
npm run dev
```

El frontend arranca en **http://localhost:5173** con proxy `/api` → `http://localhost:8080`.

---

## 3. Pasos de Demo

### 3.1 Explorar maestros (seed data ya cargado)

1. Abrir http://localhost:5173
2. Navegar a **Puntos de Suministro** → ver CUPS del CSV seed.
3. Navegar a **Lecturas** → filtrar por CUPS `ES0021000000001AA`.
4. Navegar a **Tarifas** → ver RL1, RL2.
5. Navegar a **Factores Conversión** → filtrar por zona `ZONA1`.
6. Navegar a **IVA / Impuestos** → ver registro `IVA` con tasa 0.21.

### 3.2 Ejecutar facturación

1. Navegar a **Facturación**.
2. Ingresar período: `2026-01`.
3. Pulsar **Ejecutar facturación**.
4. Verificar resultado: facturas generadas + posibles errores listados.

### 3.3 Ver facturas y PDF

1. En la sección **Facturas**, se muestra la lista actualizada.
2. Pulsar el ícono **►** (expand) en una fila para ver detalle (cabecera + líneas).
3. Pulsar **Descargar PDF** para obtener `factura-GAS-202601-...pdf`.

### 3.4 CRUD de maestro (ejemplo: nueva tarifa)

1. Navegar a **Tarifas**.
2. Pulsar **Nueva** → completar RL3, fijo 5.00, variable 0.055, vigencia 2026-01-01.
3. Guardar → aparece en la tabla.
4. Editar → cambiar fijo a 5.50 → Guardar.
5. Eliminar → confirmar → desaparece.

---

## 4. Paths SSOT y CSV samples

| Archivo                                  | Contenido                        |
|------------------------------------------|----------------------------------|
| `_data/specs/gas_csv-spec.txt`           | Esquema DB + validaciones        |
| `_data/specs/gas_logic-spec.txt`         | Lógica de facturación + cálculos |
| `_data/specs/react-standards.md`         | Estándares UI React/MUI          |
| `_data/db/samples/supply-points.csv`     | Puntos de suministro seed        |
| `_data/db/samples/gas-readings.csv`      | Lecturas de contador seed        |
| `_data/db/samples/gas-tariffs.csv`       | Tarifas seed                     |
| `_data/db/samples/gas-conversion-factors.csv` | Factores conversión seed    |
| `_data/db/samples/taxes.csv`             | IVA seed                         |

---

## 5. Tests

```bash
# Tests backend:
cd backend && mvn test

# Tests frontend (unit):
cd frontend && npm run test
```

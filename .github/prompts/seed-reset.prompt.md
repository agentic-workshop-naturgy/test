---
mode: 'agent'
description: 'Resetea la base de datos H2 y recarga el seed desde los CSV de _data/db/samples/'
---

Resetea el estado del backend del workshop GAS y recarga el seed desde cero. Sigue estos pasos:

## 1. Detectar si el backend está corriendo
- Comprueba si hay un proceso Java/Spring escuchando en el puerto 8080.
- Si está corriendo, indícalo claramente antes de continuar.

## 2. Limpiar la base de datos H2
- Si H2 es **in-memory** (url `jdbc:h2:mem:...`): basta con reiniciar la app.
- Si H2 es **file-based** (url `jdbc:h2:file:...`): localiza el fichero `.mv.db` (busca en `backend/` o raíz) y elimínalo.
- Para determinar el tipo, lee `backend/src/main/resources/application.properties` o `application.yml`.

## 3. Arrancar el backend
Ejecuta desde la carpeta `backend/`:
```
./mvnw spring-boot:run
```
o si no hay wrapper:
```
mvn spring-boot:run
```

## 4. Verificar que el seed cargó correctamente
Una vez arrancado, comprueba los conteos esperados con:
```
curl -s http://localhost:8080/api/gas/supply-points | python -m json.tool
curl -s http://localhost:8080/api/gas/readings | python -m json.tool
curl -s http://localhost:8080/api/gas/tariffs | python -m json.tool
curl -s http://localhost:8080/api/gas/conversion-factors | python -m json.tool
curl -s http://localhost:8080/api/gas/taxes | python -m json.tool
```

## 5. Criterio de éxito
Confirma que todas las tablas tienen registros > 0. Los CSV fuente están en:
- `_data/db/samples/supply-points.csv`
- `_data/db/samples/gas-readings.csv`
- `_data/db/samples/gas-tariffs.csv`
- `_data/db/samples/gas-conversion-factors.csv`
- `_data/db/samples/taxes.csv`

Si alguna tabla está vacía, revisa los logs de arranque buscando `WARN` o `ERROR` en el `SeedService`.

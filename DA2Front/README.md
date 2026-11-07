# Frontend de LogiRed

Interfaz React/Vite para ADMIN, COMERCIO, DEPOSITO y REPARTIDOR.

## Ejecucion

Requiere Node.js 22 y npm. Desde este directorio:

```powershell
npm ci
npm run dev
```

La API habitual esta en `http://localhost:8081`. El cliente compartido acepta
`VITE_API_BASE_URL` en `.env.local`; algunos clientes legados conservan la URL fija.
No colocar secretos en variables Vite. Detener el servidor con Ctrl+C.

## Verificaciones

```powershell
npm test
npm run lint
npm run build
```

Los tests nativos de Node verifican presentacion de depositos, botones por rol y
metricas de rutas y políticas de acceso a las rutas privadas. No ejecutan React en un navegador ni escriben en MySQL.
El build genera `dist/`, ignorado por Git. Consultar el README de la raiz para
configuracion del backend y el [informe de cierre](../docs/auditoria-segunda-etapa-2026-11-05.md) para limitaciones conocidas.

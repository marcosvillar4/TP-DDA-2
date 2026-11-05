# LogiRed

Proyecto de Desarrollo de Aplicaciones II (UADE): gestion logistica de comercios,
depositos, productos, inventario, pedidos y entregas de ultima milla.

## Requisitos y estructura

- `DA2Back`: Spring Boot, Spring Security/JWT, JPA, MySQL y ActiveMQ/JMS.
- `DA2Front`: React, Vite, Leaflet y SweetAlert2.
- `docs`: seguridad y procedimientos de migracion.
- Java objetivo 17, validado con Temurin 21; Maven mediante `mvnw.cmd`.
- Node.js 22 y npm; instalar dependencias con `npm ci`.

## Backend local

Copiar `DA2Back/src/main/resources/application-example.properties` a
`application.properties` en el mismo directorio. Este ultimo esta ignorado por Git.
Configurar localmente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y
`JWT_EXPIRATION` (milisegundos). No versionar sus valores.

En MySQL existente usar `JPA_DDL_AUTO=validate`. La plantilla del equipo tiene
`update` como fallback: no usarlo para reparar esquemas sin revisar sus cambios.
La migración estructural ya se aplicó en la base local `marketplace`; no repetirla.
Para otra instalación con esquema anterior, leer
[`docs/migracion-marketplace/README.md`](docs/migracion-marketplace/README.md).
El perfil `dev` usa H2 y activa datos de demostracion: no combinarlo con datos reales.

El broker ActiveMQ puede ser embebido segun `app.mensajeria.*`. Al marcar un pedido
listo, JMS puede asignar automaticamente un repartidor disponible. La toma
voluntaria reutiliza la misma operacion transaccional de asignacion.
Para evitar correos configurar `MAIL_HOST=false`; para mapas sin red,
`MAPAS_PROVEEDOR=local`. Las opciones SMTP y mapas estan en la plantilla.

Desde PowerShell, con variables cargadas y esquema previamente validado:

```powershell
cd DA2Back
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --spring.jpa.hibernate.ddl-auto=validate"
```

El seeder crea un ADMIN si no existe: revisar sus parametros antes del arranque.
Los archivos personales de VS Code no se distribuyen. Detener servidores con Ctrl+C.

## Frontend

```powershell
cd DA2Front
npm ci
npm run dev
```

Frontend habitual: `http://localhost:5173`; API: `http://localhost:8081`.
El cliente compartido admite `VITE_API_BASE_URL` en `.env.local`; algunos clientes
legados conservan la URL local fija. Nunca poner secretos en variables `VITE_*`.

## Pruebas aisladas

Detener el backend de desarrollo antes de compilar para evitar reinicios de DevTools.
La suite predeterminada usa H2 en memoria; no usar `dev` ni overrides de datasource
que apunten a bases reales. Se recomienda forzar las propiedades de aislamiento
del [informe de cierre](docs/auditoria-segunda-etapa-2026-11-05.md) al ejecutar la suite en un entorno personalizado.

```powershell
cd DA2Back
.\mvnw.cmd test "-Dspring.mail.host=false" "-Dapp.mapas.proveedor=local"
```

`SecurityCriteriaMySqlIT` es opt-in y fija exclusivamente `marketplace_security_test`.
Configuracion: [`docs/seguridad/SEC-W04-W05.md`](docs/seguridad/SEC-W04-W05.md).

```powershell
cd DA2Front
npm test
npm run lint
npm run build
```

Los tests frontend usan el runner nativo de Node y no sustituyen una prueba de
navegador. La política de rutas y los helpers de presentación también tienen pruebas.
El informe de cierre registra por separado pruebas, build y lint global.
Resultados y pendientes actuales: [informe de cierre](docs/auditoria-segunda-etapa-2026-11-05.md).
Los documentos compartidos contienen resúmenes depurados, sin evidencias privadas.

## Ciclo de pedidos

`CREADO -> LISTO_PARA_RETIRAR -> ASIGNADO -> RETIRADO -> EN_CAMINO -> ENTREGADO`.
Se cancela desde CREADO, LISTO_PARA_RETIRAR, ASIGNADO o RETIRADO, no desde EN_CAMINO
ni estados finales. Crear descuenta stock; cancelar lo restituye una sola vez.
Si faltan datos para restituir stock consistentemente, la cancelacion se rechaza.
El repartidor opera mediante `/repartidor/me/**`; asignacion, retiro, viaje y
entrega genericos son administrativos. El backend aplica roles y ownership.

La revocación de la credencial SMTP expuesta en el historial sigue pendiente.
No habilitar correo con esa credencial ni introducirla en archivos versionados.
Los scripts SQL son manuales y no idempotentes; consultar su procedimiento antes de ejecutarlos.

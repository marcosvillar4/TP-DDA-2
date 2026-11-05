# Cierre técnico: registro, permisos y pruebas

Documento publicable de la segunda etapa. Los datos y evidencias operativas
completos se conservan localmente fuera del contenido versionado.

## Registro y validación de repartidores

UsuarioFactory crea Usuario EN_EVALUACION y RepartidorService.crearPendiente
conserva patente y zona en un perfil inactivo/NO_DISPONIBLE. El vehículo sigue en
Usuario; no se duplican datos personales en el perfil operativo.

UsuarioServiceImpl.aplicarTransicion bloquea el usuario y sincroniza el perfil
al aprobar o rechazar, dentro de la misma transacción. La aprobación mantiene
los datos y habilita el mismo perfil como activo/DISPONIBLE. El rechazo conserva
los datos y lo mantiene inactivo. Usuarios antiguos sin perfil conservan el alta
administrativa posterior. RepartidorService.crear sigue exigiendo VALIDADO.

La administración continúa restringida a ADMIN. Un JWT no habilita cuentas
pendientes, rechazadas o bloqueadas. Activar/cambiar disponibilidad y la operación
compartida de asignación exigen usuario REPARTIDOR VALIDADO. No se duplican las
transiciones State ni se amplían permisos.

RegistroRepartidorIntegrationTest comprueba registro pendiente, conservación de
datos, aprobación, rechazo, autorización administrativa, impedimento de activación
y asignación previa, y rollback de alta/evaluación. Usa H2 aislada, sin correo ni JMS.

## Frontend

RoleGuard protege todas las rutas privadas, incluidos detalles y formularios.
routePermissions centraliza los permisos del sidebar y de las rutas. REPARTIDOR
opera en Mis Pedidos; DEPOSITO no accede al alta de pedidos; COMERCIO conserva
sus operaciones con ownership verificado por backend.

Se corrigieron imports/variables sin uso, dependencias de callbacks y reinicios de
formularios ligados a cambios de identidad. No se deshabilitaron reglas ESLint
ni se agregaron dependencias. Las pruebas Node cubren política de navegación y
presentación; quedan fuera las interacciones DOM y las pruebas de carga.

## Migración y configuración

El ensayo estructural y el arranque con validate se completaron; ver
[procedimiento](migracion-marketplace/README.md) y [resumen](migracion-marketplace/ensayo-2026-11-05.md).
Los scripts son manuales y no idempotentes. Ya se aplicaron a la base local
marketplace; no deben reejecutarse allí. No se distribuyen los datos históricos.

Se retiraron los fallbacks literales de JWT/SMTP de las plantillas modificadas.
La revocación SMTP permanece pendiente por decisión del responsable; su ausencia
en el archivo actual no elimina la exposición histórica. No se habilitó correo.

## Verificaciones de cierre

Resultados de la nueva ejecución de cierre, 05/11/2026:

| Verificación | Resultado real |
|---|---|
| Suite backend con H2 aislada | 257 tests, 0 fallos, 0 errores, 0 omitidos |
| SEC-W04/W05 en MySQL aislado | 2 tests, 0 fallos, 0 errores, 0 omitidos |
| Empaquetado backend | BUILD SUCCESS; tests ya ejecutados por separado |
| Tests frontend (Node) | 11 aprobados, 0 fallos, 0 omitidos |
| ESLint global | 0 errores, 0 advertencias |
| Build frontend | Correcto; avisos de bundle e imports dinámicos |
| git diff --check | Correcto |

La suite estándar excluye SecurityCriteriaMySqlIT por su sufijo IT. Sus dos casos
se ejecutan separadamente; no deben contarse dos veces. No se ejecutaron pruebas
funcionales nuevas de escritura sobre datos históricos.

La clase MySQL fija marketplace_security_test y revierte sus filas ficticias.
Esta corrida utilizó `-Dspring.jpa.properties.hibernate.hbm2ddl.auto=validate` para
impedir DDL, además de correo y JMS deshabilitados. Las credenciales se obtuvieron
solo del entorno local. La comparación anterior/posterior confirmó que las bases
protegidas conservaron estructura y datos, y el esquema aislado no cambió.

Comandos desde DA2Back (PowerShell):

```powershell
.\mvnw.cmd -o '-DforkedProcessTimeoutInSeconds=240' '-Dspring.datasource.url=jdbc:h2:mem:cierre;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE' '-Dspring.datasource.driver-class-name=org.h2.Driver' '-Dspring.datasource.username=sa' '-Dspring.datasource.password=' '-Dspring.jpa.database-platform=org.hibernate.dialect.H2Dialect' '-Dspring.jpa.hibernate.ddl-auto=create-drop' '-Dspring.mail.host=false' '-Dapp.mapas.proveedor=local' test
.\mvnw.cmd -o '-Dtest=SecurityCriteriaMySqlIT' '-DforkedProcessTimeoutInSeconds=240' '-Dspring.jpa.properties.hibernate.hbm2ddl.auto=validate' test
.\mvnw.cmd -o -DskipTests package
```

El segundo comando requiere SECURITY_TEST_DB_USERNAME y SECURITY_TEST_DB_PASSWORD
configuradas localmente y el esquema de pruebas previamente preparado. No crear
ni cambiar esquemas automáticamente como respuesta a un error de validación.

Desde DA2Front: `npm test`, `npm run lint`, `npm run build`.
Los logs crudos, informes operativos completos y respaldos se conservan localmente;
no forman parte del contenido publicable. Las credenciales no se incluyen.
Las pruebas locales no acreditan por sí solas un pipeline remoto en verde.

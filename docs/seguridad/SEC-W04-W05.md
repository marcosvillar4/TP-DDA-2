# Clase 11: SEC-W04 y SEC-W05

Responsable: Agustin. Fecha de ejecucion: 2026-10-20 (America/Buenos_Aires).
Base de trabajo: `2343c24`; rama: `agus-seguridad-sec-w04-w05`.
Alcance exclusivo: contrasenas y autorizacion de REPARTIDOR sobre `POST /depositos`.

## Entorno y aislamiento

- MySQL 8.0.46 local, esquema exclusivo `marketplace_security_test`, creado vacio.
- No se copio informacion de `marketplace`. Los datos y las contrasenas de prueba
  son ficticios; los identificadores se generan con UUID.
- Clase: `DA2Back/src/test/java/com/example/DA2Back/security/SecurityCriteriaMySqlIT.java`.
- El sufijo `IT` evita su ejecucion en la suite Surefire predeterminada. Se
  selecciona explicitamente con `-Dtest`; no se requieren nuevas dependencias.
- `IsolatedMySql.dataSource()` fija la URL MySQL a
  `jdbc:mysql://127.0.0.1:3306/marketplace_security_test`. No acepta una URL externa.
  Verifica `SELECT DATABASE()`, el producto MySQL y el perfil `security-mysql`
  antes de entregar el DataSource a Hibernate. No hay fallback a H2 ni MySQL real.
- Credenciales exclusivamente en `SECURITY_TEST_DB_USERNAME` y
  `SECURITY_TEST_DB_PASSWORD`. Si faltan, el contexto falla sin iniciar Hibernate.
- Hibernate usa `update` exclusivamente en este contexto de test para crear las
  tablas del esquema aislado. No hay `DROP`, truncado ni borrado de esquemas.
  Cada test usa una transaccion con rollback; las tablas quedan para reutilizacion.
- El seeder automatico se reemplaza con un mock para impedir altas al arrancar.
  SEC-W04 instancia el `AdminSeeder` real con repositorio y factory reales y lo
  ejecuta dentro de su transaccion. No se simulan el encoder ni la persistencia.
- Correo y JMS estan deshabilitados mediante propiedades del contexto de test.
  No se utiliza la configuracion local de arranque ni se modifica la normal.
- MockMvc ejecuta el controlador, login, JWT y cadena de seguridad reales dentro
  del proceso. No llama al servidor de desarrollo ni necesita el frontend.

## SEC-W04: BCrypt y ausencia de exposicion en logs

**DADO** un esquema aislado sin ADMIN y contrasenas ficticias.
**CUANDO** el seeder real crea el ADMIN y `UsuarioServiceImpl.registrar()` crea
un COMERCIO mediante el flujo real.
**ENTONCES** los valores leidos de MySQL tienen formato BCrypt y son distintos
del original; `matches()` acepta la contrasena correcta y rechaza otra; dos
codificaciones del mismo valor son diferentes; los logs no contienen los originales.

Metodo: `secW04_bcryptYLogsSinContrasenas`.

Referencias de produccion:

- `config/SecurityConfig.passwordEncoder()`: instancia `BCryptPasswordEncoder`.
- `Seguridad/negocio/UsuarioFactory`: codifica ADMIN y registros.
- `Seguridad/negocio/UsuarioServiceImpl.registrar()`: persiste el registro.
- `Seguridad/negocio/AdminSeeder.run()`: persiste ADMIN y emite su log.

### Evidencia anterior a la correccion

El test ejecuto ambos flujos y todas las aserciones de BCrypt pasaron. Fallo
exclusivamente la comprobacion de ausencia de contrasenas en logs:

```text
Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
SEC-W04: una contrasena aparecio en logs (valor omitido)
BUILD FAILURE
```

Veredicto anterior: **NO CUMPLE**, reproducido dinamicamente, no solo inferido.
El appender captura el mensaje del seeder en memoria y desactiva su propagacion
a consola; no se imprime el valor. Las aserciones tampoco muestran hashes.

Correccion unica: reemplazar el log de `AdminSeeder.run()` que incluia email y
contrasena por `Usuario ADMIN creado`. No cambia la creacion, el encoder ni las
condiciones del seeder. Se ejecuto nuevamente el mismo test, sin relajar aserciones.

Veredicto final: **CUMPLE en los flujos probados**.

## SEC-W05: REPARTIDOR no crea depositos

**DADO** un usuario ficticio REPARTIDOR VALIDADO que inicia sesion con
`POST /auth/login` y obtiene un JWT firmado por el servicio real.
**CUANDO** envia una sola solicitud `POST /depositos` con ese JWT y un
`RegistroDepositoDTO` valido.
**ENTONCES** recibe exactamente HTTP 403, no se crea el encargado y el contenido
de todas las tablas del esquema es identico al anterior a la solicitud.

Metodo: `secW05_repartidorNoCreaDepositos`.

El cuerpo se valida con Jakarta Validator antes de enviarlo para evitar un falso
positivo por JSON invalido. Se comparan snapshots SHA-256 de las filas completas
ordenadas por ID, incluidos usuarios y depositos. No se imprimen filas, passwords
ni JWT. La comparacion se realiza antes del rollback, de modo que este no oculte
una escritura indebida. La impresion automatica de solicitudes MockMvc esta
deshabilitada incluso ante fallos.

Referencias: `SecurityConfig.securityFilterChain()` restringe POST a COMERCIO;
`DepositoController.agregarDeposito()` tiene `@PreAuthorize("hasRole('COMERCIO')")`;
`JwtAuthenticationFilter` valida la autenticacion en la solicitud real de MockMvc.

Veredicto final: **CUMPLE en el escenario probado**: HTTP 403 y snapshots iguales.

## Ejecucion reproducible

Requisitos: Java/Maven del proyecto, MySQL local y un esquema exclusivo.
Si el esquema no existe, su administrador debe crearlo una sola vez:

```sql
CREATE DATABASE marketplace_security_test
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

Si ya existe, revisar primero su contenido: no eliminar ni sobrescribir datos.
No usar `marketplace`. Para otros entornos se recomienda una cuenta MySQL con
permisos limitados al esquema de pruebas; no se crean ni modifican cuentas aqui.

Desde PowerShell en `DA2Back`, introducir credenciales locales sin escribir la
contrasena en el historial de comandos:

```powershell
$env:SECURITY_TEST_DB_USERNAME = Read-Host 'Usuario MySQL de pruebas'
$env:SECURITY_TEST_DB_PASSWORD = [System.Net.NetworkCredential]::new(
    '', (Read-Host 'Contrasena MySQL de pruebas' -AsSecureString)).Password
try {
    .\mvnw.cmd -o "-Dtest=SecurityCriteriaMySqlIT#secW04_bcryptYLogsSinContrasenas" test
    .\mvnw.cmd -o "-Dtest=SecurityCriteriaMySqlIT#secW05_repartidorNoCreaDepositos" test
    .\mvnw.cmd -o "-Dtest=SecurityCriteriaMySqlIT" test
} finally {
    Remove-Item Env:SECURITY_TEST_DB_USERNAME, Env:SECURITY_TEST_DB_PASSWORD
}
```

`-o` usa dependencias ya disponibles localmente. Si Maven informa dependencias
faltantes, no es un fallo del criterio: preparar las dependencias antes de repetir.
No ejecutar estos comandos mediante la tarea de arranque `LogiRed: MySQL`.
Detener el backend de desarrollo antes de compilar evita reinicios de DevTools.

## Resultados y evidencias

| Ejecucion | Tests | Failures | Errors | Skipped | Resultado |
|---|---:|---:|---:|---:|---|
| SEC-W04 antes del cambio | 1 | 1 | 0 | 0 | FAIL esperado |
| SEC-W04 despues del cambio | 1 | 0 | 0 | 0 | PASS |
| SEC-W05 individual | 1 | 0 | 0 | 0 | PASS |
| Ambos juntos | 2 | 0 | 0 | 0 | PASS |

Surefire genero los reportes `TEST-com.example.DA2Back.security.SecurityCriteriaMySqlIT.xml`
y `com.example.DA2Back.security.SecurityCriteriaMySqlIT.txt` en `DA2Back/target/surefire-reports/`.
Se preservaron copias locales por ejecucion en `DA2Back/target/security-evidence/`
(`w04-before`, `w04-after`, `w05`, `w04-w05-together`), junto con las salidas Maven.
`target/` esta ignorado por Git; este documento conserva los resultados sanitizados
para el repositorio. No publicar reportes completos sin revisar sus metadatos locales.

Verificacion posterior: checksums de las doce tablas de `marketplace` identicos
antes y despues. Conserva 83 filas: usuarios 15, depositos 3, pedidos 8, historial
22, productos 11, items 6, comercios 3, inventarios 3, repartidores 7, detalles 3,
productos_backup 2; notificaciones 0. Las once tablas del esquema de tests quedaron
sin filas al finalizar. Los contadores AUTO_INCREMENT del esquema de tests pueden
avanzar aunque se reviertan las filas.

## Revision y limitaciones

Herramienta de IA utilizada: OpenAI Codex, para inspeccion, propuesta, implementacion
y revision de aislamiento, aserciones y evidencias. Los resultados anteriores
provienen de ejecuciones Maven/MySQL, no de predicciones de la IA.

- SEC-W04 cubre ADMIN y registro COMERCIO, no todos los caminos de cambio de
  contrasena ni todos los archivos de logs historicos.
- SEC-W05 verifica autorizacion de servidor con MockMvc, no una prueba de navegador
  o transporte HTTP sobre el proceso desplegado.
- No se ejecutaron otros criterios ni la suite completa. Estos tests son opt-in.
- La captura de logs y el rollback son instrumentacion de tests; no cambian
  seguridad, reglas de negocio ni configuracion productiva.

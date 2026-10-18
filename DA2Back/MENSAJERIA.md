# Mensajería asincrónica con ActiveMQ — Pedido → Repartidor

## Proceso de negocio implementado

> **Un comercio hace un pedido y un repartidor se lo entrega**, con un paso intermedio
> desacoplado por una **cola punto a punto** de ActiveMQ.

```
 Comercio                 ServicioDePedidos          ActiveMQ                 Asignador            Repartidor
    │ POST /api/pedidos         │                       │                        │                     │
    ├──────────────────────────►│ pedido CREADO         │                        │                     │
    │ PATCH .../listo-para-     │                       │                        │                     │
    │ retirar                   │ LISTO_PARA_RETIRAR    │                        │                     │
    ├──────────────────────────►│ ── commit ──┐         │                        │                     │
    │ ◄── 200 (respuesta ya)    │             ▼         │                        │                     │
    │                           │  PublicadorPedidos ──►│ cola                   │                     │
    │                           │  (AFTER_COMMIT)       │ logired.pedidos.listos │                     │
    │                           │                       ├───────────────────────►│ busca repartidor    │
    │                           │◄── asignarRepartidor ─┤                        │ DISPONIBLE          │
    │                           │ ASIGNADO              │                        │                     │
    │                           │                       │                        │      /repartidor/me/pedidos/{id}/retirar
    │                           │ RETIRADO → EN_CAMINO → ENTREGADO ◄─────────────┼─────────────────────┤ iniciar-viaje / entregar
```

- **Productor**: `PublicadorPedidosListos`. Escucha un evento de dominio (`PedidoListoParaRetirarEvent`)
  y publica en la cola **después del commit** (`@TransactionalEventListener(AFTER_COMMIT)`), para que el
  consumidor nunca vea un pedido que todavía no está en la base.
- **Consumidor**: `AsignadorAutomaticoPedidos` (`@JmsListener`). Asigna el primer repartidor
  activo y `DISPONIBLE` reutilizando `ServicioDePedidos.asignarRepartidor` (patrón State, locks y
  validaciones existentes).
- **Sin repartidor disponible**: el listener lanza excepción → rollback de la sesión JMS → ActiveMQ
  reentrega (10 veces, cada 2 s). Agotados los reintentos el mensaje va a `ActiveMQ.DLQ` y el pedido
  queda `LISTO_PARA_RETIRAR`, o sea que sigue pudiendo asignarse manualmente / ser tomado por un repartidor.
- **Idempotente**: si al llegar el mensaje el pedido ya fue asignado/cancelado, se descarta.

## Archivos

| Archivo | Cambio |
|---|---|
| `pom.xml` | + `spring-boot-starter-activemq`, `activemq-broker` |
| `mensajeria/config/BrokerActiveMqConfig.java` | **nuevo** – broker ActiveMQ embebido en `tcp://localhost:61616` |
| `mensajeria/config/JmsConfig.java` | **nuevo** – container factory transaccional |
| `mensajeria/negocio/PublicadorPedidosListos.java` | **nuevo** – productor |
| `mensajeria/negocio/AsignadorAutomaticoPedidos.java` | **nuevo** – consumidor |
| `mensajeria/excepcion/SinRepartidorDisponibleException.java` | **nuevo** |
| `pedidos/negocio/evento/PedidoListoParaRetirarEvent.java` | **nuevo** – evento de dominio |
| `pedidos/negocio/ServicioDePedidosImpl.java` | publica el evento en `marcarListoParaRetirar` |
| `repartidor/dato/RepartidorRepository.java` | + `findByActivoTrueAndEstadoOrderByIdAsc` |
| `application.properties` / `application-example.properties` | configuración del broker y la cola |
| `src/test/resources/application*.properties` | mensajería desactivada en los tests existentes |
| `integration/PedidoAsignacionAsincronaIntegrationTest.java` | **nuevo** – test end-to-end |

## Cómo correrlo (sin Docker)

### Opción A — broker embebido (no hay que instalar nada)
Es el valor por defecto (`app.mensajeria.broker-embebido=true`). Alcanza con:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
La app levanta ActiveMQ en `tcp://localhost:61616` y se conecta a él por TCP.

### Opción B — ActiveMQ instalado aparte
1. Descargar el zip binario de https://activemq.apache.org/components/classic/download/ (requiere Java 17+).
2. Descomprimir y ejecutar `bin/activemq start` (Windows: `bin\activemq start`).
3. En `application.properties` poner `app.mensajeria.broker-embebido=false`.
4. Consola web: http://localhost:8161/admin (usuario/clave `admin`/`admin`) → pestaña **Queues**
   para ver `logired.pedidos.listos` (Messages Enqueued / Dequeued) y `ActiveMQ.DLQ`.

## Demo con el seed del perfil `dev`
(comercio, depósito y producto id=1; clave de todos: `admin123`)

```bash
# 1) Login del comercio  (guardar el "token" de la respuesta como $TC)
curl -s -X POST localhost:8081/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"urbanshoes@da2back.com","password":"admin123"}'

# 2) El comercio crea el pedido
curl -s -X POST localhost:8081/api/pedidos -H "Authorization: Bearer $TC" -H 'Content-Type: application/json' \
  -d '{"depositoId":1,"direccionDestino":"Av. Rivadavia 5000","detalles":[{"productoId":1,"cantidad":2}]}'

# 3) Lo marca listo para retirar  -> publica en la cola
curl -s -X PATCH localhost:8081/api/pedidos/1/listo-para-retirar -H "Authorization: Bearer $TC"

# 4) Unos instantes después el pedido está ASIGNADO solo (log: "[MQ] Pedido 1 asignado automaticamente ...")
curl -s localhost:8081/api/pedidos/1 -H "Authorization: Bearer $TC"

# 5) Login del repartidor (repartidor@da2back.com) -> $TR, y completa la entrega
curl -s -X PATCH localhost:8081/repartidor/me/pedidos/1/retirar       -H "Authorization: Bearer $TR"
curl -s -X PATCH localhost:8081/repartidor/me/pedidos/1/iniciar-viaje -H "Authorization: Bearer $TR"
curl -s -X PATCH localhost:8081/repartidor/me/pedidos/1/entregar      -H "Authorization: Bearer $TR"
```
Para ver los reintentos: poner al repartidor en `NO_DISPONIBLE` (`PATCH /repartidores/{id}/estado`),
crear y marcar un pedido, y volver a ponerlo `DISPONIBLE` dentro de ~20 s: el pedido se asigna solo.

## Test automatizado
`PedidoAsignacionAsincronaIntegrationTest` corre el proceso completo con un ActiveMQ en memoria (`vm://`)
y verifica el historial `CREADO → LISTO_PARA_RETIRAR → ASIGNADO → RETIRADO → EN_CAMINO → ENTREGADO`, además del
reintento cuando no hay repartidor. En el resto de los tests la mensajería queda desactivada
(`app.mensajeria.habilitada=false`), así que no cambian su comportamiento ni necesitan broker.

```bash
./mvnw test -Dtest=PedidoAsignacionAsincronaIntegrationTest
```

## Decisiones y límites conocidos
- Se eligió **cola punto a punto** (un pedido lo procesa un único consumidor) y no tópico, porque cada
  pedido debe asignarse exactamente una vez.
- El broker embebido usa `persistent=false`: si se reinicia la app se pierden los mensajes en vuelo. Con
  ActiveMQ standalone los mensajes persisten (KahaDB). La publicación es *best effort* post-commit; el patrón
  Outbox sería la mejora para garantía total.
- Los mensajes son `TextMessage` con el id del pedido (sin JSON), para no depender de la versión de Jackson.

# Mensajeria asincronica (RabbitMQ) - Despacho de pedidos

Proceso de negocio de punta a punta: **Comercio hace un pedido -> un repartidor se lo entrega**.
Patron: **cola punto a punto** sobre RabbitMQ (AMQP).

## Flujo
1. `POST /api/pedidos` (COMERCIO) -> pedido `CREADO` (stock descontado).
2. `PATCH /api/pedidos/{id}/listo-para-retirar` (COMERCIO/DEPOSITO) -> `LISTO_PARA_RETIRAR`.
   El servicio emite un evento interno; **despues del commit** `PedidoDespachoPublisher`
   publica el id del pedido en el exchange `logired.pedidos` (rk `despacho`) -> cola `pedidos.despacho`.
3. `DespachoPedidoListener` (consumidor) toma el mensaje y asigna un repartidor
   `DISPONIBLE` -> pedido `ASIGNADO` (usa `ServicioDePedidos.asignarRepartidor`, con sus locks y State).
4. El repartidor continua con los endpoints existentes: `retirar` -> `iniciar-viaje` -> `entregar`.

## Topologia
| Cola | Funcion |
|---|---|
| `pedidos.despacho` | Cola principal (punto a punto). Rechazo -> DLQ |
| `pedidos.despacho.espera` | TTL 15 s; al vencer vuelve a `pedidos.despacho` (reintento sin repartidor libre) |
| `pedidos.despacho.dlq` | Mensajes agotados (8 intentos) o con error inesperado |

Si el pedido no consigue repartidor, sigue `LISTO_PARA_RETIRAR` y se puede tomar manualmente (flujo previo).

## Como probarlo
```bash
docker compose up -d            # RabbitMQ, consola en http://localhost:15672 (logired/logired)
cd DA2Back && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
Con el perfil `dev` hay repartidores `DISPONIBLE` sembrados. Marcar un pedido como listo para retirar
y consultarlo: pasa a `ASIGNADO` en segundos. Para ver el caso "sin repartidor", poner todos los
repartidores en `NO_DISPONIBLE` y observar la cola `pedidos.despacho.espera` en la consola.

Variables: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`.
`logired.mensajeria.habilitada=false` apaga la mensajeria (los tests la usan asi, sin broker).

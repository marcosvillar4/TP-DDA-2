# Auditoría funcional y de seguridad de LogiRed

Resumen publicable de la primera etapa de revisión. El estado posterior y las
verificaciones de cierre se documentan en [la segunda etapa](auditoria-segunda-etapa-2026-11-05.md).
No se incluyen rutas personales, respaldos, datos de cuentas ni identificadores de registros reales.

## Correcciones implementadas

| Problema | Corrección y evidencia de código |
|---|---|
| Sobreventa de la última unidad | ItemInventarioRepository.buscarParaActualizarStock aplica PESSIMISTIC_WRITE dentro de ServicioDePedidosImpl.crear; bloqueo por producto en orden estable |
| Cancelación incompleta o devolución duplicada | ServicioDePedidosImpl.cancelar bloquea pedido, repartidor y stock; valida los ítems y rechaza inconsistencias; una excepción revierte stock, estado e historial |
| JWT de una cuenta bloqueada seguía autenticando | JwtAuthenticationFilter verifica isEnabled e isAccountNonLocked en cada solicitud |
| Autenticación inválida respondía como falta de permisos | SecurityConfig devuelve 401; los rechazos de rol/ownership conservan 403 |
| Nombre de depósito indefinido | PedidoDetail utiliza el campo nombre y consulta el depósito mediante la API permitida para cada rol |
| Distancia cero sin GPS | RutaRepartidor y resumenRuta distinguen distancia desconocida de un cero legítimo |
| Acciones de pedido no autorizadas | pedidoPresentacion.accionesPedido refleja roles y estados; la autorización efectiva permanece en backend |
| Comentarios de transiciones anteriores | PedidosRestController documenta el ciclo actual sin cambiar State |

## Pruebas y alcance

PedidoStockIntegrationTest usa H2 aislada, transacciones reales y datos ficticios.
Cubre última unidad concurrente, doble cancelación, rollback, stock insuficiente,
liberación del repartidor y rechazo de estados finales. Solo se sustituyen las
integraciones externas necesarias para aislar los escenarios.

Los tests de presentación utilizan Node y no sustituyen pruebas de interacción
React en navegador. Las verificaciones históricas de esta etapa fueron 250 tests
backend y cinco tests frontend aprobados; las ampliaciones y resultados actuales
se informan en la segunda etapa, sin sumar ejecuciones repetidas como casos nuevos.

## Límites conocidos

El locking agregado protege creación y cancelación de Pedidos. No demuestra por
sí solo la serialización de todas las ediciones administrativas de Inventario.
No se sanea ni reasigna información histórica automáticamente. Las pruebas
manuales informadas por el equipo no se presentan como ejecuciones automatizadas.

La credencial SMTP detectada en el historial debe considerarse expuesta. Se retiró
su fallback de la configuración actual; revocarla sigue siendo una tarea separada.
No se incorporan su valor, dumps ni logs crudos a esta documentación.

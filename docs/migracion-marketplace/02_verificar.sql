-- Las expectativas de NULL y tabla vacia son inmediatas a migrar, antes de operar.
-- SOLO LECTURA. Comparar resultados con el preflight ANTES de iniciar aplicaciones.
SHOW CREATE TABLE marketplace.pedidos;
SHOW CREATE TABLE marketplace.refresh_tokens;
SHOW CREATE TABLE marketplace.items_inventario;
-- Esperado: nueve filas, todas nullable, con tipos iguales al script 01.
SELECT COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='marketplace' AND TABLE_NAME='pedidos'
AND COLUMN_NAME IN ('origen_lat','origen_lng','destino_lat','destino_lng','distancia_km',
                   'duracion_estimada_min','cantidad_objetos','costo_envio','cotizacion_estimada');
-- Esperado: CERO en ambas consultas; no inventar datos para pedidos antiguos.
SELECT COUNT(*) AS datos_nuevos_inesperados FROM marketplace.pedidos
WHERE origen_lat IS NOT NULL OR origen_lng IS NOT NULL OR destino_lat IS NOT NULL
 OR destino_lng IS NOT NULL OR distancia_km IS NOT NULL OR duracion_estimada_min IS NOT NULL
 OR cantidad_objetos IS NOT NULL OR costo_envio IS NOT NULL OR cotizacion_estimada IS NOT NULL;
SELECT COUNT(*) AS refresh_tokens_esperado_cero FROM marketplace.refresh_tokens;

SELECT 'usuarios' AS tabla,COUNT(*) AS filas FROM marketplace.usuarios
UNION ALL SELECT 'comercios',COUNT(*) FROM marketplace.comercios
UNION ALL SELECT 'depositos',COUNT(*) FROM marketplace.depositos
UNION ALL SELECT 'pedidos',COUNT(*) FROM marketplace.pedidos
UNION ALL SELECT 'historial_estado_pedido',COUNT(*) FROM marketplace.historial_estado_pedido
UNION ALL SELECT 'detalles_pedido',COUNT(*) FROM marketplace.detalles_pedido
UNION ALL SELECT 'items_inventario',COUNT(*) FROM marketplace.items_inventario;
-- Cada ID y huella deben coincidir exactamente con el preflight.
SELECT id,SHA2(CAST(JSON_ARRAY(id,comercio_id,deposito_id,direccion_destino,
 direccion_origen,fecha_creacion,estado,repartidor_id) AS CHAR),256) AS huella_original
FROM marketplace.pedidos ORDER BY id;
CHECKSUM TABLE marketplace.usuarios,marketplace.comercios,marketplace.depositos,
marketplace.historial_estado_pedido,marketplace.detalles_pedido,marketplace.inventarios,
marketplace.items_inventario,marketplace.notificaciones,marketplace.productos,
marketplace.productos_backup,marketplace.repartidores;
SELECT TABLE_NAME,INDEX_NAME,NON_UNIQUE,GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columnas
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA='marketplace' AND TABLE_NAME IN ('refresh_tokens','items_inventario')
GROUP BY TABLE_NAME,INDEX_NAME,NON_UNIQUE;

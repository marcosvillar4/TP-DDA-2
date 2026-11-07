-- Preflight para un esquema anterior: si ya esta migrado, NO aplicar el script 01.
-- SOLO LECTURA. Guardar resultados fuera de Git antes de autorizar DDL.
SELECT VERSION() AS version_mysql, @@hostname AS servidor, @@port AS puerto;
SELECT TABLE_SCHEMA,TABLE_NAME,ENGINE FROM information_schema.TABLES
WHERE TABLE_SCHEMA IN ('marketplace','marketplace_restore_test') ORDER BY TABLE_NAME,TABLE_SCHEMA;
SELECT TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,EXTRA
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA IN ('marketplace','marketplace_restore_test')
ORDER BY TABLE_NAME,COLUMN_NAME,TABLE_SCHEMA;
SELECT TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,NON_UNIQUE,
       GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columnas
FROM information_schema.STATISTICS WHERE TABLE_SCHEMA IN ('marketplace','marketplace_restore_test')
GROUP BY TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,NON_UNIQUE ORDER BY TABLE_NAME,INDEX_NAME,TABLE_SCHEMA;
SELECT CONSTRAINT_SCHEMA,TABLE_NAME,REFERENCED_TABLE_NAME,UPDATE_RULE,DELETE_RULE
FROM information_schema.REFERENTIAL_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA IN ('marketplace','marketplace_restore_test')
ORDER BY TABLE_NAME,REFERENCED_TABLE_NAME,CONSTRAINT_SCHEMA;

-- El script 01 presupone CERO en los siguientes tres resultados.
SELECT COUNT(*) AS refresh_ya_existe FROM information_schema.TABLES
WHERE TABLE_SCHEMA='marketplace' AND TABLE_NAME='refresh_tokens';
SELECT COUNT(*) AS columnas_nuevas_ya_existen FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='marketplace' AND TABLE_NAME='pedidos'
AND COLUMN_NAME IN ('origen_lat','origen_lng','destino_lat','destino_lng','distancia_km',
                   'duracion_estimada_min','cantidad_objetos','costo_envio','cotizacion_estimada');
SELECT COUNT(*) AS grupos_stock_duplicado FROM (
 SELECT inventario_id,producto_id,deposito_id FROM marketplace.items_inventario
 GROUP BY inventario_id,producto_id,deposito_id HAVING COUNT(*)>1
) AS duplicados;
-- Revisar ademas que NO exista ya un indice unico equivalente.
SELECT INDEX_NAME,GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columnas
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA='marketplace' AND TABLE_NAME='items_inventario' AND NON_UNIQUE=0
GROUP BY INDEX_NAME;

-- Informar inconsistencias legacy; NO modificarlas con esta migracion.
SELECT estado,COUNT(*) AS filas FROM marketplace.pedidos GROUP BY estado;
SELECT estado,COUNT(*) AS filas FROM marketplace.historial_estado_pedido GROUP BY estado;
SELECT COUNT(*) AS pedidos_inconsistentes FROM marketplace.pedidos p
LEFT JOIN marketplace.comercios c ON c.id=p.comercio_id
LEFT JOIN marketplace.depositos d ON d.id=p.deposito_id
WHERE c.id IS NULL OR d.id IS NULL OR NOT(p.comercio_id <=> d.comercio_id);
SELECT COUNT(*) AS grupos_usuario_comercio_duplicado FROM (
 SELECT usuario_id FROM marketplace.comercios GROUP BY usuario_id HAVING COUNT(*)>1
) AS duplicados;
SELECT COUNT(*) AS grupos_dni_duplicado FROM (
 SELECT dni FROM marketplace.usuarios GROUP BY dni HAVING COUNT(*)>1
) AS duplicados;

SELECT 'usuarios' AS tabla,COUNT(*) AS filas FROM marketplace.usuarios
UNION ALL SELECT 'comercios',COUNT(*) FROM marketplace.comercios
UNION ALL SELECT 'depositos',COUNT(*) FROM marketplace.depositos
UNION ALL SELECT 'pedidos',COUNT(*) FROM marketplace.pedidos
UNION ALL SELECT 'historial_estado_pedido',COUNT(*) FROM marketplace.historial_estado_pedido
UNION ALL SELECT 'detalles_pedido',COUNT(*) FROM marketplace.detalles_pedido
UNION ALL SELECT 'items_inventario',COUNT(*) FROM marketplace.items_inventario;

-- Huella de TODAS las columnas originales de cada pedido, sin mostrar direcciones.
SELECT id,SHA2(CAST(JSON_ARRAY(id,comercio_id,deposito_id,direccion_destino,
 direccion_origen,fecha_creacion,estado,repartidor_id) AS CHAR),256) AS huella_original
FROM marketplace.pedidos ORDER BY id;
CHECKSUM TABLE marketplace.usuarios,marketplace.comercios,marketplace.depositos,
marketplace.historial_estado_pedido,marketplace.detalles_pedido,marketplace.inventarios,
marketplace.items_inventario,marketplace.notificaciones,marketplace.productos,
marketplace.productos_backup,marketplace.repartidores;

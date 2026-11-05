-- MANUAL Y NO IDEMPOTENTE. Ya aplicado el 05/11/2026 en la base local marketplace.
-- NO reejecutar sobre una base migrada. Otra instalacion requiere preflight y autorizacion.
-- Requiere respaldo verificado y aplicaciones detenidas.
-- Leer README.md y revisar TODOS los resultados de 00_preflight.sql.
-- Ejecutar una sentencia por vez y DETENERSE ante cualquier error.
-- Este archivo NO comprueba automaticamente las precondiciones y NO es idempotente.
-- No copiar datos, sesiones ni notificaciones de marketplace_restore_test.
SET SESSION lock_wait_timeout = 15;

ALTER TABLE marketplace.pedidos
 ADD COLUMN origen_lat DOUBLE NULL,
 ADD COLUMN origen_lng DOUBLE NULL,
 ADD COLUMN destino_lat DOUBLE NULL,
 ADD COLUMN destino_lng DOUBLE NULL,
 ADD COLUMN distancia_km DOUBLE NULL,
 ADD COLUMN duracion_estimada_min INT NULL,
 ADD COLUMN cantidad_objetos INT NULL,
 ADD COLUMN costo_envio DECIMAL(12,2) NULL,
 ADD COLUMN cotizacion_estimada BIT(1) NULL;

CREATE TABLE marketplace.refresh_tokens (
 id BIGINT NOT NULL AUTO_INCREMENT,
 token_hash VARCHAR(64) NOT NULL,
 usuario_id BIGINT NOT NULL,
 creado_en DATETIME(6) NOT NULL,
 expira_en DATETIME(6) NOT NULL,
 revocado BIT(1) NOT NULL,
 PRIMARY KEY (id),
 CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE marketplace.items_inventario
 ADD CONSTRAINT uk_item_inventario_producto_deposito
 UNIQUE (inventario_id,producto_id,deposito_id);

-- Ejecutar 02_verificar.sql ANTES de iniciar la aplicacion con ddl-auto=validate.

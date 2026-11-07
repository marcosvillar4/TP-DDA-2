# Migración estructural manual de marketplace

## Estado y alcance

**Scripts manuales y no idempotentes.** Las tres DDL fueron ensayadas y ya se
aplicaron el 05/11/2026 a la base local `marketplace`. Su arranque posterior con
`spring.jpa.hibernate.ddl-auto=validate` fue satisfactorio. No reejecutar el
script 01 sobre esa base ni asumir que otras instalaciones están migradas.

La actualización agrega nueve columnas nullable de ruteo/cotización a pedidos,
crea refresh_tokens y agrega UNIQUE(inventario_id, producto_id, deposito_id).
No copia filas, no modifica estados ni corrige datos históricos por inferencia.
No incluye dumps, rutas personales, nombres de respaldos ni credenciales.

## Procedimiento para otra instalación con esquema anterior

1. Confirmar instancia y esquema. Detener backend y escritores; deshabilitar
   correo y JMS. No cerrar sesiones de otros programas automáticamente.
2. Generar respaldo completo fuera del repositorio con mysqldump, incluyendo
   estructura, datos, vistas, triggers, rutinas y eventos que existan. Incluir
   single-transaction, routines, events, triggers y hex-blob. Usar result-file y
   un mecanismo local seguro de autenticación; nunca escribir contraseñas en
   argumentos o archivos versionados. No omitir objetos sin informarlo.
3. Exigir salida 0, contenido íntegro, SHA-256 y restauración comprobada en una
   base descartable nueva. Confirmar datos, índices, FKs y objetos. Un dump con
   USE/CREATE DATABASE o referencias calificadas requiere revisión antes de
   restaurarlo con otro nombre: seleccionar otra base por sí solo no lo aísla.
4. Revisar 00_preflight.sql: sus consultas son de lectura. La comparación de
   metadatos con un esquema auxiliar es opcional. Para aplicar 01 deben faltar
   las nueve columnas y refresh_tokens, no existir stock duplicado ni un índice
   único equivalente. Si la base ya está migrada, NO continuar con 01.
5. Adaptar y auditar primero los destinos SQL para el ensayo. Comparar todos los
   datos originales antes/después, no solamente los conteos. Obtener autorización
   expresa antes de aplicar cada DDL en la instalación definitiva.
6. Ejecutar 01 individualmente y detenerse ante cualquier error. No reintentar
   todo el archivo, usar IF NOT EXISTS para ocultar diferencias ni permitir
   Hibernate update como reparación automática.
7. Ejecutar 02 antes de habilitar operaciones. Sus expectativas de columnas NULL
   y sesiones vacías corresponden al instante posterior a la migración; no se
   exigen sobre una base que ya registró actividad legítima.
8. Arrancar temporalmente con validate, correo/JMS deshabilitados y conexión de
   solo lectura para verificar esquema y acceso sin operaciones de negocio.

## Índices, datos y recuperación

MySQL puede retirar un índice individual redundante al agregar uno compuesto que
cubra su prefijo. Se observó ese caso con inventario_id: la FK se conservó y
EXPLAIN eligió el índice compuesto para inventario y la combinación completa.
Verificar cobertura y planes; no recrear índices solo para igualar sus nombres.

CHECKSUM TABLE puede variar al agregar columnas. Comparar SHA-256 calculados sobre
las mismas columnas originales permite verificar la conservación de los datos.
Las anomalías históricas requieren evidencia y autorización separadas; no se
reasignan depósitos o propietarios ni se eliminan duplicados automáticamente.

El DDL de MySQL produce commits implícitos: ROLLBACK no lo revierte. Ante un fallo,
detener escritores, registrar qué operaciones se completaron y realizar solo
lecturas diagnósticas. No borrar columnas, tablas ni restaurar sobre la base
existente automáticamente. Si hace falta recuperación, respaldar el estado
posterior al fallo, verificar el respaldo original en otra base y acordar una
restauración controlada que preserve escrituras legítimas posteriores.

El backend normal debe mantener validate tras migrar. No activar dev, create,
create-drop o update sobre una base con información que deba preservarse.

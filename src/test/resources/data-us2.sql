-- Test data for US2: panel sin convenios próximos a vencer
-- Expected: empty list, mensaje = "Sin vencimientos próximos en los próximos 30 días"
INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio Futuro', 'ONG Z', '2025-06-01', DATEADD('DAY', 45, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio Ya Vencido', 'IESS', '2024-01-01', DATEADD('DAY', -10, CURRENT_DATE), 'VENCIDO');

-- Test data for US1: panel con convenios próximos a vencer
-- Expected: 3 convenios in range (1, 15, 30 days); 2 excluded (vencido, lejano)
INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio UPS-ESPE', 'ESPE', '2025-01-01', DATEADD('DAY', 1, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Acuerdo UPS-ESPOCH', 'ESPOCH', '2025-01-01', DATEADD('DAY', 15, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio UPS-Banco Pichincha', 'Banco Pichincha', '2025-03-01', DATEADD('DAY', 30, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio Vencido', 'Empresa X', '2024-01-01', DATEADD('DAY', -3, CURRENT_DATE), 'VENCIDO');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio Lejano', 'ONG Y', '2025-06-01', DATEADD('DAY', 45, CURRENT_DATE), 'VIGENTE');

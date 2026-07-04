-- Demo data: convenios en diferentes estados para ejercer el panel sin configuración manual
INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio UPS-ESPE', 'ESPE', '2025-01-01', DATEADD('DAY', 1, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Acuerdo UPS-ESPOCH', 'ESPOCH', '2025-01-01', DATEADD('DAY', 8, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio UPS-Banco Pichincha', 'Banco Pichincha', '2025-03-01', DATEADD('DAY', 20, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Convenio UPS-ONG Amazonía', 'ONG Amazonía', '2025-06-01', DATEADD('DAY', 60, CURRENT_DATE), 'VIGENTE');

INSERT INTO convenios (nombre, entidad_socio, fecha_inicio, fecha_vencimiento, estado)
VALUES ('Acuerdo UPS-IESS', 'IESS', '2024-01-01', DATEADD('DAY', -5, CURRENT_DATE), 'VENCIDO');

CREATE TABLE IF NOT EXISTS convenios (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(255) NOT NULL,
    entidad_socio     VARCHAR(255) NOT NULL,
    fecha_inicio      DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado            VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_convenios_fecha_vencimiento
    ON convenios (fecha_vencimiento);

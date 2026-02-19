-- Base de datos para pruebas locales
-- Emula el webservice que te van a pasar

CREATE DATABASE IF NOT EXISTS equipaje_test CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE equipaje_test;

-- Tabla de choferes/usuarios
CREATE TABLE choferes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    interno VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    empresa VARCHAR(50) NOT NULL,
    dni VARCHAR(20),
    telefono VARCHAR(20),
    estado ENUM('activo', 'inactivo') DEFAULT 'activo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de servicios
CREATE TABLE servicios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    interno VARCHAR(10) NOT NULL,
    origen VARCHAR(50) NOT NULL,
    destino VARCHAR(50) NOT NULL,
    hora_salida TIME NOT NULL,
    hora_llegada TIME NOT NULL,
    fecha DATE NOT NULL,
    empresa VARCHAR(50) NOT NULL,
    estado ENUM('programado', 'en_curso', 'finalizado', 'cancelado') DEFAULT 'programado',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_interno_fecha (interno, fecha),
    INDEX idx_fecha_hora (fecha, hora_salida)
);

-- Tabla de boletos
CREATE TABLE boletos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) UNIQUE NOT NULL,
    codigo_qr VARCHAR(100) UNIQUE NOT NULL,
    pasajero_nombre VARCHAR(100) NOT NULL,
    pasajero_dni VARCHAR(20) NOT NULL,
    origen VARCHAR(50) NOT NULL,
    destino VARCHAR(50) NOT NULL,
    servicio_id INT NOT NULL,
    estado ENUM('activo', 'usado', 'cancelado') DEFAULT 'activo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    INDEX idx_codigo_qr (codigo_qr),
    INDEX idx_servicio (servicio_id)
);

-- Tabla de ribetes/marbetes
CREATE TABLE ribetes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) UNIQUE NOT NULL,
    codigo_qr VARCHAR(100) UNIQUE NOT NULL,
    estado ENUM('disponible', 'asignado', 'perdido') DEFAULT 'disponible',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_codigo_qr (codigo_qr)
);

-- Tabla de equipajes registrados
CREATE TABLE equipajes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_unico VARCHAR(50) UNIQUE NOT NULL,
    boleto_id INT NOT NULL,
    ribete_id INT NOT NULL,
    servicio_id INT NOT NULL,
    chofer_id INT NOT NULL,
    pasajero_nombre VARCHAR(100),
    pasajero_dni VARCHAR(20),
    estado ENUM('registrado', 'verificado', 'entregado', 'perdido') DEFAULT 'registrado',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (boleto_id) REFERENCES boletos(id),
    FOREIGN KEY (ribete_id) REFERENCES ribetes(id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    FOREIGN KEY (chofer_id) REFERENCES choferes(id),
    INDEX idx_ribete (ribete_id),
    INDEX idx_servicio (servicio_id)
);

-- Insertar datos de prueba

-- Choferes
INSERT INTO choferes (interno, nombre, empresa, dni, telefono) VALUES
('1001', 'Juan Pérez', 'Transportes del Norte', '23456789', '+54 11 1234-5678'),
('1002', 'Carlos Gómez', 'Transportes del Norte', '34567890', '+54 11 2345-6789'),
('1003', 'Roberto Díaz', 'Express Sur', '45678901', '+54 11 3456-7890');

-- Servicios para hoy (fecha actual)
INSERT INTO servicios (interno, origen, destino, hora_salida, hora_llegada, fecha, empresa, estado) VALUES
('1001', 'Buenos Aires', 'Rosario', '06:00:00', '09:30:00', CURDATE(), 'Transportes del Norte', 'programado'),
('1001', 'Rosario', 'Buenos Aires', '13:00:00', '16:30:00', CURDATE(), 'Transportes del Norte', 'programado'),
('1001', 'Buenos Aires', 'Córdoba', '20:00:00', '01:30:00', CURDATE(), 'Transportes del Norte', 'programado'),
('1002', 'Buenos Aires', 'La Plata', '08:00:00', '10:00:00', CURDATE(), 'Transportes del Norte', 'programado'),
('1002', 'La Plata', 'Buenos Aires', '18:00:00', '20:00:00', CURDATE(), 'Transportes del Norte', 'programado'),
('1003', 'Buenos Aires', 'Mar del Plata', '07:00:00', '12:00:00', CURDATE(), 'Express Sur', 'programado');

-- Boletos con códigos QR
INSERT INTO boletos (numero, codigo_qr, pasajero_nombre, pasajero_dni, origen, destino, servicio_id) VALUES
('BOL-001', 'QR_BOLETO_001', 'María González', '12345678', 'Buenos Aires', 'Rosario', 1),
('BOL-002', 'QR_BOLETO_002', 'José Martínez', '87654321', 'Buenos Aires', 'Rosario', 1),
('BOL-003', 'QR_BOLETO_003', 'Ana Rodríguez', '45678912', 'Rosario', 'Buenos Aires', 2),
('BOL-004', 'QR_BOLETO_004', 'Luis Fernández', '78912345', 'Buenos Aires', 'La Plata', 4),
('BOL-005', 'QR_BOLETO_005', 'Carmen López', '32165498', 'Buenos Aires', 'Rosario', 1),
('BOL-006', 'QR_BOLETO_006', 'Pedro Sánchez', '98745632', 'Buenos Aires', 'Córdoba', 3);

-- Ribetes/Marbetes
INSERT INTO ribetes (numero, codigo_qr) VALUES
('RIB-001', 'QR_RIBETE_001'),
('RIB-002', 'QR_RIBETE_002'),
('RIB-003', 'QR_RIBETE_003'),
('RIB-004', 'QR_RIBETE_004'),
('RIB-005', 'QR_RIBETE_005'),
('RIB-006', 'QR_RIBETE_006'),
('RIB-007', 'QR_RIBETE_007'),
('RIB-008', 'QR_RIBETE_008'),
('RIB-009', 'QR_RIBETE_009'),
('RIB-010', 'QR_RIBETE_010'),
('RIB-011', 'QR_RIBETE_011'),
('RIB-012', 'QR_RIBETE_012'),
('RIB-013', 'QR_RIBETE_013'),
('RIB-014', 'QR_RIBETE_014'),
('RIB-015', 'QR_RIBETE_015');

-- Verificar datos
SELECT 'Choferes cargados:' as info, COUNT(*) as total FROM choferes
UNION ALL
SELECT 'Servicios hoy:', COUNT(*) FROM servicios WHERE fecha = CURDATE()
UNION ALL
SELECT 'Boletos:', COUNT(*) FROM boletos
UNION ALL
SELECT 'Ribetes disponibles:', COUNT(*) FROM ribetes WHERE estado = 'disponible';
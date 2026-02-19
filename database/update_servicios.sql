-- Script actualizado para pruebas
USE equipaje_test;

-- Desactivar checks
SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar todo
TRUNCATE TABLE equipajes;
TRUNCATE TABLE boletos;
TRUNCATE TABLE servicios;

-- Reactivar checks
SET FOREIGN_KEY_CHECKS = 1;

-- Insertar servicios
INSERT INTO servicios (id, interno, origen, destino, hora_salida, hora_llegada, fecha, empresa, estado) VALUES
(1, '1001', 'Buenos Aires', 'Rosario', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 30 MINUTE), '%H:%i:%s'), DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 3 HOUR), '%H:%i:%s'), CURDATE(), 'Transportes del Norte', 'programado'),
(2, '1001', 'Rosario', 'Buenos Aires', DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 2 HOUR), '%H:%i:%s'), DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 5 HOUR), '%H:%i:%s'), CURDATE(), 'Transportes del Norte', 'programado'),
(3, '1002', 'Buenos Aires', 'La Plata', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 HOUR), '%H:%i:%s'), DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 2 HOUR), '%H:%i:%s'), CURDATE(), 'Transportes del Norte', 'programado'),
(4, '1003', 'Buenos Aires', 'Mar del Plata', DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 HOUR), '%H:%i:%s'), DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 6 HOUR), '%H:%i:%s'), CURDATE(), 'Express Sur', 'programado');

-- Insertar boletos con IDs específicos de servicios
INSERT INTO boletos (numero, codigo_qr, pasajero_nombre, pasajero_dni, origen, destino, servicio_id) VALUES
('BOL-001', 'QR_BOLETO_001', 'María González', '12345678', 'Buenos Aires', 'Rosario', 1),
('BOL-002', 'QR_BOLETO_002', 'José Martínez', '87654321', 'Buenos Aires', 'Rosario', 1),
('BOL-003', 'QR_BOLETO_003', 'Ana Rodríguez', '45678912', 'Rosario', 'Buenos Aires', 2),
('BOL-004', 'QR_BOLETO_004', 'Luis Fernández', '78912345', 'Buenos Aires', 'La Plata', 3),
('BOL-005', 'QR_BOLETO_005', 'Carmen López', '32165498', 'Buenos Aires', 'Rosario', 1);

-- Verificar
SELECT '=== SERVICIOS CREADOS ===' as info;
SELECT id, interno, origen, destino, TIME(hora_salida) as hora_salida, estado 
FROM servicios 
ORDER BY id;

SELECT '=== BOLETOS CREADOS ===' as info;
SELECT numero, pasajero_nombre, servicio_id FROM boletos;

SELECT '=== SERVICIOS CERCANOS PARA INTERNO 1001 ===' as info;
SELECT id, interno, origen, destino, TIME(hora_salida) as hora, estado 
FROM servicios 
WHERE interno = '1001' 
AND fecha = CURDATE()
AND hora_salida BETWEEN DATE_SUB(NOW(), INTERVAL 2 HOUR) AND DATE_ADD(NOW(), INTERVAL 2 HOUR)
AND estado IN ('programado', 'en_curso')
ORDER BY hora_salida ASC;
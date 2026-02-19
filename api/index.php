<?php
/**
 * API REST simple para emular el webservice
 * Conecta con MySQL local (equipaje_test)
 * Usuario: root (sin password)
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// Conexión a MySQL
$host = 'localhost';
$dbname = 'equipaje_test';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error de conexión: ' . $e->getMessage()]);
    exit;
}

// Función helper para respuestas
function response($success, $data = null, $message = '') {
    echo json_encode([
        'success' => $success,
        'data' => $data,
        'message' => $message
    ]);
    exit;
}

// Obtener método y datos
$method = $_SERVER['REQUEST_METHOD'];
$input = json_decode(file_get_contents('php://input'), true) ?? [];
$action = $_GET['action'] ?? '';

switch($action) {
    
    // LOGIN - POST auth/login
    case 'login':
        if ($method !== 'POST') {
            response(false, null, 'Método no permitido');
        }
        
        $interno = $input['interno'] ?? '';
        
        if (empty($interno)) {
            response(false, null, 'Interno requerido');
        }
        
        $stmt = $pdo->prepare("SELECT id, interno, nombre, empresa FROM choferes WHERE interno = ? AND estado = 'activo'");
        $stmt->execute([$interno]);
        $chofer = $stmt->fetch();
        
        if ($chofer) {
            session_start();
            $_SESSION['chofer_id'] = $chofer['id'];
            response(true, $chofer, 'Login exitoso');
        } else {
            response(false, null, 'Interno no encontrado');
        }
        break;
    
    // SERVICIOS CERCANOS - GET servicios/{interno}/cercanos
    case 'servicios_cercanos':
        if ($method !== 'GET') {
            response(false, null, 'Método no permitido');
        }
        
        $interno = $_GET['interno'] ?? '';
        
        if (empty($interno)) {
            response(false, null, 'Interno requerido');
        }
        
        // Servicios de hoy (sin filtro de hora para pruebas)
        $stmt = $pdo->prepare("
            SELECT id, interno, origen, destino, 
                   TIME_FORMAT(hora_salida, '%H:%i') as hora_salida,
                   TIME_FORMAT(hora_llegada, '%H:%i') as hora_llegada,
                   empresa, fecha, estado
            FROM servicios 
            WHERE interno = ? 
            AND fecha = CURDATE()
            AND estado IN ('programado', 'en_curso')
            ORDER BY hora_salida ASC
        ");
        $stmt->execute([$interno]);
        $servicios = $stmt->fetchAll();
        
        response(true, $servicios, count($servicios) . ' servicios encontrados');
        break;
    
    // VERIFICAR BOLETO - GET boleto/{codigo_qr}
    case 'verificar_boleto':
        if ($method !== 'GET') {
            response(false, null, 'Método no permitido');
        }
        
        $codigo = $_GET['codigo'] ?? '';
        
        if (empty($codigo)) {
            response(false, null, 'Código QR requerido');
        }
        
        $stmt = $pdo->prepare("
            SELECT b.id, b.numero, b.codigo_qr, b.pasajero_nombre, b.pasajero_dni,
                   b.origen, b.destino, s.hora_salida, s.fecha
            FROM boletos b
            JOIN servicios s ON b.servicio_id = s.id
            WHERE b.codigo_qr = ? AND b.estado = 'activo'
        ");
        $stmt->execute([$codigo]);
        $boleto = $stmt->fetch();
        
        if ($boleto) {
            response(true, $boleto, 'Boleto encontrado');
        } else {
            response(false, null, 'Boleto no encontrado o no válido');
        }
        break;
    
    // VERIFICAR RIBETE - GET ribete/{codigo_qr}
    case 'verificar_ribete':
        if ($method !== 'GET') {
            response(false, null, 'Método no permitido');
        }
        
        $codigo = $_GET['codigo'] ?? '';
        
        if (empty($codigo)) {
            response(false, null, 'Código QR requerido');
        }
        
        $stmt = $pdo->prepare("SELECT id, numero, codigo_qr, estado FROM ribetes WHERE codigo_qr = ?");
        $stmt->execute([$codigo]);
        $ribete = $stmt->fetch();
        
        if ($ribete) {
            if ($ribete['estado'] === 'asignado') {
                response(false, null, 'Ribete ya asignado a otro equipaje');
            }
            response(true, $ribete, 'Ribete disponible');
        } else {
            response(false, null, 'Ribete no encontrado');
        }
        break;
    
    // REGISTRAR EQUIPAJE - POST equipaje/registrar
    case 'registrar_equipaje':
        if ($method !== 'POST') {
            response(false, null, 'Método no permitido');
        }
        
        $codigoBoleto = $input['codigo_boleto'] ?? '';
        $codigoRibete = $input['codigo_ribete'] ?? '';
        $servicioId = $input['servicio_id'] ?? '';
        $choferId = $input['chofer_id'] ?? '';
        
        if (empty($codigoBoleto) || empty($codigoRibete) || empty($servicioId)) {
            response(false, null, 'Datos incompletos');
        }
        
        // Obtener datos del boleto
        $stmt = $pdo->prepare("SELECT id, pasajero_nombre, pasajero_dni FROM boletos WHERE codigo_qr = ?");
        $stmt->execute([$codigoBoleto]);
        $boleto = $stmt->fetch();
        
        if (!$boleto) {
            response(false, null, 'Boleto no válido');
        }
        
        // Obtener datos del ribete
        $stmt = $pdo->prepare("SELECT id, numero FROM ribetes WHERE codigo_qr = ? AND estado = 'disponible'");
        $stmt->execute([$codigoRibete]);
        $ribete = $stmt->fetch();
        
        if (!$ribete) {
            response(false, null, 'Ribete no disponible');
        }
        
        // Insertar equipaje
        $codigoUnico = 'EQ_' . uniqid();
        $stmt = $pdo->prepare("
            INSERT INTO equipajes (codigo_unico, boleto_id, ribete_id, servicio_id, chofer_id, pasajero_nombre, pasajero_dni, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'registrado')
        ");
        
        try {
            $stmt->execute([$codigoUnico, $boleto['id'], $ribete['id'], $servicioId, $choferId, $boleto['pasajero_nombre'], $boleto['pasajero_dni']]);
            
            // Marcar ribete como asignado
            $pdo->prepare("UPDATE ribetes SET estado = 'asignado' WHERE id = ?")->execute([$ribete['id']]);
            
            // Marcar boleto como usado
            $pdo->prepare("UPDATE boletos SET estado = 'usado' WHERE id = ?")->execute([$boleto['id']]);
            
            $equipajeId = $pdo->lastInsertId();
            
            response(true, [
                'equipaje_id' => $equipajeId,
                'codigo_unico' => $codigoUnico,
                'pasajero' => $boleto['pasajero_nombre'],
                'dni' => $boleto['pasajero_dni'],
                'ribete_numero' => $ribete['numero']
            ], 'Equipaje registrado exitosamente');
            
        } catch(PDOException $e) {
            response(false, null, 'Error al registrar: ' . $e->getMessage());
        }
        break;
    
    // VERIFICAR EQUIPAJE POR RIBETE - GET equipaje/verificar
    case 'verificar_equipaje':
        if ($method !== 'GET') {
            response(false, null, 'Método no permitido');
        }
        
        $codigoRibete = $_GET['codigo_ribete'] ?? '';
        
        if (empty($codigoRibete)) {
            response(false, null, 'Código de ribete requerido');
        }
        
        $stmt = $pdo->prepare("
            SELECT e.id, e.codigo_unico, e.pasajero_nombre, e.pasajero_dni, e.estado,
                   r.numero as ribete_numero, s.origen, s.destino, s.fecha, s.hora_salida,
                   c.nombre as chofer_nombre, c.interno
            FROM equipajes e
            JOIN ribetes r ON e.ribete_id = r.id
            JOIN servicios s ON e.servicio_id = s.id
            JOIN choferes c ON e.chofer_id = c.id
            WHERE r.codigo_qr = ? AND e.estado != 'perdido'
        ");
        $stmt->execute([$codigoRibete]);
        $equipaje = $stmt->fetch();
        
        if ($equipaje) {
            response(true, $equipaje, 'Equipaje encontrado');
        } else {
            response(false, null, 'Equipaje no encontrado');
        }
        break;
    
    // OBTENER EQUIPAJES DE UN SERVICIO
    case 'equipajes_servicio':
        if ($method !== 'GET') {
            response(false, null, 'Método no permitido');
        }
        
        $servicioId = $_GET['servicio_id'] ?? '';
        
        if (empty($servicioId)) {
            response(false, null, 'ID de servicio requerido');
        }
        
        $stmt = $pdo->prepare("
            SELECT e.id, e.codigo_unico, e.pasajero_nombre, e.pasajero_dni, 
                   e.estado, r.numero as ribete_numero, b.numero as boleto_numero,
                   e.fecha_registro
            FROM equipajes e
            JOIN ribetes r ON e.ribete_id = r.id
            JOIN boletos b ON e.boleto_id = b.id
            WHERE e.servicio_id = ?
            ORDER BY e.fecha_registro DESC
        ");
        $stmt->execute([$servicioId]);
        $equipajes = $stmt->fetchAll();
        
        response(true, $equipajes, count($equipajes) . ' equipajes encontrados');
        break;
    
    default:
        response(false, null, 'Acción no válida');
}
?>
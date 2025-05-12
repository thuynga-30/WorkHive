<?php
require_once "connectdb.php";
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');


ob_clean(); // Xóa sạch output đệm trước đó
ob_start();

session_start();
$method = $_SERVER['REQUEST_METHOD'];
    $action = isset($_GET['action']) ? $_GET['action'] : '';

    switch ($method) {
        case 'POST':
           delete_all($pdo);
            break;
        
        case 'GET':
            get_notifications($pdo);
            break;
        case 'PUT':
            mark_all_as_read($pdo);
            break;
        default:
            echo json_encode(['success' => false, 'message' => 'Invalid request']);
    }

function mark_all_as_read($pdo) {
   $headers = apache_request_headers();

    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $user_name = $headers['Authorization'];
    // $user_name = $data['user_name'] ?? '';


    if (!$user_name) {
        echo json_encode(['success' => false, 'message' => 'Missing user_name']);
        return;
    }

    $stmt = $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE user_name = ?");
    $stmt->execute([$user_name]);

    echo json_encode(['success' => true, 'message' => 'All notifications marked as read']);
}

function get_notifications($pdo) {
    $headers = apache_request_headers();

    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $user_name = $headers['Authorization'];

    if (!$user_name) {
        echo json_encode(['success' => false, 'message' => 'Missing user_name']);
        return;
    }
    try {
        $stmt = $pdo->prepare("SELECT * FROM notifications WHERE user_name = ? ORDER BY created_at DESC");
        $stmt->execute([$user_name]);
        $notifications = $stmt->fetchAll(PDO::FETCH_ASSOC);

        echo json_encode(['success' => true,
         'notify' => $notifications]);
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'DB error']);
    }
}

function delete_all($pdo){
     $headers = apache_request_headers();

    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $user_name = $headers['Authorization'];
   
    
    if (!$user_name) {
        echo json_encode(['success' => false, 'message' => 'ID required']);
        return;
    }

    $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_name = ?");
    $stmt->execute([$user_name]);

    echo json_encode(['success' => true, 'message' => 'All notifications are deleted']);
}


?>
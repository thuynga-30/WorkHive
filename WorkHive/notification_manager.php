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

if ($method == 'POST' || $method == 'PUT') {
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);

    if (!is_array($data)) {
        echo json_encode(['success' => false, 'message' => 'Invalid JSON']);
        exit;
    }
}
    $action = isset($_GET['action']) ? $_GET['action'] : '';

    switch ($method) {
        case 'POST':
            if ($action === 'create') createNotification($pdo,$data);
                elseif ($action === 'delete') delete_by_id($pdo,$data);
                    elseif ($action === 'delete_all') delete_all($pdo);
            break;
        
        case 'GET':
            get_notifications($pdo);
            break;
        case 'PUT':
            if ($action === 'mark_asRead') markNotificationAsRead($pdo, $data);
                else if ($action === 'mark_all') mark_all_as_read($pdo);
            break;
        default:
            echo json_encode(['success' => false, 'message' => 'Invalid request']);
    }

function createNotification($pdo,$data) {
    $headers = apache_request_headers();

    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $user_name = $headers['Authorization'];
    $content = trim($data['content'] ?? '');
    if (empty($user_name) || empty($content)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Missing user_name or content']);
        return;
    }

    try {
        $stmt = $pdo->prepare("INSERT INTO notifications (user_name, content) VALUES (?, ?)");
        $stmt->execute([$user_name, $content]);
        echo json_encode(['success' => true, 'message' => 'Notification created']);
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
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

    $stmt = $pdo->prepare("SELECT * FROM notifications WHERE user_name = ? ORDER BY created_at DESC");
    $stmt->execute([$user_name]);
    $notifications = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode(['success' => true, 'data' => $notifications]);
}

function markNotificationAsRead($db, $data) {

    $notification_id = $data['notification_id'] ?? null;
    
    $stmt = $db->prepare("UPDATE notifications SET is_read = 1 WHERE notification_id = ?");
    $stmt->execute([$notification_id]);
    echo json_encode(['success' => true, 'message' => 'Mark as read']);
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
function delete_by_id($pdo,$data){
    $notification_id = $data['notification_id'] ?? null;
    
    if (!$notification_id) {
        echo json_encode(['success' => false, 'message' => 'ID required']);
        return;
    }

    $stmt = $pdo->prepare("DELETE FROM notifications WHERE notification_id = ?");
    $stmt->execute([$notification_id]);

    echo json_encode(['success' => true, 'message' => 'Notification deleted']);
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